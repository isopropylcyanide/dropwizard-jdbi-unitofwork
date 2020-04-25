package com.github.isopropylcyanide.jdbiunitofwork.core;

import org.skife.jdbi.v2.Handle;

import java.util.concurrent.ThreadFactory;

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
     * Provide a thread factory for the caller with some identity.
     *
     * @param threadIdentity a unique identifier for the calling thread
     * @return the default thread factory in Executors
     */
    default ThreadFactory createThreadFactory(String threadIdentity) {
        throw new UnsupportedOperationException("Not Supported");
    }
}
