package pl.mrstudios.essential.service.settings.setting;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import static java.util.Arrays.stream;

@AllArgsConstructor
public enum GuildSetting {

    GUILD_NEWS_CHANNEL(
        "guild.news.channel",
        "News Channel",
        Long.class
    );

    public final @NotNull String id;
    public final @NotNull String name;
    public final @NotNull Class<?> type;

    public static @NotNull GuildSetting fromId(
        @NotNull String id
    ) {
        return stream(values())
            .filter((setting) -> setting.id.equals(id))
            .findFirst().orElseThrow();
    }

}
