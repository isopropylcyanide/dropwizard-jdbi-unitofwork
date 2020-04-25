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

package com.github.isopropylcyanide.jdbiunitofwork.core;

import org.skife.jdbi.v2.Handle;

import java.util.concurrent.ThreadFactory;

/**
 * A {@link JdbiHandleManager} is used to manage the lifecycle of a {@link Handle}
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
     * Provide a thread factory for the caller with some given identity. This can be used by the
     * caller to create multiple threads, say, using {@link java.util.concurrent.ExecutorService}
     * with a custom thread factory. The {@link JdbiHandleManager} can then use the thread factory
     * to identify and manage handle use across multiple threads.
     * <p>
     * This feature should be supported in only those implementations of {@link JdbiHandleManager}
     * which allow the handle to be safely used across multiple threads.
     *
     * @param threadIdentity a unique identifier for the calling thread
     * @return the default thread factory in Executors
     */
    default ThreadFactory createThreadFactory(String threadIdentity) {
        throw new UnsupportedOperationException("Not Supported");
    }
}
