package pl.mrstudios.essential.service.news;

import kotlin.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.commons.sql.SqlConnection;
import pl.mrstudios.essential.service.settings.GuildSettingsService;

import java.util.concurrent.atomic.AtomicInteger;

import static java.awt.Color.RED;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static net.dv8tion.jda.api.Permission.MESSAGE_EMBED_LINKS;
import static net.dv8tion.jda.api.Permission.MESSAGE_SEND;
import static net.dv8tion.jda.api.interactions.components.buttons.Button.link;
import static net.dv8tion.jda.internal.utils.PermissionUtil.checkPermission;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.mrstudios.commons.sql.statement.SqlStatement.createStatement;
import static pl.mrstudios.essential.service.news.NewsSqlRepository.*;
import static pl.mrstudios.essential.service.settings.setting.GuildSetting.GUILD_NEWS_CHANNEL;
import static pl.mrstudios.essential.wrapper.FacepunchWrapper.rssFeedEntries;

@SuppressWarnings("FieldCanBeLocal")
public class NewsService {

    private final JDA jda;
    private final SqlConnection sqlConnection;
    private final GuildSettingsService guildSettingsService;

    public NewsService(
        @NotNull JDA jda,
        @NotNull SqlConnection sqlConnection,
        @NotNull GuildSettingsService guildSettingsService
    ) {

        this.jda = jda;
        this.sqlConnection = sqlConnection;
        this.guildSettingsService = guildSettingsService;

        /* Create Table */
        createStatement(newsCreateTable)
            .execute(this.sqlConnection);

        /* Schedule */
        newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(
                this::newsExecutorRunnable,
                0, 15, MINUTES
            );

    }

    private void newsExecutorRunnable() { try {

        rssFeedEntries().stream()
            .filter(
                (entry) -> createStatement(newsSelectByUrl)
                    .setString(1, entry.getLink())
                    .fetch(this.sqlConnection).isEmpty()
            ).findFirst()
            .ifPresent((entry) -> {

                AtomicInteger index = new AtomicInteger();
                MessageEmbed messageEmbed = new EmbedBuilder()
                    .setDescription(format(
                        """
                        ### :newspaper: ‌ %s
                        %s
                        """, entry.getTitle(), entry.getDescription().getValue().split("\\|")[1]
                    )).setColor(RED)
                    .setImage(entry.getDescription().getValue().split("\\|")[0])
                    .build();

                createStatement(newsInsertInto)
                    .setString(1, entry.getLink())
                    .execute(this.sqlConnection);

                this.jda.getGuilds().stream()
                    .map(
                        (guild) -> new Pair<>(guild, this.guildSettingsService.fetchSettings(guild)
                            .stream()
                            .filter((guildSettingEntry) -> guildSettingEntry.key() == GUILD_NEWS_CHANNEL)
                            .findFirst().orElse(null)
                        )
                    ).filter((pair) -> !isNull(pair.getSecond()))
                    .forEach(
                        (pair) -> ofNullable(pair.getFirst().getTextChannelById((Long) requireNonNull(pair.getSecond().value())))
                            .filter((channel) -> checkPermission(channel, pair.getFirst().getSelfMember(), MESSAGE_SEND, MESSAGE_EMBED_LINKS))
                            .ifPresent(
                                (channel) -> channel.sendMessageEmbeds(messageEmbed)
                                    .addActionRow(link(entry.getLink(), "Article Link"))
                                    .queueAfter(index.incrementAndGet() * 200L, MILLISECONDS)
                            )
                    );

            });


    } catch (
        @NotNull Exception exception
    ) {
        getLogger(NewsService.class).error("An exception occurred while fetching news.", exception);
    } }

}
