package com.github.isopropylcyanide.jdbiunitofwork.listener;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiTransactionAspect;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

@Slf4j
class HttpGetRequestJdbiUnitOfWorkEventListener implements RequestEventListener {

    private final JdbiTransactionAspect transactionAspect;

    HttpGetRequestJdbiUnitOfWorkEventListener(JdbiHandleManager handleManager) {
        this.transactionAspect = new JdbiTransactionAspect(handleManager);
    }

    @Override
    public void onEvent(RequestEvent event) {
        RequestEvent.Type type = event.getType();
        log.debug("Handling GET Request Event {} {}", type, Thread.currentThread().getId());

        if (type == RequestEvent.Type.RESOURCE_METHOD_START) {
            transactionAspect.initHandle();

        } else if (type == RequestEvent.Type.FINISHED) {
            transactionAspect.terminateHandle();
        }
    }
}
