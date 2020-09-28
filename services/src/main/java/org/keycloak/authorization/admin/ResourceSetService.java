/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.authorization.admin;

import static org.keycloak.models.utils.ModelToRepresentation.toRepresentation;
import static org.keycloak.models.utils.RepresentationToModel.toModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.OAuthErrorException;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.ResourceStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.common.util.PathMatcher;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ResourceOwnerRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.RoleContainerResource;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ResourceSetService {
	protected static final Logger LOGGER = Logger.getLogger(ResourceSetService.class);

    private final AuthorizationProvider authorization;
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;
    private KeycloakSession session;
    private ResourceServer resourceServer;
    private final static String MENU = "menu";
    private final static String MENU_SIGN = "*";
    protected static final Logger logger = Logger.getLogger(ResourceSetService.class);
    public ResourceSetService(KeycloakSession session, ResourceServer resourceServer, AuthorizationProvider authorization, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.resourceServer = resourceServer;
        this.authorization = authorization;
        this.auth = auth;
        this.adminEvent = adminEvent.resource(ResourceType.AUTHORIZATION_RESOURCE);
    }

    @POST
    @NoCache
    @Consumes("application/json")
    @Produces("application/json")
    public Response createPost(ResourceRepresentation resource) {
    	logger.info("createPost function()");
        if (resource == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        ResourceRepresentation newResource = create(resource);

        audit(resource, resource.getId(), OperationType.CREATE);
        
        //huynq79
        //auto generate code for roles,policies and permissions
        RealmModel realm = authorization.getKeycloakSession().getContext().getRealm();
        ClientModel client = realm.getClientById(resourceServer.getId());
        RoleContainerResource autoGenRole = new RoleContainerResource(session, session.getContext().getUri(), realm, auth, client, adminEvent);
        PolicyService autoGenPol = new PolicyService(this.resourceServer, this.authorization, this.auth, adminEvent);
        //huynq79 
        //auto gen roles,policies and permissions for menu
        if (resource.getType().equalsIgnoreCase(MENU)) {
        	//gen roles
        	if (!autoGenRole.haveRole(MENU_SIGN+resource.getName())) 
        		autoGenRole.createRole(new RoleRepresentation(MENU_SIGN+resource.getName(),null,false));
        	
        	//gen policies
        	String payload;
        	Policy policy = authorization.getStoreFactory().getPolicyStore().findByName(MENU_SIGN+resource.getName(), this.resourceServer.getId());
        	if (policy == null) {
	        	RoleRepresentation roleCreated = autoGenRole.getRole(MENU_SIGN+resource.getName());
	        	payload = "{\"type\":\"role\",\"logic\":\"POSITIVE\",\"decisionStrategy\":\"UNANIMOUS\",\"name\":\""+MENU_SIGN+resource.getName()+"\",\"roles\":[{\"id\":\""+roleCreated.getId()+"\"}]}";
	        	autoGenPol = (PolicyService) autoGenPol.getResource("role");
	        	autoGenPol.create(payload, session);
        	}
        	
        	//gen permissions
        	Policy permission = authorization.getStoreFactory().getPolicyStore().findByName("."+MENU_SIGN+resource.getName(), this.resourceServer.getId());
        	if (permission == null) {
	        	Policy model = authorization.getStoreFactory().getPolicyStore().findByName(MENU_SIGN+resource.getName(), this.resourceServer.getId());
	        	payload = "{\"type\":\"resource\",\"logic\":\"POSITIVE\",\"decisionStrategy\":\"UNANIMOUS\",\"name\":\""+"."+MENU_SIGN+resource.getName()+"\",\"resources\":[\""+resource.getId()+"\"],\"policies\":[\""+model.getId()+"\"]}";
	        	autoGenPol = (PolicyService) autoGenPol.getResource("resource");
	        	autoGenPol.create(payload, session);
        	}
        	
        }else {
	        for (ScopeRepresentation scope:resource.getScopes()) {
	        	//gen roles
	        	if (!autoGenRole.haveRole(resource.getName()+" "+scope.getName())) 
	        		autoGenRole.createRole(new RoleRepresentation(resource.getName()+" "+scope.getName(),null,false));
	        	
	        	//gen policies
	        	String payload;
	        	Policy policy = authorization.getStoreFactory().getPolicyStore().findByName(resource.getName()+" "+scope.getName(), this.resourceServer.getId());
	        	if (policy == null) {
		        	RoleRepresentation roleCreated = autoGenRole.getRole(resource.getName()+" "+scope.getName());
		        	payload = "{\"type\":\"role\",\"logic\":\"POSITIVE\",\"decisionStrategy\":\"UNANIMOUS\",\"name\":\""+resource.getName()+" "+scope.getName()+"\",\"roles\":[{\"id\":\""+roleCreated.getId()+"\"}]}";
		        	autoGenPol = (PolicyService) autoGenPol.getResource("role");
		        	autoGenPol.create(payload, session);
	        	}
	        	
	        	//gen permissions
	        	Policy permission = authorization.getStoreFactory().getPolicyStore().findByName("."+resource.getName()+" "+scope.getName(), this.resourceServer.getId());
	        	if (permission == null) {
		        	Policy model = authorization.getStoreFactory().getPolicyStore().findByName(resource.getName()+" "+scope.getName(), this.resourceServer.getId());
		        	payload = "{\"type\":\"scope\",\"logic\":\"POSITIVE\",\"decisionStrategy\":\"UNANIMOUS\",\"name\":\""+"."+resource.getName()+" "+scope.getName()+"\",\"resources\":[\""+resource.getId()+"\"],\"scopes\":[\""+scope.getId()+"\"],\"policies\":[\""+model.getId()+"\"]}";
		        	autoGenPol = (PolicyService) autoGenPol.getResource("scope");
		        	autoGenPol.create(payload, session);
	        	}
	        }
        }
        return Response.status(Status.CREATED).entity(newResource).build();
    }

    public ResourceRepresentation create(ResourceRepresentation resource) {
        requireManage();
        StoreFactory storeFactory = this.authorization.getStoreFactory();
        ResourceOwnerRepresentation owner = resource.getOwner();

        if (owner == null) {
            owner = new ResourceOwnerRepresentation();
            owner.setId(resourceServer.getId());
            resource.setOwner(owner);
        }

        String ownerId = owner.getId();

        if (ownerId == null) {
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "You must specify the resource owner.", Status.BAD_REQUEST);
        }

        Resource existingResource = storeFactory.getResourceStore().findByName(resource.getName(), ownerId, this.resourceServer.getId());

        if (existingResource != null) {
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "Resource with name [" + resource.getName() + "] already exists.", Status.CONFLICT);
        }

        return toRepresentation(toModel(resource, this.resourceServer, authorization), resourceServer, authorization);
    }

    @Path("{id}")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response update(@PathParam("id") String id, ResourceRepresentation resource) {
        requireManage();
        resource.setId(id);
        StoreFactory storeFactory = this.authorization.getStoreFactory();
        ResourceStore resourceStore = storeFactory.getResourceStore();
        Resource model = resourceStore.findById(resource.getId(), resourceServer.getId());

        if (model == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        //huynq79
        
        if (resource.getName().equalsIgnoreCase(model.getName())) {
	        //auto update code for roles,policies and permission
	        RealmModel realm = authorization.getKeycloakSession().getContext().getRealm();
	        ClientModel client = realm.getClientById(resourceServer.getId());
	        RoleContainerResource autoGenRole = new RoleContainerResource(session, session.getContext().getUri(), realm, auth, client, adminEvent);
	        PolicyService autoGenPol = new PolicyService(this.resourceServer, this.authorization, this.auth, adminEvent);
        
	        //if some scopes added
	        for (ScopeRepresentation scope:resource.getScopes()) {
	        		//new scope added
	        		//gen roles
	            	if (!autoGenRole.haveRole(resource.getName()+" "+scope.getName())) {
	            		autoGenRole.createRole(new RoleRepresentation(resource.getName()+" "+scope.getName(),null,false));
	            	}
	            	
	            	//gen policies
	            	String payload;
	            	Policy policy = authorization.getStoreFactory().getPolicyStore().findByName(resource.getName()+" "+scope.getName(), this.resourceServer.getId());
	            	if (policy == null) {
	    	        	RoleRepresentation roleCreated = autoGenRole.getRole(resource.getName()+" "+scope.getName());
	    	        	payload = "{\"type\":\"role\",\"logic\":\"POSITIVE\",\"decisionStrategy\":\"UNANIMOUS\",\"name\":\""+resource.getName()+" "+scope.getName()+"\",\"roles\":[{\"id\":\""+roleCreated.getId()+"\"}]}";
	    	        	autoGenPol = (PolicyService) autoGenPol.getResource("role");
	    	        	autoGenPol.create(payload, session);
	            	}
	            	
	            	//gen permissions
	            	Policy permission = authorization.getStoreFactory().getPolicyStore().findByName("."+resource.getName()+" "+scope.getName(), this.resourceServer.getId());
	            	if (permission == null) {
	    	        	Policy mmodel = authorization.getStoreFactory().getPolicyStore().findByName(resource.getName()+" "+scope.getName(), this.resourceServer.getId());
	    	        	payload = "{\"type\":\"scope\",\"logic\":\"POSITIVE\",\"decisionStrategy\":\"UNANIMOUS\",\"name\":\""+"."+resource.getName()+" "+scope.getName()+"\",\"resources\":[\""+resource.getId()+"\"],\"scopes\":[\""+scope.getId()+"\"],\"policies\":[\""+mmodel.getId()+"\"]}";
	    	        	autoGenPol = (PolicyService) autoGenPol.getResource("scope");
	    	        	autoGenPol.create(payload, session);
	            	}
	        }
	        
	        //if some scopes deleted
	        for (Scope scope:model.getScopes()) {
	        	boolean flag = false;
	        	for(ScopeRepresentation mscope:resource.getScopes()) {
	        		if(mscope.getName().equalsIgnoreCase(scope.getName())) {
	        			flag = true;
	        			break;
	        		}
	        	}
	        	if (!flag) {
	        		//delete permissions
	        		Policy permission = authorization.getStoreFactory().getPolicyStore().findByName("."+model.getName()+" "+scope.getName(), this.resourceServer.getId());
	            	if (permission != null) {
	    	        	PolicyResourceService policyResourceService = (PolicyResourceService) autoGenPol.getResource(permission.getId());
	    	        	policyResourceService.delete();
	            	}
	        		//delete policies
	            	Policy policy = authorization.getStoreFactory().getPolicyStore().findByName(model.getName()+" "+scope.getName(), this.resourceServer.getId());
	            	if (policy != null) {
	    	        	PolicyResourceService policyResourceService = (PolicyResourceService) autoGenPol.getResource(policy.getId());
	    	        	policyResourceService.delete();
	            	}
	            	//delete roles
	            	if (autoGenRole.haveRole(model.getName()+" "+scope.getName())) {
	            		autoGenRole.deleteRole(resource.getName()+" "+scope.getName());
	            	}	
	        	}
        	}

        }else {
        	logger.info("change resource Name");
        	delete(model.getId());
        	createPost(resource);
        }

        toModel(resource, resourceServer, authorization);

        audit(resource, OperationType.UPDATE);
        return Response.noContent().build();
    }

    @Path("{id}")
    @DELETE
    public Response delete(@PathParam("id") String id) {
    	logger.info("delete function");
        requireManage();
        StoreFactory storeFactory = authorization.getStoreFactory();
        Resource resource = storeFactory.getResourceStore().findById(id, resourceServer.getId());
        if (resource == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        //huynq79
        //auto delete code for roles and policies
        RealmModel realm = authorization.getKeycloakSession().getContext().getRealm();
        ClientModel client = realm.getClientById(resourceServer.getId());
        RoleContainerResource autoGenRole = new RoleContainerResource(session, session.getContext().getUri(), realm, auth, client, adminEvent);
        PolicyService autoGenPol = new PolicyService(this.resourceServer, this.authorization, this.auth, adminEvent);
      //for menu
        if(resource.getType().equalsIgnoreCase(MENU)) {
        	//delete policies
        	Policy policy = authorization.getStoreFactory().getPolicyStore().findByName(MENU_SIGN+resource.getName(), this.resourceServer.getId());
        	if (policy != null) {
	        	PolicyResourceService policyResourceService = (PolicyResourceService) autoGenPol.getResource(policy.getId());
	        	policyResourceService.delete();
        	}
        	//delete roles
        	if (autoGenRole.haveRole(MENU_SIGN+resource.getName())) {
        		autoGenRole.deleteRole(resource.getName());
        	}
        }else {
	        for (Scope scope:resource.getScopes()) {
	        	//delete policies
	        	Policy policy = authorization.getStoreFactory().getPolicyStore().findByName(resource.getName()+" "+scope.getName(), this.resourceServer.getId());
	        	if (policy != null) {
		        	PolicyResourceService policyResourceService = (PolicyResourceService) autoGenPol.getResource(policy.getId());
		        	policyResourceService.delete();
	        	}
	        	//delete roles
	        	if (autoGenRole.haveRole(resource.getName()+" "+scope.getName())) {
	        		autoGenRole.deleteRole(resource.getName()+" "+scope.getName());
	        	}
	        }
        }
        storeFactory.getResourceStore().delete(id);

        audit(toRepresentation(resource, resourceServer, authorization), OperationType.DELETE);
        return Response.noContent().build();
    }

    @Path("{id}")
    @GET
    @NoCache
    @Produces("application/json")
    public Response findById(@PathParam("id") String id) {
    	
        return findById(id, resource -> toRepresentation(resource, resourceServer, authorization, true));
    }

    public Response findById(String id, Function<Resource, ? extends ResourceRepresentation> toRepresentation) {
        requireView();
        StoreFactory storeFactory = authorization.getStoreFactory();
        Resource model = storeFactory.getResourceStore().findById(id, resourceServer.getId());
        LOGGER.info("sort_order :"+model.getOrder());
        LOGGER.info("parentId :"+model.getParentId());
        if (model == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(toRepresentation.apply(model)).build();
    }

    @Path("{id}/scopes")
    @GET
    @NoCache
    @Produces("application/json")
    public Response getScopes(@PathParam("id") String id) {
        requireView();
        StoreFactory storeFactory = authorization.getStoreFactory();
        Resource model = storeFactory.getResourceStore().findById(id, resourceServer.getId());

        if (model == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        List<ScopeRepresentation> scopes = model.getScopes().stream().map(scope -> {
            ScopeRepresentation representation = new ScopeRepresentation();

            representation.setId(scope.getId());
            representation.setName(scope.getName());

            return representation;
        }).collect(Collectors.toList());

        if (model.getType() != null && !model.getOwner().equals(resourceServer.getId())) {
            ResourceStore resourceStore = authorization.getStoreFactory().getResourceStore();
            for (Resource typed : resourceStore.findByType(model.getType(), resourceServer.getId())) {
                if (typed.getOwner().equals(resourceServer.getId()) && !typed.getId().equals(model.getId())) {
                    scopes.addAll(typed.getScopes().stream().map(model1 -> {
                        ScopeRepresentation scope = new ScopeRepresentation();
                        scope.setId(model1.getId());
                        scope.setName(model1.getName());
                        String iconUri = model1.getIconUri();
                        if (iconUri != null) {
                            scope.setIconUri(iconUri);
                        }
                        return scope;
                    }).filter(scopeRepresentation -> !scopes.contains(scopeRepresentation)).collect(Collectors.toList()));
                }
            }
        }

        return Response.ok(scopes).build();
    }

    @Path("{id}/permissions")
    @GET
    @NoCache
    @Produces("application/json")
    public Response getPermissions(@PathParam("id") String id) {
        requireView();
        StoreFactory storeFactory = authorization.getStoreFactory();
        ResourceStore resourceStore = storeFactory.getResourceStore();
        Resource model = resourceStore.findById(id, resourceServer.getId());

        if (model == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        PolicyStore policyStore = authorization.getStoreFactory().getPolicyStore();
        Set<Policy> policies = new HashSet<>();

        policies.addAll(policyStore.findByResource(model.getId(), resourceServer.getId()));

        if (model.getType() != null) {
            policies.addAll(policyStore.findByResourceType(model.getType(), resourceServer.getId()));

            HashMap<String, String[]> resourceFilter = new HashMap<>();

            resourceFilter.put("owner", new String[]{resourceServer.getId()});
            resourceFilter.put("type", new String[]{model.getType()});

            for (Resource resourceType : resourceStore.findByResourceServer(resourceFilter, resourceServer.getId(), -1, -1)) {
                policies.addAll(policyStore.findByResource(resourceType.getId(), resourceServer.getId()));
            }
        }

        policies.addAll(policyStore.findByScopeIds(model.getScopes().stream().map(scope -> scope.getId()).collect(Collectors.toList()), id, resourceServer.getId()));
        policies.addAll(policyStore.findByScopeIds(model.getScopes().stream().map(scope -> scope.getId()).collect(Collectors.toList()), null, resourceServer.getId()));

        List<PolicyRepresentation> representation = new ArrayList<>();

        for (Policy policyModel : policies) {
            if (!"uma".equalsIgnoreCase(policyModel.getType())) {
                PolicyRepresentation policy = new PolicyRepresentation();

                policy.setId(policyModel.getId());
                policy.setName(policyModel.getName());
                policy.setType(policyModel.getType());

                if (!representation.contains(policy)) {
                    representation.add(policy);
                }
            }
        }

        return Response.ok(representation).build();
    }

    @Path("{id}/attributes")
    @GET
    @NoCache
    @Produces("application/json")
    public Response getAttributes(@PathParam("id") String id) {
        requireView();
        StoreFactory storeFactory = authorization.getStoreFactory();
        Resource model = storeFactory.getResourceStore().findById(id, resourceServer.getId());

        if (model == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(model.getAttributes()).build();
    }

    @Path("/search")
    @GET
    @NoCache
    @Produces("application/json")
    public Response find(@QueryParam("name") String name) {
        this.auth.realm().requireViewAuthorization();
        StoreFactory storeFactory = authorization.getStoreFactory();

        if (name == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        Resource model = storeFactory.getResourceStore().findByName(name, this.resourceServer.getId());

        if (model == null) {
            return Response.status(Status.OK).build();
        }

        return Response.ok(toRepresentation(model, this.resourceServer, authorization)).build();
    }

    @GET
    @NoCache
    @Produces("application/json")
    public Response find(@QueryParam("_id") String id,
                         @QueryParam("name") String name,
                         @QueryParam("uri") String uri,
                         @QueryParam("owner") String owner,
                         @QueryParam("type") String type,
                         @QueryParam("scope") String scope,
                         @QueryParam("matchingUri") Boolean matchingUri,
                         @QueryParam("exactName") Boolean exactName,
                         @QueryParam("deep") Boolean deep,
                         @QueryParam("first") Integer firstResult,
                         @QueryParam("max") Integer maxResult,
                         @QueryParam("parentId") String parentId ) {
        return find(id, name, uri, owner, type, scope, matchingUri, exactName, deep, firstResult, maxResult, parentId, (BiFunction<Resource, Boolean, ResourceRepresentation>) (resource, deep1) -> toRepresentation(resource, resourceServer, authorization, deep1));
    }

    public Response find(@QueryParam("_id") String id,
                         @QueryParam("name") String name,
                         @QueryParam("uri") String uri,
                         @QueryParam("owner") String owner,
                         @QueryParam("type") String type,
                         @QueryParam("scope") String scope,
                         @QueryParam("matchingUri") Boolean matchingUri,
                         @QueryParam("exactName") Boolean exactName,
                         @QueryParam("deep") Boolean deep,
                         @QueryParam("first") Integer firstResult,
                         @QueryParam("max") Integer maxResult,
                         @QueryParam("parentId") String parentId,
                         BiFunction<Resource, Boolean, ?> toRepresentation) {
        requireView();
        logger.info("TYPE  "+type);
        StoreFactory storeFactory = authorization.getStoreFactory();

        if (deep == null) {
            deep = true;
        }

        Map<String, String[]> search = new HashMap<>();

        if (id != null && !"".equals(id.trim())) {
            search.put("id", new String[] {id});
        }

        if (name != null && !"".equals(name.trim())) {
            search.put("name", new String[] {name});
            
            if (exactName != null && exactName) {
                search.put(Resource.EXACT_NAME, new String[] {Boolean.TRUE.toString()});
            }
        }

        if (uri != null && !"".equals(uri.trim())) {
            search.put("uri", new String[] {uri});
        }

        if (owner != null && !"".equals(owner.trim())) {
            RealmModel realm = authorization.getKeycloakSession().getContext().getRealm();
            ClientModel clientModel = realm.getClientByClientId(owner);

            if (clientModel != null) {
                owner = clientModel.getId();
            } else {
                UserModel user = authorization.getKeycloakSession().users().getUserByUsername(owner, realm);

                if (user != null) {
                    owner = user.getId();
                }
            }

            search.put("owner", new String[] {owner});
        }

        if (type != null && !"".equals(type.trim())) {
            search.put("type", new String[] {type});
        }

        if (scope != null && !"".equals(scope.trim())) {
            HashMap<String, String[]> scopeFilter = new HashMap<>();

            scopeFilter.put("name", new String[] {scope});

            List<Scope> scopes = authorization.getStoreFactory().getScopeStore().findByResourceServer(scopeFilter, resourceServer.getId(), -1, -1);

            if (scopes.isEmpty()) {
                return Response.ok(Collections.emptyList()).build();
            }

            search.put("scope", scopes.stream().map(Scope::getId).toArray(String[]::new));
        }
        List<Resource> resources = new ArrayList<Resource>();
        logger.info("parentId  "+parentId);
        
        if(parentId == null || !type.equalsIgnoreCase(MENU)){
        	resources = storeFactory.getResourceStore().findByResourceServer(search, this.resourceServer.getId(), firstResult != null ? firstResult : -1, maxResult != null ? maxResult : Constants.DEFAULT_MAX_RESULTS);		
        }
        else if(type.equalsIgnoreCase(MENU)) {
        	List<Resource> resourceTypeMenus = storeFactory.getResourceStore().findByType(MENU,  this.resourceServer.getId());
        	logger.info("number of resourceTypeMenus:"+resourceTypeMenus.size());
        	logger.info("resourceServerId:"+this.resourceServer.getId());
        	for(Resource e : resourceTypeMenus) {
        		logger.info("Resource: "+e.getName());
        		logger.info("Resource parentId: "+e.getParentId());
        		logger.info("Resource order: "+e.getOrder());
        		if(e.getParentId() != null && parentId.equalsIgnoreCase(e.getParentId())) {
        			logger.info("add to resource...");
        			resources.add(e);
        		}
        	}
        }
        
        if (matchingUri != null && matchingUri && resources.isEmpty()) {
        	logger.info("matchingUri:"+matchingUri);
            HashMap<String, String[]> attributes = new HashMap<>();

            attributes.put("uri_not_null", new String[] {"true"});
            attributes.put("owner", new String[] {resourceServer.getId()});

            List<Resource> serverResources = storeFactory.getResourceStore().findByResourceServer(attributes, this.resourceServer.getId(), firstResult != null ? firstResult : -1, maxResult != null ? maxResult : -1);

            PathMatcher<Map.Entry<String, Resource>> pathMatcher = new PathMatcher<Map.Entry<String, Resource>>() {
                @Override
                protected String getPath(Map.Entry<String, Resource> entry) {
                    return entry.getKey();
                }

                @Override
                protected Collection<Map.Entry<String, Resource>> getPaths() {
                    Map<String, Resource> result = new HashMap<>();
                    serverResources.forEach(resource -> resource.getUris().forEach(uri -> {
                        result.put(uri, resource);
                    }));

                    return result.entrySet();
                }
            };

            Map.Entry<String, Resource> matches = pathMatcher.matches(uri);

            if (matches != null) {
                resources = Collections.singletonList(matches.getValue());
            }
        }

        Boolean finalDeep = deep;

        return Response.ok(
                resources.stream()
                        .map(resource -> toRepresentation.apply(resource, finalDeep))
                        .collect(Collectors.toList()))
                .build();
    }

    private void requireView() {
        if (this.auth != null) {
            this.auth.realm().requireViewAuthorization();
        }
    }

    private void requireManage() {
        if (this.auth != null) {
            this.auth.realm().requireManageAuthorization();
        }
    }

    private void audit(ResourceRepresentation resource, OperationType operation) {
        audit(resource, null, operation);
    }

    public void audit(ResourceRepresentation resource, String id, OperationType operation) {
        if (id != null) {
            adminEvent.operation(operation).resourcePath(session.getContext().getUri(), id).representation(resource).success();
        } else {
            adminEvent.operation(operation).resourcePath(session.getContext().getUri()).representation(resource).success();
        }
    }
}
