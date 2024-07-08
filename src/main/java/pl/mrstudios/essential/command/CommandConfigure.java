package pl.mrstudios.essential.command;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.bind.Bind;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.jda.permission.DiscordPermission;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.module.settings.GuildSettingsManager;

import static java.awt.Color.RED;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static net.dv8tion.jda.api.Permission.MANAGE_SERVER;
import static pl.mrstudios.essential.utility.EmbedResponseUtility.embedResponse;

@Command(name = "configure")
@Description("Configure bot for your server.")
@DiscordPermission(MANAGE_SERVER)
public class CommandConfigure {

    @Execute(name = "news-channel")
    @Description("Set channel where news will be posted.")
    public void newsChannel(

            @Context SlashCommandInteractionEvent event,
            @Bind GuildSettingsManager guildSettingsManager,

            @Arg("channel")
            @Description("Channel where news will be posted.")
            @NotNull Channel channel

    ) {

        guildSettingsManager.guildSettings(requireNonNull(event.getGuild()))
                .newsChannelId = channel.getIdLong();

        embedResponse(event)
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
                ).build();
    }

}
