package org.keycloak.services.scheduled;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.jboss.logging.Logger;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.scheduled.models.CommonUtils;
import org.keycloak.services.scheduled.models.OrganizationBean;
import org.keycloak.services.scheduled.models.Synchronized;
import org.keycloak.services.scheduled.service.OrganizationSyncService;
import org.keycloak.timer.ScheduledTask;

public class OrganizationSynchronized implements ScheduledTask {

	public static final String TASK_NAME = "scheduled-organization";
	private static final String CHECK_CONCURRENT = "check-scheduled-organization";
	
	private static final Logger LOGGER = Logger.getLogger(OrganizationSynchronized.class);
	
	@Override
	public void run(KeycloakSession session) {
		Synchronized sync = CommonUtils.getSync();
		Date startTime = new Date();
		Long totalSync = 0L;
		int hour = startTime.getHours();
		LOGGER.info("hour:"+hour+"-- syncTime:"+startTime.getTime());
		if (hour == sync.getSyncTime()) {
			RealmModel realm = session.realms().getRealmByName(sync.getRealm());
			Long lastSyncTime = session.realms().getSyncInformationByTaskName(TASK_NAME);
			
			//check running for concurrency
			Long lastCheck = session.realms().getSyncInformationByTaskName(CHECK_CONCURRENT);
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			try {
				if(lastCheck == null || formatter.parse(formatter.format(new Date(lastCheck))) != formatter.parse(formatter.format(startTime))) {
					session.realms().addSyncInformation(realm, CHECK_CONCURRENT, totalSync, 0L, new Timestamp(startTime.getTime()),
							new Timestamp(startTime.getTime()));
					List<OrganizationBean> organizationBeans = OrganizationSyncService.getOrganizeToSync(sync, lastSyncTime);
					Collections.sort(organizationBeans, new Comparator<OrganizationBean>() {

						@Override
						public int compare(OrganizationBean o1, OrganizationBean o2) {
							if (o1.getOrgLevel() == null && o2.getOrgLevel() == null) {
								return 0;
							} else if (o1.getOrgLevel() == null) {
								return -1;
							} else if (o2.getOrgLevel() == null) {
								return 1;
							} else
								return Long.compare(o1.getOrgLevel(), o2.getOrgLevel());
						}
					});

					for (OrganizationBean bean : organizationBeans) {
						String id = realm.getGroupByVhrId(bean.getOrganizationId());
						if (id == null) {
							GroupModel groupModel = session.realms().createGroup(realm, bean.getOrganizationId().toString(),bean.getName(),"");
							update(groupModel, bean, realm);
							
						} else {
							GroupModel groupModel = session.realms().getGroupById(id, realm);
							update(groupModel, bean, realm);
						}
						
						totalSync++;
						
					}
					LOGGER.info("-------SYNC DONE !----"+ totalSync);
					Date endTime = new Date();
					session.realms().addSyncInformation(realm, TASK_NAME, totalSync, 0L, new Timestamp(startTime.getTime()),
							new Timestamp(endTime.getTime()));
				}
			} catch (ParseException ex) {
				LOGGER.error("ERROR sync position ", ex);
			}
		}
	}

	private void update(GroupModel groupModel, OrganizationBean organizationBean, RealmModel realmModel) {
		groupModel.setAddress(organizationBean.getAddress());
		groupModel.setDeptCode(organizationBean.getCode());
		groupModel.setDeptLevel(organizationBean.getOrgLevel() + "");
		groupModel.setDeptTypeId(organizationBean.getOrgTypeId());
		groupModel.setDescription(organizationBean.getDescription());
		groupModel.setVhrId(organizationBean.getOrganizationId());
		groupModel.setTelephone(organizationBean.getPhoneNumber());
		String id = realmModel.getGroupByVhrId(organizationBean.getParentId());
		if (id != null) {
			GroupModel group = realmModel.getGroupById(id);
			groupModel.setParent(group);
		}else {
			LOGGER.info("--err: no parent found for gr:"+groupModel.getId());
		}
	}

}
