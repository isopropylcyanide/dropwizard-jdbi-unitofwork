package com.github.isopropylcyanide.jdbiunitofwork.listener;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.Handle;

import static org.glassfish.jersey.server.monitoring.RequestEvent.Type.FINISHED;
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
    public void testHandleIsClosedWhenEventTypeIsFinished() {
        when(requestEvent.getType()).thenReturn(FINISHED);

        listener.onEvent(requestEvent);
        verify(handleManager, times(1)).clear();
    }
}
