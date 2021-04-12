package com.github.isopropylcyanide.example.app;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import com.google.common.collect.Sets;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")
public class JdbiUnitOfWorkModuleTest {

    private Injector injector;

    @Before
    public void setUp() {
        JdbiHandleManager mockHandleManager = mock(JdbiHandleManager.class);
        when(mockHandleManager.get()).thenReturn(mock(Handle.class));
        Set<String> daoPackages = Sets.newHashSet("com.github.isopropylcyanide.example.app");
        JdbiUnitOfWorkModule module = new JdbiUnitOfWorkModule(mockHandleManager, daoPackages);
        injector = Guice.createInjector(module);
    }

    @Test
    public void testModuleBindsTheProxiedDao() {
        assertNotNull(injector.getInstance(DaoA.class));
        assertNotNull(injector.getInstance(DaoB.class));
        assertThrows(ConfigurationException.class, () -> injector.getInstance(DaoC.class));
    }

    interface DaoA {

        @SqlUpdate
        void update();
    }

    interface DaoB {

        @SqlQuery
        void select();
    }

    interface DaoC {
    }
}
