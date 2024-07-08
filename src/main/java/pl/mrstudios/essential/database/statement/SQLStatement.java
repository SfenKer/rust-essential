package pl.mrstudios.essential.database.statement;

import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import static java.sql.JDBCType.*;

public class SQLStatement {

    private final String query;
    private final Map<Integer, SQLStatementObject> elements;

    public SQLStatement(
            @NotNull String query
    ) {
        this.query = query;
        this.elements = new HashMap<>();
    }

    public @NotNull SQLStatement setString(
            @NotNull Integer position,
            @NotNull String value
    ) {
        this.elements.put(position, new SQLStatementObject(position, VARCHAR, value));
        return this;
    }

    public @NotNull SQLStatement setLongString(
            @NotNull Integer position,
            @NotNull String value
    ) {
        this.elements.put(position, new SQLStatementObject(position, LONGVARCHAR, value));
        return this;
    }

    public @NotNull SQLStatement setInteger(
            @NotNull Integer position,
            @NotNull Integer value
    ) {
        this.elements.put(position, new SQLStatementObject(position, INTEGER, value));
        return this;
    }

    public @NotNull SQLStatement setDouble(
            @NotNull Integer position,
            @NotNull Double value
    ) {
        this.elements.put(position, new SQLStatementObject(position, DOUBLE, value));
        return this;
    }

    public @NotNull SQLStatement setLong(
            @NotNull Integer position,
            @NotNull Long value
    ) {
        this.elements.put(position, new SQLStatementObject(position, BIGINT, value));
        return this;
    }

    public @NotNull String query() {
        return this.query;
    }

    public @NotNull PreparedStatement prepare(
            @NotNull PreparedStatement preparedStatement
    ) {

        this.elements.forEach((position, object) -> {
            try {

                switch (object.type()) {

                    case VARCHAR, LONGVARCHAR ->
                            preparedStatement.setString(position, object.object().toString());

                    case INTEGER ->
                            preparedStatement.setInt(position, (Integer) object.object());

                    case BIGINT ->
                            preparedStatement.setLong(position, (Long) object.object());

                    case DOUBLE ->
                            preparedStatement.setDouble(position, (Double) object.object());

                    default ->
                            throw new IllegalStateException("Unexpected Value: " + object.type());

                }

            } catch (@NotNull Exception exception) {
                throw new RuntimeException("Unable to prepare statement due to exception.", exception);
            }
        });

        return preparedStatement;

    }

    public static @NotNull SQLStatement createStatement(
            @NotNull String query
    ) {
        return new SQLStatement(query);
    }

}
