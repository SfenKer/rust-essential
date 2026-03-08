package com.github.sfenker.essential.settings.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "guilds")
public class GuildSettingsEntity {

    @Id
    @Column(name = "id")
    public @NotNull Long id;

    @Column(name = "newsChannelId")
    public @Nullable Long newsChannelId;

}
