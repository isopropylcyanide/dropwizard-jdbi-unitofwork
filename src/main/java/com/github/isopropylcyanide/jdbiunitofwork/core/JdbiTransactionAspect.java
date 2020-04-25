package com.github.isopropylcyanide.jdbiunitofwork.core;


import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.Handle;

/**
 * An aspect providing operations around a method with the {@link com.github.isopropylcyanide.jdbiunitofwork} annotation.
 * It optionally opens a transaction. It should be created for every invocation of the resource method
 */
@Slf4j
public class JdbiTransactionAspect {

    private final JdbiHandleManager handleManager;
    private Handle handle;

    public JdbiTransactionAspect(JdbiHandleManager handleManager) {
        this.handleManager = handleManager;
    }

    public void initHandle() {
        handle = handleManager.get();
    }

    public void begin() {
        try {
            handle.begin();
            log.debug("Begin Transaction Thread Id [{}] has handle id [{}] Transaction {} Level {}", Thread.currentThread().getId(), handle.hashCode(), handle.isInTransaction(), handle.getTransactionIsolationLevel());

        } catch (Exception ex) {
            handleManager.clear();
            throw ex;
        }
    }

    public void commit() {
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
        try {
            handleManager.clear();
        } finally {
            handle = null;
        }
    }
}
