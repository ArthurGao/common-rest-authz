package com.arthur.authz.container;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.TestcontainersConfiguration;

public class MySQLExtension implements BeforeAllCallback, AfterAllCallback {

  static final MySQLContainer<?> mySqlDB;

  static {


    mySqlDB =
            new MySQLContainer<>
                    ("mysql:5.7.37")
                    .withDatabaseName("subscription")
                    .withUsername("root")
                    .withPassword("password")
                    .withInitScript("sql/data.sql");
    mySqlDB.start();

    Runtime.getRuntime()
        .addShutdownHook(new Thread(mySqlDB::stop));
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    TestcontainersConfiguration.getInstance().updateUserConfig("testcontainers.reuse.enable", "true");
    System.setProperty("spring.datasource.url", mySqlDB.getJdbcUrl());
    System.setProperty("spring.datasource.username", mySqlDB.getUsername());
    System.setProperty("spring.datasource.password", mySqlDB.getPassword());
  }

  @Override
  public void afterAll(ExtensionContext context) {
    // do nothing, Testcontainers handles container shutdown
  }
}
