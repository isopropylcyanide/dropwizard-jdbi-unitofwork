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

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation gets a new handle which is scoped to the thread requesting the handle.
 * <br><br>
 * It can be used to service requests which interact with multiple SQL objects as part of a common
 * transaction. All such SQL objects will be attached to the common handle.
 *
 * @apiNote Not suitable for requests which spawn new threads from the requesting thread as the scoped
 * handle is not preserved. This implementation, therefore, does not support thread factory creation
 */
public class RequestScopedJdbiHandleManager implements JdbiHandleManager {

    private static final Logger log = LoggerFactory.getLogger(RequestScopedJdbiHandleManager.class);
    private final DBI dbi;
    private final ThreadLocal<Handle> threadLocal = new ThreadLocal<>();

    public RequestScopedJdbiHandleManager(DBI dbi) {
        this.dbi = dbi;
    }

    @Override
    public Handle get() {
        if (threadLocal.get() == null) {
            threadLocal.set(dbi.open());
        }
        Handle handle = threadLocal.get();
        log.debug("handle [{}] : Thread Id [{}]", handle.hashCode(), Thread.currentThread().getId());
        return handle;
    }

    @Override
    public void clear() {
        Handle handle = threadLocal.get();
        if (handle != null) {
            handle.close();
            log.debug("Closed handle Thread Id [{}] has handle id [{}]", Thread.currentThread().getId(), handle.hashCode());

            threadLocal.remove();
            log.debug("Clearing handle member for thread [{}] ", Thread.currentThread().getId());
        }
    }
}
