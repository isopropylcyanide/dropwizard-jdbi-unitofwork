package com.github.isopropylcyanide.example.app.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface CountingDao {

    @SqlUpdate("insert into transaction_test (id) values (:id)")
    void insert(@Bind("id") String id);

    @SqlQuery("select count(*) from transaction_test")
    int count();

    @SqlUpdate("delete from transaction_test")
    void clear();
}
