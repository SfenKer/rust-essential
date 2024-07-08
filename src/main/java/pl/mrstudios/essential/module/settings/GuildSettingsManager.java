package pl.mrstudios.essential.module.settings;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.gson.Gson;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import pl.mrstudios.essential.database.SQLite;

import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static java.lang.Runtime.getRuntime;
import static java.time.Duration.ofMinutes;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.mrstudios.essential.database.statement.SQLStatement.createStatement;
import static pl.mrstudios.essential.module.settings.GuildSettingsSqlRepository.*;
import static pl.mrstudios.essential.utility.ThreadUtility.createThread;

public class GuildSettingsManager {

    private final SQLite sqLite;
    private final Cache<Long, GuildSettings> cache;

    public GuildSettingsManager(
            @NotNull JDA jda,
            @NotNull SQLite sqLite
    ) {

        this.sqLite = sqLite;
        this.cache = newBuilder()
                .expireAfterAccess(ofMinutes(15))
                .removalListener(
                        (key, value, cause) -> ofNullable(value)
                                .map(GuildSettings.class::cast)
                                .ifPresent((settings) -> this.sqLite.execute(
                                        createStatement(guildsUpdateSettings)
                                                .setLongString(1, gson.toJson(settings))
                                                .setLong(2, (Long) requireNonNull(key))
                                ))
                ).build();

        this.sqLite.execute(createStatement(guildsCreateTable));
        this.sqLite.fetch(createStatement(guildsSelectAllRecords))
                .stream().map((result) -> result.entry("guildId").asLong())
                .filter(
                        (guildId) -> jda.getGuilds().stream()
                                .noneMatch((guild) -> guild.getIdLong() == guildId)
                ).forEach((guildId) -> this.sqLite.execute(
                        createStatement(guildsDeleteEntry)
                                .setLong(1, guildId)
                ));

        getRuntime().addShutdownHook(createThread(() -> {
            logger.info("Saving {} guild settings entries, please wait..", this.cache.estimatedSize());
            this.cache.invalidateAll();
            logger.info("All guild settings entries have been saved.");
        }));

    }

    public @NotNull GuildSettings guildSettings(
            @NotNull Guild guild
    ) {
        return this.cache.get(
                guild.getIdLong(), (key) -> this.sqLite.fetch(
                        createStatement(guildsSelectByGuildId)
                                .setLong(1, key)
                ).stream().findFirst().map(
                        (result) -> gson.fromJson(result.entry("settings").asString(), GuildSettings.class)
                ).orElseGet(GuildSettings::new)
        );
    }

    protected static final Gson gson = new Gson();
    protected static final Logger logger = getLogger(GuildSettingsManager.class);

}
