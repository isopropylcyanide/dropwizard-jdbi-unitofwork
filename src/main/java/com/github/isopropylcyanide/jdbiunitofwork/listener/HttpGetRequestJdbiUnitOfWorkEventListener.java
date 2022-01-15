package com.github.isopropylcyanide.jdbiunitofwork.listener;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This listener binds a transaction aspect to the currently serving GET request without creating
 * any transaction context and is simply responsible for initialising and terminating handles
 * upon successful start and end of request marked by Jersey request monitoring events
 * {@code RESOURCE_METHOD_START} and {@code FINISHED} respectively
 * <br><br>
 * For creating a transaction context, see {@link NonHttpGetRequestJdbiUnitOfWorkEventListener}
 */
class HttpGetRequestJdbiUnitOfWorkEventListener implements RequestEventListener {

    private final Logger log = LoggerFactory.getLogger(HttpGetRequestJdbiUnitOfWorkEventListener.class);
    private final JdbiTransactionAspect transactionAspect;

    HttpGetRequestJdbiUnitOfWorkEventListener(JdbiHandleManager handleManager) {
        this.transactionAspect = new JdbiTransactionAspect(handleManager);
    }

    @Override
    public void onEvent(RequestEvent event) {
        RequestEvent.Type type = event.getType();
        log.debug("Handling GET Request Event {} {}", type, Thread.currentThread().getId());

        if (type == RequestEvent.Type.FINISHED) {
            transactionAspect.terminateHandle();
        }
    }
}
