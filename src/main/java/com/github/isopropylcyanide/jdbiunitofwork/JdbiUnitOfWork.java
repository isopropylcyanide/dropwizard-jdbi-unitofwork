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
package com.github.isopropylcyanide.jdbiunitofwork;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * When annotating a Jersey resource method, wraps the method in a Jdbi transaction.
 * Utilises Jersey request response lifecycle events to manage the wrapped transaction.
 * <p>
 * A transaction will be automatically started before the resource method is
 * invoked, committed if the method returned, and rolled back if an exception was thrown.
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface JdbiUnitOfWork {
}
