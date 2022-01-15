package com.github.isopropylcyanide.example.app.resource;

import com.codahale.metrics.health.HealthCheck;
import com.github.isopropylcyanide.example.app.dao.AppDao;
import com.github.isopropylcyanide.jdbiunitofwork.JdbiUnitOfWork;
import org.apache.commons.lang3.RandomUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AppResource {

    private final AppDao appDAO;

    public AppResource(AppDao appDAO) {
        this.appDAO = appDAO;
    }

    @GET
    @Path("/health")
    public String getHealth() {
        int dual = appDAO.dual();
        return HealthCheck.Result.healthy("OK -> " + dual).getMessage();
    }

    @POST
    @Path("/atomic")
    @JdbiUnitOfWork
    public int insertInTransaction(@QueryParam("fail") boolean fail) {
        int id = RandomUtils.nextInt(1, 100000);
        appDAO.createT1Entry(id, "123");
        if (fail) {
            throw new IllegalArgumentException("Gotta fail");
        }
        // expectation is for the unit of work to kick in and undo the first write
        appDAO.createT2Entry(id, "123");
        return 0;
    }
}
