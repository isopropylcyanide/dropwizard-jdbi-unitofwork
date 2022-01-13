package com.github.isopropylcyanide.jdbiunitofwork.core;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestScopedJdbiHandleManagerTest {

    private Jdbi jdbi;

    private RequestScopedJdbiHandleManager manager;

    @Before
    public void setUp() {
        jdbi = mock(Jdbi.class);
        this.manager = new RequestScopedJdbiHandleManager(jdbi);
    }

    @Test
    public void testGetSetsSameHandleForMultipleInvocationsInSameThread() {
        when(jdbi.open()).thenAnswer((Answer<Handle>) invocation -> mock(Handle.class));
        Handle firstHandle = manager.get();
        Handle secondHandle = manager.get();
        assertEquals(firstHandle, secondHandle);

        verify(jdbi, times(1)).open();
    }

    @Test
    public void testGetSetsNewHandleForEachThread() throws InterruptedException {
        when(jdbi.open()).thenAnswer((Answer<Handle>) invocation -> mock(Handle.class));
        Handle handleThreadA = manager.get();

        Thread newHandleInvokerThread = new Thread(() -> assertNotEquals(handleThreadA, manager.get()));
        newHandleInvokerThread.start();
        newHandleInvokerThread.join();
        verify(jdbi, times(2)).open();
    }

    @Test
    public void testClearClosesHandleAndClearsThreadLocal() {
        Handle mockHandle = mock(Handle.class);
        when(jdbi.open()).thenReturn(mockHandle);

        manager.get();
        manager.clear();
        verify(jdbi, times(1)).open();
        verify(mockHandle, times(1)).close();
    }

    @Test
    public void testClearDoesNothingWhenHandleIsNull() {
        manager.clear();
        verify(jdbi, never()).open();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateThreadFactoryIsNotSupported() {
        manager.createThreadFactory();
    }
}
