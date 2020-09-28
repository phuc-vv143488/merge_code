package org.keycloak.services.resources.admin;

public class UserAuth {
	private String username;
	private String password;
	private String employeeCode;
	private String oldDept;
	private String newDept;
	private String oldPosition;
	private String newPosition;
	private int status;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmployeeCode() {
		return employeeCode;
	}
	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}
	public String getOldDept() {
		return oldDept;
	}
	public void setOldDept(String oldDept) {
		this.oldDept = oldDept;
	}
	public String getNewDept() {
		return newDept;
	}
	public void setNewDept(String newDept) {
		this.newDept = newDept;
	}
	public String getOldPosition() {
		return oldPosition;
	}
	public void setOldPosition(String oldPosition) {
		this.oldPosition = oldPosition;
	}
	public String getNewPosition() {
		return newPosition;
	}
	public void setNewPosition(String newPosition) {
		this.newPosition = newPosition;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public UserAuth(String username, String password, String employeeCode, String oldDept, String newDept,
			String oldPosition, String newPosition, int status) {
		super();
		this.username = username;
		this.password = password;
		this.employeeCode = employeeCode;
		this.oldDept = oldDept;
		this.newDept = newDept;
		this.oldPosition = oldPosition;
		this.newPosition = newPosition;
		this.status = status;
	}
	public UserAuth() {
		super();
		// TODO Auto-generated constructor stub
	}
}
