package com.github.isopropylcyanide.example.app;

import com.github.isopropylcyanide.example.app.dao.AppDao;
import com.github.isopropylcyanide.example.app.dao.CountingDao;
import com.github.isopropylcyanide.example.app.resource.AppResource;
import com.github.isopropylcyanide.example.app.resource.CountingResource;
import com.github.isopropylcyanide.jdbiunitofwork.core.JdbiUnitOfWorkProvider;
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
        JdbiUnitOfWorkProvider provider = JdbiUnitOfWorkProvider.withLinked(dbi);

        environment.jersey().register(new JdbiUnitOfWorkApplicationEventListener(provider, new HashSet<>()));
        registerDao(environment, provider);
    }

    private void registerDao(Environment environment, JdbiUnitOfWorkProvider provider) {
        AppDao appDAO = (AppDao) provider.getWrappedInstanceForDaoClass(AppDao.class);
        environment.jersey().register(new AppResource(appDAO));

        CountingDao countingDao = (CountingDao) provider.getWrappedInstanceForDaoClass(CountingDao.class);
        environment.jersey().register(new CountingResource(provider, countingDao));
    }

    private DBI getDbi(SampleApplicationConfig config, Environment environment) {
        DBIFactory dbiFactory = new DBIFactory();
        return dbiFactory.build(environment, config.getDatabase(), "local-db");
    }
}
