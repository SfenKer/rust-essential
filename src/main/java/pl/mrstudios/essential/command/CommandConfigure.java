package pl.mrstudios.essential.command;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.CommandConfig;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.google.inject.Inject;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.service.settings.GuildSettingsService;
import pl.mrstudios.essential.service.settings.setting.GuildSettingContainer;

import java.util.Collection;

import static java.awt.Color.RED;
import static java.lang.String.format;
import static net.dv8tion.jda.api.Permission.*;
import static net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL;
import static net.dv8tion.jda.internal.utils.PermissionUtil.checkPermission;
import static pl.mrstudios.essential.service.settings.setting.GuildSetting.GUILD_NEWS_CHANNEL;
import static pl.mrstudios.essential.utility.EmbedUtility.embedBuilder;

@Interaction
public class CommandConfigure {

    private final GuildSettingsService guildSettingsService;

    @Inject
    public CommandConfigure(
        @NotNull GuildSettingsService guildSettingsService
    ) {
        this.guildSettingsService = guildSettingsService;
    }

    @CommandConfig(enabledFor = MANAGE_SERVER, integration = GUILD_INSTALL)
    @Command(value = "configure news-channel", desc = "Configure channel where Rust news will be sent.")
    public void newsChannel(

        @NotNull CommandEvent event,

        @Param("Channel where news will be posted.")
        @NotNull TextChannel channel

    ) {

        assert event.getGuild() != null;
        if (!checkPermission(channel, event.getGuild().getSelfMember(), MESSAGE_SEND, MESSAGE_EMBED_LINKS)) {
            event.with()
                .ephemeral(true)
                .reply(
                    embedBuilder()
                        .setColor(RED)
                        .setDescription(
                            """
                            ### :warning: ‌ Error Occurred
                            Application doesn't have permissions to send messages in that channel.
                            """
                        )
                );
            return;
        }

        Collection<GuildSettingContainer> settings = this.guildSettingsService.fetchSettings(event.getGuild());
        GuildSettingContainer container = settings.stream()
            .filter((entry) -> entry.key() == GUILD_NEWS_CHANNEL)
            .findFirst().orElse(GuildSettingContainer.guildSettingContainer(GUILD_NEWS_CHANNEL));

        container.value(channel.getIdLong());
        if (settings.stream().noneMatch((entry) -> entry.key() == GUILD_NEWS_CHANNEL))
            settings.add(container);

        this.guildSettingsService.updateSettings(event.getGuild(), settings);

        event.with()
            .ephemeral(true)
            .reply(
                embedBuilder()
                    .setColor(RED)
                    .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                    .setDescription(format(
                        """
                        ### :tools: ‌ Configuration
                        Parameter ``guild.news.channel`` has been set to %s channel.
                        """, channel.getAsMention()
                    ))
            );

    }

}
