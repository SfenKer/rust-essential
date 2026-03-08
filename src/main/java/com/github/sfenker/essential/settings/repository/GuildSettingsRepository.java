package com.github.sfenker.essential.settings.repository;

import com.github.sfenker.essential.settings.entity.GuildSettingsEntity;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@RequiredArgsConstructor
public class GuildSettingsRepository {

    final SessionFactory sessionFactory;

    public void insertEntity(
        @NotNull GuildSettingsEntity entity
    ) {
        try (var session = this.sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.merge(entity);
            transaction.commit();
        }
    }

    public @NotNull CompletableFuture<Void> insertEntityAsync(
        @NotNull GuildSettingsEntity entity
    ) {
        return runAsync(() -> insertEntity(entity));
    }

    public @NotNull Collection<GuildSettingsEntity> queryAllEntities() {
        try (var session = this.sessionFactory.openSession()) {
            return session.createQuery("FROM GuildSettingsEntity", GuildSettingsEntity.class)
                .getResultList();
        }
    }

    public @NotNull CompletableFuture<Collection<GuildSettingsEntity>> queryAllEntitiesAsync() {
        return supplyAsync(this::queryAllEntities);
    }

    public @Nullable GuildSettingsEntity queryEntity(
        @NotNull Long guildId
    ) {
        try (var session = this.sessionFactory.openSession()) {
            return session.createQuery("FROM GuildSettingsEntity WHERE id = :id", GuildSettingsEntity.class)
                .setParameter("id", guildId)
                .getSingleResultOrNull();
        }
    }

    public @NotNull CompletableFuture<GuildSettingsEntity> queryEntityAsync(
        @NotNull Long guildId
    ) {
        return supplyAsync(() -> queryEntity(guildId));
    }

    public void dropEntity(
        @NotNull GuildSettingsEntity entity
    ) {
        try (var session = this.sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.remove(entity);
            transaction.commit();
        }
    }

    public @NotNull CompletableFuture<Void> dropEntityAsync(
        @NotNull GuildSettingsEntity entity
    ) {
        return runAsync(() -> dropEntity(entity));
    }

}
