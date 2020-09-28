package org.keycloak.services.resources.admin.permissions;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.ResourceStore;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.PositionModel;
import org.keycloak.representations.idm.authorization.Permission;
import org.keycloak.services.ForbiddenException;

import java.util.*;

/**
 * @author SP_POSITION
 * @version $Revision: 1 $
 */
public class PositionPermissions implements PositionPermissionEvaluator, PositionPermissionManagement {

    private static final String MANAGE_MEMBERSHIP_SCOPE = "manage-membership";
    private static final String MANAGE_MEMBERS_SCOPE = "manage-members";
    private static final String VIEW_MEMBERS_SCOPE = "view-members";
    private static final String RESOURCE_NAME_PREFIX = "position.resource.";

    private final AuthorizationProvider authz;
    private final MgmtPermissions root;
    private final ResourceStore resourceStore;
    private final PolicyStore policyStore;

    PositionPermissions(AuthorizationProvider authz, MgmtPermissions root) {
        this.authz = authz;
        this.root = root;
        resourceStore = authz.getStoreFactory().getResourceStore();
        policyStore = authz.getStoreFactory().getPolicyStore();
    }

    private static String getPositionResourceName(PositionModel position) {
        return RESOURCE_NAME_PREFIX + position.getPosId();
    }


    private static String getManagePermissionPosition(PositionModel position) {
        return "manage.permission.position." + position.getPosId();
    }

    private static String getManageMembersPermissionPosition(PositionModel position) {
        return "manage.members.permission.position." + position.getPosId();
    }

    private static String getManageMembershipPermissionPosition(PositionModel position) {
        return "manage.membership.permission.position." + position.getPosId();
    }

    private static String getViewPermissionPosition(PositionModel position) {
        return "view.permission.position." + position.getPosId();
    }

    private static String getViewMembersPermissionPosition(PositionModel position) {
        return "view.members.permission.position." + position.getPosId();
    }

    private void initialize(PositionModel position) {
        root.initializeRealmResourceServer();
        root.initializeRealmDefaultScopes();
        ResourceServer server = root.realmResourceServer();
        Scope manageScope = root.realmManageScope();
        Scope viewScope = root.realmViewScope();
        Scope manageMembersScope = root.initializeRealmScope(MANAGE_MEMBERS_SCOPE);
        Scope viewMembersScope = root.initializeRealmScope(VIEW_MEMBERS_SCOPE);
        Scope manageMembershipScope = root.initializeRealmScope(MANAGE_MEMBERSHIP_SCOPE);

        String positionResourceName = getPositionResourceName(position);
        Resource positionResource = resourceStore.findByName(positionResourceName, server.getId());
        if (positionResource == null) {
            positionResource = resourceStore.create(positionResourceName, server, server.getId());
            Set<Scope> scopeset = new HashSet<>();
            scopeset.add(manageScope);
            scopeset.add(viewScope);
            scopeset.add(viewMembersScope);
            scopeset.add(manageMembershipScope);
            scopeset.add(manageMembersScope);
            positionResource.updateScopes(scopeset);
            positionResource.setType("Position");
        }
        String managePermissionName = getManagePermissionPosition(position);
        Policy managePermission = policyStore.findByName(managePermissionName, server.getId());
        if (managePermission == null) {
            Helper.addEmptyScopePermission(authz, server, managePermissionName, positionResource, manageScope);
        }
        String viewPermissionName = getViewPermissionPosition(position);
        Policy viewPermission = policyStore.findByName(viewPermissionName, server.getId());
        if (viewPermission == null) {
            Helper.addEmptyScopePermission(authz, server, viewPermissionName, positionResource, viewScope);
        }
        String manageMembersPermissionName = getManageMembersPermissionPosition(position);
        Policy manageMembersPermission = policyStore.findByName(manageMembersPermissionName, server.getId());
        if (manageMembersPermission == null) {
            Helper.addEmptyScopePermission(authz, server, manageMembersPermissionName, positionResource, manageMembersScope);
        }
        String viewMembersPermissionName = getViewMembersPermissionPosition(position);
        Policy viewMembersPermission = policyStore.findByName(viewMembersPermissionName, server.getId());
        if (viewMembersPermission == null) {
            Helper.addEmptyScopePermission(authz, server, viewMembersPermissionName, positionResource, viewMembersScope);
        }
        String manageMembershipPermissionName = getManageMembershipPermissionPosition(position);
        Policy manageMembershipPermission = policyStore.findByName(manageMembershipPermissionName, server.getId());
        if (manageMembershipPermission == null) {
            Helper.addEmptyScopePermission(authz, server, manageMembershipPermissionName, positionResource, manageMembershipScope);
        }

    }

