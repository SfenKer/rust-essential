package com.github.sfenker.essential.command;

import com.github.sfenker.essential.settings.GuildSettingsManager;
import com.google.inject.Inject;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.CommandConfig;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;

import static com.github.sfenker.essential.command.internal.ErrorMessageFactoryImpl.exceptionMessageTemplate;
import static com.github.sfenker.essential.utility.DiscordUtility.container;
import static com.github.sfenker.essential.utility.DiscordUtility.textDisplay;
import static net.dv8tion.jda.api.Permission.MANAGE_SERVER;
import static net.dv8tion.jda.api.Permission.MESSAGE_SEND;
import static net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL;
import static net.dv8tion.jda.internal.utils.PermissionUtil.checkPermission;

@Interaction
public class CommandConfiguration {

    @Inject
    GuildSettingsManager guildSettingsManager;

    @CommandConfig(enabledFor = MANAGE_SERVER, integration = GUILD_INSTALL)
    @Command(value = "config news-channel", desc = "Set channel where Rust news will be posted.")
    public void configNewsChannel(

        @NotNull CommandEvent event,

        @Param("Which channel should be used as news channel?")
        @NotNull TextChannel channel

    ) {

        assert event.getGuild() != null;
        if (!checkPermission(channel, event.getGuild().getSelfMember(), MESSAGE_SEND)) {
            event.with()
                .ephemeral(true)
                .reply(container(
                    textDisplay(
                        """
                        ### :warning: Error Occurred
                        I don't have permission to send messages in %s channel.
                        Please make sure I have the required permissions and try again.
                        """, channel.getAsMention()
                    )
                ));
            return;
        }

        event.deferReply(true);

        final var jdaEvent = event.jdaEvent();
        this.guildSettingsManager.getOrCreate(event.getGuild().getIdLong())
            .thenCompose(
                (settings) -> {
                    settings.newsChannelId = channel.getIdLong();
                    return this.guildSettingsManager.save(settings);
                }
            )
            .thenRun(
                () ->
                    jdaEvent.getHook()
                        .editOriginalComponents(container(
                            textDisplay(
                                """
                                ### :tools: Configuration
                                Channel %s was set as news channel.
                                """, channel.getAsMention()
                            )
                        ))
                        .useComponentsV2()
                        .queue()
            )
            .exceptionally(
                (throwable) -> {
                    jdaEvent.getHook()
                        .editOriginalComponents(exceptionMessageTemplate(throwable))
                        .useComponentsV2()
                        .queue();
                    return null;
                }
            );

    }

}
