package com.github.isopropylcyanide.jdbiunitofwork.core;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skife.jdbi.v2.Handle;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JdbiTransactionAspectTest {

    @Mock
    private JdbiHandleManager handleManager;

    @Mock
    private Handle mockHandle;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private JdbiTransactionAspect aspect;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.aspect = new JdbiTransactionAspect(handleManager);
    }

    @Test
    public void testInitHandleWorksAsExpected() {
        when(handleManager.get()).thenReturn(mockHandle);
        aspect.initHandle();

        verify(handleManager, times(1)).get();
    }

    @Test
    public void testBeginWhenHandleBeginThrowsException() {
        when(handleManager.get()).thenReturn(mockHandle);
        aspect.initHandle();

        when(mockHandle.begin()).thenThrow(IllegalArgumentException.class);
        thrown.expect(IllegalArgumentException.class);
        aspect.begin();

        verify(mockHandle, times(1)).close();
        verify(handleManager, times(1)).clear();
        verify(mockHandle, never()).commit();
    }

    @Test
    public void testBeginWorksAsExpected() {
        when(handleManager.get()).thenReturn(mockHandle);
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
        when(handleManager.get()).thenReturn(null);
        aspect.commit();

        verify(mockHandle, never()).commit();
        verify(mockHandle, never()).rollback();
    }

    @Test
    public void testCommitWhenHandleCommitThrowsException() {
        when(handleManager.get()).thenReturn(mockHandle);
        aspect.initHandle();

        when(mockHandle.commit()).thenThrow(IllegalArgumentException.class);
        thrown.expect(IllegalArgumentException.class);

        aspect.commit();
        verify(mockHandle, times(1)).rollback();
    }

    @Test
    public void testCommitWorksAsExpected() {
        when(handleManager.get()).thenReturn(mockHandle);
        aspect.initHandle();

        doReturn(mockHandle).when(mockHandle).commit();
        aspect.commit();

        verify(mockHandle, times(1)).commit();
        verify(mockHandle, never()).rollback();
    }

    @Test
    public void testRollbackDoesNothingWhenHandleIsNull() {
        when(handleManager.get()).thenReturn(null);
        aspect.rollback();

        verify(mockHandle, never()).rollback();
    }

    @Test
    public void testRollbackWhenHandleRollbackThrowsException() {
        when(handleManager.get()).thenReturn(mockHandle);
        aspect.initHandle();

        when(mockHandle.rollback()).thenThrow(IllegalArgumentException.class);
        thrown.expect(IllegalArgumentException.class);

        aspect.rollback();
        verify(mockHandle, times(1)).rollback();
    }

    @Test
    public void testRollbackWorksAsExpected() {
        when(handleManager.get()).thenReturn(mockHandle);
        aspect.initHandle();

        doReturn(mockHandle).when(mockHandle).rollback();
        aspect.rollback();

        verify(mockHandle, times(1)).rollback();
        verify(handleManager, times(1)).clear();
    }

    @Test
    public void testTerminateHandleDoesNothingWhenHandleIsNull() {
        when(handleManager.get()).thenReturn(null);
        aspect.terminateHandle();

        verify(mockHandle, never()).close();
    }

    @Test
    public void testTerminateHandleWorksAsExpected() {
        when(handleManager.get()).thenReturn(mockHandle);
        aspect.initHandle();
        aspect.terminateHandle();

        verify(handleManager, times(1)).clear();
    }
}
