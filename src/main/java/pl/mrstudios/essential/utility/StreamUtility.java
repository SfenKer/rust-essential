package pl.mrstudios.essential.utility;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.lang.String.join;
import static java.util.Objects.requireNonNull;

public class StreamUtility {

    public static @NotNull String readResource(
            @NotNull String resourceName
    ) {

        try (
                InputStream inputStream = StreamUtility.class.getClassLoader().getResourceAsStream(resourceName);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(requireNonNull(inputStream)))
        ) {
            return join("", bufferedReader.lines().toList());
        } catch (@NotNull Exception exception) {
            throw new RuntimeException("Unable to read '" + resourceName + "' resource.", exception);
        }

    }

    public static @NotNull InputStream byteArrayInputStream(
            byte[] bytes
    ) {
        return new ByteArrayInputStream(bytes);
    }

}
