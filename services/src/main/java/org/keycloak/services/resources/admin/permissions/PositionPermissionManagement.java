package org.keycloak.services.resources.admin.permissions;

import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.models.PositionModel;

import java.util.Map;

/**
 * @author SP_POSITION
 * @version $Revision: 1 $
 */
public interface PositionPermissionManagement {
    boolean isPermissionsEnabled(PositionModel position);
    void setPermissionsEnabled(PositionModel position, boolean enable);

    Policy viewMembersPermission(PositionModel position);
    Policy manageMembersPermission(PositionModel position);

    Policy manageMembershipPermission(PositionModel position);

    Policy viewPermission(PositionModel position);
    Policy managePermission(PositionModel position);

    Resource resource(PositionModel position);

    Map<String, String> getPermissions(PositionModel position);

}
