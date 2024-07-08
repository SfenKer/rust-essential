package pl.mrstudios.essential.utility;

import org.jetbrains.annotations.NotNull;

public class ThreadUtility {

    public static @NotNull Thread createThread(
            @NotNull Runnable runnable
    ) {
        return new Thread(runnable);
    }

}
