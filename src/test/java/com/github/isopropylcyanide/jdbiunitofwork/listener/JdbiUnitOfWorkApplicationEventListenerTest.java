package com.github.isopropylcyanide.jdbiunitofwork.listener;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import com.google.common.collect.Sets;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.HttpMethod;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JdbiUnitOfWorkApplicationEventListenerTest {

    @Mock
    private JdbiHandleManager handleManager;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RequestEvent mockRequestEvent;

    private JdbiUnitOfWorkApplicationEventListener applicationListener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Set<String> excludedPaths = Sets.newHashSet("excluded");
        this.applicationListener = new JdbiUnitOfWorkApplicationEventListener(handleManager, excludedPaths);
    }

    @Test
    public void testOnEventDoesNothing() {
        ApplicationEvent mockEvent = mock(ApplicationEvent.class);
        applicationListener.onEvent(mockEvent);
        verify(mockEvent, times(1)).getType();
    }

    @Test
    public void testOnRequestDoesNothingWhenRequestEventPathIsExcluded() {
        when(mockRequestEvent.getUriInfo().getPath()).thenReturn("excluded");
        assertNull(applicationListener.onRequest(mockRequestEvent));
    }

    @Test
    public void testOnRequestReturnsCorrectEventListenerWhenMethodTypeIsGet() {
        when(mockRequestEvent.getUriInfo().getPath()).thenReturn("exclude-me-not");
        when(mockRequestEvent.getContainerRequest().getMethod()).thenReturn(HttpMethod.GET);

        RequestEventListener eventListener = applicationListener.onRequest(mockRequestEvent);
        assertEquals(HttpGetRequestJdbiUnitOfWorkEventListener.class, eventListener.getClass());
    }

    @Test
    public void testOnRequestReturnsCorrectEventListenerWhenMethodTypeIsNotGet() {
        when(mockRequestEvent.getUriInfo().getPath()).thenReturn("exclude-me-not");
        when(mockRequestEvent.getContainerRequest().getMethod()).thenReturn(HttpMethod.PUT);

        RequestEventListener eventListener = applicationListener.onRequest(mockRequestEvent);
        assertEquals(NonHttpGetRequestJdbiUnitOfWorkEventListener.class, eventListener.getClass());
    }
}
