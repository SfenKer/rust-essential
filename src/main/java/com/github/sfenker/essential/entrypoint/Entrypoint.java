package com.github.sfenker.essential.entrypoint;

import com.github.sfenker.essential.command.internal.ErrorMessageFactoryImpl;
import com.github.sfenker.essential.listener.GuildActionListener;
import com.github.sfenker.essential.service.news.entity.NewsHistoryEntity;
import com.github.sfenker.essential.settings.GuildSettingsManager;
import com.github.sfenker.essential.settings.entity.GuildSettingsEntity;
import com.google.inject.Injector;
import io.github.kaktushose.jdac.guice.GuiceExtensionData;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import static com.google.inject.Guice.createInjector;
import static io.github.kaktushose.jdac.JDACommands.builder;
import static io.github.kaktushose.jdac.definitions.description.ClassFinder.reflective;
import static io.github.kaktushose.jdac.definitions.interactions.InteractionDefinition.ReplyConfig.of;
import static io.github.kaktushose.jdac.dispatching.expiration.ExpirationStrategy.AFTER_15_MINUTES;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.getenv;
import static java.lang.Thread.ofPlatform;
import static java.lang.Thread.setDefaultUncaughtExceptionHandler;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static net.dv8tion.jda.api.entities.Activity.playing;
import static net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder.createDefault;
import static net.dv8tion.jda.api.utils.Compression.NONE;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.*;

@Slf4j
public class Entrypoint {

    {
        log.info("Loading application, please wait..");
    }

    {
        setDefaultUncaughtExceptionHandler(
            (thread, throwable) -> {
                log.error("An unexpected exception was thrown in thread {}.", thread.getName());
                log.error("Stacktrace:", throwable);
            }
        );
    }

    {
        getRuntime()
            .addShutdownHook(
                ofPlatform()
                    .name("shutdown-hook")
                    .unstarted(
                        () ->
                            log.info("Application is shutting down, please wait..")
                    )
            );
    }

    {
        get("database")
            .toFile()
            .mkdirs();
    }

    final SessionFactory sessionFactory = new Configuration()
        .configure("hibernate.xml")
        .addAnnotatedClasses(
            NewsHistoryEntity.class,
            GuildSettingsEntity.class
        )
        .buildSessionFactory();

    final GuildSettingsManager guildSettingsManager =
        new GuildSettingsManager(this.sessionFactory);

    final ShardManager shardManager;

    {
        this.shardManager = createDefault(getenv("DISCORD_TOKEN"))
            .setShardsTotal(4)
            .setCompression(NONE)
            .setActivity(playing("Rust"))
            .addEventListeners(
                new GuildActionListener(this.guildSettingsManager)
            )
            .disableCache(asList(
                ACTIVITY, CLIENT_STATUS, FORUM_TAGS, ONLINE_STATUS,
                SCHEDULED_EVENTS, STICKER
            ))
            .build();
    }

    final Injector injector =
        createInjector(
            (binder) -> {

                binder.bind(SessionFactory.class)
                    .toInstance(this.sessionFactory);

                binder.bind(ShardManager.class)
                    .toInstance(this.shardManager);

                binder.bind(GuildSettingsManager.class)
                    .toInstance(this.guildSettingsManager);

            }
        );

    {
        builder(this.shardManager)
            .classFinders(
                reflective("com.github.sfenker.essential.command")
            )
            .extensionData(new GuiceExtensionData(this.injector))
            .expirationStrategy(AFTER_15_MINUTES)
            .errorMessageFactory(new ErrorMessageFactoryImpl())
            .globalReplyConfig(of(
                (config) ->
                    config.ephemeral(true)
            ))
            .start();
    }

    {
        log.info("Application was loaded successfully.");
    }

    static void main() {
        new Entrypoint();
    }

}
