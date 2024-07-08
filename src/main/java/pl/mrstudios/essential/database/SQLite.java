package pl.mrstudios.essential.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.database.result.SQLEntry;
import pl.mrstudios.essential.database.result.SQLResult;
import pl.mrstudios.essential.database.statement.SQLStatement;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.Class.forName;
import static java.nio.file.Files.*;
import static java.nio.file.Paths.get;

public class SQLite {

    private HikariDataSource dataSource;
    private final HikariConfig hikariConfig;

    @SneakyThrows
    public SQLite() {

        this.hikariConfig = new HikariConfig();
        this.hikariConfig.setJdbcUrl("jdbc:sqlite:database/database.db");

        if (!exists(DATABASE_FILE_PATH)) {
            createDirectories(DATABASE_DIR_PATH);
            createFile(DATABASE_FILE_PATH);
        }

        this.dataSource = new HikariDataSource(this.hikariConfig);

    }

    @SneakyThrows
    @SuppressWarnings("all")
    public @NotNull Collection<SQLResult> fetch(
            @NotNull SQLStatement statement
    ) {

        Collection<SQLResult> result = new ArrayList<>();

        if (this.dataSource.isClosed())
            this.dataSource = new HikariDataSource(this.hikariConfig);

        try (
                Connection connection = this.dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(statement.query())
        ) {

            statement.prepare(preparedStatement);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next())
                    result.add(this.proceedResultSet(resultSet));
            }

        }

        return result;

    }

    @SneakyThrows
    @SuppressWarnings("all")
    public void execute(
            @NotNull SQLStatement statement
    ) {

        try (
                Connection connection = this.dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(statement.query())
        ) {
            statement.prepare(preparedStatement);
            preparedStatement.execute();
        }

    }

    @SneakyThrows
    protected @NotNull SQLResult proceedResultSet(
            @NotNull ResultSet resultSet
    ) {

        SQLResult result = new SQLResult();
        ResultSetMetaData metaData = resultSet.getMetaData();

        for (int i = 1; i <= metaData.getColumnCount(); i++)
            result.add(new SQLEntry(metaData.getColumnName(i), forName(metaData.getColumnClassName(i)), resultSet.getObject(i)));

        return result;

    }

    protected static final Path DATABASE_DIR_PATH = get("database");
    protected static final Path DATABASE_FILE_PATH = get("database/", "database.db");

}
