package pl.mrstudios.essential.migration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.commons.sql.SqlConnection;
import pl.mrstudios.essential.service.settings.serializer.GuildSettingEntrySerializer;
import pl.mrstudios.essential.service.settings.serializer.GuildSettingSerializer;
import pl.mrstudios.essential.service.settings.setting.GuildSetting;
import pl.mrstudios.essential.service.settings.setting.GuildSettingContainer;

import java.util.ArrayList;
import java.util.Collection;

import static pl.mrstudios.commons.sql.statement.SqlStatement.createStatement;
import static pl.mrstudios.essential.service.settings.setting.GuildSetting.GUILD_NEWS_CHANNEL;
import static pl.mrstudios.essential.service.settings.setting.GuildSettingContainer.guildSettingContainer;

/*
 *  Used at 01/11/2024 by Hubert Kuliniak to migrate existing guild settings,
 *  from the old system to the new one. This tool was saved for future reference.
 */
public class GuildsMigrationTool {

    public static void main(
        @NotNull String[] arguments
    ) {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:sqlite:database/database.db");

        SqlConnection sqlConnection = new SqlConnection(hikariConfig);
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(GuildSetting.class, new GuildSettingSerializer())
            .registerTypeAdapter(GuildSettingContainer.class, new GuildSettingEntrySerializer())
            .create();

        createStatement("SELECT * FROM guilds;")
            .fetch(sqlConnection)
            .forEach((result) -> {

                Long guildId = result.entry("guildId").asLong();
                String settings = result.entry("settings").asString();

                JsonObject legacySettingsObject = gson.fromJson(settings, JsonObject.class);
                Collection<GuildSettingContainer> guildSettings = new ArrayList<>();

                if (legacySettingsObject.has("newsChannelId"))
                    guildSettings.add(guildSettingContainer(GUILD_NEWS_CHANNEL, legacySettingsObject.get("newsChannelId").getAsLong()));

                createStatement("UPDATE guilds SET settings=? WHERE guildId=?;")
                    .setString(1, gson.toJson(guildSettings))
                    .setLong(2, guildId)
                    .execute(sqlConnection);

            });

    }

}
