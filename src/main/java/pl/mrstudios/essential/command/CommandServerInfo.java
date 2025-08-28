package pl.mrstudios.essential.command;

import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.AutoCompleteEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.ibasco.agql.protocols.valve.source.query.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.SourceQueryOptions;
import com.ibasco.agql.protocols.valve.source.query.info.SourceServer;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static com.ibasco.agql.core.util.GeneralOptions.*;
import static com.ibasco.agql.protocols.valve.source.query.SourceQueryOptions.builder;
import static io.netty.util.ResourceLeakDetector.Level.DISABLED;
import static java.awt.Color.*;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Short.MAX_VALUE;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.IntStream.rangeClosed;
import static net.dv8tion.jda.api.entities.SkuSnowflake.fromId;
import static net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL;
import static net.dv8tion.jda.api.interactions.IntegrationType.USER_INSTALL;
import static net.dv8tion.jda.api.interactions.components.buttons.Button.premium;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.mrstudios.essential.constants.Constants.DISCORD_SKU_ID;
import static pl.mrstudios.essential.utility.EmbedUtility.embedBuilder;
import static pl.mrstudios.essential.utility.StringUtility.formatDuration;
import static pl.mrstudios.essential.wrapper.RustMapsWrapper.mapImage;

@Interaction
public class CommandServerInfo {

    @CommandConfig(integration = { GUILD_INSTALL, USER_INSTALL })
    @Command(value = "serverinfo", desc = "Show status and information about server.")
    public void executeDefault(

        @NotNull CommandEvent event,

        @Param("Server Address")
        @NotNull String host,

        @Param("Server Port")
        @Min(1) @Max(MAX_VALUE * 2)
        @NotNull Integer port

    ) {

        if (
            !event.isFromAttachedGuild() && event.getEntitlements().stream()
                .noneMatch((entitlement) -> entitlement.getSkuIdLong() == DISCORD_SKU_ID)
        ) {
            event.jdaEvent().deferReply(true)
                .addEmbeds(
                    embedBuilder()
                        .setColor(RED)
                        .setDescription(
                            """
                            ### :gem: ‌ Rust Essential+
                            Using commands on guilds where bot is not added or in direct messages requires `Rust Essential+` subscription.
                            """
                        ).build()
                )
                .addActionRow(premium(fromId(DISCORD_SKU_ID)))
                .queue();
            return;
        }

        event.with()
            .ephemeral(true)
            .reply(
                embedBuilder()
                    .setColor(RED)
                    .setDescription(
                        """
                        ### :clock1: ‌ Server Information
                        We are currently fetching information about the server, please wait..
                        """
                    )
            );

        getLogger(CommandServerInfo.class)
            .info("User '{}' requested server information for {}:{}", event.getUser().getName(), host, port);

        runAsync(() -> {

            try (
                SourceQueryClient client = new SourceQueryClient(sourceQueryOptions)
            ) {

                InetSocketAddress socketAddress = new InetSocketAddress(host, port);

                SourceServer server = client.getInfo(socketAddress)
                    .get().getResult();

                Map<String, String> serverDetails = client.getRules(socketAddress)
                    .get().getResult();

                EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(
                        (server.getNumOfPlayers() >= server.getMaxPlayers()) ?
                            YELLOW : GREEN
                    ).setDescription(format(
                        """
                        ### :notepad_spiral: ‌ Server Information
                        **Name:** ``%s``
                        **Description:**
                        ```
                        %s
                        ```
                        ### :bar_chart: ‌ Statistics
                        **Players:** ``%d/%d``
                        **Uptime:** ``%s``
                        """,
                        server.getName(), readServerDescription(serverDetails),
                        server.getNumOfPlayers(), server.getMaxPlayers(), formatDuration(ofSeconds(parseLong(
                            serverDetails.getOrDefault("uptime", "0")
                                .split("\\.")[0]
                        )))

                    ));

                ofNullable(serverDetails.get("logoimage"))
                    .ifPresent(embedBuilder::setThumbnail);

                if (server.getMapName().equals("Procedural Map"))
                    ofNullable(mapImage(
                        parseInt(serverDetails.get("world.size")),
                        parseLong(serverDetails.get("world.seed"))
                    )).ifPresent((image) -> {
                        embedBuilder.setImage(image);
                        embedBuilder.getDescriptionBuilder().append(format(
                            """
                            ### :map: ‌ Map
                            Preview of ``%s`` map which server is using.
                            """, server.getMapName()
                        ));
                    });

                event.with()
                    .ephemeral(true)
                    .reply(embedBuilder);

            } catch (@NotNull Exception exception) {
                event.with()
                    .ephemeral(true)
                    .reply(
                        embedBuilder()
                            .setColor(RED)
                            .setDescription(
                                """
                                ### :warning: ‌ Error Occurred
                                An error occurred while fetching information about the server.
                                """
                            )
                    );
            }

        });

    }

    @AutoComplete("serverinfo")
    public void autoComplete(
        @NotNull AutoCompleteEvent event
    ) {

        if (!event.getName().equals("host"))
            return;

        Collection<String> collection = new ArrayList<>();
        String original = event.getValue().toLowerCase(),
            beforeLastDot = (original.lastIndexOf('.') == -1) ?
                original : original.substring(0, original.lastIndexOf('.')),
            afterLastDot = (original.lastIndexOf('.') == -1) ?
                original : original.substring(original.lastIndexOf('.'));

        if (event.getValue().isBlank()) {
            event.replyChoiceStrings();
            return;
        }

        collection.add(event.getValue());
        domainTldCollection.stream()
            .filter((tld) -> tld.startsWith(afterLastDot))
            .map((tld) -> beforeLastDot + tld)
            .filter((string) -> !collection.contains(string))
            .forEach(collection::add);

        domainTldCollection.stream()
            .filter((tld) -> tld.startsWith(afterLastDot))
            .findFirst()
            .ifPresentOrElse(
                (_) -> {}, () -> domainTldCollection.stream()
                    .map((tld) -> original + tld)
                    .filter((string) -> !collection.contains(string))
                    .forEach(collection::add)
            );

        event.replyChoiceStrings(collection);

    }

    private static @NotNull String readServerDescription(
        @NotNull Map<String, String> details
    ) {

        Collection<String> collection = new ArrayList<>();

        ofNullable(details.get("description_0"))
            .ifPresent(collection::add);

        rangeClosed(0, 15)
            .mapToObj((index) -> details.get(format("description_%02d", index)))
            .filter(Objects::nonNull).forEach(collection::add);

        return join(
            "", collection.stream()
                .map(
                    (line) -> line.replace("\\n", "\n")
                        .replace("\\r", "\r")
                        .replace("\\t", "\t")
                ).toList()
        );

    }

    /* Domain Endings */
    private static final @NotNull Collection<String> domainTldCollection = asList(
        ".com", ".co", ".net", ".org", ".gg", ".uk", ".eu", ".de"
    );

    /* Executor Service */
    private static final ExecutorService executorService = newCachedThreadPool();
    private static final SourceQueryOptions sourceQueryOptions = builder()
        .option(READ_TIMEOUT, 5000)
        .option(THREAD_EXECUTOR_SERVICE, executorService)
        .option(RESOURCE_LEAK_DETECTOR_LEVEL, DISABLED)
        .build();

}
