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

import org.skife.jdbi.v2.Handle;

import java.util.concurrent.ThreadFactory;

/**
 * A {@link JdbiHandleManager} is used to provide the lifecycle of a {@link Handle} with respect
 * to a given scope. A scope may be session based, request based or may be invoked on every run.
 */
public interface JdbiHandleManager {

    /**
     * Provide a way to get a Jdbi handle, a wrapped connection to the underlying database
     */
    Handle get();

    /**
     * Provide a way to clear the handle rendering it useless for the other methods
     */
    void clear();

    /**
     * Provide a thread factory for the caller with some identity represented by the
     * {@link #getConversationId()}. This can be used by the caller to create multiple threads,
     * say, using {@link java.util.concurrent.ExecutorService}. The {@link JdbiHandleManager} can
     * then use the thread factory to identify and manage handle use across multiple threads.
     *
     * @apiNote By default this throws a {@link UnsupportedOperationException}
     * Implementations overriding this method must ensure that the conversation id is unique
     */
    default ThreadFactory createThreadFactory() {
        throw new UnsupportedOperationException("Thread factory creation is not supported");
    }

    /**
     * Provide a unique identifier for the conversation with a handle. No two identifiers
     * should co exist at once during the application lifecycle or else handle corruption
     * or misuse might occur.
     * <p>
     * This can be relied upon by the {@link #createThreadFactory()} to reuse handles across
     * multiple threads spawned off a request thread.
     *
     * @implNote hashcode can not be relied upon for providing a unique identifier due to the
     * possibility of collision. Instead opt for a monotonically increasing counter, such as
     * the thread id.
     */
    default String getConversationId() {
        return String.valueOf(Thread.currentThread().getId());
    }
}
