package pl.mrstudios.essential.bootstrap;

import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.bot.Bot;

import java.time.Instant;

import static java.time.Instant.now;

public class Bootstrap {

    private static final Instant applicationStartTime = now();

    public static void main(
        @NotNull String[] arguments
    ) {

        /* Initialize */
        new Bot();

    }

    public static @NotNull Instant applicationStartTime() {
        return applicationStartTime;
    }

}
