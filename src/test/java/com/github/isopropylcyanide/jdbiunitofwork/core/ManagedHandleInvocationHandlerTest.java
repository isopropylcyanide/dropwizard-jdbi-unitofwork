package com.github.isopropylcyanide.jdbiunitofwork.core;

import com.google.common.reflect.Reflection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skife.jdbi.v2.Handle;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class ManagedHandleInvocationHandlerTest {

    @Mock
    private JdbiHandleManager handleManager;

    @Mock
    private Handle mockHandle;

    private Class<DummyDao> declaringClass;

    private ManagedHandleInvocationHandler proxy;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.declaringClass = DummyDao.class;
        this.proxy = new ManagedHandleInvocationHandler(handleManager, declaringClass);
    }

    @Test
    public void testHandleIsAttachedInTheProxyClassAndIsCalled() {
        when(handleManager.get()).thenReturn(mockHandle);
        Object proxiedInstance = Reflection.newProxy(declaringClass, proxy);
        when(mockHandle.attach(declaringClass)).thenReturn(new DummyDaoImpl(mockHandle));

        DummyDao proxiedDao = declaringClass.cast(proxiedInstance);
        proxiedDao.query();
        verify(mockHandle, times(1)).select(any());
        verify(mockHandle, times(1)).attach(any());
    }

    @Test
    public void testToStringCallsTheInstanceMethodAndNotTheProxyMethod() {
        Object proxiedInstance = Reflection.newProxy(declaringClass, proxy);
        when(mockHandle.attach(declaringClass)).thenReturn(new DummyDaoImpl(mockHandle));
        DummyDao proxy = declaringClass.cast(proxiedInstance);

        String str = proxy.toString();
        assertEquals("Proxy[DummyDao]", str);
        verify(handleManager, never()).get();
    }

    interface DummyDao {
        void query();
    }

    class DummyDaoImpl implements DummyDao {

        private Handle handle;

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
