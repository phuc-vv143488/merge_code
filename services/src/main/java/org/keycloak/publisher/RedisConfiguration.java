package org.keycloak.publisher;

public class RedisConfiguration {
	protected String host;
	protected int port;
	protected String password;
	protected int database;
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getPassword() {
		return password;
	}
	public int getDatabase() {
		return database;
	}
	public void setDatabase(int database) {
		this.database = database;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
