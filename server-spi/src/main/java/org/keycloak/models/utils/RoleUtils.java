/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.models.utils;

import org.keycloak.models.GroupModel;
import org.keycloak.models.PositionModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class RoleUtils {

    /**
     *
     * @param groups
     * @param targetGroup
     * @return true if targetGroup is in groups (directly or indirectly via parent child relationship)
     */
    public static boolean isMember(Set<GroupModel> groups, GroupModel targetGroup) {
        if (groups.contains(targetGroup)) return true;

        for (GroupModel mapping : groups) {
            GroupModel child = mapping;
            while(child.getParent() != null) {
                if (child.getParent().equals(targetGroup)) return true;
                child = child.getParent();
            }
        }
        return false;
    }

    /**
     * @param roles
     * @param targetRole
     * @return true if targetRole is in roles (directly or indirectly via composite role)
     */
    public static boolean hasRole(Set<RoleModel> roles, RoleModel targetRole) {
        if (roles.contains(targetRole)) return true;

        for (RoleModel mapping : roles) {
            if (mapping.hasRole(targetRole)) return true;
        }
        return false;
    }

    /**
     * Checks whether the {@code targetRole} is contained in the given group or its parents
     * (if requested)
     * @param group Group to check role for
     * @param targetRole
     * @param checkParentGroup When {@code true}, also parent group is recursively checked for role
     * @return true if targetRole is in roles (directly or indirectly via composite role)
     */
    public static boolean hasRoleFromGroup(GroupModel group, RoleModel targetRole, boolean checkParentGroup) {
        if (group.hasRole(targetRole))
            return true;

        if (checkParentGroup) {
            GroupModel parent = group.getParent();
            return parent != null && hasRoleFromGroup(parent, targetRole, true);
        }

        return false;
    }

    /**
     * Checks whether the {@code targetRole} is contained in any of the {@code groups} or their parents
     * (if requested)
     * @param groups
     * @param targetRole
     * @param checkParentGroup When {@code true}, also parent group is recursively checked for role
     * @return true if targetRole is in roles (directly or indirectly via composite role)
     */
    public static boolean hasRoleFromGroup(Iterable<GroupModel> groups, RoleModel targetRole, boolean checkParentGroup) {
        if (groups == null) {
            return false;
        }

        return StreamSupport.stream(groups.spliterator(), false)
                .anyMatch(group -> hasRoleFromGroup(group, targetRole, checkParentGroup));
    }

    /**
     * Recursively expands composite roles into their composite.
     * @param role
     * @param visited Track roles, which were already visited. Those will be ignored and won't be added to the stream. Besides that,
     *                the "visited" set itself will be updated as a result of this method call and all the tracked roles will be added to it
     * @return Stream of containing all of the composite roles and their components.
     */
    private static Stream<RoleModel> expandCompositeRolesStream(RoleModel role, Set<RoleModel> visited) {
        Stream.Builder<RoleModel> sb = Stream.builder();

        if (!visited.contains(role)) {
            Deque<RoleModel> stack = new ArrayDeque<>();
            stack.add(role);

            while (!stack.isEmpty()) {
                RoleModel current = stack.pop();
                sb.add(current);

                if (current.isComposite()) {
                    current.getComposites().stream()
                            .filter(r -> !visited.contains(r))
                            .forEach(r -> {
                                visited.add(r);
                                stack.add(r);
                            });
                }
            }
        }

        return sb.build();
    }


    /**
     * @param roles
     * @return new set with composite roles expanded
     */
    public static Set<RoleModel> expandCompositeRoles(Set<RoleModel> roles) {
        Set<RoleModel> visited = new HashSet<>();

        return roles.stream()
                .flatMap(roleModel -> RoleUtils.expandCompositeRolesStream(roleModel, visited))
                .collect(Collectors.toSet());
    }


    /**
     * @param user
     * @return all user role mappings including all groups of user. Composite roles will be expanded
     */
    public static Set<RoleModel> getDeepUserRoleMappings(UserModel user) {
        Set<RoleModel> roleMappings = new HashSet<>(user.getRoleMappings());
        for (GroupModel group : user.getGroups()) {
            addGroupRoles(group, roleMappings);
        }

        // SP_POSITION
        addPositionRoles(user.getPosition(), roleMappings);
        // SP_POSITION

        return expandCompositeRoles(roleMappings);
    }


    private static void addGroupRoles(GroupModel group, Set<RoleModel> roleMappings) {
        roleMappings.addAll(group.getRoleMappings());
        if (group.getParentId() == null) return;
        addGroupRoles(group.getParent(), roleMappings);
    }

    // SP_POSITION
    private static void addPositionRoles(PositionModel position, Set<RoleModel> roleMappings) {
        if (position != null)
            roleMappings.addAll(position.getRoleMappings());
    }
    /**
     * SP_POSITION
     *
     * Checks whether the {@code targetRole} is contained in the given position or its parents
     * (if requested)
     * @param position Position to check role for
     * @param targetRole
     * @return true if targetRole is in roles (directly or indirectly via composite role)
     */
    public static boolean hasRoleFromPosition(PositionModel position, RoleModel targetRole) {
        if (position.hasRole(targetRole))
            return true;
        return false;
    }
    // SP_POSITION
}
