package com.github.isopropylcyanide.jdbiunitofwork.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestScopedJdbiHandleManagerTest {

    private DBI dbi;

    private RequestScopedJdbiHandleManager manager;

    @Before
    public void setUp() {
        dbi = mock(DBI.class);
        this.manager = new RequestScopedJdbiHandleManager(dbi);
    }

    @Test
    public void testGetSetsSameHandleForMultipleInvocationsInSameThread() {
        when(dbi.open()).thenAnswer((Answer<Handle>) invocation -> mock(Handle.class));
        Handle firstHandle = manager.get();
        Handle secondHandle = manager.get();
        assertEquals(firstHandle, secondHandle);

        verify(dbi, times(1)).open();
    }

    @Test
    public void testGetSetsNewHandleForEachThread() throws InterruptedException {
        when(dbi.open()).thenAnswer((Answer<Handle>) invocation -> mock(Handle.class));
        Handle handleThreadA = manager.get();

        Thread newHandleInvokerThread = new Thread(() -> assertNotEquals(handleThreadA, manager.get()));
        newHandleInvokerThread.start();
        newHandleInvokerThread.join();
        verify(dbi, times(2)).open();
    }

    @Test
    public void testClearClosesHandleAndClearsThreadLocal() {
        Handle mockHandle = mock(Handle.class);
        when(dbi.open()).thenReturn(mockHandle);

        manager.get();
        manager.clear();
        verify(dbi, times(1)).open();
        verify(mockHandle, times(1)).close();
    }

    @Test
    public void testClearDoesNothingWhenHandleIsNull() {
        manager.clear();
        verify(dbi, never()).open();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateThreadFactoryIsNotSupported() {
        manager.createThreadFactory();
    }
}
