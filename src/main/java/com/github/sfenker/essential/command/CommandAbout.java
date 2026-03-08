package com.github.sfenker.essential.command;

import com.google.inject.Inject;
import com.sun.management.OperatingSystemMXBean;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.CommandConfig;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

import static com.github.sfenker.essential.entrypoint.Bootstrap.applicationStartTime;
import static com.github.sfenker.essential.utility.EmbedUtility.embedBuilder;
import static com.github.sfenker.essential.utility.StringUtility.formatDuration;
import static java.awt.Color.RED;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.lang.management.ManagementFactory.getMemoryMXBean;
import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;
import static java.time.Duration.between;
import static java.time.Instant.now;
import static net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL;
import static net.dv8tion.jda.api.interactions.IntegrationType.USER_INSTALL;

@Interaction
public class CommandAbout {

    private final ShardManager shardManager;

    @Inject
    public CommandAbout(
        @NotNull ShardManager shardManager
    ) {
        this.shardManager = shardManager;
    }

    @CommandConfig(integration = { GUILD_INSTALL, USER_INSTALL })
    @Command(value = "about", desc = "Show information about Rust Essential.")
    public void executeDefault(
        @NotNull CommandEvent event
    ) {
        event.with()
            .ephemeral(true)
            .reply(
                embedBuilder()
                    .setColor(RED)
                    .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                    .setDescription(format(
                        """
                        ### :receipt: ‌ General Information
                        **Version:** ``{version}``
                        **JVM Version:** ``%s``
                        ### :robot: ‌ Bot Information
                        **Shard:** ``shard-%d``
                        **Uptime:** ``%s``
                        **Latency:** ``%dms``
                        **Servers:** ``%s servers``
                        ### :desktop: ‌ Hardware Information
                        **CPU Usage:** ``%s%%``
                        **Memory Usage:** ``%d MB``
                        ### :technologist: ‌ Source Code
                        Project source code is available on GitHub.
                        > [github.com/SfenKer/rust-essential](https://github.com/SfenKer/rust-essential)
                        """,

                        /* General Information */
                        getProperty("java.version"),

                        /* Bot Information */
                        event.getJDA().getShardInfo().getShardId(),
                        formatDuration(between(applicationStartTime(), now())),
                        event.getJDA().getGatewayPing(),
                        decimalFormat.format(this.shardManager.getGuilds().size()),

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

