package pl.mrstudios.essential.module.news;

import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import kotlin.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import pl.mrstudios.essential.database.SQLite;
import pl.mrstudios.essential.module.settings.GuildSettingsManager;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.awt.Color.RED;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static kong.unirest.core.Unirest.get;
import static net.dv8tion.jda.api.interactions.components.buttons.Button.link;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.mrstudios.essential.database.statement.SQLStatement.createStatement;
import static pl.mrstudios.essential.module.news.NewsSqlRepository.*;
import static pl.mrstudios.essential.utility.StreamUtility.byteArrayInputStream;

@SuppressWarnings("FieldCanBeLocal")
public class NewsService {

    private final JDA jda;
    private final SQLite sqLite;
    private final ScheduledExecutorService executorService;

    public NewsService(
            @NotNull JDA jda,
            @NotNull SQLite sqLite,
            @NotNull GuildSettingsManager guildSettingsManager
    ) {

        this.jda = jda;
        this.sqLite = sqLite;
        this.executorService = newSingleThreadScheduledExecutor();

        createStatement(newsCreateTable).execute(this.sqLite);
        this.executorService.scheduleAtFixedRate(() -> {

            try {

                new SyndFeedInput()
                        .build(new XmlReader(byteArrayInputStream(
                                get(rssFeedUrl)
                                        .header("User-Agent", "News Reader/1.0.0 (in: '{project}')")
                                        .asString().getBody()
                                        .replace("&lt;img src=\"", "")
                                        .replace("\"&gt;&lt;br/&gt;", "|")
                                        .getBytes(UTF_8)
                        ))).getEntries().stream().filter(
                                (entry) -> createStatement(newsSelectByUrl)
                                        .setString(1, entry.getLink())
                                        .fetch(this.sqLite).isEmpty()
                        ).findFirst().ifPresent((entry) -> {

                            AtomicInteger index = new AtomicInteger();

                            createStatement(newsInsertInto)
                                    .setString(1, entry.getLink())
                                    .execute(this.sqLite);

                            this.jda.getGuilds().stream()
                                    .map((guild) -> new Pair<>(guild, guildSettingsManager.guildSettings(guild).read()))
                                    .filter((pair) -> !isNull(pair.getSecond().newsChannelId))
                                    .forEach(
                                            (pair) -> ofNullable(pair.getFirst().getTextChannelById(pair.getSecond().newsChannelId))
                                                    .ifPresent(
                                                            (channel) -> channel.sendMessageEmbeds(
                                                                    new EmbedBuilder()
                                                                            .setColor(RED)
                                                                            .setDescription(format(
                                                                                    """
                                                                                    ### :newspaper: ‌ %s
                                                                                    %s
                                                                                    """, entry.getTitle(), entry.getDescription().getValue().split("\\|")[1]
                                                                            ))
                                                                            .setImage(entry.getDescription().getValue().split("\\|")[0])
                                                                            .build()
                                                            ).addActionRow(
                                                                    link(entry.getLink(), "Article Link")
                                                            ).queueAfter(index.incrementAndGet() * 200L, MILLISECONDS)
                                                    )
                                    );

                        });


            } catch (@NotNull Exception exception) {
                logger.error("Exception occurred while fetching news.", exception);
            }

        }, 0, 5, MINUTES);

    }

    protected static final Logger logger = getLogger(NewsService.class);
    protected static final String rssFeedUrl = "https://rust.facepunch.com/rss/news";

}
