package org.keycloak.services.scheduled.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.keycloak.publisher.RedisConfiguration;
import org.keycloak.services.ServicesLogger;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;

public class CommonUtils {
	public static Synchronized getSync() {
		String configDir = System.getProperty("jboss.server.config.dir");
		if (configDir != null) {
			File data = new File(configDir + File.separator + "keycloak-sync-user.json");
			if (data.isFile()) {
				try {
					Synchronized sync = JsonSerialization.readValue(new FileInputStream(data),
							new TypeReference<Synchronized>() {
							});
					return sync;
				} catch (IOException ex) {
					ServicesLogger.LOGGER.failedToLoadVhrUsers(ex);
				}
			}
		}
		return null;
	}

	public static RedisConfiguration getRedisConfiguration() {
		String configDir = System.getProperty("jboss.server.config.dir");
		if (configDir != null) {
			File data = new File(configDir + File.separator + "keycloak-sync-user.json");
			if (data.isFile()) {
				try {
					RedisConfiguration redisConfig = JsonSerialization.readValue(new FileInputStream(data),
							new TypeReference<RedisConfiguration>() {
							});
					return redisConfig;
				} catch (IOException ex) {
					ServicesLogger.LOGGER.failedToLoadVhrUsers(ex);
				}
			}
		}
		return null;
	}

	public static void disableSslVerification() throws Exception {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

	}

	public static Set<String> convertListStringToSet(List<String> items) {
		Set<String> setItem = new HashSet<String>();
		for (String item : items) {
			setItem.add(item);
		}
		return setItem;
	}

	public static Set<Long> convertListLongToSet(List<Long> items) {
		Set<Long> setItem = new HashSet<Long>();
		for (Long item : items) {
			setItem.add(item);
		}
		return setItem;
	}

	public static Date convertStringToDate(String date) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd");
			return dateFormat.parse(date);
		} catch (Exception ex) {
			return null;
		}
	}

	public static boolean isNoneBlank(String data) {
		if (data == null || data.isEmpty())
			return false;
		return true;
	}

	public static boolean isBlank(String data) {
		if (data == null || data.isEmpty())
			return true;
		return false;
	}

	public static Long getLong(String data) {
		if (isBlank(data)) {
			return null;
		}
		try {
			Long parseData = Long.parseLong(data);
			return parseData;
		} catch (Exception ex) {
			return null;
		}
	}

}
