package com.github.sfenker.essential.command;

import com.google.inject.Inject;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.CommandConfig;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import static com.github.sfenker.essential.Constants.gitHash;
import static com.github.sfenker.essential.Constants.projectVersion;
import static com.github.sfenker.essential.utility.DiscordUtility.*;
import static com.github.sfenker.essential.utility.StringUtility.formatDuration;
import static com.github.sfenker.essential.utility.SystemUtility.cpuUsage;
import static com.github.sfenker.essential.utility.SystemUtility.memoryUsage;
import static java.lang.System.getProperty;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.time.Duration.between;
import static java.time.Instant.now;
import static java.time.Instant.ofEpochMilli;
import static net.dv8tion.jda.api.components.buttons.Button.link;
import static net.dv8tion.jda.api.components.thumbnail.Thumbnail.fromFile;
import static net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL;
import static net.dv8tion.jda.api.interactions.IntegrationType.USER_INSTALL;

@Interaction
public class CommandAbout {

    @Inject
    ShardManager shardManager;

    @CommandConfig(integration = { GUILD_INSTALL, USER_INSTALL })
    @Command(value = "about", desc = "Display information about Rust Essential.")
    public void executeDefault(
        @NotNull CommandEvent event
    ) {
        event.with()
            .ephemeral(true)
            .reply(container(
                section(
                    fromFile(logoAsFileUpload()),
                    textDisplay(
                        """
                        ### :tools: Rust Essential
                        Rust Essential is a project that provides many features like News, Raid Cost Calculator and more features that will be great for your Rust Community discord server.
                        ### :receipt: General Information
                        **Version:** ``%s (git/%s)``
                        **JVM Version:** ``%s (%s)``
                        ### :robot: Bot Information
                        **Shard:** ``#%d``
                        **Uptime:** ``%s``
                        **Latency:** ``%.0fms``
                        **Guilds:** ``%d guilds``
                        **Users:** ``%d users``
                        ### :desktop: Hardware Information
                        **CPU Usage:** ``%.2f%%``
                        **Memory Usage:** ``%d MiB``
                        ### :technologist: Source Code and License
                        This project is open source and licensed under [AGPL v3](https://en.wikipedia.org/wiki/GNU_Affero_General_Public_License) license.
                        You can find the source code on GitHub and also join our Discord server to contribute or ask for help.
                        """,
                        projectVersion, gitHash,
                        getProperty("java.version"),
                        getProperty("java.vendor"),
                        event.getJDA().getShardInfo()
                            .getShardId(),
                        formatDuration(between(ofEpochMilli(startTime), now())),
                        this.shardManager.getAverageGatewayPing(),
                        this.shardManager.getGuildCache().size(),
                        this.shardManager.getUserCache().size(),
                        cpuUsage(),
                        memoryUsage()
                    )
                ),
                actionRow(
                    link("https://discord.com/invite/C8dF6zkYff", "Discord Server"),
                    link("https://github.com/SfenKer/rust-essential", "GitHub Repository")
                )
            ));
    }

    static final Long startTime =
        getRuntimeMXBean()
            .getStartTime();

}

