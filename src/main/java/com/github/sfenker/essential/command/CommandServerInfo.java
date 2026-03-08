package com.github.sfenker.essential.command;

import com.ibasco.agql.protocols.valve.source.query.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.SourceQueryOptions;
import io.github.kaktushose.jdac.annotations.constraints.Max;
import io.github.kaktushose.jdac.annotations.constraints.Min;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static com.github.sfenker.essential.builder.ComponentContainerBuilder.componentContainerBuilder;
import static com.github.sfenker.essential.utility.StringUtility.formatDuration;
import static com.github.sfenker.essential.wrapper.RustMapsWrapper.mapImageAsync;
import static com.google.common.collect.ImmutableSet.of;
import static com.ibasco.agql.core.util.GeneralOptions.*;
import static com.ibasco.agql.protocols.valve.source.query.SourceQueryOptions.builder;
import static io.netty.util.ResourceLeakDetector.Level.DISABLED;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.time.Duration.ofSeconds;
import static java.util.Map.entry;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.rangeClosed;
import static net.dv8tion.jda.api.components.thumbnail.Thumbnail.fromUrl;
import static net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL;
import static net.dv8tion.jda.api.interactions.IntegrationType.USER_INSTALL;
import static org.apache.commons.lang3.function.Failable.run;

@Slf4j
@Interaction
public class CommandServerInfo {

    @CommandConfig(integration = { GUILD_INSTALL, USER_INSTALL })
    @Command(value = "serverinfo", desc = "Display status and information about server.")
    public void executeDefault(

        @NotNull CommandEvent event,

        @Param("Server Address")
        @NotNull String host,

        @Param("Server Port")
        @Min(1) @Max(65535)
        @NotNull Integer port

    ) {

        event.deferReply(true);

        log.info(
            "User '{}' requested server information for {}:{}",
            event.getUser().getName(),
            host, port
        );

        var address = new InetSocketAddress(host, port);
        var client = new SourceQueryClient(sourceQueryOptions);

        final var jdaEvent = event.jdaEvent();
        client.getInfo(address)
            .thenCombine(
                client.getRules(address),
                (info, rules) ->
                    entry(info.getResult(), rules.getResult())
            )
            .thenCompose((entry) -> {

                var serverInfoResult = entry.getKey();
                var serverRulesResult = entry.getValue();

                var logo = ofNullable(serverRulesResult.get("logoimage"))
                    .orElse(jdaEvent.getJDA().getSelfUser().getAvatarUrl());

                var builder = componentContainerBuilder()
                    .section(
                        fromUrl(logo),
                        "### :desktop: Server Information",
                        format(
                            """
                            **Name:** ``%s``
                            **Description:**
                            ```
                            %s
                            ```
                            """,
                            serverInfoResult.getName(),
                            readServerDescription(serverRulesResult)
                        )
                    )
                    .textDisplay("### :bar_chart: Server Statistics")
                    .textDisplay(
                        """
                        **Players:** ``%d/%d``
                        **Uptime:** ``%s``
                        """,
                        serverInfoResult.getNumOfPlayers(),
                        serverInfoResult.getMaxPlayers(),
                        formatDuration(ofSeconds(parseLong(
                            serverRulesResult.getOrDefault("uptime", "0")
                                .split("\\.")[0]
                        )))
                    );

                if (serverInfoResult.getMapName().equals("Procedural Map")) {
                    var mapSize = parseInt(serverRulesResult.get("world.size"));
                    var mapSeed = parseLong(serverRulesResult.get("world.seed"));
                    return mapImageAsync(mapSize, mapSeed).thenApply(url -> {
                        builder.textDisplay("### :map: Server Map")
                            .textDisplay(
                                "Preview of ``%s`` map from that server.",
                                serverInfoResult.getMapName()
                            )
                            .gallery(url);
                        return builder;
                    });
                }

                return completedFuture(builder);

            })
            .whenComplete((builder, throwable) -> {

                run(client::close);

                if (throwable != null) {

                    log.error("Error occurred while fetching server info for {}:{}", host, port, throwable);
                    jdaEvent.getHook()
                        .editOriginalComponents(
                            componentContainerBuilder()
                                .textDisplay("### :warning: Error Occurred")
                                .textDisplay("An error occurred while fetching information about the server.")
                                .build()
                        )
                        .useComponentsV2()
                        .queue();
                    return;

                }

                jdaEvent.getHook()
                    .editOriginalComponents(builder.build())
                    .useComponentsV2()
                    .queue();

            });
    }

    @AutoComplete("serverinfo")
    public void autoComplete(
        @NotNull AutoCompleteEvent event
    ) {

        if (!event.getName().equals("host"))
            return;

        var original = event.getValue()
            .toLowerCase();

        var beforeLastDot = (original.lastIndexOf('.') == -1) ?
            original : original.substring(0, original.lastIndexOf('.'));

        var afterLastDot = (original.lastIndexOf('.') == -1) ?
            original : original.substring(original.lastIndexOf('.'));

        if (event.getValue().isBlank()) {
            event.replyChoiceStrings();
            return;
        }

        event.replyChoiceStrings(
            domainTldCollection.stream()
                .collect(collectingAndThen(
                    partitioningBy(
                        (tld) ->
                            tld.startsWith(afterLastDot)),
                    (partitions) ->
                        (partitions.get(true).isEmpty()) ?
                            partitions.get(false)
                                .stream()
                                .map(
                                    (tld) ->
                                        original + tld
                                ) :
                            partitions.get(true)
                                .stream()
                                .map(
                                    (tld) ->
                                        beforeLastDot + tld
                                )
                ))
                .toList()
        );
    }

    private static @NotNull String readServerDescription(
        @NotNull Map<String, String> details
    ) {
        return rangeClosed(0, 15)
            .mapToObj(
                (index) ->
                    details.get(format("description_%02d", index))
            )
            .filter(Objects::nonNull)
            .map(
                (line) ->
                    line.replace("\\n", "\n")
                        .replace("\\r", "\r")
                        .replace("\\t", "\t")
            )
            .collect(joining());
    }

    /* Domain Endings */
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

    /* Executor Service */
    static final ExecutorService executorService = newCachedThreadPool();
    static final SourceQueryOptions sourceQueryOptions = builder()
        .option(READ_TIMEOUT, 5000)
        .option(THREAD_EXECUTOR_SERVICE, executorService)
        .option(RESOURCE_LEAK_DETECTOR_LEVEL, DISABLED)
        .build();

}
