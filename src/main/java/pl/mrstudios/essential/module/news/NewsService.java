package pl.mrstudios.essential.module.news;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import kotlin.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import pl.mrstudios.commons.sql.SqlConnection;
import pl.mrstudios.essential.module.settings.GuildSettingsManager;

import java.util.concurrent.atomic.AtomicInteger;

import static java.awt.Color.RED;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static kong.unirest.core.Unirest.get;
import static net.dv8tion.jda.api.Permission.MESSAGE_EMBED_LINKS;
import static net.dv8tion.jda.api.Permission.MESSAGE_SEND;
import static net.dv8tion.jda.api.interactions.components.buttons.Button.link;
import static net.dv8tion.jda.internal.utils.PermissionUtil.checkPermission;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.mrstudios.commons.sql.statement.SqlStatement.createStatement;
import static pl.mrstudios.essential.module.news.NewsSqlRepository.*;
import static pl.mrstudios.essential.module.settings.setting.GuildSetting.GUILD_NEWS_CHANNEL;
import static pl.mrstudios.essential.utility.StreamUtility.byteArrayInputStream;

@SuppressWarnings("FieldCanBeLocal")
public class NewsService {

    private final JDA jda;
    private final Logger logger;
    private final SqlConnection sqlConnection;
    private final GuildSettingsManager guildSettingsManager;

    public NewsService(
        @NotNull JDA jda,
        @NotNull SqlConnection sqlConnection,
        @NotNull GuildSettingsManager guildSettingsManager
    ) {

        this.jda = jda;
        this.sqlConnection = sqlConnection;
        this.logger = getLogger(NewsService.class);
        this.guildSettingsManager = guildSettingsManager;

        /* Create Table */
        createStatement(newsCreateTable)
            .execute(this.sqlConnection);

        /* Schedule */
        newSingleThreadScheduledExecutor().scheduleAtFixedRate(
            this::newsScheduleLogic,
            0, 15, MINUTES
        );

    }

    private void newsScheduleLogic() { try {

        syndFeed()
            .getEntries()
            .stream().filter(
                (entry) -> createStatement(newsSelectByUrl)
                    .setString(1, entry.getLink())
                    .fetch(this.sqlConnection).isEmpty()
            ).findFirst().ifPresent((entry) -> {

                AtomicInteger index = new AtomicInteger();

                createStatement(newsInsertInto)
                    .setString(1, entry.getLink())
                    .execute(this.sqlConnection);

                this.jda.getGuilds().stream()
                    .map(
                        (guild) -> new Pair<>(guild, guildSettingsManager.fetchSettings(guild)
                            .stream()
                            .filter((guildSettingEntry) -> guildSettingEntry.key() == GUILD_NEWS_CHANNEL)
                            .findFirst().orElse(null)
                        )
                    )
                    .filter((pair) -> !isNull(pair.getSecond()))
                    .forEach(
                        (pair) -> ofNullable(pair.getFirst().getTextChannelById((Long) requireNonNull(pair.getSecond().value())))
                            .filter((channel) -> checkPermission(channel, pair.getFirst().getSelfMember(), MESSAGE_SEND, MESSAGE_EMBED_LINKS))
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
                                    )
                                    .addActionRow(link(entry.getLink(), "Article Link"))
                                    .queueAfter(index.incrementAndGet() * 200L, MILLISECONDS)
                            )
                    );

            });


    } catch (@NotNull Exception exception) {
        this.logger.error("Exception occurred while fetching news.", exception);
    } }

    protected static @NotNull SyndFeed syndFeed() throws Exception {
        return new SyndFeedInput()
            .build(new XmlReader(byteArrayInputStream(
                get(RSS_FEED_URL)
                    .header("User-Agent", RSS_USER_AGENT)
                    .asString().getBody()
                    .replace("&lt;img src=\"", "")
                    .replace("\"&gt;&lt;br/&gt;", "|")
                    .getBytes(UTF_8)
            )));
    }

    protected static final @NotNull String RSS_FEED_URL = "https://rust.facepunch.com/rss/news";
    protected static final @NotNull String RSS_USER_AGENT = "News Reader/1.0.0 (in: '{project}')";

}
