package com.github.isopropylcyanide.jdbiunitofwork.listener;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skife.jdbi.v2.Handle;

import static org.glassfish.jersey.server.monitoring.RequestEvent.Type.FINISHED;
import static org.glassfish.jersey.server.monitoring.RequestEvent.Type.ON_EXCEPTION;
import static org.glassfish.jersey.server.monitoring.RequestEvent.Type.RESOURCE_METHOD_START;
import static org.glassfish.jersey.server.monitoring.RequestEvent.Type.RESP_FILTERS_START;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NonHttpGetRequestJdbiUnitOfWorkEventListenerTest {

    @Mock
    private JdbiHandleManager handleManager;

    @Mock
    private Handle mockHandle;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RequestEvent mockEvent;

    private NonHttpGetRequestJdbiUnitOfWorkEventListener listener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(handleManager.get()).thenReturn(mockHandle);
        when(mockEvent.getContainerRequest().getMethod()).thenReturn("PUT");
        this.listener = new NonHttpGetRequestJdbiUnitOfWorkEventListener(handleManager);
    }

    @Test
    public void testHandleIsInitialisedWhenEventTypeIsResourceMethodStartButNotTransactional() {
        when(mockEvent.getType()).thenReturn(RESOURCE_METHOD_START);
        when(mockEvent.getUriInfo().getMatchedResourceMethod()).thenReturn(null);

        listener.onEvent(mockEvent);
        verify(handleManager, times(1)).get();
    }

    @Test
    public void testHandleIsNotCommittedWhenEventTypeIsRespFilterStartButNotTransactional() {
        when(mockEvent.getType()).thenReturn(RESP_FILTERS_START);
        when(mockEvent.getUriInfo().getMatchedResourceMethod()).thenReturn(null);

        listener.onEvent(mockEvent);
        verify(handleManager, never()).get();
    }

    @Test
    public void testHandleIsNotRolledBackWhenEventTypeIsOnExceptionButNotTransactional() {
        when(mockEvent.getType()).thenReturn(ON_EXCEPTION);
        when(mockEvent.getUriInfo().getMatchedResourceMethod()).thenReturn(null);

        listener.onEvent(mockEvent);
        verify(handleManager, never()).get();
    }

    @Test
    public void testHandleIsTerminatedWhenEventTypeIsResourceMethodStartButNotTransactional() {
        when(mockEvent.getType()).thenReturn(RESOURCE_METHOD_START).thenReturn(FINISHED);
        when(mockEvent.getUriInfo().getMatchedResourceMethod()).thenReturn(null);

        listener.onEvent(mockEvent);
        verify(handleManager, times(1)).get();

        listener.onEvent(mockEvent);
        verify(handleManager, times(1)).clear();
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
