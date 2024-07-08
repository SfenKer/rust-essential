package pl.mrstudios.essential;

import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import pl.mrstudios.commons.inject.Injector;
import pl.mrstudios.essential.command.CommandAbout;
import pl.mrstudios.essential.config.Configuration;
import pl.mrstudios.essential.config.ConfigurationFactory;
import pl.mrstudios.essential.database.SQLite;
import pl.mrstudios.essential.feature.calculator.command.CommandCalculator;
import pl.mrstudios.essential.listener.UserInteractionListener;

import static dev.rollczi.litecommands.annotations.LiteCommandsAnnotations.ofClasses;
import static dev.rollczi.litecommands.jda.LiteJDAFactory.builder;
import static dev.rollczi.litecommands.schematic.SchematicFormat.angleBrackets;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static net.dv8tion.jda.api.JDABuilder.createDefault;
import static net.dv8tion.jda.api.entities.Activity.playing;
import static net.dv8tion.jda.api.utils.Compression.ZLIB;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.*;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.mrstudios.essential.config.ConfigurationFactory.configurationFactory;

@SuppressWarnings("FieldCanBeLocal")
public class Application {

    private final JDA jda;
    private final SQLite sqLite;
    private final Injector injector;

    /* Logger */
    private final Logger logger = getLogger(Application.class);

    /* Configuration */
    private final Configuration configuration;
    private final ConfigurationFactory configurationFactory;

    {
        this.logger.info("Loading application, please wait...");
    }

    {

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

        /* Injector */
        this.injector = new Injector()

                .register(JDA.class, this.jda)
                .register(Logger.class, this.logger)

                /* SQLite */
                .register(SQLite.class, this.sqLite)

                /* Configuration */
                .register(Configuration.class, this.configuration)
                .register(ConfigurationFactory.class, this.configurationFactory);

        /* Commands */
        builder(this.jda)

                /* Commands */
                .commands(ofClasses(
                        CommandCalculator.class,
                        CommandAbout.class
                ))

                /* Bind */
                .bind(Configuration.class, () -> this.configuration)
                .bind(ConfigurationFactory.class, () -> this.configurationFactory)

                /* Schematic */
                .schematicGenerator(angleBrackets())

                /* Build */
                .build();

    }

    {
        this.logger.info("Application was loaded successfully.");
    }

}
