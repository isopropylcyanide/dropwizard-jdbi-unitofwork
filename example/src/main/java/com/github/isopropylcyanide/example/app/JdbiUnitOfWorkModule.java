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
