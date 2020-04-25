package com.github.isopropylcyanide.jdbiunitofwork.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultJdbiHandleManagerTest {

    @Mock
    private DBI dbi;

    private DefaultJdbiHandleManager manager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.manager = new DefaultJdbiHandleManager(dbi);
    }

    @Test
    public void testGetSetsTheHandlePerInvocation() throws InterruptedException {
        when(dbi.open()).thenAnswer((Answer<Handle>) invocation -> mock(Handle.class));
        Handle firstHandle = manager.get();
        Handle secondHandle = manager.get();
        assertNotEquals(firstHandle, secondHandle);

        Thread newHandleInvokerThread = new Thread(() -> assertNotEquals(firstHandle, manager.get()));
        newHandleInvokerThread.start();
        newHandleInvokerThread.join();
        verify(dbi, times(3)).open();
    }

    @Test
    public void testClear() {
        manager.clear();
        verify(dbi, never()).open();
    }
}
