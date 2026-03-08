package com.github.sfenker.essential.scheduler.annotation.type;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.concurrent.TimeUnit.SECONDS;

@Target(TYPE)
@Retention(RUNTIME)
public @interface Scheduler {
    int period();
    TimeUnit unit() default SECONDS;
}
