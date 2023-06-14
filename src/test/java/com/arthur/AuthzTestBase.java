package com.arthur;

import com.arthur.authz.config.FoobarAppConfig;
import com.arthur.authz.container.RedisExtension;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static com.arthur.authz.helper.JwtTokenTestHelper.createExternalToken;
import static java.lang.String.format;

@Log4j2
@AutoConfigureMockMvc
@SpringBootTest
public abstract class AuthzTestBase {

  public static final int USER_ID = 10;

  public static final int ACCOUNT_ID = 100;

  public static final int EXPIRY_ELAPSED_TIME_IN_SECONDS = -10000;
  public static final int VALID_TIMEOUT_IN_SECONDS = 10000;

  public String EXTERNAL_TOKEN;

  public static final String USER_NAME = "test@arthur.com";
  public static final String USER_NAME_DOES_NOT_EXIST = "test2@arthur.com";
  public static final String API_USER_NAME = "apiuser@test.com";
  public static RMapCache<String, String> internalJwtCacheStore;

  @BeforeAll
  static void beforeAll() {

    try {
      Config config = new Config();
      config.useMasterSlaveServers()
              .setMasterAddress(
                      format("redis://%s:%s", "localhost", RedisExtension.HOST_EXPOSED_PORT)
              ).setReadMode(ReadMode.MASTER);
      config.setCodec(new StringCodec());
      internalJwtCacheStore = Redisson.create(config).getMapCache(FoobarAppConfig.REDIS_JWT_CACHE_KEY_NAME);
    } catch (Exception ex) {
      log.warn("Redis is down", ex);
      internalJwtCacheStore = null;
    }
  }

  @BeforeEach
  void setUp() {
    if (internalJwtCacheStore != null) {
      internalJwtCacheStore.remove(USER_NAME);
      internalJwtCacheStore.remove(USER_NAME_DOES_NOT_EXIST);
      internalJwtCacheStore.remove(API_USER_NAME);
    }
    EXTERNAL_TOKEN = createExternalToken(USER_NAME, VALID_TIMEOUT_IN_SECONDS);
  }

}

