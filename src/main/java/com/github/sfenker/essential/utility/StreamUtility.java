package com.github.sfenker.essential.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;

import static javax.imageio.ImageIO.read;

public class StreamUtility {

    public static @NotNull InputStream resourceStream(
        @NotNull String path
    ) {
        return StreamUtility.class.getClassLoader()
            .getResourceAsStream(path);
    }

    public static @NotNull BufferedImage imageResource(
        @NotNull String path
    ) {
        try (var stream = resourceStream(path)) {
            return read(stream);
        } catch (@NotNull Exception exception) {
            throw new RuntimeException("Unable to load image resource at path: " + path, exception);
        }
    }

    public static <TARGET_TYPE> @NotNull TARGET_TYPE jsonResource(
        @NotNull String path,
        @NotNull Class<TARGET_TYPE> targetType
    ) {
        try (
            var stream = resourceStream(path);
            var streamReader = new InputStreamReader(stream);
        ) {
            return gson.fromJson(streamReader, targetType);
        } catch (Exception exception) {
            throw new RuntimeException("Unable to load json resource at path: " + path, exception);
        }
    }

    static final Gson gson = new GsonBuilder()
        .create();

}
