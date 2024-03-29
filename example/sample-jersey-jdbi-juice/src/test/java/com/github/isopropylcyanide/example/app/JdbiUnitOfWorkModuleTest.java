package com.github.isopropylcyanide.example.app;

import com.github.isopropylcyanide.example.app.dao.CountingDao;
import com.github.isopropylcyanide.example.app.resource.CountingResource;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import jersey.repackaged.com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class JdbiUnitOfWorkModuleTest {

    private Injector injector;

    @Before
    public void setUp() {
        List<String> daoPackages = Lists.newArrayList("com.github.isopropylcyanide.example.app");
        JdbiUnitOfWorkModule module = new JdbiUnitOfWorkModule(mock(DBI.class), daoPackages);
        injector = Guice.createInjector(module);
    }

    @Test
    public void testModuleBindsTheProxiedDaoButFailsForOther() {
        assertNotNull(injector.getInstance(CountingDao.class));
        assertThrows(ConfigurationException.class, () -> injector.getInstance(CountingResource.class));
    }
}
