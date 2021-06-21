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
package com.github.isopropylcyanide.jdbiunitofwork.core;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation gets a new handle each time it is invoked. It simulates the default
 * behaviour of creating new handles each time the dao method is invoked.
 * <br><br>
 * It can be used to service requests which interact with only a single method in a single handle.
 * This is a lightweight implementation suitable for testing, such as with embedded databases.
 * Any serious application should not be using this as it may quickly leak / run out of handles
 *
 * @apiNote Not suitable for requests spanning multiple Dbi as the handle returned is different
 * This implementation, therefore, does not support thread factory creation.
 */
public class DefaultJdbiHandleManager implements JdbiHandleManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultJdbiHandleManager.class);
    private final Jdbi jdbi;

    public DefaultJdbiHandleManager(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Handle get() {
        Handle handle = jdbi.open();
        log.debug("handle [{}] : Thread Id [{}]", handle.hashCode(), Thread.currentThread().getId());
        return handle;
    }

    @Override
    public void clear() {
        log.debug("No Op");
    }
}