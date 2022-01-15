package com.github.isopropylcyanide.example.app;

import com.github.isopropylcyanide.example.app.dao.AppDao;
import com.github.isopropylcyanide.example.app.resource.AppResource;
import com.github.isopropylcyanide.jdbiunitofwork.JdbiUnitOfWorkProvider;
import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiHandleManager;
import com.github.isopropylcyanide.jdbiunitofwork.core.RequestScopedJdbiHandleManager;
import com.github.isopropylcyanide.jdbiunitofwork.listener.JdbiUnitOfWorkApplicationEventListener;
import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

import java.util.HashSet;

public class SampleApplication extends Application<SampleApplicationConfig> {

    public static void main(String[] args) throws Exception {
        new SampleApplication().run(args);
    }

    @Override
    public void run(SampleApplicationConfig config, Environment environment) {
        DBI dbi = getDbi(config, environment);
        JdbiHandleManager handleManager = new RequestScopedJdbiHandleManager(dbi);
        JdbiUnitOfWorkProvider unitOfWorkProvider = new JdbiUnitOfWorkProvider(handleManager);

        AppDao appDAO = (AppDao) unitOfWorkProvider.getWrappedInstanceForDaoClass(AppDao.class);
        environment.jersey().register(new AppResource(appDAO));
        environment.jersey().register(new JdbiUnitOfWorkApplicationEventListener(handleManager, new HashSet<>()));
    }

    private DBI getDbi(SampleApplicationConfig config, Environment environment) {
        DBIFactory dbiFactory = new DBIFactory();
        return dbiFactory.build(environment, config.getDatabase(), "mysql-local-database");
    }
}
