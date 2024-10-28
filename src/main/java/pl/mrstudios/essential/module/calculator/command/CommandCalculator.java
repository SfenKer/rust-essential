package pl.mrstudios.essential.module.calculator.command;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.gson.Gson;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.jda.permission.DiscordPermission;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.module.calculator.resources.StructureExplosivesSet;
import pl.mrstudios.essential.module.calculator.session.CalculatorSession;
import pl.mrstudios.essential.utility.EmbedResponseUtility;

import java.text.DecimalFormat;
import java.util.function.BiConsumer;

import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static java.awt.Color.RED;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.time.Duration.ofMinutes;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static net.dv8tion.jda.api.Permission.USE_APPLICATION_COMMANDS;
import static net.dv8tion.jda.api.entities.emoji.Emoji.fromCustom;
import static net.dv8tion.jda.api.interactions.components.buttons.Button.success;
import static net.dv8tion.jda.api.interactions.components.selections.SelectOption.of;
import static net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.create;
import static net.dv8tion.jda.api.interactions.components.text.TextInputStyle.SHORT;
import static pl.mrstudios.essential.utility.EmbedResponseUtility.embedResponse;
import static pl.mrstudios.essential.utility.EmojiUtility.customEmoji;
import static pl.mrstudios.essential.utility.ModalResponseUtility.modalResponse;
import static pl.mrstudios.essential.utility.StreamUtility.readResource;

@Command(name = "calculator")
@Description("Calculator of Raid Cost.")
@DiscordPermission(USE_APPLICATION_COMMANDS)
public class CommandCalculator {

    private final Cache<Long, CalculatorSession> cache = newBuilder()
        .expireAfterAccess(ofMinutes(5))
        .build();

    @Execute
    public @NotNull EmbedResponseUtility execute(
        @Context User user,
        @Context SlashCommandInteractionEvent event
    ) {

        this.cache.invalidate(user.getIdLong());

        return embedResponse(event)
            .ephemeral()
            .embed(
                (embedBuilder) -> embedBuilder.setColor(RED)
                    .setDescription(
                        """
                        ### :wave: ‌ Welcome in Raid Cost Calculator!
                        Using this tool, you can easily calculate the cost of raiding a base in Rust.

                        ### :notebook_with_decorative_cover: ‌ Usage Guide
                        ``1.`` Select explosives set for calculations.
                        ``2.`` Select structure from the list below.
                        ``3.`` Click button and provide the amount of structures.
                        ``4.`` Done! You will see the cost of raiding the selected structure, you can also add more structures and you will see total cost.
                        """
                    )
            ).component(
                create("calculator:explosive_set")
                    .setPlaceholder("Select Explosive Set")
                    .addOptions(
                        stream(this.structureExplosivesSets)
                            .map(
                                (explosiveSet) -> of(explosiveSet.name, format("calculator:explosive_set:%s", explosiveSet.id))
                                    .withEmoji(fromCustom(format("rust_%s", explosiveSet.id), explosiveSet.emoji, false))
                            ).toList()
                    ).build(),
                (executor, callback) -> {

                    CalculatorSession session = this.cache.get(executor.getIdLong(), (key) -> new CalculatorSession());

                    session.currentExplosivesSet = stream(this.structureExplosivesSets)
                        .filter(
                            (set) -> callback.getValues()
                                .getFirst()
                                .replace("calculator:explosive_set:", "")
                                .equals(set.id)
                        ).findFirst()
                        .orElseThrow();

                    ofNullable(session.currentStructure)
                        .ifPresent(
                            (currentStructure) -> session.currentStructure = stream(session.currentExplosivesSet.structures)
                                .filter((structure) -> structure.id.equals(currentStructure.id))
                                .findFirst().orElseThrow()
                        );

                    callback.editSelectMenu(
                        callback.getSelectMenu()
                            .createCopy()
                            .setDefaultValues(callback.getValues())
                            .build()
                    ).queue();

                }
            ).component(
                create("calculator:structure")
                    .setPlaceholder("Select Structure")
                    .addOptions(
                        stream(this.structureExplosivesSets)
                            .findFirst().stream()
                            .flatMap((explosiveSet) -> stream(explosiveSet.structures))
                            .map(
                                (structure) -> of(structure.name, format("calculator:structure:%s", structure.id))
                                    .withEmoji(fromCustom(format("rust_%s", structure.id), structure.emoji, false))
                            ).toList()
                    ).build(),
                (executor, callback) -> {

                    CalculatorSession session = this.cache.get(executor.getIdLong(), (key) -> new CalculatorSession());

                    ofNullable(session.currentExplosivesSet)
                        .ifPresentOrElse(
                            (explosiveSet) -> session.currentStructure = stream(session.currentExplosivesSet.structures)
                                .filter(
                                    (structure) -> callback.getValues()
                                        .getFirst()
                                        .replace("calculator:structure:", "")
                                        .equals(structure.id)
                                ).findFirst()
                                .orElseThrow(),
                            () -> callback.deferReply(true)
                                .setEmbeds(
                                    new EmbedBuilder()
                                        .setColor(RED)
                                        .setDescription(
                                            """
                                            ### :warning: ‌ Error Occurred
                                            You must select explosives set before selecting structure.
                                            """
                                        ).build()
                                ).queue()
                        );

                    if (!callback.isAcknowledged())
                        callback.editSelectMenu(
                            callback.getSelectMenu()
                                .createCopy()
                                .setDefaultValues(callback.getValues())
                                .build()
                        ).queue();

                }
            ).component(
                success("calculator:provide_amount", "Provide Amount"),
                (executor, callback) -> {

                    CalculatorSession session = this.cache.get(executor.getIdLong(), (key) -> new CalculatorSession());

                    if (isNull(session.currentStructure)) {
                        callback.deferReply(true)
                            .setEmbeds(
                                new EmbedBuilder()
                                    .setColor(RED)
                                    .setDescription(
                                        """
                                        ### :warning: ‌ Error Occurred
                                        You must select structure before providing amount.
                                        """
                                    ).build()
                            ).queue();
                        return;
                    }

                    modalResponse(callback)
                        .id("calculator:modal:provide_amount")
                        .title("Calculator")
                        .components(
                            TextInput.create("calculator:modal:provide_amount:amount", "Amount", SHORT)
                                .setRequired(true)
                                .build()
                        ).handler(this.modalHandler)
                        .build();

                }
            );

    }

