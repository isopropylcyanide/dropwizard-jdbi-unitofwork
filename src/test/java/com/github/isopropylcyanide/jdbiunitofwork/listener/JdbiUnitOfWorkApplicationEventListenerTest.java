package com.github.isopropylcyanide.jdbiunitofwork.listener;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiUnitOfWorkProvider;
import com.google.common.collect.Sets;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skife.jdbi.v2.Handle;

import javax.ws.rs.HttpMethod;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JdbiUnitOfWorkApplicationEventListenerTest {

    private RequestEvent requestEvent;

    private JdbiUnitOfWorkApplicationEventListener applicationListener;

    @BeforeEach
    public void setUp() {
        JdbiHandleManager handleManager = mock(JdbiHandleManager.class);
        when(handleManager.get()).thenReturn(mock(Handle.class));
        JdbiUnitOfWorkProvider provider = mock(JdbiUnitOfWorkProvider.class);
        when(provider.getHandleManager()).thenReturn(handleManager);

        requestEvent = mock(RequestEvent.class, Mockito.RETURNS_DEEP_STUBS);
        Set<String> excludedPaths = Sets.newHashSet("excluded");
        this.applicationListener = new JdbiUnitOfWorkApplicationEventListener(provider, excludedPaths);
    }

    @Test
    public void testOnEventDoesNothing() {
        ApplicationEvent applicationEvent = mock(ApplicationEvent.class);
        applicationListener.onEvent(applicationEvent);
        verify(applicationEvent, times(1)).getType();
    }

    @Test
    public void testOnRequestDoesNothingWhenRequestEventPathIsExcluded() {
        when(requestEvent.getUriInfo().getPath()).thenReturn("excluded");
        assertNull(applicationListener.onRequest(requestEvent));
    }

    @Test
    public void testOnRequestReturnsCorrectEventListenerWhenMethodTypeIsGet() {
        when(requestEvent.getUriInfo().getPath()).thenReturn("exclude-me-not");
        when(requestEvent.getContainerRequest().getMethod()).thenReturn(HttpMethod.GET);

        RequestEventListener eventListener = applicationListener.onRequest(requestEvent);
        assertEquals(HttpGetRequestJdbiUnitOfWorkEventListener.class, eventListener.getClass());
    }

    @Test
    public void testOnRequestReturnsCorrectEventListenerWhenMethodTypeIsNotGet() {
        when(requestEvent.getUriInfo().getPath()).thenReturn("exclude-me-not");
        when(requestEvent.getContainerRequest().getMethod()).thenReturn(HttpMethod.PUT);

        RequestEventListener eventListener = applicationListener.onRequest(requestEvent);
        assertEquals(NonHttpGetRequestJdbiUnitOfWorkEventListener.class, eventListener.getClass());
    }
}
