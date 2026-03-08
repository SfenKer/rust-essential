package com.github.sfenker.essential.scheduler.factory;

import com.github.sfenker.essential.scheduler.annotation.function.Entrypoint;
import com.github.sfenker.essential.scheduler.annotation.function.Inject;
import com.github.sfenker.essential.scheduler.annotation.function.ParameterSupplier;
import com.github.sfenker.essential.scheduler.annotation.type.Scheduler;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static ch.qos.logback.core.util.ExecutorServiceUtil.newScheduledExecutorService;
import static com.github.sfenker.essential.utility.ReflectionUtility.supplyMethod;
import static com.github.sfenker.essential.utility.ReflectionUtility.writeField;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.cartesianProduct;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.stream;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

public class SchedulerFactory {

    final Set<Class<?>> tasks =
        newHashSet();

    final Map<Class<?>, Object> parameters =
        newHashMap();

    final ScheduledExecutorService executorService =
        newScheduledExecutorService();

    public @NotNull SchedulerFactory registerScheduler(
        @NotNull Class<?> clazz
    ) {
        checkArgument(
            clazz.isAnnotationPresent(Scheduler.class),
            "Class %s is not annotated with @Scheduler",
            clazz.getName()
        );
        this.tasks.add(clazz);
        return this;
    }

    public @NotNull SchedulerFactory registerParameter(
        @NotNull Class<?> type,
        @NotNull Object value
    ) {
        this.parameters.put(type, value);
        return this;
    }

    public void build() {

        this.tasks.stream()
            .collect(toMap(
                (clazz) -> clazz,
                this::initScheduledTask
            ))
            .forEach(
                (clazz, runnable) ->
                    this.executorService.scheduleAtFixedRate(
                        runnable, 0,
                        clazz.getAnnotation(Scheduler.class).period(),
                        clazz.getAnnotation(Scheduler.class).unit()
                    )
            );

    }

    @SneakyThrows
    @SuppressWarnings("rawtypes")
    @NotNull Runnable initScheduledTask(
        @NotNull Class<?> clazz
    ) {

        var instance = clazz.getDeclaredConstructor()
            .newInstance();

        stream(clazz.getDeclaredFields())
            .filter(
                (field) ->
                    field.isAnnotationPresent(Inject.class)
            )
            .peek(
                (field) ->
                    checkArgument(
                        this.parameters.containsKey(field.getType()),
                        "No registered parameter for injected field %s of type %s in %s",
                        field.getName(), field.getType().getName(), clazz.getName()
                    )
            )
            .forEach(
                (field) ->
                    writeField(field, instance, this.parameters.get(field.getType()))
            );

        var method = stream(clazz.getDeclaredMethods())
            .filter((entry) -> entry.isAnnotationPresent(Entrypoint.class))
            .findFirst()
            .orElseThrow();

        var suppliers = stream(clazz.getDeclaredMethods())
            .filter((entry) -> entry.isAnnotationPresent(ParameterSupplier.class))
            .collect(toMap(
                (entry) ->
                    entry.getAnnotation(ParameterSupplier.class)
                        .type(),
                identity()
            ));

        var parameters = stream(method.getParameters())
            .map(
                (parameter) ->
                    (Supplier) () -> supplyMethod(
                        suppliers.get(parameter.getType()),
                        instance, null
                    )
            )
            .toList();

        return () -> {

            var args = parameters.stream()
                .map(Supplier::get)
                .map((object) -> copyOf((Collection<?>) object))
                .toList();

            cartesianProduct(args)
                .forEach(
                    (arguments) ->
                        supplyMethod(method, instance, arguments.toArray())
                );

        };

    }

    public static @NotNull SchedulerFactory createSchedulerFactory() {
        return new SchedulerFactory();
    }

}
