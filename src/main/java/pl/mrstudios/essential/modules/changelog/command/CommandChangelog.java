package pl.mrstudios.essential.modules.changelog.command;

import com.google.gson.Gson;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.modules.changelog.resources.Changelog;
import pl.mrstudios.essential.utility.builder.EmbedResponseBuilder;

import java.util.List;

import static java.awt.Color.RED;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode;
import static net.dv8tion.jda.api.interactions.components.selections.SelectMenu.OPTIONS_MAX_AMOUNT;
import static net.dv8tion.jda.api.interactions.components.selections.SelectOption.of;
import static net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.create;
import static pl.mrstudios.essential.utility.StreamUtility.readResource;
import static pl.mrstudios.essential.utility.builder.EmbedResponseBuilder.embedResponse;

@Command(name = "changelog")
@Description("Display changelog of Rust Essential.")
public class CommandChangelog {

    @Execute
    public @NotNull EmbedResponseBuilder executeDefault() {
        return embedResponse()
            .ephemeral()
            .embed(
                (embedBuilder) -> embedBuilder.setColor(RED)
                    .setDescription(this.changelogs.getFirst().toString())
                    .build()
            ).component(
                this.changelogSelectMenu,
                (executor, callback) -> {

                    callback.editSelectMenu(
                        callback.getSelectMenu()
                            .createCopy()
                            .setDefaultValues(callback.getValues())
                            .build()
                    ).queue();

                    callback.getHook().editOriginalEmbeds(
                        new EmbedBuilder()
                            .setColor(RED)
                            .setDescription(
                                this.changelogs.stream()
                                    .filter(
                                        (entry) -> callback.getValues()
                                            .getFirst()
                                            .replace("changelog:display:", "")
                                            .equals(entry.version)
                                    ).findFirst().orElseThrow()
                                    .toString()
                            ).build()
                    ).queue();

                }
            );
    }

    private final Gson gson = new Gson();
    private final List<Changelog> changelogs = stream(gson.fromJson(
        readResource("data/general/changelog.json"),
        Changelog[].class
    )).toList();

    private final StringSelectMenu changelogSelectMenu = create("changelog:version")
        .setPlaceholder("Select Version")
        .addOptions(
            this.changelogs.stream()
                .limit(OPTIONS_MAX_AMOUNT)
                .map(
                    (changelog) -> of(format("%s (v%s)", changelog.title, changelog.version), format("changelog:display:%s", changelog.version))
                        .withEmoji(fromUnicode("🗒️"))
                ).toList()
        )
        .build();

}
