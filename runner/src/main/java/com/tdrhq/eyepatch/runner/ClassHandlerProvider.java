package com.tdrhq.eyepatch.runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used on EyePatch tests to indicate which classes
 * need to be mockable in the test.
 *
 * Currently, all mocked classes are *completely* mocked, i.e you
 * can't partially mock classes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ClassHandlerProvider {
    Class value();
}
