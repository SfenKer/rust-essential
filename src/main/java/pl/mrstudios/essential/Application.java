package pl.mrstudios.essential;

import com.zaxxer.hikari.HikariConfig;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import pl.mrstudios.commons.sql.SqlConnection;
import pl.mrstudios.essential.command.CommandAbout;
import pl.mrstudios.essential.command.CommandConfigure;
import pl.mrstudios.essential.command.CommandServerInfo;
import pl.mrstudios.essential.command.result.EmbedResponseResult;
import pl.mrstudios.essential.config.Configuration;
import pl.mrstudios.essential.config.ConfigurationFactory;
import pl.mrstudios.essential.listener.UserInteractionListener;
import pl.mrstudios.essential.module.calculator.command.CommandCalculator;
import pl.mrstudios.essential.module.changelog.command.CommandChangelog;
import pl.mrstudios.essential.module.news.NewsService;
import pl.mrstudios.essential.module.settings.GuildSettingsManager;
import pl.mrstudios.essential.utility.EmbedResponseUtility;

import java.nio.file.Path;

import static dev.rollczi.litecommands.annotations.LiteCommandsAnnotations.ofClasses;
import static dev.rollczi.litecommands.jda.LiteJDAFactory.builder;
import static dev.rollczi.litecommands.schematic.SchematicFormat.angleBrackets;
import static java.lang.Class.forName;
import static java.lang.Runtime.getRuntime;
import static java.nio.file.Files.*;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static net.dv8tion.jda.api.JDABuilder.createDefault;
import static net.dv8tion.jda.api.entities.Activity.playing;
import static net.dv8tion.jda.api.utils.Compression.ZLIB;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.*;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.mrstudios.essential.config.ConfigurationFactory.configurationFactory;
import static pl.mrstudios.essential.utility.ThreadUtility.createThread;
import static pl.mrstudios.essential.wrapper.RustMapsAPI.provideRustMapsApiKey;

@SuppressWarnings("FieldCanBeLocal")
public class Application {

    private final JDA jda;

    private final HikariConfig hikariConfig;
    private final SqlConnection sqlConnection;

    /* Logger */
    private final Logger logger = getLogger(Application.class);

    /* Configuration */
    private final Configuration configuration;
    private final ConfigurationFactory configurationFactory;

    /* Managers */
    private final GuildSettingsManager guildSettingsManager;

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
        this.jda = createDefault(this.configuration.token)
                .disableCache(asList(
                        ACTIVITY, CLIENT_STATUS, FORUM_TAGS, ONLINE_STATUS,
                        SCHEDULED_EVENTS, STICKER
                ))
                .setCompression(ZLIB)
                .setActivity(playing("Rust"))
                .addEventListeners(
                        new UserInteractionListener()
                ).build();

        /* Managers */
        this.guildSettingsManager = new GuildSettingsManager(this.jda, this.sqlConnection);

        /* Commands */
        builder(this.jda)

                /* Commands */
                .commands(ofClasses(
                        CommandCalculator.class,
                        CommandServerInfo.class,
                        CommandConfigure.class,
                        CommandChangelog.class,
                        CommandAbout.class
                ))

                /* Result */
                .result(EmbedResponseUtility.class, new EmbedResponseResult())

                /* Bind */
                .bind(Logger.class, () -> this.logger)
                .bind(SqlConnection.class, () -> this.sqlConnection)

                .bind(Configuration.class, () -> this.configuration)
                .bind(ConfigurationFactory.class, () -> this.configurationFactory)

                .bind(GuildSettingsManager.class, () -> this.guildSettingsManager)

                /* Schematic */
                .schematicGenerator(angleBrackets())

                /* Build */
                .build();

        /* Services */
        new NewsService(this.jda, this.sqlConnection, this.guildSettingsManager);

        /* API */
        provideRustMapsApiKey(this.configuration.rustMapsApiKey);

    }

    {
        this.logger.info("Application was loaded successfully.");
    }

    protected static final Path DATABASE_DIR_PATH = get("database");
    protected static final Path DATABASE_FILE_PATH = get("database/", "database.db");

}
