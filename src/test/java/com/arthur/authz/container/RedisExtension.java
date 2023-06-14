package com.arthur.authz.container;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class RedisExtension implements BeforeAllCallback, AfterAllCallback {

  public static final int HOST_EXPOSED_PORT = 63790;
  private static final GenericContainer<?> redisContainer;

  static {
    redisContainer = new GenericContainer<>(
        DockerImageName.parse("redis").withTag("6.2.7-alpine")
    );
    redisContainer.withExposedPorts(6379);
    redisContainer.getPortBindings().add(String.format("%s:6379/tcp", HOST_EXPOSED_PORT));

    Runtime.getRuntime().addShutdownHook(new Thread(redisContainer::stop));
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) {
    redisContainer.start();
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    redisContainer.stop();
  }
}
