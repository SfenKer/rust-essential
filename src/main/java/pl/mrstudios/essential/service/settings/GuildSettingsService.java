package pl.mrstudios.essential.service.settings;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.commons.sql.SqlConnection;
import pl.mrstudios.essential.service.settings.serializer.GuildSettingEntrySerializer;
import pl.mrstudios.essential.service.settings.serializer.GuildSettingSerializer;
import pl.mrstudios.essential.service.settings.setting.GuildSetting;
import pl.mrstudios.essential.service.settings.setting.GuildSettingContainer;

import java.util.ArrayList;
import java.util.Collection;

import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static java.time.Duration.ofMinutes;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.mrstudios.commons.sql.statement.SqlStatement.createStatement;
import static pl.mrstudios.essential.service.settings.GuildSettingsSqlRepository.*;

public class GuildSettingsService {

    private SqlConnection sqlConnection;
    private Cache<@NotNull Long, Collection<GuildSettingContainer>> cache;

    public GuildSettingsService(
        @NotNull ShardManager shardManager,
        @NotNull SqlConnection sqlConnection
    ) { try {

        /* Await Ready */
        shardManager.getShards()
            .forEach((shard) -> {

                try {
                    shard.awaitReady();
                } catch (@NotNull Exception exception) {
                    getLogger(GuildSettingsService.class)
                        .error("An exception occurred while waiting for shard to become available. (shardId: {})", shard.getShardInfo().getShardId());
                }

            });

        /* Then Complete */
        this.sqlConnection = sqlConnection;
        this.cache = newBuilder()
            .expireAfterAccess(ofMinutes(15))
            .build();

        /* Statements */
        createStatement(guildsCreateTable)
            .execute(this.sqlConnection);

        createStatement(guildsSelectAllRecords)
            .fetch(this.sqlConnection).stream()
            .map((result) -> result.entry("guildId").asLong())
            .filter(
                (guildId) -> shardManager.getGuilds().stream()
                    .noneMatch((guild) -> guild.getIdLong() == guildId)
            ).forEach(
                (guildId) -> createStatement(guildsDeleteEntry)
                    .setLong(1, guildId)
                    .execute(this.sqlConnection)
            );

    } catch (
        @NotNull Exception exception
    ) {
        getLogger(GuildSettingsService.class)
            .error("An exception occurred while loading guild settings.");
    } }

    public @NotNull Collection<GuildSettingContainer> fetchSettings(
        @NotNull Guild guild
    ) {
        return fetchSettings(guild.getIdLong());
    }

    public @NotNull Collection<GuildSettingContainer> fetchSettings(
        @NotNull Long guildId
    ) {
        return requireNonNull(this.cache.get(
            guildId, (key) -> createStatement(guildsSelectByGuildId)
                .setLong(1, key)
                .fetch(this.sqlConnection).stream()
                .findFirst()
                .map((result) -> this.gson.fromJson(result.entry("settings").asString(), GuildSettingContainer[].class))
                .map((entries) -> stream(entries).collect(toList()))
                .orElseGet(() -> {

                    createStatement(guildsInsertInto)
                        .setLong(1, key)
                        .setLongString(2, "[]")
                        .execute(this.sqlConnection);

                    return new ArrayList<>();

                })
        ));
    }

    public void updateSettings(
        @NotNull Guild guild,
        @NotNull Collection<GuildSettingContainer> settings
    ) {
        updateSettings(guild.getIdLong(), settings);
    }

    public void updateSettings(
        @NotNull Long guildId,
        @NotNull Collection<GuildSettingContainer> settings
    ) {
        this.cache.put(guildId, settings);
        createStatement(guildsUpdateSettings)
            .setString(1, this.gson.toJson(settings))
            .setLong(2, guildId)
            .execute(this.sqlConnection);
    }

    private final @NotNull Gson gson = new GsonBuilder()
        .registerTypeAdapter(GuildSetting.class, new GuildSettingSerializer())
        .registerTypeAdapter(GuildSettingContainer.class, new GuildSettingEntrySerializer())
        .create();

}
