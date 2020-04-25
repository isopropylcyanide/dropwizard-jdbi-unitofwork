package com.github.isopropylcyanide.jdbiunitofwork.core;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

/**
 * This implementation gets a new handle which is scoped to the thread requesting the handle.
 * <p>
 * It can be used to service requests which interact with multiple handles as part of a common transaction
 * Note: Not suitable for requests which spawn new threads as the scoped handle is not preserved.
 */
@Slf4j
@AllArgsConstructor
public class RequestScopedJdbiHandleManager implements JdbiHandleManager {

    private final DBI dbi;
    private final ThreadLocal<Handle> threadLocal = new ThreadLocal<>();

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
