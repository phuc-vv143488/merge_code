package org.keycloak.services.scheduled.models;

public class Constants {
	public interface API_METHOD {
		public static final String POST = "POST";
		public static final String GET = "GET";
		public static final String DELETE = "DELETE";
		public static final String PUT = "PUT";
	}

	public interface API {
		String NOT_FOUND = "NOT_FOUND";
		String STATUS_TYPE = "statusType";
		String ENTITY = "entity";
		String CONTENT = "content";
		String PER_PAGE = "1000";
		String ALL = "ALL";
		String DOMAIN = "148841";
		Long lastSyncTime = 631168388000L;
	}
}
