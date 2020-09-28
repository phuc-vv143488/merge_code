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
package org.keycloak.services.resources.admin.permissions;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.common.ClientModelIdentity;
import org.keycloak.authorization.common.DefaultEvaluationContext;
import org.keycloak.authorization.common.UserModelIdentity;
import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.ResourceStore;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.PositionModel;
import org.keycloak.models.ImpersonationConstants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.authorization.Permission;
import org.keycloak.services.ForbiddenException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Manages default policies for all users.
 *
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
class UserPermissions implements UserPermissionEvaluator, UserPermissionManagement {

    private static final String MAP_ROLES_SCOPE="map-roles";
    private static final String IMPERSONATE_SCOPE="impersonate";
    private static final String USER_IMPERSONATED_SCOPE="user-impersonated";
    private static final String MANAGE_GROUP_MEMBERSHIP_SCOPE="manage-group-membership";
    private static final String MAP_ROLES_PERMISSION_USERS = "map-roles.permission.users";
    private static final String ADMIN_IMPERSONATING_PERMISSION = "admin-impersonating.permission.users";
    private static final String USER_IMPERSONATED_PERMISSION = "user-impersonated.permission.users";
    private static final String MANAGE_GROUP_MEMBERSHIP_PERMISSION_USERS = "manage-group-membership.permission.users";
    private static final String MANAGE_PERMISSION_USERS = "manage.permission.users";
    private static final String VIEW_PERMISSION_USERS = "view.permission.users";
    private static final String USERS_RESOURCE = "Users";
    // SP_POSITION
    private static final String MANAGE_POSITION_MEMBERSHIP_SCOPE="manage-position-membership";
    private static final String MANAGE_POSITION_MEMBERSHIP_PERMISSION_USERS = "manage-position-membership.permission.users";
    // SP_POSITION

    private final KeycloakSession session;
    private final AuthorizationProvider authz;
    private final MgmtPermissions root;
    private final PolicyStore policyStore;
    private final ResourceStore resourceStore;
    private boolean grantIfNoPermission = false;

    UserPermissions(KeycloakSession session, AuthorizationProvider authz, MgmtPermissions root) {
        this.session = session;
        this.authz = authz;
        this.root = root;
        policyStore = authz.getStoreFactory().getPolicyStore();
        resourceStore = authz.getStoreFactory().getResourceStore();
    }


