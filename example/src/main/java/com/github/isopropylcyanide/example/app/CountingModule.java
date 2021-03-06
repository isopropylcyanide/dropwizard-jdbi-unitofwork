package com.github.isopropylcyanide.example.app;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import com.github.isopropylcyanide.jdbiunitofwork.core.LinkedRequestScopedJdbiHandleManager;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import org.skife.jdbi.v2.DBI;

import java.util.Set;

public class CountingModule extends AbstractModule {

    private final DBI dbi;

    CountingModule(DBI dbi) {
        this.dbi = dbi;
    }

    @Override
    protected void configure() {
        Set<String> daoPackages = Sets.newHashSet("com.github.isopropylcyanide.example.app.dao");
        JdbiHandleManager handleManager = new LinkedRequestScopedJdbiHandleManager(dbi);

        bind(JdbiHandleManager.class).toInstance(handleManager);
        install(new JdbiUnitOfWorkModule(handleManager, daoPackages));
    }
}
