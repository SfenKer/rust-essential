package com.github.sfenker.essential.command;

import com.github.sfenker.essential.service.server.ServerInfoService;
import com.google.inject.Inject;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.github.sfenker.essential.utility.DiscordUtility.*;
import static com.github.sfenker.essential.utility.StringUtility.formatDuration;
import static com.google.common.collect.ImmutableSet.of;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.partitioningBy;
import static net.dv8tion.jda.api.components.buttons.Button.link;
import static net.dv8tion.jda.api.components.separator.Separator.Spacing.SMALL;
import static net.dv8tion.jda.api.components.separator.Separator.createInvisible;
import static net.dv8tion.jda.api.components.thumbnail.Thumbnail.fromUrl;
import static net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL;
import static net.dv8tion.jda.api.interactions.IntegrationType.USER_INSTALL;

@Slf4j
@Interaction
public class CommandServerInfo {

    @Inject
    ServerInfoService serverInfoService;

    @CommandConfig(integration = { GUILD_INSTALL, USER_INSTALL })
    @Command(value = "serverinfo", desc = "Display status and information about server.")
    public void executeDefault(

        @NotNull CommandEvent event,

        @Param("Server Address")
        @NotNull String address

    ) {

        event.deferReply(true);

        final var jdaEvent = event.jdaEvent();
        this.serverInfoService.queryServerInfo(address)
            .thenApply(
                (serverInfo) ->
                    container(
                        section(
                            fromUrl(jdaEvent.getJDA().getSelfUser().getAvatarUrl()),
                            textDisplay(
                                """
                                ### :desktop: Server Information
                                **Name:** ``%s``
                                **Description:**
                                ```
                                %s
                                ```
                                ### :bar_chart: Server Statistics
                                **Players:** ``%d/%d``
                                **Uptime:** ``%s``
                                ### :map: Server Map
                                **Map:** ``%s``
                                """,
                                serverInfo.name, serverInfo.description,
                                serverInfo.players, serverInfo.maxPlayers,
                                formatDuration(serverInfo.uptime),
                                serverInfo.mapName
                            )
                        ),
                        (serverInfo.mapThumbnail != null) ?
                            mediaGallery(mediaGalleryItem(serverInfo.mapThumbnail)) : createInvisible(SMALL),
                        actionRow(
                            link(format("https://www.battlemetrics.com/servers/rust/%d", serverInfo.id), "BattleMetrics")
                        )
                    )
            )
            .thenAccept(
                (container) ->
                jdaEvent.getHook()
                    .editOriginalComponents(container)
                    .useComponentsV2()
                    .queue()
            )
            .exceptionally((_) -> {
                jdaEvent.getHook()
                    .editOriginalComponents(container(
                        textDisplay(
                            """
                            ### :warning: Error Occurred
                            Unable to retrieve server information for the provided address.
                            Please ensure the address is correct and try again.
                            """
                        )
                    ))
                    .useComponentsV2()
                    .queue();
                return null;
            });

    }

    @AutoComplete("serverinfo")
    public void autoComplete(
        @NotNull AutoCompleteEvent event
    ) {

        if (!event.getName().equals("address"))
            return;

        var original = event.getValue()
            .toLowerCase();

        if (original.isBlank()) {
            event.replyChoiceStrings(emptyList());
            return;
        }

        var colonIndex = original.lastIndexOf(':');
        if (colonIndex != -1) {

            var domainPart = original.substring(0, colonIndex);
            var portPart = original.substring(colonIndex + 1);

            var suggestions = commonPorts.stream()
                .map(String::valueOf)
                .filter(
                    (port) ->
                        port.startsWith(portPart)
                )
                .map(
                    (port) ->
                        domainPart + ":" + port
                )
                .limit(25)
                .toList();

            if (suggestions.isEmpty() && !portPart.isEmpty())
                suggestions = emptyList();

            event.replyChoiceStrings(suggestions);
            return;

        }

        var beforeLastDot = (original.lastIndexOf('.') == -1) ?
            original : original.substring(0, original.lastIndexOf('.'));

        var afterLastDot = (original.lastIndexOf('.') == -1) ?
            original : original.substring(original.lastIndexOf('.'));

        event.replyChoiceStrings(
            domainTldCollection.stream()
                .collect(collectingAndThen(
                    partitioningBy(
                        (tld) ->
                            tld.startsWith(afterLastDot)
                    ),
                    (partitions) ->
                        (partitions.get(true).isEmpty()) ?
                            partitions.get(false).stream().map(tld -> original + tld) :
                            partitions.get(true).stream().map(tld -> beforeLastDot + tld)
                ))
                .limit(25)
                .toList()
        );

    }

    static final @NotNull Set<String> domainTldCollection =
        of(
            ".com",
            ".co",
            ".net",
            ".org",
            ".gg",
            ".uk",
            ".eu",
            ".de"
        );

    static final @NotNull Set<Integer> commonPorts =
        of(
            28015,
            28016,
            28017,
            28018,
            28019,
            28020,
            28082
        );

}
