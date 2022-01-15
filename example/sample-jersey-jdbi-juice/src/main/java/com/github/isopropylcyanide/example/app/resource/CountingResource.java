package com.github.isopropylcyanide.example.app.resource;

import com.github.isopropylcyanide.example.app.dao.CountingDao;
import com.github.isopropylcyanide.jdbiunitofwork.JdbiUnitOfWork;
import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiUnitOfWorkProvider;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@Path("/")
public class CountingResource {

    private final JdbiUnitOfWorkProvider unitOfWorkProvider;
    private final CountingDao dao;

    public CountingResource(JdbiUnitOfWorkProvider unitOfWorkProvider, CountingDao dao) {
        this.dao = dao;
        this.unitOfWorkProvider = unitOfWorkProvider;
    }

    @GET
    @Path("/count")
    public Integer getCount() {
        return dao.count();
    }

    @POST
    @Path("/insert")
    public void insertWithFailureNonAtomic(Integer size, @QueryParam("failOn") @DefaultValue("-1") Integer failOn) {
        insert(size, failOn);
    }

    @POST
    @Path("/insert/unitofwork")
    @JdbiUnitOfWork
    public void insertWithFailureAtomic(Integer size, @QueryParam("failOn") @DefaultValue("-1") Integer failOn) {
        insert(size, failOn);
    }

    @POST
    @Path("/insert/multi/unitofwork")
    @JdbiUnitOfWork
    public void insertWithFailureAtomicUsingMultipleThreads(@QueryParam("numThreads") Integer numThreads,
                                                            @QueryParam("failOnce") boolean failOnce,
                                                            @QueryParam("failOn") @DefaultValue("-1") int failOn,
                                                            int size) throws Throwable {
        ExecutorService service = Executors.newCachedThreadPool();
        insertMultiThreaded(numThreads, failOnce, failOn, size, service);
    }

    @POST
    @Path("/insert/multi/unitofwork/factory")
    @JdbiUnitOfWork
    public void insertWithFailureAtomicUsingMultipleThreadsWithFactory(@QueryParam("numThreads") Integer numThreads,
                                                                       @QueryParam("failOnce") boolean failOnce,
                                                                       @QueryParam("failOn") @DefaultValue("-1") int failOn,
                                                                       int size) throws Throwable {
        ThreadFactory threadFactory = unitOfWorkProvider.getHandleManager().createThreadFactory();
        ExecutorService service = Executors.newCachedThreadPool(threadFactory);
        insertMultiThreaded(numThreads, failOnce, failOn, size, service);
    }

    private void insertMultiThreaded(@QueryParam("numThreads") Integer numThreads, @QueryParam("failOnce") boolean failOnce, @DefaultValue("-1") @QueryParam("failOn") int failOn, int size, ExecutorService service) throws Throwable {
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(numThreads);
        AtomicBoolean failOnceForSomeThread = new AtomicBoolean(failOnce);
        AtomicReference<Throwable> throwableAtomicReference = new AtomicReference<>(null);

        for (int i = 0; i < numThreads; i++) {
            service.execute(() -> {
                try {
                    startGate.await();
                    if (failOnceForSomeThread.compareAndSet(true, false)) {
                        insert(size, failOn);
                    } else {
                        insert(size, -1);
                    }
                } catch (Exception exception) {
                    throwableAtomicReference.compareAndSet(null, exception);
                } finally {
                    endGate.countDown();
                }
            });
        }
        service.shutdown();
        startGate.countDown();
        endGate.await();
        Throwable throwable = throwableAtomicReference.get();
        if (throwable != null) {
            throw throwable;
        }
    }

    private void insert(Integer size, @DefaultValue("-1") @QueryParam("failOn") Integer failOn) {
        IntStream.rangeClosed(1, size).forEach(i -> {
            if (i == failOn) {
                throw new RuntimeException("Expected failure during insertion");
            }
            dao.insert(String.valueOf(i));
        });
    }
}
