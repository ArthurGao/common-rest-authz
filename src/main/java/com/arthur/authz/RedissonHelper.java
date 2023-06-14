package com.arthur.authz;

import lombok.extern.log4j.Log4j2;
import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;

import static java.lang.String.format;

@Log4j2
public class RedissonHelper {

    private RedissonHelper() {}

    public static RedissonClient getRedissonClient(AuthorizationProperties configuration) {
        RedissonClient redissonClient = null;
        try {
            Config config = new Config();
            config.useMasterSlaveServers()
                    .setMasterAddress(
                            format("redis://%s:%s", configuration.getRedisCacheHost(), configuration.getRedisCachePort())
                    ).setReadMode(ReadMode.MASTER);
            config.setCodec(new StringCodec());
            redissonClient =  Redisson.create(config);
        } catch (Exception e) {
            log.warn("Error in getting redisson client ", e);
        }
        return redissonClient;
    }

    public static RMapCache<String, String> initInternalJwtCacheStore(RedissonClient redissonClient, AuthorizationProperties authorizationProperties) {
        RMapCache<String, String> internalJwtCacheStore = null;
        if (redissonClient != null) {
            log.debug("Redisson client is available, hence getting the map from redis");
            internalJwtCacheStore = redissonClient.getMapCache(authorizationProperties.getRedisJwtCacheKeyName());
        }
        return internalJwtCacheStore;
    }
}
