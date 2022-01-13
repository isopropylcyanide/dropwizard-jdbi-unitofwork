package com.github.isopropylcyanide.jdbiunitofwork.listener;

import javax.ws.rs.core.MediaType;

import com.github.isopropylcyanide.jdbiunitofwork.JdbiUnitOfWork;
import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.jdbi.v3.core.Handle;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.glassfish.jersey.server.monitoring.RequestEvent.Type.FINISHED;
import static org.glassfish.jersey.server.monitoring.RequestEvent.Type.ON_EXCEPTION;
import static org.glassfish.jersey.server.monitoring.RequestEvent.Type.RESOURCE_METHOD_START;
import static org.glassfish.jersey.server.monitoring.RequestEvent.Type.RESP_FILTERS_START;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NonHttpGetRequestJdbiUnitOfWorkEventListenerTest {

    private JdbiHandleManager handleManager;

    private Handle handle;

    private RequestEvent requestEvent;

    private NonHttpGetRequestJdbiUnitOfWorkEventListener listener;

    @Before
    public void setUp() {
        handleManager = mock(JdbiHandleManager.class);
        handle = mock(Handle.class);
        when(handleManager.get()).thenReturn(handle);
        requestEvent = mock(RequestEvent.class, Mockito.RETURNS_DEEP_STUBS);
        when(requestEvent.getContainerRequest().getMethod()).thenReturn("PUT");
        this.listener = new NonHttpGetRequestJdbiUnitOfWorkEventListener(handleManager);
    }

    @Test
    public void testHandleIsInitialisedWhenEventTypeIsResourceMethodStartButNotTransactional() {
        when(requestEvent.getType()).thenReturn(RESOURCE_METHOD_START);
        when(requestEvent.getUriInfo().getMatchedResourceMethod()).thenReturn(null);

        listener.onEvent(requestEvent);
        verify(handleManager, times(1)).get();
    }

    @Test
    public void testHandleIsNotCommittedWhenEventTypeIsRespFilterStartButNotTransactional() {
        when(requestEvent.getType()).thenReturn(RESP_FILTERS_START);
        when(requestEvent.getUriInfo().getMatchedResourceMethod()).thenReturn(null);

        listener.onEvent(requestEvent);
        verify(handleManager, never()).get();
    }

    @Test
    public void testHandleIsNotRolledBackWhenEventTypeIsOnExceptionButNotTransactional() {
        when(requestEvent.getType()).thenReturn(ON_EXCEPTION);
        when(requestEvent.getUriInfo().getMatchedResourceMethod()).thenReturn(null);

        listener.onEvent(requestEvent);
        verify(handleManager, never()).get();
    }

    @Test
    public void testHandleIsTerminatedWhenEventTypeIsResourceMethodStartButNotTransactional() {
        when(requestEvent.getType()).thenReturn(RESOURCE_METHOD_START).thenReturn(FINISHED);
        when(requestEvent.getUriInfo().getMatchedResourceMethod()).thenReturn(null);

        listener.onEvent(requestEvent);
        verify(handleManager, times(1)).get();

        listener.onEvent(requestEvent);
        verify(handleManager, times(1)).clear();
    }

    @Test
    public void testHandleIsClosedWhenEventTypeIsFinished() {
        when(requestEvent.getType()).thenReturn(RESOURCE_METHOD_START).thenReturn(FINISHED);
        listener.onEvent(requestEvent);
        verify(handleManager, times(1)).get();

        listener.onEvent(requestEvent);
        verify(handleManager, times(1)).clear();
    }

    @Test
    public void testHandleIsInitialisedWhenEventTypeIsResourceMethodStartTransactional() throws NoSuchMethodException {
        when(requestEvent.getType()).thenReturn(RESOURCE_METHOD_START);
        when(requestEvent.getUriInfo().getMatchedResourceMethod()).thenReturn(getMockResourceMethod());

        listener.onEvent(requestEvent);
        verify(handleManager, times(1)).get();
        verify(handle, times(1)).begin();
    }

    @Test
    public void testHandleIsNotCommittedWhenEventTypeIsRespFilterStartTransactional() throws NoSuchMethodException {
        when(requestEvent.getType()).thenReturn(RESOURCE_METHOD_START).thenReturn(RESP_FILTERS_START);
        when(requestEvent.getUriInfo().getMatchedResourceMethod()).thenReturn(getMockResourceMethod());

        listener.onEvent(requestEvent);
        verify(handleManager, times(1)).get();

        listener.onEvent(requestEvent);
        verify(handle, times(1)).commit();
    }

    @Test
    public void testHandleIsNotRolledBackWhenEventTypeIsOnExceptionTransactional() throws NoSuchMethodException {
        when(requestEvent.getType()).thenReturn(RESOURCE_METHOD_START).thenReturn(ON_EXCEPTION);
        when(requestEvent.getUriInfo().getMatchedResourceMethod()).thenReturn(getMockResourceMethod());

        listener.onEvent(requestEvent);
        verify(handleManager, times(1)).get();

        listener.onEvent(requestEvent);
        verify(handle, times(1)).rollback();
    }

    private ResourceMethod getMockResourceMethod() throws NoSuchMethodException {
        return Resource
                .builder()
                .addMethod("PUT")
                .produces(MediaType.TEXT_PLAIN_TYPE)
                .handledBy(ResourceMethodStub.class, ResourceMethodStub.class.getMethod("apply"))
                .build();
    }

    static class ResourceMethodStub {
        @JdbiUnitOfWork
        public String apply() {
            return "";
        }
    }
}