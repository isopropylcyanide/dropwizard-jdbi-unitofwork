package com.github.isopropylcyanide.example.app;

import com.github.isopropylcyanide.example.app.dao.CountingDao;
import com.github.isopropylcyanide.example.app.exception.CountingResourceExceptionMapper;
import com.github.isopropylcyanide.example.app.resource.CountingResource;
import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import com.github.isopropylcyanide.jdbiunitofwork.listener.JdbiUnitOfWorkApplicationEventListener;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ApplicationIntegrationTest extends JerseyTest {

    private static CountingDao dao;

    private static JdbiHandleManager handleManager;

    @ClassRule
    public static ExpectedException thrown;

    @ClassRule
    public static H2Datasource datasource = new H2Datasource("h2_schema.sql", "h2_schema_drop.sql");

    private static final Logger log = LoggerFactory.getLogger(ApplicationIntegrationTest.class);

    @BeforeClass
    public static void setup() {
        CountingModule module = new CountingModule(datasource.getDbi());
        Injector injector = Guice.createInjector(module);
        dao = injector.getInstance(CountingDao.class);
        handleManager = injector.getInstance(JdbiHandleManager.class);
    }

    @Override
    protected Application configure() {
        dao.clear();
        return new ResourceConfig()
                .register(new CountingResource(dao))
                .register(CountingResourceExceptionMapper.class)
                .register(new JdbiUnitOfWorkApplicationEventListener(handleManager, Sets.newHashSet("excluded")));
    }

    @Test
    public void testGetCountWorksAsExpected() {
        Response response = target("/count").request().get();
        Integer currentCount = response.readEntity(Integer.class);

        assertEquals(200, response.getStatus());
        assertEquals(Integer.valueOf(0), currentCount);
    }

    @Test
    public void testInsertWithFailureNonAtomicWorksAsExpectedWhenNoFailureIsSet() {
        Response response = target("/insert").request()
                .post(Entity.entity(4, MediaType.TEXT_PLAIN_TYPE));

        int currentCount = dao.count();
        assertEquals(204, response.getStatus());
        assertEquals(4, currentCount);
    }

    @Test
    public void testInsertWithFailureAtomicCommitsTransactionWhenNoFailureIsSet() {
        Response response = target("/insert/unitofwork").request()
                .post(Entity.entity(4, MediaType.TEXT_PLAIN_TYPE));

        int currentCount = dao.count();
        assertEquals(204, response.getStatus());
        assertEquals(4, currentCount);
    }

    @Test
    public void testInsertWithFailureNonAtomicDoesNotRollbackTransactionWhenFailureIsSet() {
        int failOn = 6;
        int expectedCountIfRolledBack = 0;
        Response response = target("/insert")
                .queryParam("failOn", failOn).request()
                .post(Entity.entity(10, MediaType.TEXT_PLAIN_TYPE));

        int currentCount = dao.count();
        assertEquals(500, response.getStatus());
        assertEquals(failOn, currentCount);
        assertNotEquals(expectedCountIfRolledBack, currentCount);
    }

    @Test
    public void testInsertWithFailureAtomicRollsBackTransactionWhenFailureIsSet() {
        int failOn = 6;
        int expectedCountIfRolledBack = 0;
        Response response = target("/insert/unitofwork")
                .queryParam("failOn", failOn).request()
                .post(Entity.entity(10, MediaType.TEXT_PLAIN_TYPE));

        int currentCount = dao.count();
        assertEquals(500, response.getStatus());
        assertEquals(0, currentCount);
        assertEquals(expectedCountIfRolledBack, currentCount);
    }

    @Test
    public void testInsertWithFailureAtomicCommitForNConcurrentThreads() throws InterruptedException {
        int numThread = 6;
        int countPerThread = 5;
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(numThread);

        for (int i = 0; i < numThread; i++) {
            new Thread(() -> {
                try {
                    startGate.await();
                    target("/insert/unitofwork").request()
                            .post(Entity.entity(countPerThread, MediaType.TEXT_PLAIN_TYPE));

                } catch (InterruptedException ex) {
                    log.error(ex.getMessage(), ex);
                } finally {
                    endGate.countDown();
                }
            }).start();
        }
        startGate.countDown();
        endGate.await();
        assertEquals(numThread * countPerThread, dao.count());
    }

    @Test
    public void testInsertWithFailureAtomicRollbackForNConcurrentThreads() throws InterruptedException {
        int numThread = 4;
        int countPerThread = 3;
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(numThread);

        for (int i = 0; i < numThread; i++) {
            new Thread(() -> {
                try {
                    startGate.await();
                    target("/insert/unitofwork")
                            .queryParam("failOn", 2)
                            .request()
                            .post(Entity.entity(countPerThread, MediaType.TEXT_PLAIN_TYPE));

                } catch (InterruptedException ex) {
                    log.error(ex.getMessage(), ex);
                } finally {
                    endGate.countDown();
                }
            }).start();
        }
        startGate.countDown();
        endGate.await();
        assertEquals(0, dao.count());
    }
}
