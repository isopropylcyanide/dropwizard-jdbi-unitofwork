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

import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

/**
 * This implementation gets a new handle each time it is invoked. It simulates the default
 * behaviour of creating new handles each time the dao method is invoked.
 * <p>
 * It can be used to service requests which interact with only a single method in a single handle
 * Note: Not suitable for requests spanning multiple Dbi as the handle returned is different
 */
@Slf4j
public class DefaultJdbiHandleManager implements JdbiHandleManager {

    private final DBI dbi;

    public DefaultJdbiHandleManager(DBI dbi) {
        this.dbi = dbi;
    }

    @Override
    public Handle get() {
        Handle handle = dbi.open();
        log.debug("handle [{}] : Thread Id [{}]", handle.hashCode(), Thread.currentThread().getId());
        return handle;
    }

    @Override
    public void clear() {
        log.debug("No Op");
    }
}
