package org.keycloak.services.scheduled;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.scheduled.models.CommonUtils;
import org.keycloak.services.scheduled.models.Synchronized;
import org.keycloak.services.scheduled.models.UserBean;
import org.keycloak.services.scheduled.service.UserSyncService;
import org.keycloak.timer.ScheduledTask;
import org.keycloak.util.TokenUtil;

public class UserSynchronized implements ScheduledTask {
	private static final String SSO = "oidc";
	public static final String TASK_NAME = "scheduled-user";
	private static final String CHECK_CONCURRENT = "check-scheduled-user";
	private static final Logger LOGGER = Logger.getLogger(UserSynchronized.class);

	public UserSynchronized() {
	}

	@Override
	public void run(KeycloakSession session) {
		try {
			Synchronized sync = CommonUtils.getSync();
			Date startTime = new Date();
			int hour = startTime.getHours();
			LOGGER.info("hour:"+hour+" - sync.getSyncTime():"+sync.getSyncTime());
			if(hour == sync.getSyncTime()) {
				UserProvider users = session.users();
				Long totalSync = 0L;
				RealmModel realm = session.realms().getRealmByName(sync.getRealm());
				Long lastSyncTime = session.realms().getSyncInformationByTaskName(TASK_NAME);
				
				//check running for concurrency
				Long lastCheck = session.realms().getSyncInformationByTaskName(CHECK_CONCURRENT);
				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
				if(lastCheck == null || formatter.parse(formatter.format(new Date(lastCheck))) != formatter.parse(formatter.format(startTime))) {
					session.realms().addSyncInformation(realm, CHECK_CONCURRENT, totalSync, 0L, new Timestamp(startTime.getTime()),
							new Timestamp(startTime.getTime()));
					List<UserBean> userBeans = UserSyncService.getUserToSync(sync, lastSyncTime);
					for (UserBean u : userBeans) {
						UserModel user = users.getUserByUsername(u.getEmployeeCode(), realm);
						List<String> defaultRole = realm.getDefaultRoles();
						UserRepresentation userRep = getUserRep(u, realm, sync);
						LOGGER.info("Sync user " + u.getEmployeeCode());
						if (user != null) {
							updateUser(user, u, realm);

						} else {
							userRep.setCreatedTimestamp(startTime.getTime());
							FederatedIdentityRepresentation federatedIdentity = new FederatedIdentityRepresentation();
							federatedIdentity.setIdentityProvider(SSO);
							federatedIdentity.setUserId(u.getEmployeeCode());
							federatedIdentity.setUserName(u.getEmployeeCode());
							userRep.setFederatedIdentities(Arrays.asList(federatedIdentity));
							CredentialRepresentation credential = new CredentialRepresentation();
							credential.setCreatedDate(startTime.getTime());
							credential.setTemporary(false);
							credential.setType(CredentialRepresentation.PASSWORD);
							credential.setValue(TokenUtil.encrytion(u.getEmployeeCode()));
							userRep.setCredentials(Arrays.asList(credential));
							userRep.setRealmRoles(defaultRole);
							RepresentationToModel.createUser(session, realm, userRep);
						}
						totalSync++;

					}
					Date endTime = new Date();
					session.realms().addSyncInformation(realm, TASK_NAME, totalSync, 0L, new Timestamp(startTime.getTime()),
					new Timestamp(endTime.getTime()));
				}
			}		
		} catch (Exception ex) {
			LOGGER.error("ERROR sync user ", ex);
		}
	}

	private void updateUser(UserModel model, UserBean bean, RealmModel realm) {
		model.setEnabled(bean.getStatus().equals(1L));
		model.setFirstName(bean.getFirstName());
		model.setLastName(bean.getLastName());
		String positionId = realm.getPositionByVhrId(bean.getPositionId());
		model.joinPosition(positionId);
	}

	private UserRepresentation getUserRep(UserBean bean, RealmModel realm, Synchronized sync) {
		UserRepresentation userRep = new UserRepresentation();
		userRep.setUsername(bean.getEmployeeCode());
		userRep.setEmail(bean.getEmail());
		userRep.setFirstName(bean.getFirstName());
		userRep.setLastName(bean.getLastName());
		userRep.setEnabled(bean.getStatus().equals(1L));
		String positionId = realm.getPositionByVhrId(bean.getPositionId());
		userRep.setPositionId(positionId);
		return userRep;
	}

}
