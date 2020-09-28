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
import org.keycloak.services.scheduled.models.OrganizationBean;
import org.keycloak.services.scheduled.models.Synchronized;
import org.keycloak.services.scheduled.models.TTNSAPIUtils;
import org.keycloak.services.scheduled.models.UserBean;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class OrganizationSyncService {

	private static final String ORGANIZATION_TRANFER_VHR_TO_SSO = "/api/v2/organization";
	private static final Logger LOGGER = Logger.getLogger(OrganizationSyncService.class);
	private static final String PER_PAGE = "1000";
	private static final String NOT_FOUND = "NOT_FOUND";
	
	
	public static List<OrganizationBean> getOrganizeToSync(Synchronized sync, Long lastSyncTime) {
		if (lastSyncTime == null) {
			lastSyncTime = Constants.API.lastSyncTime;
		}
		Long page = 0L;
		
		List<OrganizationBean> orgBeans = new ArrayList<OrganizationBean>();
		List<OrganizationBean> lstOrgBean = new ArrayList<OrganizationBean>();
		do {
			lstOrgBean = getDataSync(lastSyncTime, page, sync);
			if (!lstOrgBean.isEmpty()) {
				orgBeans.addAll(lstOrgBean);
			}
			page++;
		} while (!lstOrgBean.isEmpty());
		return orgBeans;
	}
	
	/**
	 * Lay thong tin organization de dong bo
	 * 
	 * @param employeeCode
	 * @param domainData
	 * @param lastUpTime
	 * @return
	 */
	public static List<OrganizationBean> getDataSync(Long lastSyncTime, Long pageNumber, Synchronized sync) {
		try {
            List<OrganizationBean> lst = new ArrayList<OrganizationBean>();
            MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
            formData.add("syncTime", lastSyncTime.toString());
            formData.add("page", pageNumber.toString());
            formData.add("per_page", PER_PAGE);
            formData.add("type", "ALL");
            TTNSAPIUtils ttnsapiUtils = new TTNSAPIUtils(sync);
            String result = ttnsapiUtils.callService(formData, ORGANIZATION_TRANFER_VHR_TO_SSO, Constants.API_METHOD.GET);
            if (CommonUtils.isNoneBlank(result)) {
                JSONParser parser = new JSONParser();
                JSONObject jObj = (JSONObject) parser.parse(result);
                JSONArray entity = (JSONArray) jObj.get("content");
                Type listType = new TypeToken<LinkedList<OrganizationBean>>() {
                }.getType();
                Gson gson = new Gson();
                lst = gson.fromJson(entity.toString(), listType);
                formData.clear();
            }
            LOGGER.info("loading page:"+pageNumber);
            return lst;
        } catch (Exception e) {
			LOGGER.error("error", e);
			return null;
		}
	}

}