    private void initialize() {
        root.initializeRealmResourceServer();
        root.initializeRealmDefaultScopes();
        ResourceServer server = root.realmResourceServer();
        Scope manageScope = root.realmManageScope();
        Scope viewScope = root.realmViewScope();
        Scope mapRolesScope = root.initializeRealmScope(MAP_ROLES_SCOPE);
        Scope impersonateScope = root.initializeRealmScope(IMPERSONATE_SCOPE);
        Scope userImpersonatedScope = root.initializeRealmScope(USER_IMPERSONATED_SCOPE);
        Scope manageGroupMembershipScope = root.initializeRealmScope(MANAGE_GROUP_MEMBERSHIP_SCOPE);

        // SP_POSITION
        Scope managePositionMembershipScope = root.initializeRealmScope(MANAGE_POSITION_MEMBERSHIP_SCOPE);
        // SP_POSITION

        Resource usersResource = resourceStore.findByName(USERS_RESOURCE, server.getId());
        if (usersResource == null) {
            usersResource = resourceStore.create(USERS_RESOURCE, server, server.getId());
            Set<Scope> scopeset = new HashSet<>();
            scopeset.add(manageScope);
            scopeset.add(viewScope);
            scopeset.add(mapRolesScope);
            scopeset.add(impersonateScope);
            scopeset.add(manageGroupMembershipScope);
            // SP_POSITION
            scopeset.add(managePositionMembershipScope);
            // SP_POSITION
            scopeset.add(userImpersonatedScope);
            usersResource.updateScopes(scopeset);
        }
        Policy managePermission = policyStore.findByName(MANAGE_PERMISSION_USERS, server.getId());
        if (managePermission == null) {
            Helper.addEmptyScopePermission(authz, server, MANAGE_PERMISSION_USERS, usersResource, manageScope);
        }
        Policy viewPermission = policyStore.findByName(VIEW_PERMISSION_USERS, server.getId());
        if (viewPermission == null) {
            Helper.addEmptyScopePermission(authz, server, VIEW_PERMISSION_USERS, usersResource, viewScope);
        }
        Policy mapRolesPermission = policyStore.findByName(MAP_ROLES_PERMISSION_USERS, server.getId());
        if (mapRolesPermission == null) {
            Helper.addEmptyScopePermission(authz, server, MAP_ROLES_PERMISSION_USERS, usersResource, mapRolesScope);
        }
        Policy membershipPermission = policyStore.findByName(MANAGE_GROUP_MEMBERSHIP_PERMISSION_USERS, server.getId());
        if (membershipPermission == null) {
            Helper.addEmptyScopePermission(authz, server, MANAGE_GROUP_MEMBERSHIP_PERMISSION_USERS, usersResource, manageGroupMembershipScope);
        }
        // SP_POSITION
        Policy positionMembershipPermission = policyStore.findByName(MANAGE_POSITION_MEMBERSHIP_PERMISSION_USERS, server.getId());
        if (positionMembershipPermission == null) {
            Helper.addEmptyScopePermission(authz, server, MANAGE_POSITION_MEMBERSHIP_PERMISSION_USERS, usersResource, managePositionMembershipScope);
        }
        // SP_POSITION
        Policy impersonatePermission = policyStore.findByName(ADMIN_IMPERSONATING_PERMISSION, server.getId());
        if (impersonatePermission == null) {
            Helper.addEmptyScopePermission(authz, server, ADMIN_IMPERSONATING_PERMISSION, usersResource, impersonateScope);
        }
        impersonatePermission = policyStore.findByName(USER_IMPERSONATED_PERMISSION, server.getId());
        if (impersonatePermission == null) {
            Helper.addEmptyScopePermission(authz, server, USER_IMPERSONATED_PERMISSION, usersResource, userImpersonatedScope);
        }
    }

    @Override
    public Map<String, String> getPermissions() {
        initialize();
        Map<String, String> scopes = new LinkedHashMap<>();
        scopes.put(AdminPermissionManagement.VIEW_SCOPE, viewPermission().getId());
        scopes.put(AdminPermissionManagement.MANAGE_SCOPE, managePermission().getId());
        scopes.put(MAP_ROLES_SCOPE, mapRolesPermission().getId());
        scopes.put(MANAGE_GROUP_MEMBERSHIP_SCOPE, manageGroupMembershipPermission().getId());
        // SP_POSITION
        scopes.put(MANAGE_POSITION_MEMBERSHIP_SCOPE, managePositionMembershipPermission().getId());
        // SP_POSITION
        scopes.put(IMPERSONATE_SCOPE, adminImpersonatingPermission().getId());
        scopes.put(USER_IMPERSONATED_SCOPE, userImpersonatedPermission().getId());
        return scopes;
    }

    @Override
    public boolean isPermissionsEnabled() {
        ResourceServer server = root.realmResourceServer();
        if (server == null) return false;

        Resource resource =  resourceStore.findByName(USERS_RESOURCE, server.getId());
        if (resource == null) return false;

        Policy policy = managePermission();

        return policy != null;
    }

    @Override
    public void setPermissionsEnabled(boolean enable) {
        if (enable) {
            initialize();
        } else {
            deletePermissionSetup();
        }
    }

    public boolean canManageDefault() {
        return root.hasOneAdminRole(AdminRoles.MANAGE_USERS);
    }

    @Override
    public Resource resource() {
        ResourceServer server = root.realmResourceServer();
        if (server == null) return null;

        return  resourceStore.findByName(USERS_RESOURCE, server.getId());
    }

    @Override
    public Policy managePermission() {
        return policyStore.findByName(MANAGE_PERMISSION_USERS, root.realmResourceServer().getId());
    }

    @Override
    public Policy viewPermission() {
        return policyStore.findByName(VIEW_PERMISSION_USERS, root.realmResourceServer().getId());
    }

