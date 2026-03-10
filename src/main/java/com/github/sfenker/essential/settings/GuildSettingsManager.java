package com.github.sfenker.essential.settings;

import com.github.sfenker.essential.settings.entity.GuildSettingsEntity;
import com.github.sfenker.essential.settings.repository.GuildSettingsRepository;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static com.github.sfenker.essential.settings.entity.GuildSettingsEntity.guildSettings;

public class GuildSettingsManager {

    final GuildSettingsRepository repository;

    public GuildSettingsManager(
        @NotNull SessionFactory sessionFactory
    ) {
        this.repository = new GuildSettingsRepository(sessionFactory);
    }

    public @NotNull CompletableFuture<Void> create(
        @NotNull Long guildId
    ) {
        return this.repository.insertEntityAsync(guildSettings(guildId));
    }

    public @NotNull CompletableFuture<GuildSettingsEntity> get(
        @NotNull Long guildId
    ) {
        return this.repository.queryEntityAsync(guildId);
    }

    public @NotNull CompletableFuture<GuildSettingsEntity> getOrCreate(
        @NotNull Long guildId
    ) {
        return create(guildId)
            .thenCompose((_) -> get(guildId))
            .exceptionallyCompose((_) -> get(guildId));
    }

    public @NotNull CompletableFuture<Void> save(
        @NotNull GuildSettingsEntity entity
    ) {
        return this.repository.updateEntityAsync(entity);
    }

    public @NotNull CompletableFuture<Void> drop(
        @NotNull GuildSettingsEntity entity
    ) {
        return this.repository.dropEntityAsync(entity);
    }

}
