package com.github.isopropylcyanide.jdbiunitofwork;

import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import com.google.common.collect.Sets;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")
public class JdbiUnitOfWorkModuleTest {

    @Mock
    private JdbiHandleManager mockHandleManager;

    private Injector injector;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Set<String> daoPackages = Sets.newHashSet("com.github.isopropylcyanide.jdbiunitofwork");

        when(mockHandleManager.get()).thenReturn(mock(Handle.class));
        JdbiUnitOfWorkModule module = new JdbiUnitOfWorkModule(mockHandleManager, daoPackages);
        injector = Guice.createInjector(module);
    }

    @Test
    public void testModuleBindsTheProxiedDao() {
        assertNotNull(injector.getInstance(DaoA.class));
        assertNotNull(injector.getInstance(DaoB.class));

        thrown.expect(ConfigurationException.class);
        assertNotNull(injector.getInstance(DaoC.class));

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
