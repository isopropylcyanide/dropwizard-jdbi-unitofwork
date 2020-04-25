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
