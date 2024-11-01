package pl.mrstudios.essential.command;

import com.sun.management.OperatingSystemMXBean;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.utility.builder.EmbedResponseBuilder;

import java.text.DecimalFormat;
import java.time.Instant;

import static java.awt.Color.RED;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.lang.management.ManagementFactory.getMemoryMXBean;
import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;
import static java.time.Duration.between;
import static java.time.Instant.now;
import static pl.mrstudios.essential.utility.StringUtility.formatDuration;
import static pl.mrstudios.essential.utility.builder.EmbedResponseBuilder.embedResponse;

@Command(name = "about")
@Description("Show information about Rust Essential.")
public class CommandAbout {

    private final Instant applicationStartTime;

    public CommandAbout() {
        this.applicationStartTime = now();
    }

    @Execute
    public @NotNull EmbedResponseBuilder executeDefault(
        @Context SlashCommandInteractionEvent event
    ) {
        return embedResponse()
            .ephemeral()
            .embed(
                (embedBuilder) -> embedBuilder.setColor(RED)
                    .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                    .setDescription(format(
                        """
                        ### :receipt: ‌ General Information
                        **Version:** ``{version}``
                        **JVM Version:** ``%s``

                        ### :robot: ‌ Bot Information
                        **Uptime:** ``%s``
                        **Latency:** ``%dms``
                        **Servers:** ``%s servers``

                        ### :desktop: ‌ Hardware Information
                        **CPU Usage:** ``%s%%``
                        **Memory Usage:** ``%d MB``

                        ### :busts_in_silhouette: ‌ Support
                        This bot is developed by MrStudios Industries, if you need help join our Official Discord server.
                        > https://discord.com/invite/C8dF6zkYff
                        """,

                        /* General Information */
                        getProperty("java.version"),

                        /* Bot Information */
                        formatDuration(between(this.applicationStartTime, now())),
                        event.getJDA().getGatewayPing(),
                        decimalFormat.format(event.getJDA().getGuilds().size()),

                        /* Hardware Information */
                        currentCpuUsage(), currentMemoryUsage()

                    ))
            );
    }

    private static @NotNull String currentCpuUsage() {
        return format("%.2f", ((OperatingSystemMXBean) getOperatingSystemMXBean()).getCpuLoad())
            .replace('.', ',');
    }

    private static @NotNull Integer currentMemoryUsage() {
        return (int) (getMemoryMXBean().getNonHeapMemoryUsage().getUsed()) / 1_048_576;
    }

    private final DecimalFormat decimalFormat = new DecimalFormat("#,###");

}

