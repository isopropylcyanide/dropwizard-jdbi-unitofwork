package com.github.isopropylcyanide.jdbiunitofwork.core;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
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

    private final Logger log = LoggerFactory.getLogger(DefaultJdbiHandleManager.class);
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
