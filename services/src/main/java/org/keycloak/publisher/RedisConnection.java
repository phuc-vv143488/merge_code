package org.keycloak.publisher;

import org.keycloak.services.scheduled.models.CommonUtils;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

public class RedisConnection {
	public static final RedisClient INSTANCE =  null;
	
	public static RedisClient getInstance() {
		RedisConfiguration redisConfiguration = CommonUtils.getRedisConfiguration();
		if(INSTANCE == null) {
			if(redisConfiguration != null) {
				RedisURI.Builder redisURI = RedisURI.Builder.redis(redisConfiguration.getHost(), redisConfiguration.getPort()).withDatabase(redisConfiguration.getDatabase());
				if(CommonUtils.isNoneBlank(redisConfiguration.password)) {
					redisURI.withPassword(redisConfiguration.getPassword());
				}
				RedisClient redisClient = RedisClient.create(redisURI.build()); 
				return redisClient;
			}
		}
		return INSTANCE;
	}
	
}
