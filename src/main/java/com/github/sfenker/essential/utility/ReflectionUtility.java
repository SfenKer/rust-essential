package com.github.sfenker.essential.utility;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import static java.util.Arrays.stream;

public class ReflectionUtility {

    @SneakyThrows
    public static @NotNull <RETURN> RETURN supplyMethod(
        @NotNull String name,
        @NotNull Object instance,
        @Nullable Object[] parameters
    ) {
        return supplyMethod(
            method(
                name, instance.getClass(),
                stream(parameters)
                    .filter(Objects::nonNull)
                    .map(Object::getClass)
                    .toArray(Class[]::new)
            ), instance, parameters
        );
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static @NotNull <RETURN> RETURN supplyMethod(
        @NotNull Method method,
        @NotNull Object instance,
        @Nullable Object[] parameters
    ) {
        method.setAccessible(true);
        return (RETURN) method.invoke(instance, parameters);
    }

    @SneakyThrows
    public static @NotNull Method method(
        @NotNull String name,
        @NotNull Class<?> clazz,
        @NotNull Class<?>... parameters
    ) {
        return clazz.getDeclaredMethod(name, parameters);
    }

    public static void writeField(
        @NotNull String name,
        @NotNull Object instance,
        @Nullable Object value
    ) {
        writeField(field(name, instance.getClass()), instance, value);
    }

    @SneakyThrows
    public static void writeField(
        @NotNull Field field,
        @NotNull Object instance,
        @Nullable Object value
    ) {
        field.setAccessible(true);
        field.set(instance, value);
    }

    public static @NotNull Field field(
        @NotNull String name,
        @NotNull Class<?> clazz
    ) {
        return stream(clazz.getDeclaredFields())
            .filter((field) -> field.getName().equals(name))
            .peek((field) -> field.setAccessible(true))
            .findFirst()
            .orElseThrow();
    }

}
