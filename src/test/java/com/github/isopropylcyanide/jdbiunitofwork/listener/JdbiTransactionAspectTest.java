package com.github.isopropylcyanide.jdbiunitofwork.listener;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skife.jdbi.v2.Handle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @BeforeEach
    public void setUp() {
        handleManager = mock(JdbiHandleManager.class);
        mockHandle = mock(Handle.class);
        when(handleManager.get()).thenReturn(mockHandle);
        this.aspect = new JdbiTransactionAspect(handleManager);
    }

    @Test
    public void testBeginWhenHandleBeginThrowsException() {
        when(mockHandle.begin()).thenThrow(IllegalArgumentException.class);
        assertThrows(IllegalArgumentException.class, () -> aspect.begin());
        verify(handleManager, times(1)).clear();
        verify(mockHandle, never()).commit();
    }

    @Test
    public void testBeginWorksAsExpected() {
        doReturn(mockHandle).when(mockHandle).begin();
        aspect.begin();

        verify(mockHandle, times(1)).begin();
        verify(mockHandle, never()).close();
        verify(handleManager, never()).clear();
        verify(mockHandle, never()).commit();
    }

    @Test
    public void testCommitDoesNothingWhenHandleIsNull() {
        assertDoesNotThrow(() -> aspect.commit());
    }

    @Test
    public void testCommitWhenHandleCommitThrowsException() {
        when(mockHandle.commit()).thenThrow(IllegalArgumentException.class);
        assertThrows(IllegalArgumentException.class, () -> aspect.commit());
        verify(mockHandle, times(1)).rollback();
    }

    @Test
    public void testCommitWorksAsExpected() {
        doReturn(mockHandle).when(mockHandle).commit();
        aspect.commit();

        verify(mockHandle, times(1)).commit();
        verify(mockHandle, never()).rollback();
    }

    @Test
    public void testRollbackDoesNothingWhenHandleIsNull() {
        assertDoesNotThrow(() -> aspect.rollback());
    }

    @Test
    public void testRollbackWhenHandleRollbackThrowsException() {
        when(mockHandle.rollback()).thenThrow(IllegalArgumentException.class);
        assertThrows(IllegalArgumentException.class, () -> aspect.rollback());
        verify(mockHandle, times(1)).rollback();
    }

    @Test
    public void testRollbackWorksAsExpected() {
        doReturn(mockHandle).when(mockHandle).rollback();
        aspect.rollback();

        verify(mockHandle, times(1)).rollback();
        verify(handleManager, times(1)).clear();
    }

    @Test
    public void testTerminateHandleDoesNothingWhenHandleIsNull() {
        assertDoesNotThrow(() -> aspect.terminateHandle());
    }

    @Test
    public void testTerminateHandleWorksAsExpected() {
        aspect.terminateHandle();
        verify(handleManager, times(1)).clear();
    }
}
