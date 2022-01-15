package com.github.isopropylcyanide.example.app;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiUnitOfWorkProvider;
import com.google.inject.AbstractModule;
import org.skife.jdbi.v2.DBI;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class JdbiUnitOfWorkModule extends AbstractModule {

    private final List<String> daoPackages;
    private final JdbiUnitOfWorkProvider unitOfWorkProvider;

    public JdbiUnitOfWorkModule(DBI dbi, List<String> daoPackages) {
        this.daoPackages = daoPackages;
        this.unitOfWorkProvider = JdbiUnitOfWorkProvider.withDefault(dbi);
    }

    @Override
    protected void configure() {
        bind(JdbiUnitOfWorkProvider.class).toInstance(unitOfWorkProvider);
        Map<? extends Class, Object> instanceProxies = unitOfWorkProvider.getWrappedInstanceForDaoPackage(daoPackages);
        for (Class klass : instanceProxies.keySet()) {
            bind(klass).toInstance(instanceProxies.get(klass));
        }
    }
}
