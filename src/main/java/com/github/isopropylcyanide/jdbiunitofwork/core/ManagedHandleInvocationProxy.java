package com.github.isopropylcyanide.jdbiunitofwork.core;

import com.google.common.reflect.AbstractInvocationHandler;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.Handle;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

@SuppressWarnings({"UnstableApiUsage"})
@AllArgsConstructor
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class ManagedHandleInvocationProxy<T> extends AbstractInvocationHandler {

    private final JdbiHandleManager handleManager;
    private final Class<T> underlying;

    @Override
    public Object handleInvocation(@Nonnull Object proxy, Method method, @Nonnull Object[] args) throws Throwable {
        Handle handle = handleManager.get();
        log.debug("{}.{} [{}] Thread Id [{}] with handle id [{}]", method.getDeclaringClass().getSimpleName(), method.getName(), underlying.getSimpleName(), Thread.currentThread().getId(), handle.hashCode());

        Object dao = handle.attach(underlying);
        return method.invoke(dao, args);
    }

    @Override
    public String toString() {
        return "Proxy[" + getClass().getSimpleName() + "]";
    }
}
