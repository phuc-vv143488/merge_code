package org.keycloak.services.scheduled;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.PositionModel;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.PositionRepresentation;
import org.keycloak.services.scheduled.models.CommonUtils;
import org.keycloak.services.scheduled.models.Constants;
import org.keycloak.services.scheduled.models.PositionBean;
import org.keycloak.services.scheduled.models.Synchronized;
import org.keycloak.services.scheduled.service.PositionSyncService;
import org.keycloak.timer.ScheduledTask;

public class PositionSynchronized implements ScheduledTask {
	public static final String TASK_NAME = "scheduled-position";
	private static final String CHECK_CONCURRENT = "check-scheduled-position";
	
	private Long START_TIME = 0L;

	private static final Logger LOGGER = Logger.getLogger(PositionSynchronized.class);

	@Override
	public void run(KeycloakSession session) {
		Synchronized sync = CommonUtils.getSync();
		Date startTime = new Date();
		int hour = startTime.getHours();
		if(hour == sync.getSyncTime()) {

			Long totalSync = 0L;
			RealmModel realm = session.realms().getRealmByName(sync.getRealm());
			
			//check running for concurrency
			Long lastCheck = session.realms().getSyncInformationByTaskName(CHECK_CONCURRENT);
			LOGGER.info("lastCheck:"+lastCheck);
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			try {
				if(lastCheck == null || formatter.parse(formatter.format(new Date(lastCheck))) != formatter.parse(formatter.format(startTime))) {
					session.realms().addSyncInformation(realm, CHECK_CONCURRENT, totalSync, 0L, new Timestamp(startTime.getTime()),
						new Timestamp(startTime.getTime()));
					List<PositionBean> positionBeans = PositionSyncService.getDataSync(START_TIME, 0L, Constants.API.ALL, sync);
					
					for (PositionBean bean : positionBeans) {
						String positionId = session.realms().getPositionByVhrId(bean.getPositionId(), realm);
						if (CommonUtils.isNoneBlank(positionId)) {
							LOGGER.info("Update position");
							PositionModel model =  session.realms().getPositionById(positionId, realm);
							update(model, bean);
						}
						else {
							PositionRepresentation positionRepresentation = getPositionRep(bean);
							session.realms().createPosition(realm, positionRepresentation);
						}
						LOGGER.info("SYNC POSITION  "+ bean.getPositionName());
						totalSync++;
						
					}
					LOGGER.info("totalSync:  "+totalSync);
					Date endTime = new Date();
					session.realms().addSyncInformation(realm, TASK_NAME, totalSync, 0L, new Timestamp(startTime.getTime()),
							new Timestamp(endTime.getTime()));
				}
			} catch (ParseException ex) {
				LOGGER.error("ERROR sync position ", ex);
			}
		}
	}

	private PositionRepresentation getPositionRep(PositionBean bean) {
		PositionRepresentation rep = new PositionRepresentation();
		rep.setPosCode(bean.getPositionCode());
		rep.setPosName(bean.getPositionName());
		rep.setVhrId(bean.getPositionId());
		rep.setType(CommonUtils.getLong(bean.getType()));
		
		return rep;
	}
	
	private void update(PositionModel model, PositionBean bean) {
		model.setPosCode(bean.getPositionCode());
		model.setPosName(bean.getPositionName());
		model.setType(CommonUtils.getLong(bean.getType()));
		model.setDescription(bean.getDescription());
		model.setVhrId(bean.getPositionId());;
	}

}
