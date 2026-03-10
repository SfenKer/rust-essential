package com.github.sfenker.essential.service.news.repository;

import com.github.sfenker.essential.service.news.entity.NewsHistoryEntity;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@RequiredArgsConstructor
public class NewsHistoryRepository {

    final SessionFactory sessionFactory;

    public void insertEntity(
        @NotNull NewsHistoryEntity entity
    ) {
        try (var session = this.sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.merge(entity);
            transaction.commit();
        }
    }

    public @NotNull CompletableFuture<Void> insertEntityAsync(
        @NotNull NewsHistoryEntity entity
    ) {
        return runAsync(() -> insertEntity(entity));
    }

    public @NotNull Collection<NewsHistoryEntity> queryAllEntities() {
        try (var session = this.sessionFactory.openSession()) {
            return session.createQuery("FROM NewsHistoryEntity", NewsHistoryEntity.class)
                .getResultList();
        }
    }

    public @NotNull CompletableFuture<Collection<NewsHistoryEntity>> queryAllEntitiesAsync() {
        return supplyAsync(this::queryAllEntities);
    }

    public @Nullable NewsHistoryEntity queryEntity(
        @NotNull Long id
    ) {
        try (var session = this.sessionFactory.openSession()) {
            return session.createQuery("FROM NewsHistoryEntity WHERE id = :id", NewsHistoryEntity.class)
                .setParameter("id", id)
                .getSingleResultOrNull();
        }
    }

    public @NotNull CompletableFuture<NewsHistoryEntity> queryEntityAsync(
        @NotNull Long id
    ) {
        return supplyAsync(() -> queryEntity(id));
    }

    public @Nullable NewsHistoryEntity queryEntityBy(
        @NotNull String string,
        @NotNull Object value
    ) {
        try (var session = this.sessionFactory.openSession()) {
            var query = format("FROM NewsHistoryEntity WHERE %s = :value", string);
            return session.createQuery(query, NewsHistoryEntity.class)
                .setParameter("value", value)
                .getSingleResultOrNull();
        }
    }

    public @NotNull CompletableFuture<NewsHistoryEntity> queryEntityByAsync(
        @NotNull String string,
        @NotNull Object value
    ) {
        return supplyAsync(() -> queryEntityBy(string, value));
    }

    public void dropEntity(
        @NotNull NewsHistoryEntity entity
    ) {
        try (var session = this.sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.remove(entity);
            transaction.commit();
        }
    }

    public @NotNull CompletableFuture<Void> dropEntityAsync(
        @NotNull NewsHistoryEntity entity
    ) {
        return runAsync(() -> dropEntity(entity));
    }

}
