package com.github.isopropylcyanide.jdbiunitofwork.listener;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.jdbi.v3.core.Handle;
import org.junit.Before;
import org.junit.Test;

import static org.glassfish.jersey.server.monitoring.RequestEvent.Type.FINISHED;
import static org.glassfish.jersey.server.monitoring.RequestEvent.Type.RESOURCE_METHOD_START;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpGetRequestJdbiUnitOfWorkEventListenerTest {

    private JdbiHandleManager handleManager;

    private RequestEvent requestEvent;

    private HttpGetRequestJdbiUnitOfWorkEventListener listener;

    @Before
    public void setUp() {
        handleManager = mock(JdbiHandleManager.class);
        when(handleManager.get()).thenReturn(mock(Handle.class));
        requestEvent = mock(RequestEvent.class);
        this.listener = new HttpGetRequestJdbiUnitOfWorkEventListener(handleManager);
    }

    @Test
    public void testHandleIsInitialisedWhenEventTypeIsResourceMethodStart() {
        when(requestEvent.getType()).thenReturn(RESOURCE_METHOD_START);
        listener.onEvent(requestEvent);
        verify(handleManager, times(1)).get();
    }

    @Test
    public void testHandleIsClosedWhenEventTypeIsFinished() {
        when(requestEvent.getType()).thenReturn(RESOURCE_METHOD_START).thenReturn(FINISHED);
        listener.onEvent(requestEvent);
        verify(handleManager, times(1)).get();

        listener.onEvent(requestEvent);
        verify(handleManager, times(1)).clear();
    }
}
