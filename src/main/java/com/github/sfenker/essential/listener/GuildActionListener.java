package com.github.sfenker.essential.listener;

import com.github.sfenker.essential.settings.GuildSettingsManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
@RequiredArgsConstructor
public class GuildActionListener extends ListenerAdapter {

    final GuildSettingsManager guildSettingsManager;

    @Override
    public void onGuildJoin(
        @NotNull GuildJoinEvent event
    ) {
        log.info("Application has joined to '{}' guild.", event.getGuild().getName());
    }

    @Override
    public void onGuildLeave(
        @NotNull GuildLeaveEvent event
    ) {
        log.info("Application left from '{}' guild.", event.getGuild().getName());
        this.guildSettingsManager.get(event.getGuild().getIdLong())
            .thenApply(this.guildSettingsManager::drop);
    }

}
