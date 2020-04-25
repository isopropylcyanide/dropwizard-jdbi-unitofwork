package com.github.isopropylcyanide.jdbiunitofwork.listener;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.ws.rs.HttpMethod;
import java.util.Set;

/**
 * This application event listener establishes a new request event listener for every request.
 * The request listener triggers calls appropriate methods on a transaction aspect based on the
 * request lifecycle methods returned from Jersey
 */
@Slf4j
public class JdbiUnitOfWorkApplicationEventListener implements ApplicationEventListener {

    private final JdbiHandleManager handleManager;
    private final Set<String> excludedPaths;

    public JdbiUnitOfWorkApplicationEventListener(JdbiHandleManager handleManager, Set<String> excludedPaths) {
        this.handleManager = handleManager;
        this.excludedPaths = excludedPaths;
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        log.debug("Received Application event {}", event.getType());
    }

    @Override
    public RequestEventListener onRequest(RequestEvent event) {
        String path = event.getUriInfo().getPath();
        if (excludedPaths.stream().anyMatch(path::contains)) {
            return null;
        }
        if (event.getContainerRequest().getMethod().equals(HttpMethod.GET)) {
            return new HttpGetRequestJdbiUnitOfWorkEventListener(handleManager);
        }
        return new NonHttpGetRequestJdbiUnitOfWorkEventListener(handleManager);
    }
}