    @Override
    public boolean canList() {
        return canView() || root.hasOneAdminRole(AdminRoles.VIEW_USERS, AdminRoles.MANAGE_USERS, AdminRoles.QUERY_POSITIONS);
    }

    @Override
    public void requireList() {
        if (!canList()) {
            throw new ForbiddenException();
        }
    }

    @Override
    public boolean isPermissionsEnabled(PositionModel position) {
        ResourceServer server = root.realmResourceServer();
        if (server == null) return false;

        return resourceStore.findByName(getPositionResourceName(position), server.getId()) != null;
    }

    @Override
    public void setPermissionsEnabled(PositionModel position, boolean enable) {
       if (enable) {
           initialize(position);
       } else {
           deletePermissions(position);
       }
    }

    @Override
    public Policy viewMembersPermission(PositionModel position) {
        ResourceServer server = root.realmResourceServer();
        if (server == null) return null;
        return policyStore.findByName(getViewMembersPermissionPosition(position), server.getId());
    }

    @Override
    public Policy manageMembersPermission(PositionModel position) {
        ResourceServer server = root.realmResourceServer();
        if (server == null) return null;
        return policyStore.findByName(getManageMembersPermissionPosition(position), server.getId());
    }

    @Override
    public Policy manageMembershipPermission(PositionModel position) {
        ResourceServer server = root.realmResourceServer();
        if (server == null) return null;
        return policyStore.findByName(getManageMembershipPermissionPosition(position), server.getId());
    }

    @Override
    public Policy viewPermission(PositionModel position) {
        ResourceServer server = root.realmResourceServer();
        if (server == null) return null;
        return policyStore.findByName(getViewPermissionPosition(position), server.getId());
    }

    @Override
    public Policy managePermission(PositionModel position) {
        ResourceServer server = root.realmResourceServer();
        if (server == null) return null;
        return policyStore.findByName(getManagePermissionPosition(position), server.getId());
    }

    @Override
    public Resource resource(PositionModel position) {
        ResourceServer server = root.realmResourceServer();
        if (server == null) return null;
        Resource resource =  resourceStore.findByName(getPositionResourceName(position), server.getId());
        if (resource == null) return null;
        return resource;
    }

    @Override
    public Map<String, String> getPermissions(PositionModel position) {
        initialize(position);
        Map<String, String> scopes = new LinkedHashMap<>();
        scopes.put(AdminPermissionManagement.VIEW_SCOPE, viewPermission(position).getId());
        scopes.put(AdminPermissionManagement.MANAGE_SCOPE, managePermission(position).getId());
        scopes.put(VIEW_MEMBERS_SCOPE, viewMembersPermission(position).getId());
        scopes.put(MANAGE_MEMBERS_SCOPE, manageMembersPermission(position).getId());
        scopes.put(MANAGE_MEMBERSHIP_SCOPE, manageMembershipPermission(position).getId());
        return scopes;
    }

    @Override
    public boolean canManage(PositionModel position) {
        if (canManage()) {
            return true;
        }

        if (!root.isAdminSameRealm()) {
            return false;
        }

        return hasPermission(position, MgmtPermissions.MANAGE_SCOPE);
    }

    @Override
    public void requireManage(PositionModel position) {
        if (!canManage(position)) {
            throw new ForbiddenException();
        }
    }

    @Override
    public boolean canView(PositionModel position) {
        if (canView() || canManage()) {
            return true;
        }

        if (!root.isAdminSameRealm()) {
            return false;
        }

        return hasPermission(position, MgmtPermissions.VIEW_SCOPE, MgmtPermissions.MANAGE_SCOPE);
    }

    @Override
    public void requireView(PositionModel position) {
        if (!canView(position)) {
            throw new ForbiddenException();
        }
    }

    @Override
    public boolean canManage() {
        return root.users().canManageDefault();
    }

    @Override
    public void requireManage() {
        if (!canManage()) {
            throw new ForbiddenException();
        }
    }
    @Override
    public boolean canView() {
        return root.users().canViewDefault();
    }

    @Override
    public void requireView() {
        if (!canView()) {
            throw new ForbiddenException();
        }
    }

