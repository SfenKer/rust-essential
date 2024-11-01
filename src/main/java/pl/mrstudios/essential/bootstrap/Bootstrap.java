package pl.mrstudios.essential.bootstrap;

import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.bot.Entrypoint;

public class Bootstrap {

    public static void main(
        @NotNull String[] arguments
    ) {
        new Entrypoint();
    }

}
