package org.keycloak.services.resources.admin;

import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.*;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.PositionRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * @resource Positions
 * @author SP_POSITION
 */
public class PositionResource {

    private final RealmModel realm;
    private final KeycloakSession session;
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;
    private final PositionModel position;
    protected static final Logger logger = Logger.getLogger(PositionResource.class);

    public PositionResource(RealmModel realm, PositionModel position, KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.realm = realm;
        this.session = session;
        this.auth = auth;
        this.adminEvent = adminEvent.resource(ResourceType.POSITION);
        this.position = position;
    }

     /**
     *
     *
     * @return
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public PositionRepresentation getPosition() {
        this.auth.positions().requireView(position);

        PositionRepresentation rep = ModelToRepresentation.toRepresentation(position, true);

        rep.setAccess(auth.positions().getAccess(position));

        return rep;
    }

    /**
     * Update position
     *
     * @param rep
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePosition(PositionRepresentation rep) {
        this.auth.positions().requireManage(position);

        updatePosition(rep, position);
        adminEvent.operation(OperationType.UPDATE).resourcePath(session.getContext().getUri()).representation(rep).success();

        return Response.noContent().build();
    }

    @DELETE
    public void deletePosition() {
        this.auth.positions().requireManage(position);

        realm.removePosition(position);
        adminEvent.operation(OperationType.DELETE).resourcePath(session.getContext().getUri()).success();
    }

    public static void updatePosition(PositionRepresentation rep, PositionModel model) {
        if (rep.getPosName() != null) model.setPosName(rep.getPosName());
        if (rep.getPosCode() != null) model.setPosCode(rep.getPosCode());
        if (rep.getDescription() != null) model.setDescription(rep.getDescription());
        if (rep.getStatus() != null) model.setStatus(rep.getStatus());
        if (rep.getType() != null) model.setType(rep.getType());
        if (rep.getMaxUserChiNhanh() != null) model.setMaxUserChiNhanh(rep.getMaxUserChiNhanh());
        if (rep.getMaxUserTrungTam() != null) model.setMaxUserTrungTam(rep.getMaxUserTrungTam());
        if (rep.getLimitType() != null) model.setLimitType(rep.getLimitType());
        if (rep.getValidDateStart() != null) model.setValidDateStart(rep.getValidDateStart());
        if (rep.getValidDateEnd() != null) model.setValidDateEnd(rep.getValidDateEnd());

        if (rep.getAttributes() != null) {
            Set<String> attrsToRemove = new HashSet<>(model.getAttributes().keySet());
            attrsToRemove.removeAll(rep.getAttributes().keySet());
            for (Map.Entry<String, List<String>> attr : rep.getAttributes().entrySet()) {
                model.setAttribute(attr.getKey(), attr.getValue());
            }

            for (String attr : attrsToRemove) {
                model.removeAttribute(attr);
            }
        }
    }

    @Path("role-mappings")
    public RoleMapperResource getRoleMappings() {
        AdminPermissionEvaluator.RequirePermissionCheck manageCheck = () -> auth.positions().requireManage(position);
        AdminPermissionEvaluator.RequirePermissionCheck viewCheck = () -> auth.positions().requireView(position);
        RoleMapperResource resource =  new RoleMapperResource(realm, auth, position, adminEvent, manageCheck, viewCheck);
        ResteasyProviderFactory.getInstance().injectProperties(resource);
        return resource;

    }

    /**
     * Get users
     *
     * Returns a list of users, filtered according to query parameters
     *
     * @param firstResult Pagination offset
     * @param maxResults Maximum results size (defaults to 100)
     * @param briefRepresentation Only return basic information (only guaranteed to return id, username, created, first and last name,
     *  email, enabled state, email verification state, federation link, and access.
     *  Note that it means that namely user attributes, required actions, and not before are not returned.)
     * @return
     */
    @GET
    @NoCache
    @Path("members")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserRepresentation> getMembers(@QueryParam("first") Integer firstResult,
                                               @QueryParam("max") Integer maxResults,
                                               @QueryParam("briefRepresentation") Boolean briefRepresentation) {
        this.auth.positions().requireViewMembers(position);

        firstResult = firstResult != null ? firstResult : 0;
        maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;
        boolean briefRepresentationB = briefRepresentation != null && briefRepresentation;

        List<UserRepresentation> results = new ArrayList<UserRepresentation>();
        List<UserModel> userModels = session.users().getPositionMembers(realm, position, firstResult, maxResults);

        for (UserModel user : userModels) {
            UserRepresentation userRep = briefRepresentationB
                    ? ModelToRepresentation.toBriefRepresentation(user)
                    : ModelToRepresentation.toRepresentation(session, realm, user);

            results.add(userRep);
        }
        return results;
    }

    /**
     * delete position of user
     */
    @GET
    @NoCache
    @Path("members/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public void deletePositionOfUser(@QueryParam("userId") String userId) {
        this.auth.positions().requireViewMembers(position);
        UserModel userModel = session.users().getPositionMember(realm, position, userId);
        if (userModel != null) {
            userModel.leavePosition();
        }
    }

    /**
     * Return object stating whether client Authorization permissions have been initialized or not and a reference
     *
     * @return
     */
//    @Path("management/permissions")
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @NoCache
//    public ManagementPermissionReference getManagementPermissions() {
//        auth.positions().requireView(position);
//
//        AdminPermissionManagement permissions = AdminPermissions.management(session, realm);
//        if (!permissions.positions().isPermissionsEnabled(position)) {
//            return new ManagementPermissionReference();
//        }
//        return toMgmtRef(position, permissions);
//    }
//
//    public static ManagementPermissionReference toMgmtRef(PositionModel position, AdminPermissionManagement permissions) {
//        ManagementPermissionReference ref = new ManagementPermissionReference();
//        ref.setEnabled(true);
//        ref.setResource(permissions.positions().resource(position).getId());
//        ref.setScopePermissions(permissions.positions().getPermissions(position));
//        return ref;
//    }


    /**
     * Return object stating whether client Authorization permissions have been initialized or not and a reference
     *
     *
     * @return initialized manage permissions reference
     */
//    @Path("management/permissions")
//    @PUT
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @NoCache
//    public ManagementPermissionReference setManagementPermissionsEnabled(ManagementPermissionReference ref) {
//        auth.positions().requireManage(position);
//        AdminPermissionManagement permissions = AdminPermissions.management(session, realm);
//        permissions.positions().setPermissionsEnabled(position, ref.isEnabled());
//        if (ref.isEnabled()) {
//            return toMgmtRef(position, permissions);
//        } else {
//            return new ManagementPermissionReference();
//        }
//    }

}

