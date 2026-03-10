package com.github.sfenker.essential.registry;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
import static net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode;

public class CustomEmojiRegistry {

    public static void initCustomEmojiRegistry(
        @NotNull ShardManager shardManager
    ) {
        shardManager.getShards()
            .getFirst()
            .retrieveApplicationEmojis()
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
        return customEmojis.getOrDefault(name, DEFAULT_EMOJI);
    }

    public static @NotNull Emoji customEmoji(
        @NotNull String key,
        @NotNull String name
    ) {
        return customEmojis.getOrDefault(
            format("%s_%s", key, name),
            DEFAULT_EMOJI
        );
    }

    static final Emoji DEFAULT_EMOJI =
        fromUnicode("❓");

    static final Map<String, Emoji> customEmojis =
        newHashMap();

}
