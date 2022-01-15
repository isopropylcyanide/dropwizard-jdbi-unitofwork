package com.github.isopropylcyanide.jdbiunitofwork.core;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JdbiUnitOfWorkProviderTest {

    @Mock
    private DBI dbi;

    private JdbiUnitOfWorkProvider provider;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.provider = JdbiUnitOfWorkProvider.withDefault(dbi);
    }

    @Test
    public void testGetWrappedInstanceForDaoClass() {
        assertNotNull(provider.getWrappedInstanceForDaoClass(DaoA.class));
        assertNotNull(provider.getWrappedInstanceForDaoClass(DaoB.class));
        assertThrows(IllegalArgumentException.class, () -> provider.getWrappedInstanceForDaoClass(DaoC.class));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testGetWrappedInstanceForDaoPackage() {
        Map<? extends Class, Object> instanceObjectMap = provider.getWrappedInstanceForDaoPackage(Lists.newArrayList(
                "com.github.isopropylcyanide.jdbiunitofwork"
        ));
        assertEquals(2, instanceObjectMap.size());
        assertNotNull(instanceObjectMap.get(DaoA.class));
        assertNotNull(instanceObjectMap.get(DaoB.class));
        assertNull(instanceObjectMap.get(DaoC.class));
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