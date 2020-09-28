package org.keycloak.representations.idm;

import java.sql.Timestamp;


public class SyncInformationRepresentation {
	protected Long id;

	private String taskName;

	private Timestamp startTime;

	private Timestamp endTime;

	private Long totalSync;
	
	private Long totalError;

	private Long createdDate;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public Long getTotalSync() {
		return totalSync;
	}

	public void setTotalSync(Long totalSync) {
		this.totalSync = totalSync;
	}

	public Long getTotalError() {
		return totalError;
	}

	public void setTotalError(Long totalError) {
		this.totalError = totalError;
	}

	public Long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Long createdDate) {
		this.createdDate = createdDate;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	private String realm;
}
