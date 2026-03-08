package com.github.sfenker.essential.service.news.scheduler;

import com.github.sfenker.essential.scheduler.annotation.function.Entrypoint;
import com.github.sfenker.essential.scheduler.annotation.function.Inject;
import com.github.sfenker.essential.scheduler.annotation.function.ParameterSupplier;
import com.github.sfenker.essential.scheduler.annotation.type.Scheduler;
import com.github.sfenker.essential.service.news.NewsService;
import com.github.sfenker.essential.settings.GuildSettingsManager;
import com.github.sfenker.essential.types.news.News;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.github.sfenker.essential.builder.ComponentContainerBuilder.componentContainerBuilder;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MINUTES;
import static net.dv8tion.jda.api.components.buttons.Button.link;
import static net.dv8tion.jda.api.components.thumbnail.Thumbnail.fromUrl;

@Scheduler(period = 5, unit = MINUTES)
public class NewsCheckerTask {

    @Inject
    ShardManager shardManager;

    @Inject
    NewsService newsService;

    @Inject
    GuildSettingsManager guildSettingsManager;

    @Entrypoint
    void entrypoint(
        @NotNull News news,
        @NotNull Guild guild
    ) {

        var settings = this.guildSettingsManager.get(guild.getIdLong())
            .join();

        assert settings.newsChannelId != null;
        var channel = guild.getTextChannelById(settings.newsChannelId);
        if (channel == null)
            return;

        channel.sendMessageComponents(
                componentContainerBuilder()
                    .section(
                        fromUrl(guild.getJDA().getSelfUser().getAvatarUrl()),
                        format("### %s", news.title),
                        news.description
                    )
                    .gallery(news.thumbnail)
                    .actionRow(
                        link(news.url, "Read More")
                    )
                    .build()
            )
            .useComponentsV2()
            .queue();

    }

    @ParameterSupplier(type = News.class)
    @NotNull Stream<News> retrieveNotPostedNews() {
        return this.newsService.retrieveNews()
            .stream()
            .filter(
                (news) ->
                    !ofNullable(this.newsService.wasPostedBefore(news))
                        .orElse(false)
            )
            .peek(this.newsService::markAsPosted);
    }

    @ParameterSupplier(type = Guild.class)
    @NotNull Stream<CompletableFuture<Guild>> retrieveGuilds() {
        return this.shardManager.getGuilds()
            .stream()
            .map(
                (guild) ->
                    this.guildSettingsManager.get(guild.getIdLong())
                        .thenApply(
                            (settings) ->
                                (settings != null && settings.newsChannelId != null) ?
                                    guild : null
                        )
            );
    }

}
