package pl.mrstudios.essential.command;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.bind.Bind;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.jda.permission.DiscordPermission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.module.settings.GuildSettingsManager;
import pl.mrstudios.essential.module.settings.setting.GuildSettingEntry;
import pl.mrstudios.essential.utility.builder.EmbedResponseBuilder;

import java.util.Collection;

import static java.awt.Color.RED;
import static java.lang.String.format;
import static net.dv8tion.jda.api.Permission.*;
import static net.dv8tion.jda.internal.utils.PermissionUtil.checkPermission;
import static pl.mrstudios.essential.module.settings.setting.GuildSetting.GUILD_NEWS_CHANNEL;
import static pl.mrstudios.essential.module.settings.setting.GuildSettingEntry.guildSettingEntry;
import static pl.mrstudios.essential.utility.builder.EmbedResponseBuilder.embedResponse;

@Command(name = "configure")
@DiscordPermission(MANAGE_SERVER)
@Description("Configure settings of Rust Essential.")
public class CommandConfigure {

    @Execute(name = "news-channel")
    @Description("Set channel where news will be posted.")
    public @NotNull EmbedResponseBuilder newsChannel(

        @Context Guild guild,
        @Bind GuildSettingsManager guildSettingsManager,

        @Arg("channel")
        @Description("Channel where news will be posted.")
        @NotNull Channel channel

    ) {

        if (!(channel instanceof TextChannel textChannel))
            return embedResponse()
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

        if (!checkPermission(textChannel, guild.getSelfMember(), MESSAGE_SEND, MESSAGE_EMBED_LINKS))
            return embedResponse()
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

        Collection<GuildSettingEntry> settings = guildSettingsManager.fetchSettings(guild);
        GuildSettingEntry guildSettingEntry = settings.stream()
            .filter((entry) -> entry.key() == GUILD_NEWS_CHANNEL)
            .findFirst().orElse(guildSettingEntry(GUILD_NEWS_CHANNEL));

        guildSettingEntry.value(textChannel.getIdLong());
        if (settings.stream().noneMatch((entry) -> entry.key() == GUILD_NEWS_CHANNEL))
            settings.add(guildSettingEntry);

        guildSettingsManager.updateSettings(guild, settings);

        return embedResponse()
            .ephemeral()
            .embed(
                (embedBuilder) -> embedBuilder.setColor(RED)
                    .setThumbnail(guild.getSelfMember().getAvatarUrl())
                    .setDescription(format(
                        """
                        ### :tools: ‌ Configuration
                        Parameter ``guild.news.channel`` has been set to %s channel.
                        """, channel.getAsMention()
                    ))
            );
    }

}
