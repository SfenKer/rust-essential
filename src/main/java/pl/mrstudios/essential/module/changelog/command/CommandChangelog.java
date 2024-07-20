package pl.mrstudios.essential.module.changelog.command;

import com.google.gson.Gson;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.jda.permission.DiscordPermission;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.module.changelog.resources.Changelog;
import pl.mrstudios.essential.utility.EmbedResponseUtility;

import java.util.List;

import static java.awt.Color.RED;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static net.dv8tion.jda.api.Permission.USE_APPLICATION_COMMANDS;
import static net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode;
import static net.dv8tion.jda.api.interactions.components.selections.SelectMenu.OPTIONS_MAX_AMOUNT;
import static net.dv8tion.jda.api.interactions.components.selections.SelectOption.of;
import static net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.create;
import static pl.mrstudios.essential.utility.EmbedResponseUtility.embedResponse;
import static pl.mrstudios.essential.utility.StreamUtility.readResource;

@Command(name = "changelog")
@Description("Read changelog of Rust Essential")
@DiscordPermission(USE_APPLICATION_COMMANDS)
public class CommandChangelog {

    @Execute
    public @NotNull EmbedResponseUtility execute(
            @Context SlashCommandInteractionEvent event
    ) {
        return embedResponse(event)
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

    protected final Gson gson = new Gson();
    protected final List<Changelog> changelogs = stream(gson.fromJson(
            readResource("data/general/changelog.json"),
            Changelog[].class
    )).toList();

    protected final StringSelectMenu changelogSelectMenu = create("changelog:version")
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
