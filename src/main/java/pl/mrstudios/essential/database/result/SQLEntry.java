package pl.mrstudios.essential.database.result;

import org.jetbrains.annotations.NotNull;

public record SQLEntry(
        @NotNull String key,
        @NotNull Class<?> type,
        @NotNull Object object
) {

    public @NotNull Long asLong() {
        return (Long) this.object;
    }

    public @NotNull String asString() {
        return (String) this.object;
    }

    public @NotNull Integer asInteger() {
        return (Integer) this.object;
    }

    public @NotNull Double asDouble() {
        return (Double) this.object;
    }

}
