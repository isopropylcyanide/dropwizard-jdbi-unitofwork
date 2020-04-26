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
package com.github.isopropylcyanide.jdbiunitofwork.listener;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiTransactionAspect;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This listener binds a transaction aspect to the currently serving GET request without creating
 * any transaction context and is simply responsible for initialising and terminating handles
 * upon successful start and end of request marked by Jersey request monitoring events
 * {@code RESOURCE_METHOD_START} and {@code FINISHED} respectively
 * <br><br>
 * For creating a transaction context, see {@link NonHttpGetRequestJdbiUnitOfWorkEventListener}
 */
class HttpGetRequestJdbiUnitOfWorkEventListener implements RequestEventListener {

    private static final Logger log = LoggerFactory.getLogger(HttpGetRequestJdbiUnitOfWorkEventListener.class);
    private final JdbiTransactionAspect transactionAspect;

    HttpGetRequestJdbiUnitOfWorkEventListener(JdbiHandleManager handleManager) {
        this.transactionAspect = new JdbiTransactionAspect(handleManager);
    }

    @Override
    public void onEvent(RequestEvent event) {
        RequestEvent.Type type = event.getType();
        log.debug("Handling GET Request Event {} {}", type, Thread.currentThread().getId());

        if (type == RequestEvent.Type.RESOURCE_METHOD_START) {
            transactionAspect.initHandle();

        } else if (type == RequestEvent.Type.FINISHED) {
            transactionAspect.terminateHandle();
        }
    }
}
