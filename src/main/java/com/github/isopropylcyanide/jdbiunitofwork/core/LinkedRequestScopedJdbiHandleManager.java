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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

/**
 * This implementation provides a handle scoped to a thread and all other threads Y spawned from X
 * All Y threads must follow a particular name format extracted from the conversation id
 * This is one of the ways the manager can know of the grouping and establish re-usability of
 * handles across such grouped threads.
 * <br><br>
 * It can be used to service requests where only a single handle instance has to be used by multiple
 * threads that are spawned with the specified name format from an initial thread. Use this only
 * when you have complete control over the threads you create. The threads must not run once the
 * parent thread is returned to the pool or else the handles will be invalid or in other words
 * parent thread must block on the results of children.
 *
 * <br>
 * It relies on the fact that the {@code Jdbi.Handle} is inherently thread safe and can be used to service
 * dao requests between multiple threads.
 * Note: Not suitable when you can not set the name format for the newly spawned threads.
 **/
public class LinkedRequestScopedJdbiHandleManager implements JdbiHandleManager {

    private final Logger log = LoggerFactory.getLogger(LinkedRequestScopedJdbiHandleManager.class);
    private final Map<String, Handle> parentThreadHandleMap = new ConcurrentHashMap<>();
    private final DBI dbi;

    public LinkedRequestScopedJdbiHandleManager(DBI dbi) {
        this.dbi = dbi;
    }

    @Override
    public Handle get() {
        String parent = substringBetween(Thread.currentThread().getName());
        Handle handle;
        if (parent == null) {
            handle = getHandle();
            log.debug("Owner of handle [{}] : Parent Thread Id [{}]", handle.hashCode(), Thread.currentThread().getId());

        } else {
            handle = parentThreadHandleMap.get(parent);
            if (handle == null) {
                throw new IllegalStateException(String.format("Handle to be reused in child thread [%s] is null for parent thread [%s]", Thread.currentThread().getId(), parent));
            }
            log.debug("Reusing parent thread handle [{}] for [{}]", handle.hashCode(), Thread.currentThread().getId());
        }
        return handle;
    }

    @Override
    public void clear() {
        String parent = getConversationId();
        Handle handle = parentThreadHandleMap.get(parent);
        if (handle != null) {
            handle.close();
            log.debug("Closed handle Thread Id [{}] has handle id [{}]", Thread.currentThread().getId(), handle.hashCode());

            parentThreadHandleMap.remove(parent);
            log.debug("Clearing handle member for parent thread [{}] ", Thread.currentThread().getId());
        }
    }

    @Override
    public ThreadFactory createThreadFactory() {
        String threadName = String.format("[%s]-%%d", getConversationId());
        return new ThreadFactoryBuilder().setNameFormat(threadName).build();
    }

    private Handle getHandle() {
        String threadIdentity = getConversationId();
        if (parentThreadHandleMap.containsKey(threadIdentity)) {
            return parentThreadHandleMap.get(threadIdentity);
        }
        Handle handle = dbi.open();
        parentThreadHandleMap.putIfAbsent(threadIdentity, handle);
        return handle;
    }

    @Nullable
    private String substringBetween(String threadName) {
        final int start = threadName.indexOf("[");
        if (start != -1) {
            final int end = threadName.indexOf("]", start + "[".length());
            if (end != -1) {
                return threadName.substring(start + "[".length(), end);
            }
        }
        return null;
    }
}
