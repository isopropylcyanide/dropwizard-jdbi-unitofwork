package com.github.isopropylcyanide.example.app.resource;

import com.github.isopropylcyanide.example.app.dao.CountingDao;
import com.github.isopropylcyanide.jdbiunitofwork.JdbiUnitOfWork;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.stream.IntStream;

@Path("/")
public class CountingResource {

    private final CountingDao dao;

    public CountingResource(CountingDao dao) {
        this.dao = dao;
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

    private void insert(Integer size, @DefaultValue("-1") @QueryParam("failOn") Integer failOn) {
        IntStream.range(0, size).forEach(i -> {
            if (i == failOn) {
                throw new RuntimeException("Expected failure during insertion");
            }
            dao.insert(String.valueOf(i));
        });
    }
}
