package org.keycloak.services.resources;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.AuthorizationService;
import org.keycloak.credential.PasswordCredentialProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.ResponseEntity;
import org.keycloak.services.resources.admin.UserAuth;
import org.keycloak.utils.MediaType;

@Path("/api/v2")
public class UserService {

	protected static final Logger logger = Logger.getLogger(UserService.class);

	 @Context
	 protected KeycloakSession session;
	public UserService() {
		
	}
	
	@GET
    @Path("/{realm}/test")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getData(final @PathParam("realm") String realmName, final UserAuth userAuth) {
		RealmModel realm = init(realmName);
		UserModel user = session.users().getUserByUsername(userAuth.getUsername(), realm);
		if(user == null) {
			return Response.ok( ResponseEntity.error("1", "Username or password is incorrect")).build();
		}
		PasswordCredentialProvider credentialProvider = new PasswordCredentialProvider(session);
		PasswordCredentialModel password = credentialProvider.getPassword(realm, user);
		PasswordCredentialModel userPassword = PasswordCredentialModel.createFromValues(password.getPasswordCredentialData().getAlgorithm(), password.getPasswordSecretData().getSalt(), password.getPasswordCredentialData().getHashIterations(), userAuth.getPassword());
		if(!password.getCredentialData().equalsIgnoreCase(userPassword.getCredentialData())) {
			return Response.ok( ResponseEntity.error("2", "Username or password is incorrect")).build();	
		}
		
		return Response.ok( ResponseEntity.success()).build();
		
	}
	
	 private RealmModel init(String realmName) {
	        RealmManager realmManager = new RealmManager(session);
	        RealmModel realm = realmManager.getRealmByName(realmName);
	        if (realm == null) {
	            throw new NotFoundException("Realm does not exist");
	        }
	        session.getContext().setRealm(realm);
	        return realm;
	    }
}