    @Override
    public Policy manageGroupMembershipPermission() {
        return policyStore.findByName(MANAGE_GROUP_MEMBERSHIP_PERMISSION_USERS, root.realmResourceServer().getId());
    }

    @Override
    public Policy mapRolesPermission() {
        return policyStore.findByName(MAP_ROLES_PERMISSION_USERS, root.realmResourceServer().getId());
    }


    @Override
    public Policy adminImpersonatingPermission() {
        return policyStore.findByName(ADMIN_IMPERSONATING_PERMISSION, root.realmResourceServer().getId());
    }

    @Override
    public Policy userImpersonatedPermission() {
        return policyStore.findByName(USER_IMPERSONATED_PERMISSION, root.realmResourceServer().getId());
    }

    /**
     * Is admin allowed to manage all users?  In Authz terms, does the admin have the "manage" scope for the Users Authz resource?
     *
     * This method will follow the old default behavior (does the admin have the manage-users role) if any of these conditions
     * are met.:
     * - The admin is from the master realm managing a different realm
     * - If the Authz objects are not set up correctly for the Users resource in Authz
     * - The "manage" permission for the Users resource has an empty associatedPolicy list.
     *
     * Otherwise, it will use the Authz policy engine to resolve this answer.
     *
     * @return
     */
    @Override
    public boolean canManage() {
        if (canManageDefault()) {
            return true;
        }

        if (!root.isAdminSameRealm()) {
            return false;
        }

        return hasPermission(MgmtPermissions.MANAGE_SCOPE);
    }

    @Override
    public void requireManage() {
        if (!canManage()) {
            throw new ForbiddenException();
        }
    }


    /**
     * Does current admin have manage permissions for this particular user?
     *
     * @param user
     * @return
     */
    @Override
    public boolean canManage(UserModel user) {
        return canManage() || canManageByGroup(user)
                || canManageByPosition(user); // SP_POSITION
    }

    @Override
    public void requireManage(UserModel user) {
        if (!canManage(user)) {
            throw new ForbiddenException();
        }
    }

    @Override
    public boolean canQuery() {
        return canView() || root.hasOneAdminRole(AdminRoles.QUERY_USERS);
    }

    @Override
    public void requireQuery() {
        if (!canQuery()) {
            throw new ForbiddenException();
        }
    }

    /**
     * Is admin allowed to view all users?  In Authz terms, does the admin have the "view" scope for the Users Authz resource?
     *
     * This method will follow the old default behavior (does the admin have the view-users role) if any of these conditions
     * are met.:
     * - The admin is from the master realm managing a different realm
     * - If the Authz objects are not set up correctly for the Users resource in Authz
     * - The "view" permission for the Users resource has an empty associatedPolicy list.
     *
     * Otherwise, it will use the Authz policy engine to resolve this answer.
     *
     * @return
     */
    @Override
    public boolean canView() {
        if (canViewDefault() || canManageDefault()) {
            return true;
        }

        if (!root.isAdminSameRealm()) {
            return false;
        }

        return hasPermission(MgmtPermissions.VIEW_SCOPE, MgmtPermissions.MANAGE_SCOPE);
    }

    /**
     * Does current admin have view permissions for this particular user?
     *
     * Evaluates in this order. If any true, return true:
     * - canViewUsers
     * - canManageUsers
     *
     *
     * @param user
     * @return
     */
    @Override
    public boolean canView(UserModel user) {
        return canView() || canViewByGroup(user)
                || canViewByPosition(user); // SP_POSITION
    }

    @Override
    public void requireView(UserModel user) {
        if (!canView(user)) {
            throw new ForbiddenException();
        }
    }

    @Override
    public void requireView() {
        if (!(canView())) {
            throw new ForbiddenException();
        }
    }

    @Override
    public boolean canClientImpersonate(ClientModel client, UserModel user) {
        ClientModelIdentity identity = new ClientModelIdentity(session, client);
        EvaluationContext context = new DefaultEvaluationContext(identity, session) {
            @Override
            public Map<String, Collection<String>> getBaseAttributes() {
                Map<String, Collection<String>> attributes = super.getBaseAttributes();
                attributes.put("kc.client.id", Arrays.asList(client.getClientId()));
                return attributes;
            }

        };
        return canImpersonate(context) && isImpersonatable(user);

    }

