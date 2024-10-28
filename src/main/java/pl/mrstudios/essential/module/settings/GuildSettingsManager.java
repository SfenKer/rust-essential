package pl.mrstudios.essential.module.settings;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.commons.sql.SqlConnection;
import pl.mrstudios.essential.module.settings.document.JsonDocument;

import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static java.time.Duration.ofMinutes;
import static pl.mrstudios.commons.sql.statement.SqlStatement.createStatement;
import static pl.mrstudios.essential.module.settings.GuildSettingsSqlRepository.*;

public class GuildSettingsManager {

    private final SqlConnection sqlConnection;
    private final Cache<Long, JsonDocument<GuildSettings>> cache;

    @SneakyThrows
    public GuildSettingsManager(
        @NotNull JDA jda,
        @NotNull SqlConnection sqlConnection
    ) {

        /* Await Ready */
        jda.awaitReady();

        /* Then Complete */
        this.sqlConnection = sqlConnection;
        this.cache = newBuilder()
            .expireAfterAccess(ofMinutes(5))
            .build();

        createStatement(guildsCreateTable).execute(this.sqlConnection);
        createStatement(guildsSelectAllRecords).fetch(this.sqlConnection)
            .stream().map((result) -> result.entry("guildId").asLong())
            .filter(
                (guildId) -> jda.getGuilds().stream()
                    .noneMatch((guild) -> guild.getIdLong() == guildId)
            ).forEach(
                (guildId) -> createStatement(guildsDeleteEntry)
                    .setLong(1, guildId)
                    .execute(this.sqlConnection)
            );

    }

    public @NotNull JsonDocument<GuildSettings> guildSettings(
        @NotNull Guild guild
    ) {
        return this.guildSettings(guild.getIdLong());
    }

    public @NotNull JsonDocument<GuildSettings> guildSettings(
        @NotNull Long guildId
    ) {
        return this.cache.get(guildId, (key) -> new JsonDocument<>() {

            private final GuildSettings settings = createStatement(guildsSelectByGuildId)
                .setLong(1, key).fetch(sqlConnection).stream().findFirst()
                .map(
                    (result) -> gson.fromJson(result.entry("settings").asString(), GuildSettings.class)
                ).orElseGet(() -> {

                    createStatement(guildsInsertInto)
                        .setLong(1, key)
                        .setLongString(2, "{}")
                        .execute(sqlConnection);

                    return new GuildSettings();

                });

            @Override
            public @NotNull GuildSettings read() {
                return this.settings;
            }

            @Override
            public void save() {
                createStatement(guildsUpdateSettings)
                    .setLongString(1, gson.toJson(this.settings))
                    .setLong(2, guildId)
                    .execute(sqlConnection);
            }

        });
    }

    protected static final Gson gson = new Gson();

}
