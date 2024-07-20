package pl.mrstudios.essential.command;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.bind.Bind;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.jda.permission.DiscordPermission;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.module.settings.GuildSettings;
import pl.mrstudios.essential.module.settings.GuildSettingsManager;
import pl.mrstudios.essential.module.settings.document.JsonDocument;
import pl.mrstudios.essential.utility.EmbedResponseUtility;

import static java.awt.Color.RED;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static net.dv8tion.jda.api.Permission.*;
import static net.dv8tion.jda.internal.utils.PermissionUtil.checkPermission;
import static pl.mrstudios.essential.utility.EmbedResponseUtility.embedResponse;

@Command(name = "configure")
@Description("Configure settings of Rust Essential.")
@DiscordPermission(MANAGE_SERVER)
public class CommandConfigure {

    @Execute(name = "news-channel")
    @Description("Set channel where news will be posted.")
    public @NotNull EmbedResponseUtility newsChannel(

            @Context SlashCommandInteractionEvent event,
            @Bind GuildSettingsManager guildSettingsManager,

            @Arg("channel")
            @Description("Channel where news will be posted.")
            @NotNull Channel channel

    ) {

        if (!(channel instanceof TextChannel textChannel))
            return embedResponse(event)
                    .ephemeral()
                    .embed(
                            (embedBuilder) -> embedBuilder.setColor(RED)
                                    .setDescription(
                                            """
                                            ### :warning: ‌ Error Occurred
                                            You can only choose text channels as news channel.
                                            """
                                    )
                    );

        if (!checkPermission(textChannel, requireNonNull(event.getGuild()).getSelfMember(), MESSAGE_SEND, MESSAGE_EMBED_LINKS))
            return embedResponse(event)
                    .ephemeral()
                    .embed(
                            (embedBuilder) -> embedBuilder.setColor(RED)
                                    .setDescription(
                                            """
                                            ### :warning: ‌ Error Occurred
                                            Application doesn't have permissions to send messages in that channel.
                                            """
                                    )
                    );

        JsonDocument<GuildSettings> settings = guildSettingsManager.guildSettings(requireNonNull(event.getGuild()));

        settings.read().newsChannelId = textChannel.getIdLong();
        settings.save();

        return embedResponse(event)
                .ephemeral()
                .embed(
                        (embedBuilder) -> embedBuilder.setColor(RED)
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