    @Override
    public boolean canImpersonate(UserModel user) {
        if (!canImpersonate()) {
            return false;
        }

        return isImpersonatable(user);
    }

    @Override
    public boolean isImpersonatable(UserModel user) {
        ResourceServer server = root.realmResourceServer();

        if (server == null) {
            return true;
        }

        Resource resource =  resourceStore.findByName(USERS_RESOURCE, server.getId());

        if (resource == null) {
            return true;
        }

        Policy policy = authz.getStoreFactory().getPolicyStore().findByName(USER_IMPERSONATED_PERMISSION, server.getId());

        if (policy == null) {
            return true;
        }

        Set<Policy> associatedPolicies = policy.getAssociatedPolicies();
        // if no policies attached to permission then just do default behavior
        if (associatedPolicies == null || associatedPolicies.isEmpty()) {
            return true;
        }

        return hasPermission(new DefaultEvaluationContext(new UserModelIdentity(root.realm, user), session), USER_IMPERSONATED_SCOPE);
    }

    @Override
    public boolean canImpersonate() {
        if (root.hasOneAdminRole(ImpersonationConstants.IMPERSONATION_ROLE)) return true;

        Identity identity = root.identity;

        if (!root.isAdminSameRealm()) {
            return false;
        }

        return canImpersonate(new DefaultEvaluationContext(identity, session));
    }

    @Override
    public void requireImpersonate(UserModel user) {
        if (!canImpersonate(user)) {
            throw new ForbiddenException();
        }
    }

    @Override
    public Map<String, Boolean> getAccess(UserModel user) {
        Map<String, Boolean> map = new HashMap<>();
        map.put("view", canView(user));
        map.put("manage", canManage(user));
        map.put("mapRoles", canMapRoles(user));
        map.put("manageGroupMembership", canManageGroupMembership(user));
        // SP_POSITION
        map.put("managePositionMembership", canManagePositionMembership(user));
        // SP_POSITION
        map.put("impersonate", canImpersonate(user));
        return map;
    }

    @Override
    public boolean canMapRoles(UserModel user) {
        if (canManage(user)) return true;

        if (!root.isAdminSameRealm()) {
            return false;
        }

        return hasPermission(MAP_ROLES_SCOPE);

    }

    @Override
    public void requireMapRoles(UserModel user) {
        if (!canMapRoles(user)) {
            throw new ForbiddenException();
        }

    }

    @Override
    public boolean canManageGroupMembership(UserModel user) {
        if (canManage(user)) return true;

        if (!root.isAdminSameRealm()) {
            return false;
        }

        return hasPermission(MANAGE_GROUP_MEMBERSHIP_SCOPE);

    }

    @Override
    public void grantIfNoPermission(boolean grantIfNoPermission) {
        this.grantIfNoPermission = grantIfNoPermission;
    }

    @Override
    public void requireManageGroupMembership(UserModel user) {
        if (!canManageGroupMembership(user)) {
            throw new ForbiddenException();
        }

    }

    private boolean hasPermission(String... scopes) {
        return hasPermission(null, scopes);
    }

