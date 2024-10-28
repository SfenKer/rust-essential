package pl.mrstudios.essential.module.news;

public class NewsSqlRepository {

    public static final String newsCreateTable =
        """
        CREATE TABLE IF NOT EXISTS news (
              url VARCHAR(1024) NOT NULL
        );
        """;

    public static final String newsSelectByUrl =
        """
        SELECT * FROM news WHERE url=?;
        """;

    public static final String newsInsertInto =
        """
        INSERT INTO news (url) VALUES (?);
        """;

}
