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
