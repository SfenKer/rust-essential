package pl.mrstudios.essential.database.statement;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLType;

public record SQLStatementObject(
        @NotNull Integer position,
        @NotNull SQLType type,
        @NotNull Object object
) {}
