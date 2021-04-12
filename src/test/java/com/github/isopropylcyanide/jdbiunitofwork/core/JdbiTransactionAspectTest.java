package com.github.isopropylcyanide.jdbiunitofwork.core;

import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.Handle;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JdbiTransactionAspectTest {

    private JdbiHandleManager handleManager;

    private Handle mockHandle;

    private JdbiTransactionAspect aspect;

    @Before
    public void setUp() {
        handleManager = mock(JdbiHandleManager.class);
        mockHandle = mock(Handle.class);
        when(handleManager.get()).thenReturn(mockHandle);
        this.aspect = new JdbiTransactionAspect(handleManager);
    }

    @Test
    public void testInitHandleWorksAsExpected() {
        aspect.initHandle();

        verify(handleManager, times(1)).get();
    }

    @Test
    public void testBeginWhenHandleBeginThrowsException() {
        aspect.initHandle();

        when(mockHandle.begin()).thenThrow(IllegalArgumentException.class);
        assertThrows(IllegalArgumentException.class, () -> aspect.begin());
        verify(handleManager, times(1)).clear();
        verify(mockHandle, never()).commit();
    }

    @Test
    public void testBeginWorksAsExpected() {
        aspect.initHandle();

        doReturn(mockHandle).when(mockHandle).begin();
        aspect.begin();

        verify(mockHandle, times(1)).begin();
        verify(mockHandle, never()).close();
        verify(handleManager, never()).clear();
        verify(mockHandle, never()).commit();
    }

    @Test
    public void testCommitDoesNothingWhenHandleIsNull() {
        mockHandle = null;
        aspect.commit();
        // No exception means no method called on the null handle
    }

    @Test
    public void testCommitWhenHandleCommitThrowsException() {
        aspect.initHandle();

        when(mockHandle.commit()).thenThrow(IllegalArgumentException.class);
        assertThrows(IllegalArgumentException.class, () -> aspect.commit());
        verify(mockHandle, times(1)).rollback();
    }

    @Test
    public void testCommitWorksAsExpected() {
        aspect.initHandle();

        doReturn(mockHandle).when(mockHandle).commit();
        aspect.commit();

        verify(mockHandle, times(1)).commit();
        verify(mockHandle, never()).rollback();
    }

    @Test
    public void testRollbackDoesNothingWhenHandleIsNull() {
        mockHandle = null;
        aspect.rollback();
        // No exception means no method called on the null handle
    }

    @Test
    public void testRollbackWhenHandleRollbackThrowsException() {
        aspect.initHandle();

        when(mockHandle.rollback()).thenThrow(IllegalArgumentException.class);
        assertThrows(IllegalArgumentException.class, () -> aspect.rollback());
        verify(mockHandle, times(1)).rollback();
    }

    @Test
    public void testRollbackWorksAsExpected() {
        aspect.initHandle();

        doReturn(mockHandle).when(mockHandle).rollback();
        aspect.rollback();

        verify(mockHandle, times(1)).rollback();
        verify(handleManager, times(1)).clear();
    }

    @Test
    public void testTerminateHandleDoesNothingWhenHandleIsNull() {
        mockHandle = null;
        aspect.terminateHandle();
        // No exception means no method called on the null handle
    }

    @Test
    public void testTerminateHandleWorksAsExpected() {
        aspect.initHandle();
        aspect.terminateHandle();

        verify(handleManager, times(1)).clear();
    }
}
