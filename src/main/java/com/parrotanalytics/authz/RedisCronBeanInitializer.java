package com.arthur.authz;

import lombok.extern.log4j.Log4j2;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.arthur.authz.RedissonHelper.getRedissonClient;

@Component
@Log4j2
public class RedisCronBeanInitializer {

    @Autowired
    private GenericApplicationContext context;

    @Scheduled(fixedRate = 60000)
    public void refreshBeans() {
        DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();

        Map<String, RedissonClient> redissonClientMap = beanFactory.getBeansOfType(RedissonClient.class);
        AuthorizationProperties authorizationProperties = beanFactory.getBean(AuthorizationProperties.class);

        if (redissonClientMap.isEmpty()) {
            log.warn("Redisson client is not good, need to recreate and register");
            RedissonClient redissonClient = getRedissonClient(authorizationProperties);
            if (redissonClient != null) {
                beanFactory.registerSingleton("org.redisson.api.RedissonClient", redissonClient);
            }
        }
    }
}
