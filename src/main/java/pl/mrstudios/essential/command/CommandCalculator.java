package pl.mrstudios.essential.command;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.data.resources.StructureExplosivesSet;
import pl.mrstudios.essential.data.session.CalculatorSession;

import java.text.DecimalFormat;
import java.util.List;

import static com.github.kaktushose.jda.commands.dispatching.reply.Component.stringSelect;
import static java.awt.Color.RED;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL;
import static net.dv8tion.jda.api.interactions.IntegrationType.USER_INSTALL;
import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.SUCCESS;
import static net.dv8tion.jda.api.interactions.components.text.TextInputStyle.SHORT;
import static pl.mrstudios.essential.utility.EmbedUtility.embedBuilder;
import static pl.mrstudios.essential.utility.EmojiUtility.customEmoji;
import static pl.mrstudios.essential.utility.StreamUtility.readResource;

@Interaction
@SuppressWarnings({ "UnstableApiUsage" })
public class CommandCalculator {

    private final CalculatorSession session;

    {
        this.session = new CalculatorSession();
    }

    @CommandConfig(integration = { GUILD_INSTALL, USER_INSTALL })
    @Command(value = "calculator", desc = "Calculator of Raid Cost.")
    public void executeCommand(
        @NotNull CommandEvent event
    ) {

        event.with()
            .ephemeral(true)
            .components("explosiveSetMenu")
            .components("structureMenu")
            .components("provideAmount")
            .reply(
                embedBuilder()
                    .setColor(RED)
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
            );

    }

    @MenuOptionContainer(value = {
        @MenuOption(value = "sulphur", label = "Any Explosives", emoji = "<:rust_sulphur:1300428595117948949>"),
        @MenuOption(value = "rocket", label = "Rocket", emoji = "<:rust_rocket:1300428554051387512>"),
        @MenuOption(value = "timed_explosive_charge", label = "Timed Explosive Charge", emoji = "<:rust_timed_explosive_charge:1300428605297524826>"),
        @MenuOption(value = "satchel_explosive_charge", label = "Satchel Explosive Charge", emoji = "<:rust_satchel_explosive_charge:1300428562150723634>"),
        @MenuOption(value = "explosive_ammo", label = "Explosive Ammo", emoji = "<:rust_explosive_ammo:1300428472476237874>")
    }) @StringSelectMenu("Select Explosive Set")
    public void explosiveSetMenu(
        @NotNull ComponentEvent event,
        @NotNull List<String> choices
    ) {

        this.session.currentExplosivesSet = stream(structureExplosivesSets)
            .filter(
                (set) -> choices.getFirst()
                    .equals(set.id)
            ).findFirst()
            .orElseThrow();

        ofNullable(this.session.currentStructure)
            .ifPresent(
                (currentStructure) -> this.session.currentStructure = stream(this.session.currentExplosivesSet.structures)
                    .filter((structure) -> structure.id.equals(currentStructure.id))
                    .findFirst().orElseThrow()
            );

        event.deferEdit();

    }

    @MenuOptionContainer(value = {
        @MenuOption(value = "wooden_wall", label = "Wooden Wall", emoji = "<:rust_wooden_wall:1300428624977203260>"),
        @MenuOption(value = "stone_wall", label = "Stone Wall", emoji = "<:rust_stone_wall:1300428584338325524>"),
        @MenuOption(value = "sheet_metal_wall", label = "Sheet Metal Wall", emoji = "<:rust_sheet_metal_wall:1300428526373044255>"),
        @MenuOption(value = "high_quality_metal_wall", label = "Armored Wall", emoji = "<:rust_high_quality_metal_wall:1300428506123210782>"),
        @MenuOption(value = "metal_shop_front", label = "Metal Shop Front", emoji = "<:rust_metal_shop_front:1300428534971502604>"),
        @MenuOption(value = "wooden_door", label = "Wooden Door", emoji = "<:rust_wooden_door:1300428637400465448>"),
        @MenuOption(value = "sheet_metal_door", label = "Sheet Metal Door", emoji = "<:rust_sheet_metal_door:1300428570572754954>"),
        @MenuOption(value = "armored_door", label = "Armored Door", emoji = "<:rust_armored_door:1300428456139427880>"),
        @MenuOption(value = "garage_door", label = "Garage Door", emoji = "<:rust_garage_door:1300428499429097482>"),
        @MenuOption(value = "ladder_hatch", label = "Ladder Hatch", emoji = "<:rust_ladder_hatch:1300428513379356742>"),
        @MenuOption(value = "wooden_window_bars", label = "Wooden Window Bars", emoji = "<:rust_wooden_window_bars:1300428647689228390>"),
        @MenuOption(value = "metal_window_bars", label = "Metal Window Bars", emoji = "<:rust_metal_window_bars:1300428541783179314>"),
        @MenuOption(value = "vending_machine", label = "Vending Machine", emoji = "<:rust_vending_machine:1300428620594020503>"),
        @MenuOption(value = "external_wooden_wall", label = "External Wooden Wall", emoji = "<:rust_external_wooden_wall:1300428492206506005>"),
        @MenuOption(value = "external_stone_wall", label = "External Stone Wall", emoji = "<:rust_external_stone_wall:1300428483079438407>")
    }) @StringSelectMenu("Select Structure")
    public void structureMenu(
        @NotNull ComponentEvent event,
        @NotNull List<String> choices
    ) {

        ofNullable(this.session.currentExplosivesSet)
            .ifPresentOrElse(
                (explosiveSet) -> this.session.currentStructure = stream(this.session.currentExplosivesSet.structures)
                    .filter(
                        (structure) -> choices.getFirst()
                            .equals(structure.id)
                    ).findFirst()
                    .orElseThrow(),
                () -> event.with()
                    .ephemeral(true)
                    .editReply(false)
                    .keepComponents(false)
                    .reply(
                        embedBuilder()
                            .setColor(RED)
                            .setDescription(
                                """
                                ### :warning: ‌ Error Occurred
                                You must select explosives set before selecting structure.
                                """
                            )
                    )
            );

        if (!event.jdaEvent().isAcknowledged())
            event.deferEdit();

    }

