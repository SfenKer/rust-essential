package pl.mrstudios.essential.bot;

import com.github.kaktushose.jda.commands.guice.GuiceExtensionData;
import com.google.inject.Injector;
import com.zaxxer.hikari.HikariConfig;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import pl.mrstudios.commons.sql.SqlConnection;
import pl.mrstudios.essential.command.internal.ErrorMessageFactoryImpl;
import pl.mrstudios.essential.config.Configuration;
import pl.mrstudios.essential.config.ConfigurationFactory;
import pl.mrstudios.essential.listener.GuildActionListener;
import pl.mrstudios.essential.service.news.NewsService;
import pl.mrstudios.essential.service.settings.GuildSettingsService;

import java.nio.file.Path;

import static com.github.kaktushose.jda.commands.JDACommands.builder;
import static com.github.kaktushose.jda.commands.definitions.interactions.InteractionDefinition.ReplyConfig.of;
import static com.github.kaktushose.jda.commands.dispatching.expiration.ExpirationStrategy.AFTER_15_MINUTES;
import static com.google.inject.Guice.createInjector;
import static java.lang.Runtime.getRuntime;
import static java.nio.file.Files.*;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static net.dv8tion.jda.api.entities.Activity.playing;
import static net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder.createDefault;
import static net.dv8tion.jda.api.utils.Compression.ZLIB;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.*;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.mrstudios.essential.config.ConfigurationFactory.configurationFactory;
import static pl.mrstudios.essential.utility.ThreadUtility.createThread;
import static pl.mrstudios.essential.wrapper.RustMapsWrapper.provideRustMapsApiKey;

@SuppressWarnings("FieldCanBeLocal")
public class Bot {

    private final ShardManager shardManager;

    private final HikariConfig hikariConfig;
    private final SqlConnection sqlConnection;

    /* Injector */
    private final Injector injector;

    /* Logger */
    private final Logger logger = getLogger(Bot.class);

    /* Configuration */
    private final Configuration configuration;
    private final ConfigurationFactory configurationFactory;

    /* Managers */
    private final GuildSettingsService guildSettingsService;

    {
        this.logger.info("Loading application, please wait...");
    }

    {

        /* Shutdown */
        getRuntime().addShutdownHook(createThread(
            () -> this.logger.info("Application is shutting down, please wait..")
        ));

        /* Configuration */
        this.configurationFactory = configurationFactory(get("config"));
        this.configuration = this.configurationFactory.produce(Configuration.class, "config.yml");

        /* SqlConnection */
        this.hikariConfig = new HikariConfig();
        this.hikariConfig.setJdbcUrl("jdbc:sqlite:database/database.db");

        try {
            if (!exists(DATABASE_FILE_PATH)) {
                createDirectories(DATABASE_DIR_PATH);
                createFile(DATABASE_FILE_PATH);
            }
        } catch (@NotNull Exception exception) {
            this.logger.error("An error occurred while creating the database file.", exception);
        }

        this.sqlConnection = new SqlConnection(this.hikariConfig);

        /* JDA */
        this.shardManager = createDefault(this.configuration.token)
            .setShardsTotal(4)
            .setCompression(ZLIB)
            .setActivity(playing("Rust"))
            .addEventListeners(
                new GuildActionListener()
            ).disableCache(asList(
                ACTIVITY, CLIENT_STATUS, FORUM_TAGS, ONLINE_STATUS,
                SCHEDULED_EVENTS, STICKER
            )).build();

        /* Managers */
        this.guildSettingsService = new GuildSettingsService(this.shardManager, this.sqlConnection);

        /* Injector */
        this.injector = createInjector((binder) -> {

            binder.bind(ShardManager.class)
                .toInstance(this.shardManager);

            binder.bind(Configuration.class)
                .toInstance(this.configuration);

            binder.bind(SqlConnection.class)
                .toInstance(this.sqlConnection);

            binder.bind(GuildSettingsService.class)
                .toInstance(this.guildSettingsService);

        });

        /* Commands */
        builder(this.shardManager, Bot.class, "pl.mrstudios.essential")
            .extensionData(new GuiceExtensionData(this.injector))
            .expirationStrategy(AFTER_15_MINUTES)
            .errorMessageFactory(new ErrorMessageFactoryImpl())
            .globalReplyConfig(of((config) -> config.ephemeral(true)))
            .start();

        /* Services */
        this.injector.getInstance(NewsService.class);

        /* API */
        provideRustMapsApiKey(this.configuration.rustMapsApiKey);

    }

    {
        this.logger.info("Application was loaded successfully.");
    }

    private static final Path DATABASE_DIR_PATH = get("database");
    private static final Path DATABASE_FILE_PATH = get("database/", "database.db");

}
