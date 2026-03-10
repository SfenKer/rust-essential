package com.github.sfenker.essential.utility;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationWords;

public class StringUtility {

    public static @NotNull String throwableToString(
        @NotNull Throwable throwable
    ) {
        return stream(throwable.getStackTrace())
            .limit(20)
            .map(
                (element) ->
                    "    at " + element.toString()
            )
            .reduce(
                throwable.toString(),
                (string, line) ->
                    string + "\n" + line
            );
    }

    public static @NotNull String formatDuration(
        @NotNull Duration duration
    ) {
        return formatDurationWords(duration.toMillis(), true, true)
            .replaceAll("\\s(?=\\d)", ", ")
            .replaceFirst(",(?=[^,]*$)", " and");
    }

}
