package pl.mrstudios.essential.command;

import com.sun.management.OperatingSystemMXBean;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.jda.permission.DiscordPermission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

import static java.awt.Color.RED;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.lang.System.getProperty;
import static java.lang.management.ManagementFactory.getMemoryMXBean;
import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;
import static java.time.Duration.between;
import static java.time.Instant.now;
import static net.dv8tion.jda.api.Permission.USE_APPLICATION_COMMANDS;
import static pl.mrstudios.essential.utility.EmbedResponseUtility.embedResponse;
import static pl.mrstudios.essential.utility.StringUtility.formatDuration;

@Command(name = "about")
@Description("Show information about bot.")
@DiscordPermission(USE_APPLICATION_COMMANDS)
public class CommandAbout {

    private final Instant applicationStartTime;

    public CommandAbout() {
        this.applicationStartTime = now();
    }

    @Execute
    public void execute(
            @Context User user,
            @Context SlashCommandInteractionEvent event
    ) {
        embedResponse(event)
                .ephemeral()
                .embed(
                        (embedBuilder) -> embedBuilder.setColor(RED)
                                .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                                .setDescription(format(
                                        """
                                        ### :receipt: General Information
                                        **Version:** ``{version}``
                                        **JVM Version:** ``%s``

                                        ### :robot: Bot Information
                                        **Uptime:** ``%s``
                                        **Latency:** ``%dms``
                                        **Servers:** ``%d servers``

                                        ### :desktop: Hardware Information
                                        **CPU Usage:** ``%s%%``
                                        **Memory Usage:** ``%d MB``
                                        
                                        ### :busts_in_silhouette: Support
                                        This bot is developed by MrStudios Industries, if you need help join our Official Discord server.
                                        > https://discord.com/invite/C8dF6zkYff
                                        """,

                                        /* General Information */
                                        getProperty("java.version"),

                                        /* Bot Information */
                                        formatDuration(between(this.applicationStartTime, now())),
                                        event.getJDA().getGatewayPing(),
                                        event.getJDA().getGuilds().size(),

                                        /* Hardware Information */
                                        currentCpuUsage(), currentMemoryUsage()

                                ))
                ).build();
    }

    protected static @NotNull String currentCpuUsage() {
        return format("%.2f", ((OperatingSystemMXBean) getOperatingSystemMXBean()).getProcessCpuLoad())
                .replace('.', ',');
    }

    protected static @NotNull Integer currentMemoryUsage() {
        return (int) (getMemoryMXBean().getHeapMemoryUsage().getUsed()) / 1_048_576;
    }

}
