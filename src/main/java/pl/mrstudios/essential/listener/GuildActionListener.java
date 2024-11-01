package pl.mrstudios.essential.listener;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static org.slf4j.LoggerFactory.getLogger;

public class GuildActionListener extends ListenerAdapter {

    @Override
    public void onGuildJoin(
        @NotNull GuildJoinEvent event
    ) {
        getLogger(GuildActionListener.class).info("Application has joined to '{}' guild.", event.getGuild().getName());
    }

    @Override
    public void onGuildLeave(
        @NotNull GuildLeaveEvent event
    ) {
        getLogger(GuildActionListener.class).info("Application left from '{}' guild.", event.getGuild().getName());
    }

}
