package pl.mrstudios.essential.module.changelog.resources;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static java.util.Arrays.asList;

public class Changelog {

    public String title;
    public String version;
    public String date;
    public Map<String, String[]> changes;

    @Override
    public @NotNull String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("## :notepad_spiral: ‌ ").append(this.title).append(" ")
                .append("(v").append(this.version).append(")")
                .append("\n").append("Version was released on ``").append(this.date).append("``.");

        this.changes.forEach((category, changes) -> {
            stringBuilder.append("\n### ").append(category);
            asList(changes).forEach(
                    (change) -> stringBuilder.append("\n- ")
                            .append(change)
            );
        });

        return stringBuilder.toString();

    }

}
