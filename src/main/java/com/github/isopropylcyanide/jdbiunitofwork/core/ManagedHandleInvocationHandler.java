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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Implementation of {@link InvocationHandler} that attaches the underlying class to a handle
 * obtained through {@link JdbiHandleManager} on every invocation.
 * <br><br>
 * Note: Attaching a handle to a class is an idempotent operation. If a handle {@literal H}
 * is attached to a class, attaching {@literal H} to the same class again serves no purpose.
 * <br><br>
 * Also delegates {@link Object#toString} to the real object instead of the proxy which is
 * helpful for debugging
 */
public class ManagedHandleInvocationHandler<T> implements InvocationHandler {

    private final Logger log = LoggerFactory.getLogger(ManagedHandleInvocationHandler.class);
    private static final Object[] NO_ARGS = {};
    private final JdbiHandleManager handleManager;
    private final Class<T> underlying;

    public ManagedHandleInvocationHandler(JdbiHandleManager handleManager, Class<T> underlying) {
        this.handleManager = handleManager;
        this.underlying = underlying;
    }

    /**
     * {@inheritDoc}
     * <ul>
     * <li>{@code proxy.toString()} delegates to {@link ManagedHandleInvocationHandler#toString}
     * <li>other method calls are dispatched to {@link ManagedHandleInvocationHandler#handleInvocation}.
     * </ul>
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args == null) {
            args = NO_ARGS;
        }
        if (args.length == 0 && method.getName().equals("toString")) {
            return toString();
        }
        return handleInvocation(method, args);
    }

    private Object handleInvocation(Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
        Handle handle = handleManager.get();
        log.debug("{}.{} [{}] Thread Id [{}] with handle id [{}]", method.getDeclaringClass().getSimpleName(), method.getName(), underlying.getSimpleName(), Thread.currentThread().getId(), handle.hashCode());

        Object dao = handle.attach(underlying);
        return method.invoke(dao, args);
    }

    @Override
    public String toString() {
        return "Proxy[" + underlying.getSimpleName() + "]";
    }
}
