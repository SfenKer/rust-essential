package com.github.sfenker.essential.registry;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode;

public class CustomEmojiRegistry {

    public static void initCustomEmojiRegistry(
        @NotNull JDA jda
    ) {
        jda.retrieveApplicationEmojis()
            .queue(
                (result) ->
                    result.forEach(
                        (emoji) ->
                            customEmojis.put(emoji.getName(), emoji)
                    )
            );
    }

    public static @NotNull Emoji customEmoji(
        @NotNull String name
    ) {
        return customEmojis.get(name);
    }

    static final Emoji DEFAULT_EMOJI =
        fromUnicode("❓");

    static final Map<String, Emoji> customEmojis =
        newHashMap();

}
