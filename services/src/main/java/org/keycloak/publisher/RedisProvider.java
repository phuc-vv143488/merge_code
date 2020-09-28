package org.keycloak.publisher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

public class RedisProvider {
	private static final String CHANNEL_NAME = "ch1";
	 
		    public static void publish(List<String> client , List<String> userNames) {
//		    	RedisConfiguration redisConfiguration = CommonUtils.getRedisConfiguration();
		    	
//		    	RedisURI.Builder redisURI = RedisURI.Builder.redis(redisConfiguration.getHost(), redisConfiguration.getPort()).withDatabase(redisConfiguration.getDatabase());
//				if(CommonUtils.isNoneBlank(redisConfiguration.getPassword())) {
//					redisURI.withPassword(redisConfiguration.getPassword());
//				}
//				RedisClient redisClient = RedisClient.create(redisURI.build()); 
		    	RedisClient redisClient = RedisClient.create(RedisURI.Builder.redis("125.212.207.13", 6380).withPassword("chatTest@123").withDatabase(0).build());
		    	StatefulRedisConnection<String, String> connection = redisClient.connect();
		    	Map<String, Object> map = new HashMap<String, Object>();
		    	map.put("client", client);
		    	map.put("userNames", userNames);
		    	Gson gson = new Gson();
		    	connection.sync().publish(CHANNEL_NAME, gson.toJson(map));
		    	connection.close();
		    	redisClient.shutdown();
		    }
	 }
