package pl.mrstudios.essential.utility;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.String.format;
import static java.lang.String.join;

public class StringUtility {

    public static @NotNull String formatDuration(
        @NotNull Duration duration
    ) {

        Collection<String> collection = new ArrayList<>();

        if (duration.toDaysPart() > 0)
            collection.add(pluralize(duration.toDaysPart(), "day", "days"));

        if (duration.toHoursPart() > 0)
            collection.add(pluralize(duration.toHoursPart(), "hour", "hours"));

        if (duration.toMinutesPart() > 0)
            collection.add(pluralize(duration.toMinutesPart(), "minute", "minutes"));

        if (duration.toSecondsPart() > 0)
            collection.add(pluralize(duration.toSecondsPart(), "second", "seconds"));

        return join(", ", collection);

    }

    public static @NotNull String pluralize(
        @NotNull Number count,
        @NotNull String singular,
        @NotNull String plural
    ) {
        return format("%s %s", count, (count.longValue() == 1) ? singular : plural);
    }

    public static @NotNull String throwableToString(
        @NotNull Throwable throwable
    ) {
        try (
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter)
        ) {

            throwable.printStackTrace(printWriter);

            return stringWriter.getBuffer()
                .toString();

        } catch (
            @NotNull Exception exception
        ) {
            throw new RuntimeException(exception);
        }
    }

}
