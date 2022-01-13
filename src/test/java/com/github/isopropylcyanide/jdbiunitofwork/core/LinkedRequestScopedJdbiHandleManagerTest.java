package com.github.isopropylcyanide.jdbiunitofwork.core;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

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

    private Jdbi jdbi;

    private LinkedRequestScopedJdbiHandleManager manager;

    @Before
    public void setUp() {
        jdbi = mock(Jdbi.class);
        this.manager = new LinkedRequestScopedJdbiHandleManager(jdbi);
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
    public void testGetSetsSameHandleForChildThreadsIfTheThreadFactoryIsPlaced() throws InterruptedException {
        when(jdbi.open()).thenAnswer((Answer<Handle>) invocation -> mock(Handle.class));
        Handle parentHandle = manager.get();
        ThreadFactory threadFactory = manager.createThreadFactory();

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
        verify(jdbi, times(1)).open();
    }

    @Test
    public void testGetSetsNewHandleForChildThreadsIfTheThreadFactoryIsNotPlaced() throws InterruptedException {
        when(jdbi.open()).thenAnswer((Answer<Handle>) invocation -> mock(Handle.class));
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
        verify(jdbi, times(NUM_THREADS + 1)).open();
    }

    @Test
    public void testClearClosesHandleAndClearsHandle() {
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
}
