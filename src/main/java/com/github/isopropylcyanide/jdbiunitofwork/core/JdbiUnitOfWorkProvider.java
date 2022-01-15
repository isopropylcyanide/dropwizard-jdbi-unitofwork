package com.github.isopropylcyanide.jdbiunitofwork.core;

import com.google.common.collect.Sets;
import com.google.common.reflect.Reflection;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlCall;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"UnstableApiUsage", "rawtypes", "unchecked"})
public class JdbiUnitOfWorkProvider {

    private final Logger log = LoggerFactory.getLogger(JdbiUnitOfWorkProvider.class);
    private final JdbiHandleManager handleManager;

    private JdbiUnitOfWorkProvider(JdbiHandleManager handleManager) {
        this.handleManager = handleManager;
    }

    public static JdbiUnitOfWorkProvider withDefault(DBI dbi) {
        JdbiHandleManager handleManager = new RequestScopedJdbiHandleManager(dbi);
        return new JdbiUnitOfWorkProvider(handleManager);
    }

    public static JdbiUnitOfWorkProvider withLinked(DBI dbi) {
        JdbiHandleManager handleManager = new LinkedRequestScopedJdbiHandleManager(dbi);
        return new JdbiUnitOfWorkProvider(handleManager);
    }

    public JdbiHandleManager getHandleManager() {
        return handleManager;
    }

    /**
     * getWrappedInstanceForDaoClass generates a proxy instance of the dao class for which
     * the jdbi unit of work aspect would be wrapped around with.
     * <p>
     * Note: It is recommended to use {@link JdbiUnitOfWorkProvider#getWrappedInstanceForDaoPackage(List)} instead
     * as passing a list of packages is easier than passing each instance individually.
     * <p>
     * This method however may be used in case the classpath scanning is disabled.
     * If the original class is null or contains no relevant JDBI annotations, this method throws an
     * exception
     *
     * @param daoClass the DAO class for which a proxy needs to be created fo
     * @return the wrapped instance ready to be passed around
     */
    public Object getWrappedInstanceForDaoClass(Class daoClass) {
        if (daoClass == null) {
            throw new IllegalArgumentException("DAO Class cannot be null");
        }
        boolean atLeastOneJdbiMethod = false;
        for (Method method : daoClass.getDeclaredMethods()) {
            if (method.getDeclaringClass() == daoClass) {
                atLeastOneJdbiMethod = method.getAnnotation(SqlQuery.class) != null;
                atLeastOneJdbiMethod = atLeastOneJdbiMethod || method.getAnnotation(SqlUpdate.class) != null;
                atLeastOneJdbiMethod = atLeastOneJdbiMethod || method.getAnnotation(SqlUpdate.class) != null;
                atLeastOneJdbiMethod = atLeastOneJdbiMethod || method.getAnnotation(SqlBatch.class) != null;
                atLeastOneJdbiMethod = atLeastOneJdbiMethod || method.getAnnotation(SqlCall.class) != null;
            }
        }
        if (!atLeastOneJdbiMethod) {
            throw new IllegalArgumentException(String.format("Class [%s] has no method annotated with a Jdbi SQL Object", daoClass.getSimpleName()));
        }

        log.info("Binding class [{}] with proxy handler [{}] ", daoClass.getSimpleName(), handleManager.getClass().getSimpleName());
        ManagedHandleInvocationHandler handler = new ManagedHandleInvocationHandler<>(handleManager, daoClass);
        Object proxiedInstance = Reflection.newProxy(daoClass, handler);
        return daoClass.cast(proxiedInstance);
    }

    /**
     * getWrappedInstanceForDaoPackage generates a map where every DAO class identified
     * through the given list of packages is mapped to its initialised proxy instance
     * the jdbi unit of work aspect would be wrapped around with.
     * <p>
     * In case classpath scanning is disabled, use {@link JdbiUnitOfWorkProvider#getWrappedInstanceForDaoClass(Class)}
     * <p>
     * If the original package list is null, this method throws an exception
     *
     * @param daoPackages the list of packages that contain the DAO classes
     * @return the map mapping dao classes to its initialised proxies
     */
    public Map<? extends Class, Object> getWrappedInstanceForDaoPackage(List<String> daoPackages) {
        if (daoPackages == null) {
            throw new IllegalArgumentException("DAO Class package list cannot be null");
        }

        Set<? extends Class<?>> allDaoClasses = daoPackages.stream()
                .map(this::getDaoClassesForPackage)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Map<Class, Object> classInstanceMap = new HashMap<>();
        for (Class klass : allDaoClasses) {
            log.info("Binding class [{}] with proxy handler [{}] ", klass.getSimpleName(), handleManager.getClass().getSimpleName());
            Object instance = getWrappedInstanceForDaoClass(klass);
            classInstanceMap.put(klass, instance);
        }
        return classInstanceMap;
    }

    private Set<? extends Class<?>> getDaoClassesForPackage(String pkg) {
        Set<Method> daoClasses = new HashSet<>();

        Sets.SetView<Method> union = Sets.union(daoClasses, new Reflections(pkg, Scanners.MethodsAnnotated).getMethodsAnnotatedWith(SqlQuery.class));
        union = Sets.union(union, new Reflections(pkg, Scanners.MethodsAnnotated).getMethodsAnnotatedWith(SqlUpdate.class));
        union = Sets.union(union, new Reflections(pkg, Scanners.MethodsAnnotated).getMethodsAnnotatedWith(SqlBatch.class));
        union = Sets.union(union, new Reflections(pkg, Scanners.MethodsAnnotated).getMethodsAnnotatedWith(SqlCall.class));

        return union.stream()
                .map(Method::getDeclaringClass)
                .collect(Collectors.toSet());
    }
}
