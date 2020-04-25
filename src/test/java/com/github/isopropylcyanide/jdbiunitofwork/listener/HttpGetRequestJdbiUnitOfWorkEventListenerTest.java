package com.github.isopropylcyanide.jdbiunitofwork.listener;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skife.jdbi.v2.Handle;

import static org.glassfish.jersey.server.monitoring.RequestEvent.Type.FINISHED;
import static org.glassfish.jersey.server.monitoring.RequestEvent.Type.RESOURCE_METHOD_START;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpGetRequestJdbiUnitOfWorkEventListenerTest {

    @Mock
    private JdbiHandleManager handleManager;

    @Mock
    private Handle mockHandle;

    @Mock
    private RequestEvent mockEvent;

    private HttpGetRequestJdbiUnitOfWorkEventListener listener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(handleManager.get()).thenReturn(mockHandle);
        this.listener = new HttpGetRequestJdbiUnitOfWorkEventListener(handleManager);
    }

    @Test
    public void testHandleIsInitialisedWhenEventTypeIsResourceMethodStart() {
        when(mockEvent.getType()).thenReturn(RESOURCE_METHOD_START);
        listener.onEvent(mockEvent);
        verify(handleManager, times(1)).get();
    }

    @Test
    public void testHandleIsClosedWhenEventTypeIsFinished() {
        when(mockEvent.getType()).thenReturn(RESOURCE_METHOD_START).thenReturn(FINISHED);
        listener.onEvent(mockEvent);
        verify(handleManager, times(1)).get();

        listener.onEvent(mockEvent);
        verify(handleManager, times(1)).clear();
    }
}
