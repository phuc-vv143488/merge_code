package org.keycloak.services.resources.admin.permissions;

import org.keycloak.models.PositionModel;

import java.util.Map;
import java.util.Set;

/**
 * @author SP_POSITION
 * @version $Revision: 1 $
 */
public interface PositionPermissionEvaluator {
    boolean canList();

    void requireList();

    boolean canManage(PositionModel position);

    void requireManage(PositionModel position);

    boolean canView(PositionModel position);

    void requireView(PositionModel position);

    boolean canManage();

    void requireManage();

    boolean canView();

    void requireView();

    boolean getPositionsWithViewPermission(PositionModel position);

    void requireViewMembers(PositionModel position);

    boolean canManageMembers(PositionModel position);

    boolean canManageMembership(PositionModel position);

    void requireManageMembership(PositionModel position);

    Map<String, Boolean> getAccess(PositionModel position);

    Set<String> getPositionsWithViewPermission();
}
