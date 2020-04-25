/*	/*
 * Licensed under the Apache License, Version 2.0 (the "License");	 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.	 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	 * You may obtain a copy of the License at
 *	 *
 * http://www.apache.org/licenses/LICENSE-2.0	 * http://www.apache.org/licenses/LICENSE-2.0
 *	 *
 * Unless required by applicable law or agreed to in writing, software	 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,	 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and	 * See the License for the specific language governing permissions and
 * limitations under the License.	 * limitations under the License.
 */

package com.github.isopropylcyanide.jdbiunitofwork.listener;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.ws.rs.HttpMethod;
import java.util.Set;

/**
 * This application event listener establishes a new request event listener for every request.
 * The request listener triggers calls appropriate methods on a transaction aspect based on the
 * request lifecycle methods returned from Jersey
 */
@Slf4j
public class JdbiUnitOfWorkApplicationEventListener implements ApplicationEventListener {

    private final JdbiHandleManager handleManager;
    private final Set<String> excludedPaths;

    JdbiUnitOfWorkApplicationEventListener(JdbiHandleManager handleManager, Set<String> excludedPaths) {
        this.handleManager = handleManager;
        this.excludedPaths = excludedPaths;
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        log.debug("Received Application event {}", event.getType());
    }

    @Override
    public RequestEventListener onRequest(RequestEvent event) {
        String path = event.getUriInfo().getPath();
        if (excludedPaths.stream().anyMatch(path::contains)) {
            return null;
        }
        if (event.getContainerRequest().getMethod().equals(HttpMethod.GET)) {
            return new HttpGetRequestJdbiUnitOfWorkEventListener(handleManager);
        }
        return new NonHttpGetRequestJdbiUnitOfWorkEventListener(handleManager);
    }
}