    private boolean hasPermission(EvaluationContext context, String... scopes) {
        ResourceServer server = root.realmResourceServer();

        if (server == null) {
            return false;
        }

        Resource resource =  resourceStore.findByName(USERS_RESOURCE, server.getId());
        List<String> expectedScopes = Arrays.asList(scopes);

        if (resource == null) {
            return grantIfNoPermission && expectedScopes.contains(MgmtPermissions.MANAGE_SCOPE) && expectedScopes.contains(MgmtPermissions.VIEW_SCOPE);
        }

        Collection<Permission> permissions;

        if (context == null) {
            permissions = root.evaluatePermission(new ResourcePermission(resource, resource.getScopes(), server), server);
        } else {
            permissions = root.evaluatePermission(new ResourcePermission(resource, resource.getScopes(), server), server, context);
        }

        for (Permission permission : permissions) {
            for (String scope : permission.getScopes()) {
                if (expectedScopes.contains(scope)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void deletePermissionSetup() {
        ResourceServer server = root.realmResourceServer();
        if (server == null) return;
        Policy policy = managePermission();
        if (policy != null) {
            policyStore.delete(policy.getId());

        }
        policy = viewPermission();
        if (policy != null) {
            policyStore.delete(policy.getId());

        }
        policy = mapRolesPermission();
        if (policy != null) {
            policyStore.delete(policy.getId());

        }
        policy = manageGroupMembershipPermission();
        if (policy != null) {
            policyStore.delete(policy.getId());

        }
        // SP_POSITION
        policy = managePositionMembershipPermission();
        if (policy != null) {
            policyStore.delete(policy.getId());

        }
        // SP_POSITION
        policy = adminImpersonatingPermission();
        if (policy != null) {
            policyStore.delete(policy.getId());

        }
        policy = userImpersonatedPermission();
        if (policy != null) {
            policyStore.delete(policy.getId());

        }
        Resource usersResource = resourceStore.findByName(USERS_RESOURCE, server.getId());
        if (usersResource != null) {
            resourceStore.delete(usersResource.getId());
        }
    }

    private boolean canImpersonate(EvaluationContext context) {
        return hasPermission(context, IMPERSONATE_SCOPE);
    }

    private boolean evaluateHierarchy(UserModel user, Predicate<GroupModel> eval) {
        Set<GroupModel> visited = new HashSet<>();
        for (GroupModel group : user.getGroups()) {
            if (evaluateHierarchy(eval, group, visited)) return true;
        }
        return false;
    }

    private boolean evaluateHierarchy(Predicate<GroupModel> eval, GroupModel group, Set<GroupModel> visited) {
        if (visited.contains(group)) return false;
        if (eval.test(group)) {
            return true;
        }
        visited.add(group);
        if (group.getParent() == null) return false;
        return evaluateHierarchy(eval, group.getParent(), visited);
    }

    private boolean canManageByGroup(UserModel user) {
        return evaluateHierarchy(user, (group) -> root.groups().canManageMembers(group));

    }
    private boolean canViewByGroup(UserModel user) {
        return evaluateHierarchy(user, (group) -> root.groups().getGroupsWithViewPermission(group));
    }

    public boolean canViewDefault() {
        return root.hasOneAdminRole(AdminRoles.MANAGE_USERS, AdminRoles.VIEW_USERS);
    }

    // SP_POSITION
    private boolean evaluateHierarchyPosition(UserModel user, Predicate<PositionModel> eval) {
        Set<PositionModel> visited = new HashSet<>();
        if (evaluateHierarchy(eval, user.getPosition(), visited)) return true;
        return false;
    }

    private boolean evaluateHierarchy(Predicate<PositionModel> eval, PositionModel position, Set<PositionModel> visited) {
        if (visited.contains(position)) return false;
        if (eval.test(position)) {
            return true;
        }
        visited.add(position);
        return evaluateHierarchy(eval, position, visited);
    }
    private boolean canManageByPosition(UserModel user) {
        return evaluateHierarchyPosition(user, (position) -> root.positions().canManageMembers(position));

    }
    private boolean canViewByPosition(UserModel user) {
        return evaluateHierarchyPosition(user, (position) -> root.positions().getPositionsWithViewPermission(position));
    }

    @Override
    public void requireManagePositionMembership(UserModel user) {
        if (!canManagePositionMembership(user)) {
            throw new ForbiddenException();
        }
    }
    @Override
    public boolean canManagePositionMembership(UserModel user) {
        if (canManage(user)) return true;

        if (!root.isAdminSameRealm()) {
            return false;
        }
        return hasPermission(MANAGE_POSITION_MEMBERSHIP_SCOPE);
    }
    @Override
    public Policy managePositionMembershipPermission() {
        return policyStore.findByName(MANAGE_POSITION_MEMBERSHIP_PERMISSION_USERS, root.realmResourceServer().getId());
    }
    // SP_POSITION
}
