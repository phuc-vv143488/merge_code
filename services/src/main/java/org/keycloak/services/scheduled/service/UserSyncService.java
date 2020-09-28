package org.keycloak.services.scheduled.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.jboss.logging.Logger;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.keycloak.services.scheduled.models.CommonUtils;
import org.keycloak.services.scheduled.models.Constants;
import org.keycloak.services.scheduled.models.Synchronized;
import org.keycloak.services.scheduled.models.TTNSAPIUtils;
import org.keycloak.services.scheduled.models.UserBean;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class UserSyncService {

	private static final String EMPLOYEE_TRANFER_VHR_TO_SSO_PAGEABLE = "/api/v1/employee/pageable";
	private static final Logger LOGGER = Logger.getLogger(UserSyncService.class);

	public static List<UserBean> getUserToSync(Synchronized sync, Long lastSyncTime) {
		if (lastSyncTime == null) {
			lastSyncTime = Constants.API.lastSyncTime;
		}
		Long page = 0L;
		
		List<UserBean> userBeans = new ArrayList<UserBean>();
		List<UserBean> lstUserBean = new ArrayList<UserBean>();
		do {
			lstUserBean = getLstEmpFromVhrPageable(null, Constants.API.DOMAIN, lastSyncTime, page, sync);
			if (!lstUserBean.isEmpty()) {
				userBeans.addAll(lstUserBean);
			}
			page++;
		} while (!lstUserBean.isEmpty());
		return userBeans;
	}

	public static List<UserBean> getLstEmpFromVhrPageable(String employeeCode, String domainData, Long lastUpTime,
			Long page, Synchronized sync) {
		try {
			List<UserBean> lst = new ArrayList<UserBean>();
			MultivaluedMap<String, String> formData = new MultivaluedMapImpl<String, String>();
			formData.add("syn_time", lastUpTime +"");
			formData.add("page", page + "");
			formData.add("per_page", Constants.API.PER_PAGE);
			if (employeeCode != null) {
				formData.add("employee_code", employeeCode);
			}
			formData.add("organization_id", domainData);
			formData.add("type", Constants.API.ALL);
			TTNSAPIUtils ttnsapiUtils = new TTNSAPIUtils(sync);
			String result = ttnsapiUtils.callService(formData, EMPLOYEE_TRANFER_VHR_TO_SSO_PAGEABLE,
					Constants.API_METHOD.GET);
			LOGGER.info(result);
			if (CommonUtils.isNoneBlank(result)) {
				JSONParser parser = new JSONParser();
				JSONObject jObj = (JSONObject) parser.parse(result);
				if (!Constants.API.NOT_FOUND.equals(jObj.get(Constants.API.STATUS_TYPE))) {
					JSONArray entity = (JSONArray) jObj.get(Constants.API.CONTENT);
					Type listType = new TypeToken<LinkedList<UserBean>>() {
					}.getType();
					Gson gson = new Gson();
					lst = gson.fromJson(entity.toString(), listType);
					formData.clear();
				}
			}
			return lst;
		} catch (Exception e) {
			LOGGER.error("error", e);
			return null;
		}
	}
}
