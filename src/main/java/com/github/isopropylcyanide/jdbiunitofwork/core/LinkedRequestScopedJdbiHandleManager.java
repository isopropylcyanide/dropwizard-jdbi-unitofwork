package com.github.isopropylcyanide.jdbiunitofwork.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

/**
 * This implementation provides a handle scoped to a thread X and all other threads Y
 * All Y threads must follow a particular name format "{@value NAME_FORMAT}" where {@code %s}
 * should be replaced by the hashcode of the parent. This is the only way the manager can know
 * that the threads are common. Other ways include thread groups etc.
 * <p><br>
 * It can be used to service requests where only a single handle instance has to be used by multiple
 * threads that are spawned with the specified name format from an initial thread. Use this only
 * when you have complete control over the threads you create. The threads must not run once the
 * parent thread is returned to the pool or else the handles will be invalid or in other words
 * parent thread must block on the results of children.
 *
 * <p><br>
 * It relies on the fact that the {@code Jdbi.Handle} is inherently thread safe and can be used to service
 * dao requests between multiple threads.
 * Note: Not suitable when you can not set the name format for the newly spawned threads.
 **/
@Slf4j
public class LinkedRequestScopedJdbiHandleManager implements JdbiHandleManager {

    private static final String NAME_FORMAT = "[%s]-%%d";
    private final Map<String, Handle> parentThreadHandleMap = new ConcurrentHashMap<>();
    private final DBI dbi;

    public LinkedRequestScopedJdbiHandleManager(DBI dbi) {
        this.dbi = dbi;
    }

    @Override
    public Handle get() {
        String parent = StringUtils.substringBetween(Thread.currentThread().getName(), "[", "]");
        Handle handle;
        if (parent == null) {
            handle = getHandle();
            log.debug("Owner of handle [{}] : Parent Thread Id [{}]", handle.hashCode(), Thread.currentThread().getId());

        } else {
            handle = parentThreadHandleMap.get(parent);
            if (handle == null) {
                throw new IllegalStateException(String.format("Handle to be reused in child thread [%s] is null for parent thread [%s]", Thread.currentThread().getId(), parent));
            }
            log.debug("Reusing parent thread handle [{}] for [{}]", handle.hashCode(), Thread.currentThread().getId());
        }
        return handle;
    }

    @Override
    public void clear() {
        String parent = getThreadIdentity();
        Handle handle = parentThreadHandleMap.get(parent);
        if (handle != null) {
            handle.close();
            log.debug("Closed handle Thread Id [{}] has handle id [{}]", Thread.currentThread().getId(), handle.hashCode());

            parentThreadHandleMap.remove(parent);
            log.debug("Clearing handle member for parent thread [{}] ", Thread.currentThread().getId());
        }
    }

    @Override
    public ThreadFactory createThreadFactory(String threadIdentity) {
        String threadName = String.format(NAME_FORMAT, threadIdentity);
        return new ThreadFactoryBuilder().setNameFormat(threadName).build();
    }

    private Handle getHandle() {
        String threadIdentity = getThreadIdentity();
        if (parentThreadHandleMap.containsKey(threadIdentity)) {
            return parentThreadHandleMap.get(threadIdentity);
        }
        Handle handle = dbi.open();
        parentThreadHandleMap.putIfAbsent(threadIdentity, handle);
        return handle;
    }

    private String getThreadIdentity() {
        return String.valueOf(Thread.currentThread().getId());
    }
}
