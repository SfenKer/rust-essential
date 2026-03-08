package com.github.sfenker.essential.settings;

import com.github.sfenker.essential.settings.entity.GuildSettingsEntity;
import com.github.sfenker.essential.settings.repository.GuildSettingsRepository;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static java.util.Optional.ofNullable;

public class GuildSettingsManager {

    final GuildSettingsRepository repository;

    public GuildSettingsManager(
        @NotNull SessionFactory sessionFactory
    ) {
        this.repository = new GuildSettingsRepository(sessionFactory);
    }

    public @NotNull GuildSettingsEntity create() {
        var entity = new GuildSettingsEntity();
        this.repository.insertEntityAsync(entity);
        return entity;
    }

    public @NotNull CompletableFuture<GuildSettingsEntity> get(
        @NotNull Long guildId
    ) {
        return this.repository.queryEntityAsync(guildId);
    }

    public @NotNull CompletableFuture<GuildSettingsEntity> getOrCreate(
        @NotNull Long guildId
    ) {
        return get(guildId)
            .thenApply(
                (settings) ->
                    ofNullable(settings)
                        .orElseGet(this::create)
            );
    }

    public @NotNull CompletableFuture<Void> drop(
        @NotNull GuildSettingsEntity entity
    ) {
        return this.repository.dropEntityAsync(entity);
    }

}
