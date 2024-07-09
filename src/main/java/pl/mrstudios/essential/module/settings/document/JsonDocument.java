package pl.mrstudios.essential.module.settings.document;

import org.jetbrains.annotations.NotNull;

public interface JsonDocument<DOCUMENT> {
    @NotNull DOCUMENT read();
    void save();
}
