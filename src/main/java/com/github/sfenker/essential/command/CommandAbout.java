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
import static com.github.sfenker.essential.builder.ComponentContainerBuilder.componentContainerBuilder;
import static com.github.sfenker.essential.utility.StringUtility.formatDuration;
import static com.github.sfenker.essential.utility.SystemUtility.cpuUsage;
import static com.github.sfenker.essential.utility.SystemUtility.memoryUsage;
import static java.lang.System.getProperty;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.time.Duration.between;
import static java.time.Instant.now;
import static java.time.Instant.ofEpochMilli;
import static java.util.Arrays.asList;
import static net.dv8tion.jda.api.components.buttons.Button.link;
import static net.dv8tion.jda.api.components.thumbnail.Thumbnail.fromUrl;
import static net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL;
import static net.dv8tion.jda.api.interactions.IntegrationType.USER_INSTALL;
import static org.apache.commons.lang3.StringUtils.join;

@Interaction
public class CommandAbout {

    @Inject
    ShardManager shardManager;

    @CommandConfig(integration = { GUILD_INSTALL, USER_INSTALL })
    @Command(value = "about", desc = "Display information about Rust Essential.")
    public void executeDefault(
        @NotNull CommandEvent event
    ) {
        event.jdaEvent()
            .replyComponents(
                componentContainerBuilder()
                    .section(
                        fromUrl(event.getJDA().getSelfUser().getAvatarUrl()),
                        "### :tools: Rust Essential",
                        "Rust Essential is a project that provides many features like News, Raid Cost Calculator and more features that will be great for your Rust Community discord server."
                    )
                    .textDisplay("### :receipt: General Information")
                    .textDisplay(
                        join(asList(
                            "**Version:** ``%s (git/%s)``",
                            "**JVM Version:** ``%s (%s)``"
                        ), "\n"),
                        projectVersion, gitHash,
                        getProperty("java.version"),
                        getProperty("java.vendor")
                    )
                    .textDisplay("### :robot: Bot Information")
                    .textDisplay(
                        join(asList(
                            "**Shard:** ``%d``",
                            "**Uptime:** ``%s``",
                            "**Latency:** ``%.0fms``",
                            "**Guilds:** ``%d guilds``",
                            "**Users:** ``%d users``"
                        ), "\n"),
                        event.getJDA().getShardInfo()
                            .getShardId(),
                        formatDuration(between(ofEpochMilli(startTime), now())),
                        this.shardManager.getAverageGatewayPing(),
                        this.shardManager.getGuildCache().size(),
                        this.shardManager.getUserCache().size()
                    )
                    .textDisplay("### :desktop: Hardware Information")
                    .textDisplay(
                        join(asList(
                            "**CPU Usage:** ``%.2f%%``",
                            "**Memory Usage:** ``%d MiB``"
                        ), "\n"),
                        cpuUsage(),
                        memoryUsage()
                    )
                    .textDisplay("### :technologist: Source Code and License")
                    .textDisplay(join(
                        new String[] {
                            "This project is open source and licensed under [AGPL v3](https://en.wikipedia.org/wiki/GNU_Affero_General_Public_License) license.",
                            "You can find the source code on GitHub and also join our Discord server to contribute or ask for help."
                        }, "\n"
                    ))
                    .actionRow(
                        link("https://github.com/SfenKer/rust-essential", "GitHub Repository"),
                        link("https://discord.com/invite/C8dF6zkYff", "Discord Server")
                    )
                    .build()
            )
            .useComponentsV2()
            .setEphemeral(true)
            .queue();
    }

    static final Long startTime =
        getRuntimeMXBean()
            .getStartTime();

}