    @Override
    public boolean getPositionsWithViewPermission(PositionModel position) {
        if (root.users().canView() || root.users().canManage()) {
            return true;
        }

        if (!root.isAdminSameRealm()) {
            return false;
        }

        ResourceServer server = root.realmResourceServer();
        if (server == null) return false;

        return hasPermission(position, VIEW_MEMBERS_SCOPE, MANAGE_MEMBERS_SCOPE);
    }

    @Override
    public Set<String> getPositionsWithViewPermission() {
        if (root.users().canView() || root.users().canManage()) return Collections.emptySet();

        if (!root.isAdminSameRealm()) {
            return Collections.emptySet();
        }

        ResourceServer server = root.realmResourceServer();

        if (server == null) {
            return Collections.emptySet();
        }

        Set<String> granted = new HashSet<>();

        resourceStore.findByType("Position", server.getId(), resource -> {
            if (hasPermission(resource, null, VIEW_MEMBERS_SCOPE, MANAGE_MEMBERS_SCOPE)) {
                granted.add(resource.getName().substring(RESOURCE_NAME_PREFIX.length()));
            }
        });

        return granted;
    }

    @Override
    public void requireViewMembers(PositionModel position) {
        if (!getPositionsWithViewPermission(position)) {
            throw new ForbiddenException();
        }
    }

    @Override
    public boolean canManageMembers(PositionModel position) {
        if (root.users().canManage()) return true;

        if (!root.isAdminSameRealm()) {
            return false;
        }

        ResourceServer server = root.realmResourceServer();
        if (server == null) return false;

        return hasPermission(position, MANAGE_MEMBERS_SCOPE);
    }

    @Override
    public boolean canManageMembership(PositionModel position) {
        if (canManage(position)) return true;

        if (!root.isAdminSameRealm()) {
            return false;
        }

        return hasPermission(position, MANAGE_MEMBERSHIP_SCOPE);
    }

    @Override
    public void requireManageMembership(PositionModel position) {
        if (!canManageMembership(position)) {
            throw new ForbiddenException();
        }
    }

    @Override
    public Map<String, Boolean> getAccess(PositionModel position) {
        Map<String, Boolean> map = new HashMap<>();
        map.put("view", canView(position));
        map.put("manage", canManage(position));
        map.put("manageMembership", canManageMembership(position));
        return map;
    }

    private boolean hasPermission(PositionModel position, String... scopes) {
        return hasPermission(position, null, scopes);
    }

    private boolean hasPermission(PositionModel position, EvaluationContext context, String... scopes) {
        ResourceServer server = root.realmResourceServer();

        if (server == null) {
            return false;
        }

        Resource resource =  resourceStore.findByName(getPositionResourceName(position), server.getId());

        if (resource == null) {
            return false;
        }

        return hasPermission(resource, context, scopes);
    }

    private boolean hasPermission(Resource resource, EvaluationContext context, String... scopes) {
        ResourceServer server = root.realmResourceServer();
        Collection<Permission> permissions;

        if (context == null) {
            permissions = root.evaluatePermission(new ResourcePermission(resource, resource.getScopes(), server), server);
        } else {
            permissions = root.evaluatePermission(new ResourcePermission(resource, resource.getScopes(), server), server, context);
        }

        List<String> expectedScopes = Arrays.asList(scopes);


        for (Permission permission : permissions) {
            for (String scope : permission.getScopes()) {
                if (expectedScopes.contains(scope)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Resource positionResource(PositionModel position) {
        ResourceServer server = root.realmResourceServer();
        if (server == null) return null;
        String positionResourceName = getPositionResourceName(position);
        return resourceStore.findByName(positionResourceName, server.getId());
    }

    private void deletePermissions(PositionModel position) {
        ResourceServer server = root.realmResourceServer();
        if (server == null) return;
        Policy managePermission = managePermission(position);
        if (managePermission != null) {
            policyStore.delete(managePermission.getId());
        }
        Policy viewPermission = viewPermission(position);
        if (viewPermission != null) {
            policyStore.delete(viewPermission.getId());
        }
        Policy manageMembersPermission = manageMembersPermission(position);
        if (manageMembersPermission != null) {
            policyStore.delete(manageMembersPermission.getId());
        }
        Policy viewMembersPermission = viewMembersPermission(position);
        if (viewMembersPermission != null) {
            policyStore.delete(viewMembersPermission.getId());
        }
        Policy manageMembershipPermission = manageMembershipPermission(position);
        if (manageMembershipPermission != null) {
            policyStore.delete(manageMembershipPermission.getId());
        }
        Resource resource = positionResource(position);
        if (resource != null) resourceStore.delete(resource.getId());
    }
}
