package org.keycloak.models;

import java.sql.Timestamp;

public interface SyncInformationModel {
	Long getId();

	String getTaskName();

	Timestamp getStartTime();

	Timestamp getEndTime();

	Long getTotalSync();

	Long getTotalError();

	Long getCreatedDate();
	
	String getRealm();
}
