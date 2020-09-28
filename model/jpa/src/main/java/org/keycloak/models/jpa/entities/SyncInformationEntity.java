package org.keycloak.models.jpa.entities;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@NamedQueries({
	@NamedQuery(name="getSyncInformationByTaskName", query="select s.createdDate from SyncInformationEntity s where s.taskName = :taskName order by s.createdDate DESC")
})
@Entity
@Table(name="SYNC_INFORMATION")
public class SyncInformationEntity {
	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy  = GenerationType.IDENTITY)
	protected Long id;

	@Column(name = "TASK_NAME")
	private String taskName;

	@Column(name = "START_TIME")
	private Timestamp startTime;

	@Column(name = "END_TIME")
	private Timestamp endTime;

	@Column(name = "TOTAL_SYNC")
	private Long totalSync;
	
	@Column(name = "TOTAL_ERROR")
	private Long totalError;

	@Column(name = "CREATED_DATE")
	private Long createdDate;
	
	@Column(name =  "REALM")
	private String realm;

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
}
