package org.keycloak.services.resources.admin;

import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.PositionModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.PositionRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @resource Positions
 * @author SP_POSITION
 */
public class PositionsResource {

    private final RealmModel realm;
    private final KeycloakSession session;
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;
    protected static final Logger logger = Logger.getLogger(PositionsResource.class);

    public PositionsResource(RealmModel realm, KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.realm = realm;
        this.session = session;
        this.auth = auth;
        this.adminEvent = adminEvent.resource(ResourceType.POSITION);
    }

    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public List<PositionRepresentation> getPositionsBySearch(@QueryParam("search") String searchName,
                                                             @QueryParam("code") String searchCode,
                                                             @QueryParam("first") Integer firstResult,
                                                             @QueryParam("max") Integer maxResults,
                                                             @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation) {
        auth.positions().requireList();
        firstResult = firstResult != null ? firstResult : -1;
        maxResults = maxResults != null ? maxResults : null;

        List<PositionRepresentation> results = Collections.emptyList();
        if (Objects.nonNull(searchName) && Objects.nonNull(searchCode)) {
            results = ModelToRepresentation.searchForPositionByNameAndCode(realm, !briefRepresentation, searchName.trim(), searchCode.trim(), firstResult, maxResults);
        } else if (Objects.nonNull(searchName)) {
            results = ModelToRepresentation.searchForPositionByNameAndCode(realm, !briefRepresentation, searchName.trim(),"", firstResult, maxResults);
        } else if (Objects.nonNull(searchCode)) {
            results = ModelToRepresentation.searchForPositionByNameAndCode(realm, !briefRepresentation, "", searchCode.trim(), firstResult, maxResults);
        } else {
            results = ModelToRepresentation.searchForPositionByNameAndCode(realm, !briefRepresentation, "", "", firstResult, maxResults);
        }

        return results;
    }

    /**
     * Does not expand hierarchy.  SubPositions will not be set.
     *
     * @param id
     * @return
     */
    @Path("{posId}")
    public PositionResource getPositionById(@PathParam("posId") String id) {
        PositionModel position = realm.getPositionById(id);
        if (position == null) {
            throw new NotFoundException("Could not find position by id");
        }
        PositionResource resource =  new PositionResource(realm, position, session, this.auth, adminEvent);
        ResteasyProviderFactory.getInstance().injectProperties(resource);
        return resource;
    }

    /**
     * create or add a top level realm PositionSet or create child.  This will update the Position and set the parent if it exists.  Create it and set the parent
     * if the Position doesn't exist.
     *
     * @param rep
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addPosition(PositionRepresentation rep) {
        this.auth.positions().requireManage();

        if (ObjectUtil.isBlank(rep.getPosName())) {
            return ErrorResponse.error("Position name is missing", Response.Status.BAD_REQUEST);
        }
        if (ObjectUtil.isBlank(rep.getPosCode())) {
            return ErrorResponse.error("Position code is missing", Response.Status.BAD_REQUEST);
        }

        String code = rep.getPosCode();

        List<Integer> listCheck = realm.checkPositionByNameAndCode(rep.getPosName() , code);
        if (listCheck.get(0) == 1 && listCheck.get(1) == 1) {
            return ErrorResponse.error("Name and Code already exists ", Response.Status.BAD_REQUEST);
        }

        if (listCheck.get(0) == 1) {
            return ErrorResponse.error("Code already exists ", Response.Status.BAD_REQUEST);
        }

        if (listCheck.get(1) == 1) {
            return ErrorResponse.error("Name already exists ", Response.Status.BAD_REQUEST);
        }

        Response.ResponseBuilder builder = Response.status(204);
        PositionModel pos = null;
        if (rep.getPosId() != null) {
            throw new NotFoundException("Could not create position!");
        } else {
            pos = realm.createPosition(rep);
            URI uri = session.getContext().getUri().getBaseUriBuilder()
                    .path(session.getContext().getUri().getMatchedURIs().get(2))
                    .path(pos.getPosId()).build();
            builder.status(201).location(uri);
            rep.setPosId(pos.getPosId());
            adminEvent.operation(OperationType.CREATE);

        }
        adminEvent.resourcePath(session.getContext().getUri()).representation(rep).success();
        return builder.type(MediaType.APPLICATION_JSON_TYPE).entity(pos).build();
    }
}