    @Button(value = "Provide Amount", style = SUCCESS)
    public void provideAmount(
        @NotNull ComponentEvent event
    ) {

        if (isNull(this.session.currentStructure)) {
            event.with()
                .ephemeral(true)
                .editReply(false)
                .keepComponents(false)
                .reply(
                    embedBuilder()
                        .setColor(RED)
                        .setDescription(
                            """
                            ### :warning: ‌ Error Occurred
                            You must select structure before providing amount.
                            """
                        )
                );
            return;
        }

        event.replyModal("provideAmountModal");

    }

    @Modal("Calculator")
    public void provideAmountModal(
        @NotNull ModalEvent event,
        @TextInput(value = "Amount", style = SHORT)
        @NotNull String amountString
    ) {

        try {

            StringBuilder stringBuilder = new StringBuilder();
            Integer amount = parseInt(amountString);

            this.session.rockets += (this.session.currentStructure.costs.rocket * amount);
            this.session.bombs += (this.session.currentStructure.costs.bomb * amount);
            this.session.explosiveAmmo += (this.session.currentStructure.costs.explosive * amount);
            this.session.satchels += (this.session.currentStructure.costs.satchel * amount);

            if (this.session.rockets > 0)
                stringBuilder.append("\n").append("\u200C \u200C \u200C").append("``").append(decimalFormat.format(this.session.rockets)).append("x").append("``")
                    .append(" ").append(customEmoji("rocket").getFormatted())
                    .append(" Rocket");

            if (this.session.bombs > 0)
                stringBuilder.append("\n").append("\u200C \u200C \u200C").append("``").append(decimalFormat.format(this.session.bombs)).append("x").append("``")
                    .append(" ").append(customEmoji("timed_explosive_charge").getFormatted())
                    .append(" Timed Explosive Charge");

            if (this.session.satchels > 0)
                stringBuilder.append("\n").append("\u200C \u200C \u200C").append("``").append(decimalFormat.format(this.session.satchels)).append("x").append("``")
                    .append(" ").append(customEmoji("satchel_explosive_charge").getFormatted())
                    .append(" Satchel Explosive Charge");

            if (this.session.explosiveAmmo > 0)
                stringBuilder.append("\n").append("\u200C \u200C \u200C").append("``").append(decimalFormat.format(this.session.explosiveAmmo)).append("x").append("``")
                    .append(" ").append(customEmoji("explosive_ammo").getFormatted())
                    .append(" Explosive Ammo");

            event.with()
                .ephemeral(true)
                .components(
                    stringSelect("explosiveSetMenu")
                        .modify((builder) -> builder.setDefaultValues(this.session.currentExplosivesSet.id))
                )
                .components(
                    stringSelect("structureMenu")
                        .modify((builder) -> builder.setDefaultValues(this.session.currentStructure.id))
                )
                .components("provideAmount")
                .reply(
                    embedBuilder()
                        .setColor(RED)
                        .setDescription(format(
                            """
                            ### :notepad_spiral: ‌ Raid Calculator
                            You need ``%sx`` %s Sulfur to raid that base, with that sulfur you should make: %s
                            ### :notebook_with_decorative_cover: ‌ Usage Guide
                            ``1.`` Select structure from the list below.
                            ``2.`` Click button and provide the amount of structures.
                            ``3.`` Done! You will see the cost of raiding the selected structure, you can also add more structures and you will see total cost.
                            """, decimalFormat.format(session.totalSulphurNeeded()), customEmoji("sulphur").getFormatted(), stringBuilder
                        ))
                );

        } catch (
            @NotNull Exception exception
        ) {
            event.with()
                .ephemeral(true)
                .editReply(false)
                .keepComponents(false)
                .reply(
                    embedBuilder()
                        .setColor(RED)
                        .setDescription(
                            """
                            ### :warning: ‌ Error Occurred
                            You must provide a number in the input.
                            """
                        )
                );
        }

    }

    private static final DecimalFormat decimalFormat = new DecimalFormat("#,###");

    private static final Gson gson = new Gson();
    private static final StructureExplosivesSet[] structureExplosivesSets = gson.fromJson(
        readResource("data/rust/calculator/structure_explosives_set.json"),
        StructureExplosivesSet[].class
    );

}
