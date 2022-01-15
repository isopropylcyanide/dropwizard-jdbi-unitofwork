package com.github.isopropylcyanide.jdbiunitofwork.core;

import com.google.common.reflect.Reflection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skife.jdbi.v2.Handle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"UnstableApiUsage"})
public class ManagedHandleInvocationHandlerTest {

    private JdbiHandleManager handleManager;

    private Handle mockHandle;

    DummyDao proxiedDao;

    @BeforeEach
    public void setUp() {
        handleManager = mock(JdbiHandleManager.class);
        mockHandle = mock(Handle.class);
        when(handleManager.get()).thenReturn(mockHandle);
        Class<DummyDao> declaringClass = DummyDao.class;
        ManagedHandleInvocationHandler<DummyDao> proxy = new ManagedHandleInvocationHandler<>(handleManager, declaringClass);
        Object proxiedInstance = Reflection.newProxy(declaringClass, proxy);
        when(mockHandle.attach(declaringClass)).thenReturn(new DummyDaoImpl(mockHandle));
        proxiedDao = declaringClass.cast(proxiedInstance);
    }

    @Test
    public void testHandleIsAttachedInTheProxyClassAndIsCalled() {
        proxiedDao.query();
        verify(mockHandle, times(1)).select(any());
        verify(mockHandle, times(1)).attach(any());
    }

    @Test
    public void testToStringCallsTheInstanceMethodAndNotTheProxyMethod() {
        String str = proxiedDao.toString();
        assertEquals("Proxy[DummyDao]", str);
        verify(handleManager, never()).get();
    }

    interface DummyDao {
        void query();
    }

    class DummyDaoImpl implements DummyDao {

        private final Handle handle;

        DummyDaoImpl(Handle handle) {
            this.handle = handle;
        }

        @Override
        public void query() {
            handle.select("select * from some_table");
            assertEquals(handle, mockHandle);
        }
    }
}
