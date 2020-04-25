package com.github.isopropylcyanide.jdbiunitofwork.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LinkedRequestScopedJdbiHandleManagerTest {

    @Mock
    private DBI dbi;

    private LinkedRequestScopedJdbiHandleManager manager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.manager = new LinkedRequestScopedJdbiHandleManager(dbi);
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
    public void testGetSetsSameHandleForChildThreadsIfTheThreadFactoryIsPlaced() throws InterruptedException {
        when(dbi.open()).thenAnswer((Answer<Handle>) invocation -> mock(Handle.class));
        Handle parentHandle = manager.get();

        String parentThreadIdentity = String.valueOf(Thread.currentThread().getId());
        ThreadFactory threadFactory = manager.createThreadFactory(parentThreadIdentity);

        final int NUM_THREADS = 6;
        CountDownLatch endGate = new CountDownLatch(NUM_THREADS);
        ExecutorService service = Executors.newFixedThreadPool(NUM_THREADS, threadFactory);

        for (int i = 0; i < NUM_THREADS; i++) {
            service.submit(() -> {
                Handle childHandle = manager.get();
                assertEquals(parentHandle, childHandle);
                endGate.countDown();
            });
        }
        service.shutdown();
        endGate.await();
        verify(dbi, times(1)).open();
    }

    @Test
    public void testGetSetsNewHandleForChildThreadsIfTheThreadFactoryIsNotPlaced() throws InterruptedException {
        when(dbi.open()).thenAnswer((Answer<Handle>) invocation -> mock(Handle.class));
        Handle parentHandle = manager.get();

        final int NUM_THREADS = 5;
        CountDownLatch endGate = new CountDownLatch(NUM_THREADS);
        ExecutorService service = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            service.submit(() -> {
                Handle childHandle = manager.get();
                assertNotEquals(parentHandle, childHandle);
                endGate.countDown();
            });
        }
        service.shutdown();
        endGate.await();
        verify(dbi, times(NUM_THREADS + 1)).open();
    }

    @Test
    public void testClearClosesHandleAndClearsHandle() {
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
}
