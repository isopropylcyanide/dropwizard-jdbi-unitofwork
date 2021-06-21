package com.github.isopropylcyanide.jdbiunitofwork.core;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultJdbiHandleManagerTest {

    private Jdbi jdbi;

    private DefaultJdbiHandleManager manager;

    @Before
    public void setUp() {
        jdbi = mock(Jdbi.class);
        this.manager = new DefaultJdbiHandleManager(jdbi);
    }

    @Test
    public void testGetSetsTheHandlePerInvocation() throws InterruptedException {
        when(jdbi.open()).thenAnswer((Answer<Handle>) invocation -> mock(Handle.class));
        Handle firstHandle = manager.get();
        Handle secondHandle = manager.get();
        assertNotEquals(firstHandle, secondHandle);

        Thread newHandleInvokerThread = new Thread(() -> assertNotEquals(firstHandle, manager.get()));
        newHandleInvokerThread.start();
        newHandleInvokerThread.join();
        verify(jdbi, times(3)).open();
    }

    @Test
    public void testClear() {
        manager.clear();
        verify(jdbi, never()).open();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateThreadFactoryIsNotSupported() {
        manager.createThreadFactory();
    }
}
