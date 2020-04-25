package com.github.isopropylcyanide.jdbiunitofwork;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import com.github.isopropylcyanide.jdbiunitofwork.core.ManagedHandleInvocationProxy;
import com.google.common.collect.Sets;
import com.google.common.reflect.Reflection;
import com.google.inject.AbstractModule;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class JdbiUnitOfWorkModule extends AbstractModule {

    private JdbiHandleManager handleManager;
    private Set<String> daoPackages;

    public JdbiUnitOfWorkModule(JdbiHandleManager handleManager, Set<String> daoPackages) {
        this.handleManager = handleManager;
        this.daoPackages = daoPackages;
    }

    @Override
    protected void configure() {
        Set<? extends Class<?>> allDaoClasses = daoPackages.stream().map(
                package_ -> Sets.union(
                        new Reflections(package_, new MethodAnnotationsScanner()).getMethodsAnnotatedWith(SqlQuery.class),
                        new Reflections(package_, new MethodAnnotationsScanner()).getMethodsAnnotatedWith(SqlUpdate.class)
                ).stream().map(Method::getDeclaringClass).collect(Collectors.toSet())
        ).flatMap(Collection::stream).collect(Collectors.toSet());

        for (Class klass : allDaoClasses) {
            bind(klass).toInstance(createNewProxy(klass, handleManager));
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private <T> T createNewProxy(Class<T> daoClass, JdbiHandleManager handleManager) {
        Object proxiedInstance = Reflection.newProxy(daoClass, new ManagedHandleInvocationProxy<>(handleManager, daoClass));
        return daoClass.cast(proxiedInstance);
    }
}
