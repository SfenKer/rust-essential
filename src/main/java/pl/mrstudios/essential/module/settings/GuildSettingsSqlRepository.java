package pl.mrstudios.essential.module.settings;

public class GuildSettingsSqlRepository {

    public static final String guildsCreateTable =
            """
            CREATE TABLE IF NOT EXISTS guilds (
                  guildId BIGINT NOT NULL,
                  settings VARCHAR(81920) NOT NULL
            );
            """;

    public static final String guildsSelectAllRecords =
            """
            SELECT * FROM guilds;
            """;

    public static final String guildsSelectByGuildId =
            """
            SELECT * FROM guilds WHERE guildId=?;
            """;

    public static final String guildsInsertInto =
            """
            INSERT INTO guilds (guildId, settings) VALUES (?, ?);
            """;

    public static final String guildsDeleteEntry =
            """
            DELETE FROM guilds WHERE guildId=?;
            """;

    public static final String guildsUpdateSettings =
            """
            UPDATE guilds SET settings=? WHERE guildId=?;
            """;

}
