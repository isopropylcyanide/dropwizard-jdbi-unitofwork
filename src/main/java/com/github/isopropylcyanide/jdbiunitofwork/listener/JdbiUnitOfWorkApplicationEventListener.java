package com.github.isopropylcyanide.jdbiunitofwork.listener;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiUnitOfWorkProvider;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.HttpMethod;
import java.util.Set;

/**
 * This application event listener establishes a new request event listener for every request.
 * The request listener triggers calls appropriate methods on a transaction aspect based on the
 * request lifecycle methods returned from Jersey
 * <br><br>
 * {@code HttpMethod.GET} requests are assumed to be in non transaction boundary and are routed
 * to {@link HttpGetRequestJdbiUnitOfWorkEventListener}
 * <br><br>
 * Non {@code HttpMethod.GET} requests are assumed to be in a transaction boundary and are routed
 * to {@link NonHttpGetRequestJdbiUnitOfWorkEventListener}
 *
 * @implNote For requests that never not require a connection with the database, such as ELB health
 * checks or computate only use cases, opening and closing a handle is redundant and wasteful
 * Such request URIs should be added in the set of {@link #excludedPaths}
 */
public class JdbiUnitOfWorkApplicationEventListener implements ApplicationEventListener {

    private final Logger log = LoggerFactory.getLogger(JdbiUnitOfWorkApplicationEventListener.class);
    private final JdbiUnitOfWorkProvider unitOfWorkProvider;
    private final Set<String> excludedPaths;

    public JdbiUnitOfWorkApplicationEventListener(JdbiUnitOfWorkProvider unitOfWorkProvider, Set<String> excludedPaths) {
        this.unitOfWorkProvider = unitOfWorkProvider;
        this.excludedPaths = excludedPaths;
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        log.debug("Received Application event {}", event.getType());
    }

    @Override
    @Nullable
    public RequestEventListener onRequest(RequestEvent event) {
        String path = event.getUriInfo().getPath();
        if (excludedPaths.stream().anyMatch(path::contains)) {
            return null;
        }
        if (event.getContainerRequest().getMethod().equals(HttpMethod.GET)) {
            return new HttpGetRequestJdbiUnitOfWorkEventListener(unitOfWorkProvider.getHandleManager());
        }
        return new NonHttpGetRequestJdbiUnitOfWorkEventListener(unitOfWorkProvider.getHandleManager());
    }
}
