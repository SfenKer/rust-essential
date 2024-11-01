package pl.mrstudios.essential.module.settings;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.commons.sql.SqlConnection;
import pl.mrstudios.essential.module.settings.serializer.GuildSettingEntrySerializer;
import pl.mrstudios.essential.module.settings.serializer.GuildSettingSerializer;
import pl.mrstudios.essential.module.settings.setting.GuildSetting;
import pl.mrstudios.essential.module.settings.setting.GuildSettingEntry;

import java.util.ArrayList;
import java.util.Collection;

import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static java.time.Duration.ofMinutes;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static pl.mrstudios.commons.sql.statement.SqlStatement.createStatement;
import static pl.mrstudios.essential.module.settings.GuildSettingsSqlRepository.*;

public class GuildSettingsManager {

    private final SqlConnection sqlConnection;
    private final Cache<Long, Collection<GuildSettingEntry>> cache = newBuilder()
        .expireAfterAccess(ofMinutes(15))
        .build();

    @SneakyThrows
    public GuildSettingsManager(
        @NotNull JDA jda,
        @NotNull SqlConnection sqlConnection
    ) {

        /* Await Ready */
        jda.awaitReady();

        /* Then Complete */
        this.sqlConnection = sqlConnection;

        /* Statements */
        createStatement(guildsCreateTable)
            .execute(this.sqlConnection);

        createStatement(guildsSelectAllRecords)
            .fetch(this.sqlConnection).stream()
            .map((result) -> result.entry("guildId").asLong())
            .filter(
                (guildId) -> jda.getGuilds().stream()
                    .noneMatch((guild) -> guild.getIdLong() == guildId)
            ).forEach(
                (guildId) -> createStatement(guildsDeleteEntry)
                    .setLong(1, guildId)
                    .execute(this.sqlConnection)
            );

    }

    public @NotNull Collection<GuildSettingEntry> fetchSettings(
        @NotNull Guild guild
    ) {
        return fetchSettings(guild.getIdLong());
    }

    public @NotNull Collection<GuildSettingEntry> fetchSettings(
        @NotNull Long guildId
    ) {
        return this.cache.get(
            guildId, (key) -> createStatement(guildsSelectByGuildId)
                .setLong(1, key)
                .fetch(this.sqlConnection).stream()
                .findFirst()
                .map((result) -> this.gson.fromJson(result.entry("settings").asString(), GuildSettingEntry[].class))
                .map((entries) -> stream(entries).collect(toList()))
                .orElseGet(() -> {

                    createStatement(guildsInsertInto)
                        .setLong(1, key)
                        .setLongString(2, "[]")
                        .execute(this.sqlConnection);

                    return new ArrayList<>();

                })
        );
    }

    public void updateSettings(
        @NotNull Guild guild,
        @NotNull Collection<GuildSettingEntry> settings
    ) {
        updateSettings(guild.getIdLong(), settings);
    }

    public void updateSettings(
        @NotNull Long guildId,
        @NotNull Collection<GuildSettingEntry> settings
    ) {
        this.cache.put(guildId, settings);
        createStatement(guildsUpdateSettings)
            .setString(1, this.gson.toJson(settings))
            .setLong(2, guildId)
            .execute(this.sqlConnection);
    }

    protected final @NotNull Gson gson = new GsonBuilder()
        .registerTypeAdapter(GuildSetting.class, new GuildSettingSerializer())
        .registerTypeAdapter(GuildSettingEntry.class, new GuildSettingEntrySerializer())
        .create();

}
