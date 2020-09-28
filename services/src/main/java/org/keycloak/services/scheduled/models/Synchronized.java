package org.keycloak.services.scheduled.models;

public class Synchronized {
	protected String realm;
	protected String clientId;
	protected String clientSecret;
	protected String userName;
	protected String password;
	protected String baseUrl;
	protected int syncTime;

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public int getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(int syncTime) {
		this.syncTime = syncTime;
	}

}