    protected final DecimalFormat decimalFormat = new DecimalFormat("#,###");

    protected final Gson gson = new Gson();
    protected final StructureExplosivesSet[] structureExplosivesSets = gson.fromJson(
        readResource("data/rust/calculator/structure_explosives_set.json"),
        StructureExplosivesSet[].class
    );

    /* Modal Handler */
    protected final BiConsumer<User, ModalInteractionEvent> modalHandler = (executor, callback) -> {

        CalculatorSession session = this.cache.get(executor.getIdLong(), (key) -> new CalculatorSession());

        try {

            StringBuilder stringBuilder = new StringBuilder();
            Integer amount = parseInt(requireNonNull(callback.getValue("calculator:modal:provide_amount:amount")).getAsString());

            session.rockets += (session.currentStructure.costs.rocket * amount);
            session.bombs += (session.currentStructure.costs.bomb * amount);
            session.explosiveAmmo += (session.currentStructure.costs.explosive * amount);
            session.satchels += (session.currentStructure.costs.satchel * amount);

            if (session.rockets > 0)
                stringBuilder.append("\n").append("\u200C \u200C \u200C").append("``").append(this.decimalFormat.format(session.rockets)).append("x").append("``")
                    .append(" ").append(customEmoji("rocket").getFormatted())
                    .append(" Rocket");

            if (session.bombs > 0)
                stringBuilder.append("\n").append("\u200C \u200C \u200C").append("``").append(this.decimalFormat.format(session.bombs)).append("x").append("``")
                    .append(" ").append(customEmoji("timed_explosive_charge").getFormatted())
                    .append(" Timed Explosive Charge");

            if (session.satchels > 0)
                stringBuilder.append("\n").append("\u200C \u200C \u200C").append("``").append(this.decimalFormat.format(session.satchels)).append("x").append("``")
                    .append(" ").append(customEmoji("satchel_explosive_charge").getFormatted())
                    .append(" Satchel Explosive Charge");

            if (session.explosiveAmmo > 0)
                stringBuilder.append("\n").append("\u200C \u200C \u200C").append("``").append(this.decimalFormat.format(session.explosiveAmmo)).append("x").append("``")
                    .append(" ").append(customEmoji("explosive_ammo").getFormatted())
                    .append(" Explosive Ammo");

            embedResponse(callback)
                .deferEdit()
                .embed(
                    (embedBuilder) -> embedBuilder.setColor(RED)
                        .setDescription(format(
                            """
                            ### :notepad_spiral: ‌ Raid Calculator
                            You need ``%sx`` %s Sulfur to raid that base, with that sulfur you should make: %s

                            ### :notebook_with_decorative_cover: ‌ Usage Guide
                            ``1.`` Select structure from the list below.
                            ``2.`` Click button and provide the amount of structures.
                            ``3.`` Done! You will see the cost of raiding the selected structure, you can also add more structures and you will see total cost.
                            """, this.decimalFormat.format(session.totalSulphurNeeded()), customEmoji("sulphur").getFormatted(), stringBuilder
                        ))
                ).build();

        } catch (@NotNull Exception exception) {
            callback.deferReply(true)
                .setEmbeds(
                    new EmbedBuilder()
                        .setColor(RED)
                        .setDescription(
                            """
                            ### :warning: ‌ Error Occurred
                            You must provide a number in the input.
                            """
                        ).build()
                ).queue();
        }
    };

}
