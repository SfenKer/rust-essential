package com.github.sfenker.essential.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

public class EmbedUtility {

    public static @NotNull EmbedBuilder embedBuilder() {
        return new EmbedBuilder();
    }

}
