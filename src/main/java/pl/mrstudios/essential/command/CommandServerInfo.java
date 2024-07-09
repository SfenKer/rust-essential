package pl.mrstudios.essential.command;

import com.github.benmanes.caffeine.cache.Cache;
import com.ibasco.agql.protocols.valve.source.query.SourceQueryClient;
import com.ibasco.agql.protocols.valve.source.query.SourceQueryOptions;
import com.ibasco.agql.protocols.valve.source.query.info.SourceServer;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.jda.permission.DiscordPermission;
import kotlin.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.utility.EmbedResponseUtility;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static com.ibasco.agql.core.util.GeneralOptions.*;
import static com.ibasco.agql.protocols.valve.source.query.SourceQueryOptions.builder;
import static io.netty.util.ResourceLeakDetector.Level.DISABLED;
import static java.awt.Color.*;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.IntStream.rangeClosed;
import static net.dv8tion.jda.api.Permission.USE_APPLICATION_COMMANDS;
import static pl.mrstudios.essential.utility.EmbedResponseUtility.embedResponse;
import static pl.mrstudios.essential.utility.StringUtility.formatDuration;
import static pl.mrstudios.essential.wrapper.RustMapsAPI.mapImage;

@Command(name = "serverinfo")
@Description("Show status and information about server.")
@DiscordPermission(USE_APPLICATION_COMMANDS)
public class CommandServerInfo {

    private final Cache<String, Pair<SourceServer, Map<String, String>>> cache = newBuilder()
            .expireAfterWrite(ofMinutes(15))
            .build();

    @Execute
    public @NotNull EmbedResponseUtility executeDefault(

            @Context SlashCommandInteractionEvent event,

            @Arg("host")
            @Description("Server Address")
            @NotNull String host,

            @Arg("port")
            @Description("Server Port")
            @NotNull Optional<Integer> port

    ) {

        runAsync(() -> {

            try (
                    SourceQueryClient client = new SourceQueryClient(this.sourceQueryOptions)
            ) {

                InetSocketAddress socketAddress = new InetSocketAddress(host, port.orElse(28015));
                Pair<SourceServer, Map<String, String>> pair = this.cache.get(
                        format("%s:%d", host, port.orElse(28015)),
                        (key) -> {
                            try {
                                return new Pair<>(
                                        client.getInfo(socketAddress).get().getResult(),
                                        client.getRules(socketAddress).get().getResult()
                                );
                            } catch (@NotNull Exception exception) {
                                throw new RuntimeException("Unable to fetch server information due to exception.", exception);
                            }
                        }
                );

                SourceServer server = pair.getFirst();
                Map<String, String> details = pair.getSecond();

                EmbedBuilder mainEmbed = new EmbedBuilder()
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
                                server.getName(), readServerDescription(details),
                                server.getNumOfPlayers(), server.getMaxPlayers(), formatDuration(ofSeconds(parseLong(
                                        details.getOrDefault("uptime", "0")
                                                .split("\\.")[0]
                                )))

                        ));

                ofNullable(details.get("logoimage"))
                        .ifPresent(mainEmbed::setThumbnail);

                ofNullable(mapImage(
                        parseInt(details.get("world.size")),
                        parseLong(details.get("world.seed"))
                )).ifPresent((image) -> {
                    mainEmbed.setImage(image);
                    mainEmbed.getDescriptionBuilder().append(format(
                            """
                            ### :map: ‌ Map
                            Preview of ``%s`` map which server is using.
                            """, server.getMapName()
                    ));
                });

                event.getHook().editOriginalEmbeds(mainEmbed.build())
                        .queue();

            } catch (@NotNull Exception exception) {
                event.getHook().editOriginalEmbeds(
                        new EmbedBuilder()
                                .setColor(RED)
                                .setDescription(
                                        """
                                        ### :warning: ‌ Error Occurred
                                        An error occurred while fetching information about the server.
                                        """
                                ).build()
                ).queue();
            }

        });

        return embedResponse(event)
                .ephemeral()
                .embed(
                        (embedBuilder) -> embedBuilder
                                .setColor(RED)
                                .setDescription(
                                        """
                                        ### :clock1: ‌ Server Information
                                        We are currently fetching information about the server, please wait..
                                        """
                                )
                );

    }

    protected static @NotNull String readServerDescription(
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

    protected final ExecutorService executorService = newCachedThreadPool();
    protected final SourceQueryOptions sourceQueryOptions = builder()
            .option(READ_TIMEOUT, 5000)
            .option(THREAD_EXECUTOR_SERVICE, this.executorService)
            .option(RESOURCE_LEAK_DETECTOR_LEVEL, DISABLED)
            .build();

}
