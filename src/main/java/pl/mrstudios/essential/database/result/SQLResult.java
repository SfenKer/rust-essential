package pl.mrstudios.essential.database.result;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.List.copyOf;

public class SQLResult {

    private final Collection<SQLEntry> entries = new ArrayList<>();

    public void add(
            @NotNull SQLEntry entry
    ) {
        this.entries.add(entry);
    }

    public @NotNull Collection<SQLEntry> entries() {
        return copyOf(this.entries);
    }

    public @NotNull SQLEntry entry(
            @NotNull String key
    ) {
        return this.entries.stream()
                .filter((entry) -> entry.key().equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow();
    }

}
