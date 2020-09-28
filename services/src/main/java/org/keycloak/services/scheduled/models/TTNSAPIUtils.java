package org.keycloak.services.scheduled.models;

import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.logging.Logger;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class TTNSAPIUtils {
	private static String baseUrl;
	private static String clientId;
	private static  String clientSecret;
	private String userName;
	private String password;
	private static final int RECALL_NUMBER = 5;
	private int callNumber = 0;
	private static Long CREATED_REFRESH_TOKEN_TIME = 0L;
	private static Long EXPIRES_REFRESH_TOKEN_TIME = 0L;
	private static Long TIME_WINDOW = 2000L;
	private static String API_KEY_NAME = "X-Gravitee-Api-Key";
	private static String API_KEY_VALUES = "cc4e0f41-3e01-49fd-aa13-55d4257093c4";

	
	 private static final Logger LOGGER = Logger.getLogger(TTNSAPIUtils.class);

	public TTNSAPIUtils(Synchronized sync) {
		init(sync);
	}
	private void init(Synchronized sync) {
		try {
			clientId = sync.getClientId();
			clientSecret = sync.getClientSecret();
			userName = sync.getUserName();
			password = sync.getPassword();
			baseUrl = sync.getBaseUrl();
			if (!CommonUtils.isNoneBlank(baseUrl)) {
				throw new Exception("base Url is null or empty");
			}
			if (baseUrl.startsWith("https")) {
				CommonUtils.disableSslVerification();
			}
		} catch (Exception ex) {
			LOGGER.error("Init TTNS APIs ERROR: ", ex);
		}
	}

	private static String TTNS_API_REFRESH_TOKEN;

	private static synchronized void changeRefreshToken(String str, Long expireTime) {
		Date now = new Date();
		TTNS_API_REFRESH_TOKEN = str;
		CREATED_REFRESH_TOKEN_TIME = now.getTime();
		EXPIRES_REFRESH_TOKEN_TIME = expireTime * 1000;
	}

	private static String TTNS_API_ACCESS_TOKEN;

	private static synchronized void changeAccessToken(String str) {
		TTNS_API_ACCESS_TOKEN = str;
	}

	/**
	 * Get refresh token.
	 * 
	 * @return
	 */
	private String getRefreshToken() {
		String oauthClient = "";
		Date now = new Date();
		try {
			if (CREATED_REFRESH_TOKEN_TIME + EXPIRES_REFRESH_TOKEN_TIME
					- TIME_WINDOW < now.getTime()) {
				callNumber++;
				Client client = Client.create();
				WebResource webResource = client.resource(baseUrl + "/oauth/token");
				String payLoad = "grant_type=password&client_id=" + clientId + "&client_secret=" + clientSecret
						+ "&username=" + userName + "&password=" + password;
				ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED)
						.header(API_KEY_NAME, API_KEY_VALUES).post(ClientResponse.class, payLoad);
				oauthClient = response.getEntity(String.class);
				Gson gson = new Gson();
				OauthModel oauth = gson.fromJson(oauthClient, OauthModel.class);
				if (CommonUtils.isNoneBlank(oauth.getRefresh_token())) {
					callNumber = 0;
					changeRefreshToken(oauth.getRefresh_token(), oauth.getExpires_in());
					changeAccessToken(oauth.getAccess_token());
					LOGGER.info("TTNSAPIUtils: Change refresh token success!");
					return oauth.getRefresh_token();
				} else {
					LOGGER.error("TTNSAPIUtils: Change refresh token error, result: " + oauthClient);
					throw new Exception("GET refresh token ERROR");
				}
			} else {
				return TTNS_API_REFRESH_TOKEN;
			}
		} catch (Exception e) {
			LOGGER.error("TTNSAPIUtils: Change refresh token error, result: " + oauthClient);
			if (callNumber <= RECALL_NUMBER) {
				return getRefreshToken();
			} else {
				LOGGER.error("GET refresh token ERROR: ", e);
				return null;
			}
		}
	}

	/**
	 * Call TTNS APIs.
	 * 
	 * @param formData
	 * @param serviceUrl
	 * @param method
	 * @return
	 */
	public ClientResponse getResponse(MultivaluedMap formData, String serviceUrl, String method) {
		try {
			callNumber++;
			Client client = Client.create();
			WebResource webResource = client.resource(this.baseUrl);
			ClientResponse response;
			if (formData != null) {
				if (Constants.API_METHOD.GET.equals(method)) {
					response = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).get(ClientResponse.class);
				} else if (Constants.API_METHOD.POST.equals(method)) {
					response = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).post(ClientResponse.class);
				} else if (Constants.API_METHOD.PUT.equals(method)) {
					response = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).put(ClientResponse.class);
				} else if (Constants.API_METHOD.DELETE.equals(method)) {
					response = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).delete(ClientResponse.class);
				} else {
					response = null;
				}
			} else {
				if (Constants.API_METHOD.GET.equals(method)) {
					response = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).get(ClientResponse.class);
				} else if (Constants.API_METHOD.POST.equals(method)) {
					response = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).post(ClientResponse.class);
				} else if (Constants.API_METHOD.PUT.equals(method)) {
					response = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).put(ClientResponse.class);
				} else if (Constants.API_METHOD.DELETE.equals(method)) {
					response = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).delete(ClientResponse.class);
				} else {
					response = null;
				}
			}
			String result = "";
			if (response != null) {
				result = response.getEntity(String.class);
			}
			LOGGER.debug("Call service " + method + " " + baseUrl + serviceUrl + " fromData: "
					+ String.valueOf(formData) + " return: " + result);
			if ((CommonUtils.isNoneBlank(result) && result.contains("invalid_grant"))
					|| (CommonUtils.isNoneBlank(result)&& result.contains("invalid_token"))) {
				getRefreshToken();
				throw new Exception(serviceUrl + " ERROR");
			} else {
				callNumber = 0;
				return response;
			}
		} catch (Exception ex) {
			if (callNumber <= RECALL_NUMBER) {
				return getResponse(formData, serviceUrl, method);
			} else {
				LOGGER.error(serviceUrl + " ERROR", ex);
				return null;
			}
		}
	}

	/**
	 * Call TTNS APIs.
	 * 
	 * @param formData
	 * @param serviceUrl
	 * @param method
	 * @return
	 */
	public String callService(MultivaluedMap formData, String serviceUrl, String method) {
		try {
			callNumber++;
			LOGGER.info("callService 3 arg:"+callNumber);
			Client client = Client.create();
			WebResource webResource = client.resource(this.baseUrl);
			final ClientResponse response;
			if (formData != null) {
				if (Constants.API_METHOD.GET.equals(method)) {
					response = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).get(ClientResponse.class);
				} else if (Constants.API_METHOD.POST.equals(method)) {
					response = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).post(ClientResponse.class);
				} else if (Constants.API_METHOD.PUT.equals(method)) {
					response = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).put(ClientResponse.class);
				} else if (Constants.API_METHOD.DELETE.equals(method)) {
					response = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).delete(ClientResponse.class);
				} else {
					response = null;
				}
			} else {
				if (Constants.API_METHOD.GET.equals(method)) {
					response = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).get(ClientResponse.class);
				} else if (Constants.API_METHOD.POST.equals(method)) {
					response = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).post(ClientResponse.class);
				} else if (Constants.API_METHOD.PUT.equals(method)) {
					response = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).put(ClientResponse.class);
				} else if (Constants.API_METHOD.DELETE.equals(method)) {
					response = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).delete(ClientResponse.class);
				} else {
					response = null;
				}
			}
			String result = "";
			if (response != null) {
				result = response.getEntity(String.class);
			}
			LOGGER.info("Call service 3 arg" + method + " " + baseUrl + serviceUrl + " fromData: "
					+ String.valueOf(formData) + " return: " + result.length());
			if (((CommonUtils.isNoneBlank(result) && result.contains("invalid_grant"))
					|| CommonUtils.isNoneBlank(result) && result.contains("invalid_token"))) {
				getRefreshToken();
				throw new Exception(serviceUrl + " ERROR");
			} else {
				callNumber = 0;
				return result;
			}
		} catch (Exception ex) {
			if (callNumber <= RECALL_NUMBER) {
				return callService(formData, serviceUrl, method);
			} else {
				LOGGER.error(serviceUrl + " ERROR", ex);
				return null;
			}
		}
	}

	public String callService(MultivaluedMap formData, String serviceUrl, String method, String postData) {
		try {
			callNumber++;
			LOGGER.info("callService 4 arg:"+callNumber);
			Client client = Client.create();
			WebResource webResource = client.resource(this.baseUrl);
			final ClientResponse response;
			if (formData != null) {
				if (Constants.API_METHOD.GET.equals(method)) {
					response = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).get(ClientResponse.class);
				} else if (Constants.API_METHOD.POST.equals(method)) {
					response = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).post(ClientResponse.class, postData);
				} else if (Constants.API_METHOD.PUT.equals(method)) {
					response = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).put(ClientResponse.class, postData);
				} else if (Constants.API_METHOD.DELETE.equals(method)) {
					response = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).delete(ClientResponse.class);
				} else {
					response = null;
				}
			} else {
				if (Constants.API_METHOD.GET.equals(method)) {
					response = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).get(ClientResponse.class);
				} else if (Constants.API_METHOD.POST.equals(method)) {
					response = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).post(ClientResponse.class, postData);
				} else if (Constants.API_METHOD.PUT.equals(method)) {
					response = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).put(ClientResponse.class, postData);
				} else if (Constants.API_METHOD.DELETE.equals(method)) {
					response = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
							.header("Authorization", "Bearer " + this.TTNS_API_ACCESS_TOKEN)
							.header(API_KEY_NAME, API_KEY_VALUES).delete(ClientResponse.class);
				} else {
					response = null;
				}
			}
			String result = response.getEntity(String.class);
			LOGGER.debug("Call service 4 args " + method + " " + baseUrl + serviceUrl + " fromData: "
					+ String.valueOf(formData) + " return: " + result);
			if (((CommonUtils.isNoneBlank(result) && result.contains("invalid_grant"))
					|| (CommonUtils.isNoneBlank(result) && result.contains("invalid_token")))) {
				getRefreshToken();
				throw new Exception(serviceUrl + " ERROR");
			} else {
				callNumber = 0;
				return result;
			}
		} catch (Exception ex) {
			if (callNumber <= RECALL_NUMBER) {
				return callService(formData, serviceUrl, method, postData);
			} else {
				LOGGER.error(serviceUrl + " ERROR", ex);
				return null;
			}
		}
	}
}
