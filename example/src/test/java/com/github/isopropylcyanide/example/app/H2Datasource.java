package com.github.isopropylcyanide.example.app;

import com.codahale.metrics.MetricRegistry;
import com.google.common.io.Resources;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.ManagedPooledDataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.junit.rules.ExternalResource;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The {@code H2DatabaseHelper} rule allows you to set up and clear a managed H2 Datasource
 * You have to add the {@code H2DatabaseHelper} rule to your test and provide a valid schema
 * source for the initial script as well as the clean up script
 */
public class H2Datasource extends ExternalResource {

    private static final Logger log = LoggerFactory.getLogger(H2Datasource.class);
    private DBI dbi;
    private final String createSchemaSqlFile;
    private final String dropSchemaSqlFile;

    public H2Datasource(String createSchemaSqlFile, String dropSchemaSqlFile) {
        this.createSchemaSqlFile = createSchemaSqlFile;
        this.dropSchemaSqlFile = dropSchemaSqlFile;
    }

    @Override
    protected void before() {
        dbi = getDBI();
        try (Handle handle = dbi.open()) {
            log.debug("Creating database script using schema {}", createSchemaSqlFile);
            handle.createScript(fixture(createSchemaSqlFile)).execute();
        }
    }

    @Override
    protected void after() {
        try (Handle handle = dbi.open()) {
            log.debug("Dropping database script using schema {}", dropSchemaSqlFile);
            handle.createScript(fixture(dropSchemaSqlFile)).execute();
        }
    }

    public DBI getDbi() {
        return dbi;
    }

    private DBI getDBI() {
        final PoolProperties poolConfig = new PoolProperties();
        poolConfig.setUrl("jdbc:h2:mem:approvalservice;MVCC=true;DEFAULT_LOCK_TIMEOUT=10000;LOCK_MODE=0;DATABASE_TO_UPPER=FALSE");
        poolConfig.setDriverClassName("org.h2.Driver");
        poolConfig.setUsername("root");
        poolConfig.setPassword("");
        ManagedDataSource managedDataSource = new ManagedPooledDataSource(poolConfig, new MetricRegistry());
        return new DBI(managedDataSource);
    }

    @SuppressWarnings("UnstableApiUsage")
    private static String fixture(String filename) {
        try {
            return Resources.toString(Resources.getResource(filename), StandardCharsets.UTF_8).trim();
        } catch (IOException var3) {
            throw new IllegalArgumentException(var3);
        }
    }
}
