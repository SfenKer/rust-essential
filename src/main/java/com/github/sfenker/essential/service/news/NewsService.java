package com.github.sfenker.essential.service.news;

import com.github.sfenker.essential.scheduler.factory.SchedulerFactory;
import com.github.sfenker.essential.service.news.entity.NewsHistoryEntity;
import com.github.sfenker.essential.service.news.repository.NewsHistoryRepository;
import com.github.sfenker.essential.service.news.scheduler.NewsCheckerTask;
import com.github.sfenker.essential.types.news.News;
import lombok.SneakyThrows;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Node;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.IntStream.range;
import static javax.xml.parsers.DocumentBuilderFactory.newInstance;

public class NewsService {

    final NewsHistoryRepository newsHistoryRepository;

    public NewsService(
        @NotNull SessionFactory sessionFactory,
        @NotNull SchedulerFactory schedulerFactory
    ) {
        this.newsHistoryRepository = new NewsHistoryRepository(sessionFactory);
        schedulerFactory.registerParameter(NewsService.class, this)
            .registerParameter(NewsHistoryRepository.class, this.newsHistoryRepository)
            .registerScheduler(NewsCheckerTask.class);
    }

    public @Nullable Boolean wasPostedBefore(
        @NotNull News news
    ) {
        return this.newsHistoryRepository.queryEntityBy("url", news.url) != null;
    }

    public @NotNull CompletableFuture<Boolean> wasPostedBeforeAsync(
        @NotNull News news
    ) {
        return this.newsHistoryRepository.queryEntityByAsync("url", news.url)
            .thenApply(Objects::nonNull);
    }

    public void markAsPosted(
        @NotNull News news
    ) {
        var entity = new NewsHistoryEntity();
        entity.url = news.url;
        this.newsHistoryRepository.insertEntityAsync(entity);
    }

    @SneakyThrows
    public @NotNull Collection<News> retrieveNews() {

        var builderFactory = newInstance();
        var documentBuilder = builderFactory.newDocumentBuilder();
        var document = documentBuilder.parse(RSS_FEED_URL);

        document.getDocumentElement()
            .normalize();

        var nodes = document.getElementsByTagName("item");

        return range(0, nodes.getLength())
            .mapToObj(nodes::item)
            .map(Node::getChildNodes)
            .map(News::parseFromXml)
            .toList();

    }

    public @NotNull CompletableFuture<Collection<News>> retrieveNewsAsync() {
        return supplyAsync(this::retrieveNews);
    }

    static final String RSS_FEED_URL = "https://rust.facepunch.com/rss/news";

}
