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
import org.keycloak.services.scheduled.models.PositionBean;
import org.keycloak.services.scheduled.models.Synchronized;
import org.keycloak.services.scheduled.models.TTNSAPIUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class PositionSyncService  {
	private static final String POSITION_TRANFER_VHR_TO_SSO = "/api/v1/employee/position/all";
	
	private static final Logger LOGGER = Logger.getLogger(PositionSyncService.class);
	
	public static List<PositionBean> getDataSync(Long lastUpTime, Long pageNumber, String type, Synchronized sync){
		 try {
	            List<PositionBean> lst = new ArrayList<PositionBean>();
	            MultivaluedMap<String, String> formData = new MultivaluedMapImpl<String, String>();
	            formData.add("syncTime", lastUpTime.toString());
	            formData.add("page", pageNumber.toString());
	            formData.add("per_page", String.valueOf(Integer.MAX_VALUE));
	            formData.add("type", type);
	            TTNSAPIUtils ttnsapiUtils = new TTNSAPIUtils(sync);
	            String result = ttnsapiUtils.callService(formData, POSITION_TRANFER_VHR_TO_SSO, Constants.API_METHOD.GET);
	            if (CommonUtils.isNoneBlank(result)) {
	                JSONParser parser = new JSONParser();
	                JSONObject jObj = (JSONObject) parser.parse(result);
	                if (!Constants.API.NOT_FOUND.equals(jObj.get(Constants.API.STATUS_TYPE)))  {
	                    JSONArray entity = (JSONArray) jObj.get(Constants.API.CONTENT);
	                    Type listType = new TypeToken<LinkedList<PositionBean>>() {
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
