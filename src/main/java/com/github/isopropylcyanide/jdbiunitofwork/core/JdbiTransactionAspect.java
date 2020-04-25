/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.isopropylcyanide.jdbiunitofwork.core;

import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.Handle;

/**
 * An aspect providing low level operations around a {@link Handle}
 * This is inspired from Dropwizard's Unit of work aspect used to manage handles for hibernate.
 *
 * @see <a href="https://github.com/dropwizard/dropwizard/blob/master/dropwizard-hibernate/src/main/java/io/dropwizard/hibernate/UnitOfWorkAspect.java">
 * Unit Of Work Aspect</a>
 */
@Slf4j
public class JdbiTransactionAspect {

    private final JdbiHandleManager handleManager;
    private Handle handle;

    public JdbiTransactionAspect(JdbiHandleManager handleManager) {
        this.handleManager = handleManager;
    }

    public void initHandle() {
        handle = handleManager.get();
    }

    public void begin() {
        try {
            handle.begin();
            log.debug("Begin Transaction Thread Id [{}] has handle id [{}] Transaction {} Level {}", Thread.currentThread().getId(), handle.hashCode(), handle.isInTransaction(), handle.getTransactionIsolationLevel());

        } catch (Exception ex) {
            handleManager.clear();
            throw ex;
        }
    }

    public void commit() {
        if (handle == null) {
            log.debug("Handle was found to be null during commit for Thread Id [{}]. It might have already been closed", Thread.currentThread().getId());
            return;
        }
        try {
            handle.commit();
            log.debug("Performing commit Thread Id [{}] has handle id [{}] Transaction {} Level {}", Thread.currentThread().getId(), handle.hashCode(), handle.isInTransaction(), handle.getTransactionIsolationLevel());

        } catch (Exception ex) {
            handle.rollback();
            throw ex;
        }
    }

    public void rollback() {
        if (handle == null) {
            log.debug("Handle was found to be null during rollback for [{}]", Thread.currentThread().getId());
            return;
        }
        try {
            handle.rollback();
            log.debug("Performed rollback on Thread Id [{}] has handle id [{}] Transaction {} Level {}", Thread.currentThread().getId(), handle.hashCode(), handle.isInTransaction(), handle.getTransactionIsolationLevel());
        } finally {
            terminateHandle();
        }
    }

    public void terminateHandle() {
        try {
            handleManager.clear();
        } finally {
            handle = null;
        }
    }
}
