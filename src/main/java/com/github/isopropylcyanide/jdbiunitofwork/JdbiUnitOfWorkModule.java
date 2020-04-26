/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.isopropylcyanide.jdbiunitofwork;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import com.github.isopropylcyanide.jdbiunitofwork.core.ManagedHandleInvocationHandler;
import com.google.common.collect.Sets;
import com.google.common.reflect.Reflection;
import com.google.inject.AbstractModule;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class JdbiUnitOfWorkModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(JdbiUnitOfWorkModule.class);
    private JdbiHandleManager handleManager;
    private Set<String> daoPackages;

    public JdbiUnitOfWorkModule(JdbiHandleManager handleManager, Set<String> daoPackages) {
        this.handleManager = handleManager;
        this.daoPackages = daoPackages;
    }

    @Override
    protected void configure() {
        Set<? extends Class<?>> allDaoClasses = daoPackages.stream().map(
                pkg -> Sets.union(
                        new Reflections(pkg, new MethodAnnotationsScanner()).getMethodsAnnotatedWith(SqlQuery.class),
                        new Reflections(pkg, new MethodAnnotationsScanner()).getMethodsAnnotatedWith(SqlUpdate.class)
                ).stream().map(Method::getDeclaringClass).collect(Collectors.toSet())
        ).flatMap(Collection::stream).collect(Collectors.toSet());

        for (Class klass : allDaoClasses) {
            log.debug("Binding class [{}] with proxy handler [{}] ", klass.getSimpleName(), handleManager.getClass().getSimpleName());
            bind(klass).toInstance(createNewProxy(klass, handleManager));
        }
    }

    private <T> T createNewProxy(Class<T> daoClass, JdbiHandleManager handleManager) {
        Object proxiedInstance = Reflection.newProxy(daoClass, new ManagedHandleInvocationHandler<>(handleManager, daoClass));
        return daoClass.cast(proxiedInstance);
    }
}
