package com.github.isopropylcyanide.example.app.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface AppDao {

    @SqlQuery("select 2+2 from dual")
    int dual();

    @SqlUpdate("insert into t1(id, val) values (:id, :val)")
    void createT1Entry(@Bind("id") int id, @Bind("val") String val);

    @SqlUpdate("insert into t2(id, val) values (:id, :val)")
    void createT2Entry(@Bind("id") int id, @Bind("val") String val);
}
