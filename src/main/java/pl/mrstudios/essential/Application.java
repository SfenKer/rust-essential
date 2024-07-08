package pl.mrstudios.essential;

import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import pl.mrstudios.essential.command.CommandAbout;
import pl.mrstudios.essential.command.CommandConfigure;
import pl.mrstudios.essential.config.Configuration;
import pl.mrstudios.essential.config.ConfigurationFactory;
import pl.mrstudios.essential.database.SQLite;
import pl.mrstudios.essential.listener.UserInteractionListener;
import pl.mrstudios.essential.module.calculator.command.CommandCalculator;
import pl.mrstudios.essential.module.news.NewsService;
import pl.mrstudios.essential.module.settings.GuildSettingsManager;

import static dev.rollczi.litecommands.annotations.LiteCommandsAnnotations.ofClasses;
import static dev.rollczi.litecommands.jda.LiteJDAFactory.builder;
import static dev.rollczi.litecommands.schematic.SchematicFormat.angleBrackets;
import static java.lang.Runtime.getRuntime;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static net.dv8tion.jda.api.JDABuilder.createDefault;
import static net.dv8tion.jda.api.entities.Activity.playing;
import static net.dv8tion.jda.api.utils.Compression.ZLIB;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.*;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.mrstudios.essential.config.ConfigurationFactory.configurationFactory;
import static pl.mrstudios.essential.utility.ThreadUtility.createThread;

@SuppressWarnings("FieldCanBeLocal")
public class Application {

    private final JDA jda;
    private final SQLite sqLite;

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

        /* SQLite */
        this.sqLite = new SQLite();

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
        this.guildSettingsManager = new GuildSettingsManager(this.jda, this.sqLite);

        /* Commands */
        builder(this.jda)

                /* Commands */
                .commands(ofClasses(
                        CommandCalculator.class,
                        CommandConfigure.class,
                        CommandAbout.class
                ))

                /* Bind */
                .bind(Logger.class, () -> this.logger)
                .bind(SQLite.class, () -> this.sqLite)

                .bind(Configuration.class, () -> this.configuration)
                .bind(ConfigurationFactory.class, () -> this.configurationFactory)

                .bind(GuildSettingsManager.class, () -> this.guildSettingsManager)

                /* Schematic */
                .schematicGenerator(angleBrackets())

                /* Build */
                .build();

        /* Services */
        new NewsService(this.jda, this.sqLite, this.guildSettingsManager);

    }

    {
        this.logger.info("Application was loaded successfully.");
    }

}
