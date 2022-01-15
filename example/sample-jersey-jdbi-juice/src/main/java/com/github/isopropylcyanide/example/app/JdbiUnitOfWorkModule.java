package com.github.isopropylcyanide.example.app;

import com.github.isopropylcyanide.jdbiunitofwork.JdbiUnitOfWorkProvider;
import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import com.google.inject.AbstractModule;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class JdbiUnitOfWorkModule extends AbstractModule {

    private final List<String> daoPackages;
    private final JdbiUnitOfWorkProvider unitOfWorkProvider;

    public JdbiUnitOfWorkModule(JdbiHandleManager handleManager, List<String> daoPackages) {
        this.daoPackages = daoPackages;
        this.unitOfWorkProvider = new JdbiUnitOfWorkProvider(handleManager);
    }

    @Override
    protected void configure() {
        Map<? extends Class, Object> instanceProxies = unitOfWorkProvider.getWrappedInstanceForDaoPackage(daoPackages);
        for (Class klass : instanceProxies.keySet()) {
            bind(klass).toInstance(instanceProxies.get(klass));
        }
    }
}
