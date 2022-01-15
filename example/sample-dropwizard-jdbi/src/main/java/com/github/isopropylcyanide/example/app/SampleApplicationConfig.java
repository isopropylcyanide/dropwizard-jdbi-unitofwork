package com.github.isopropylcyanide.example.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.constraints.NotNull;

public class SampleApplicationConfig extends Configuration {

    @NotNull
    @JsonProperty("database")
    private DataSourceFactory database;

    public DataSourceFactory getDatabase() {
        return database;
    }
}
