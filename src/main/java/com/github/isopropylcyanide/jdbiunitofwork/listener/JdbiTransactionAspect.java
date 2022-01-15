package com.github.isopropylcyanide.jdbiunitofwork.listener;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An aspect providing low level operations around a {@link Handle}
 * This is inspired from Dropwizard's Unit of work aspect used to manage handles for hibernate.
 *
 * @see <a href="https://github.com/dropwizard/dropwizard/blob/master/dropwizard-hibernate/src/main/java/io/dropwizard/hibernate/UnitOfWorkAspect.java">
 * Unit Of Work Aspect</a>
 */
public class JdbiTransactionAspect {

    private final Logger log = LoggerFactory.getLogger(JdbiTransactionAspect.class);
    private final JdbiHandleManager handleManager;

    public JdbiTransactionAspect(JdbiHandleManager handleManager) {
        this.handleManager = handleManager;
    }

    public void begin() {
        try {
            Handle handle = handleManager.get();
            handle.begin();
            log.debug("Begin Transaction Thread Id [{}] has handle id [{}] Transaction {} Level {}", Thread.currentThread().getId(), handle.hashCode(), handle.isInTransaction(), handle.getTransactionIsolationLevel());

        } catch (Exception ex) {
            handleManager.clear();
            throw ex;
        }
    }

    public void commit() {
        Handle handle = handleManager.get();
        if (handle == null) {
            log.debug("Handle was found to be null during commit for Thread Id [{}]. It might have already been closed", Thread.currentThread().getId());
            return;
        }
        try {
            handle.commit();
            log.debug("Performing commit Thread Id [{}] has handle id [{}] Transaction {} Level {}", Thread.currentThread().getId(), handle.hashCode(), handle.isInTransaction(), handle.getTransactionIsolationLevel());

        } catch (Exception ex) {
            handle.rollback();
            throw ex;
        }
    }

    public void rollback() {
        Handle handle = handleManager.get();
        if (handle == null) {
            log.debug("Handle was found to be null during rollback for [{}]", Thread.currentThread().getId());
            return;
        }
        try {
            handle.rollback();
            log.debug("Performed rollback on Thread Id [{}] has handle id [{}] Transaction {} Level {}", Thread.currentThread().getId(), handle.hashCode(), handle.isInTransaction(), handle.getTransactionIsolationLevel());
        } finally {
            terminateHandle();
        }
    }

    public void terminateHandle() {
        handleManager.clear();
    }
}
