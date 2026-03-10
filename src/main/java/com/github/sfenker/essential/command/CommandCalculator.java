package com.github.sfenker.essential.command;

import com.github.sfenker.essential.types.calculator.CalculatorSession;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ComponentEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ModalEvent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.sfenker.essential.Constants.decimalFormat;
import static com.github.sfenker.essential.registry.CustomEmojiRegistry.customEmoji;
import static com.github.sfenker.essential.registry.StructureExplosiveSetRegistry.structureExplosivesSetRegistry;
import static com.github.sfenker.essential.utility.DiscordUtility.*;
import static io.github.kaktushose.jdac.dispatching.reply.Component.button;
import static io.github.kaktushose.jdac.dispatching.reply.Component.stringSelect;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static net.dv8tion.jda.api.components.buttons.ButtonStyle.SUCCESS;
import static net.dv8tion.jda.api.components.replacer.ComponentReplacer.byUniqueId;
import static net.dv8tion.jda.api.components.selections.StringSelectMenu.create;
import static net.dv8tion.jda.api.components.textinput.TextInputStyle.SHORT;
import static net.dv8tion.jda.api.components.thumbnail.Thumbnail.fromFile;
import static net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL;
import static net.dv8tion.jda.api.interactions.IntegrationType.USER_INSTALL;

@Interaction
@SuppressWarnings("ConstantConditions")
public class CommandCalculator {

    final CalculatorSession session =
        new CalculatorSession();

    @CommandConfig(integration = { GUILD_INSTALL, USER_INSTALL })
    @Command(value = "calculator", desc = "Calculator of Raid Cost.")
    public void executeCommand(
        @NotNull CommandEvent event
    ) {
        event.with()
            .ephemeral(true)
            .reply(container(
                section(
                    fromFile(logoAsFileUpload()),
                    textDisplay(
                        """
                        ### :wave: Welcome in Raid Cost Calculator!
                        Using this tool, you can easily calculate the cost of raiding a base in Rust.
                        """
                    ).withUniqueId(HEADER_COMPONENT_ID),
                    textDisplay(
                        """
                        ### :notebook_with_decorative_cover: Usage Guide
                        ``1.`` Select explosives set for calculations.
                        ``2.`` Select structure from the list below.
                        ``3.`` Click button and provide the amount of structures.
                        ``4.`` **Done!** You will see the cost of raiding the selected structure, you can also add more structures and you will see total cost.
                        """
                    )
                ),
                actionRow(
                    stringSelect("selectExplosivesStringMenu")
                        .modify(
                            (builder) ->
                                create(builder.getCustomId())
                                    .setUniqueId(EXPLOSIVES_COMPONENT_ID)
                                    .setMaxValues(builder.getMaxValues())
                                    .setPlaceholder(builder.getPlaceholder())
                                    .addOptions(
                                        structureExplosivesSetRegistry()
                                            .stream()
                                            .map(
                                                (set) ->
                                                    selectOption(set.name, set.id)
                                                        .withEmoji(customEmoji("rust", set.id))
                                            )
                                            .toList()
                                    )
                        )
                ),
                actionRow(
                    stringSelect("selectStructureStringMenu")
                        .modify(
                            (builder) ->
                                create(builder.getCustomId())
                                    .setUniqueId(STRUCTURE_COMPONENT_ID)
                                    .setMaxValues(builder.getMaxValues())
                                    .setPlaceholder(builder.getPlaceholder())
                                    .setDisabled(true)
                                    .addOptions(
                                        structureExplosivesSetRegistry()
                                            .stream()
                                            .limit(1)
                                            .flatMap(
                                                (set) ->
                                                    stream(set.structures)
                                            )
                                            .map(
                                                (structure) ->
                                                    selectOption(structure.name, structure.id)
                                                        .withEmoji(customEmoji("rust", structure.id))
                                            )
                                            .toList()
                                    )
                        )
                ),
                actionRow(
                    button("provideAmountButton")
                        .modify(
                            (builder) ->
                                builder.withDisabled(this.session.currentStructure == null)
                                    .withUniqueId(PROVIDE_AMOUNT_COMPONENT_ID)
                        )
                )
            ));
    }

    @MenuOption(label = "null", value = "null")
    @StringMenu("Select Explosive Set")
    public void selectExplosivesStringMenu(
        @NotNull ComponentEvent event,
        @NotNull List<String> choices
    ) {

        this.session.currentExplosivesSet =
            structureExplosivesSetRegistry()
                .stream()
                .filter(
                    (set) ->
                        choices.getFirst()
                            .equals(set.id)
                )
                .findFirst()
                .orElseThrow();

        ofNullable(this.session.currentStructure)
            .ifPresent(
                (currentStructure) ->
                    this.session.currentStructure =
                        stream(this.session.currentExplosivesSet.structures)
                            .filter(
                                (structure) ->
                                    structure.id.equals(currentStructure.id)
                            )
                            .findFirst()
                            .orElseThrow()
            );

        event.jdaEvent()
            .editComponents(
                event.jdaEvent()
                    .getMessage()
                    .getComponentTree()
                    .replace(byUniqueId(
                        EXPLOSIVES_COMPONENT_ID, (component) ->
                            ((StringSelectMenu) component).createCopy()
                                .setDefaultValues(this.session.currentExplosivesSet.id)
                                .build()
                    ))
                    .replace(byUniqueId(
                        STRUCTURE_COMPONENT_ID, (component) ->
                            ((StringSelectMenu) component).createCopy()
                                .setDefaultValues(
                                    ofNullable(this.session.currentStructure)
                                        .map(
                                            (structure) ->
                                                structure.id
                                        )
                                        .map(List::of)
                                        .orElse(emptyList())
                                )
                                .setDisabled(this.session.currentExplosivesSet == null)
                                .build()
                    ))
            )
            .useComponentsV2()
            .queue();

    }

    @MenuOption(label = "null", value = "null")
    @StringMenu("Select Structure")
    public void selectStructureStringMenu(
        @NotNull ComponentEvent event,
        @NotNull List<String> choices
    ) {

        ofNullable(this.session.currentExplosivesSet)
            .ifPresent(
                (_) ->
                    this.session.currentStructure =
                        stream(this.session.currentExplosivesSet.structures)
                            .filter(
                                (structure) ->
                                    choices.getFirst()
                                        .equals(structure.id)
                            )
                            .findFirst()
                            .orElseThrow()
            );

        event.jdaEvent()
            .editComponents(
                event.jdaEvent()
                    .getMessage()
                    .getComponentTree()
                    .replace(byUniqueId(
                        STRUCTURE_COMPONENT_ID, (component) ->
                            ((StringSelectMenu) component).createCopy()
                                .setDefaultValues(
                                    ofNullable(this.session.currentStructure)
                                        .map(
                                            (structure) ->
                                                structure.id
                                        )
                                        .map(List::of)
                                        .orElse(emptyList())
                                )
                                .build()
                    ))
                    .replace(byUniqueId(
                        PROVIDE_AMOUNT_COMPONENT_ID, (component) ->
                            ((net.dv8tion.jda.api.components.buttons.Button) component)
                                .withDisabled(this.session.currentStructure == null)
                    ))
            )
            .useComponentsV2()
            .queue();

    }

    @Button(value = "Provide Amount", style = SUCCESS)
    public void provideAmountButton(
        @NotNull ComponentEvent event
    ) {

        if (isNull(this.session.currentStructure)) {
            event.with()
                .ephemeral(true)
                .reply(container(
                    textDisplay(
                        """
                        ### :warning: Error Occurred
                        You must select structure before providing amount.
                        """
                    )
                ));
            return;
        }

        event.replyModal(
            "provideAmountModal",
            Label.of(
                "Amount",
                TextInput.of("amount", SHORT)
            )
        );

    }

    @Modal("Calculator")
    public void provideAmountModal(
        @NotNull ModalEvent event
    ) {

        try {

            var stringBuilder = new StringBuilder();
            var amount = parseInt(
                event.value("amount")
                    .getAsString()
            );

            this.session.rockets +=
                (this.session.currentStructure.costs.rocket * amount);

            this.session.bombs +=
                (this.session.currentStructure.costs.bomb * amount);

            this.session.explosiveAmmo +=
                (this.session.currentStructure.costs.explosive * amount);

            this.session.satchels +=
                (this.session.currentStructure.costs.satchel * amount);

            final var fmt = "\u200C \u200C \u200C``%sx`` %s %s\n";
            if (this.session.rockets > 0)
                stringBuilder.append(fmt.formatted(
                    decimalFormat.format(this.session.rockets),
                    customEmoji("rust", "rocket")
                        .getFormatted(), "Rocket"
                ));

            if (this.session.bombs > 0)
                stringBuilder.append(fmt.formatted(
                    decimalFormat.format(this.session.bombs),
                    customEmoji("rust", "timed_explosive_charge")
                        .getFormatted(), "Timed Explosive Charge"
                ));

            if (this.session.satchels > 0)
                stringBuilder.append(fmt.formatted(
                    decimalFormat.format(this.session.satchels),
                    customEmoji("rust", "satchel_explosive_charge")
                        .getFormatted(), "Satchel Explosive Charge"
                ));

            if (this.session.explosiveAmmo > 0)
                stringBuilder.append(fmt.formatted(
                    decimalFormat.format(this.session.explosiveAmmo),
                    customEmoji("rust", "explosive_ammo")
                        .getFormatted(), "Explosive Ammo"
                ));

            event.jdaEvent()
                .editComponents(
                    event.jdaEvent()
                        .getMessage()
                        .getComponentTree()
                        .replace(byUniqueId(
                            HEADER_COMPONENT_ID,
                            textDisplay(
                                """
                                ### :notepad_spiral: Raid Calculator
                                You need ``%sx`` %s Sulfur to raid that base, with that sulfur you should make:
                                %s\u200C
                                """,
                                decimalFormat.format(session.totalSulphurNeeded()),
                                customEmoji("rust", "sulphur")
                                    .getFormatted(), stringBuilder
                            ).withUniqueId(HEADER_COMPONENT_ID)
                        ))
                        .replace(byUniqueId(
                            EXPLOSIVES_COMPONENT_ID, (component) ->
                                ((StringSelectMenu) component).createCopy()
                                    .setDefaultValues(this.session.currentExplosivesSet.id)
                                    .build()
                        ))
                        .replace(byUniqueId(
                            STRUCTURE_COMPONENT_ID, (component) ->
                                ((StringSelectMenu) component).createCopy()
                                    .setDefaultValues(this.session.currentStructure.id)
                                    .build()
                        ))
                )
                .useComponentsV2()
                .queue();

        } catch (Exception exception) {
            event.with()
                .ephemeral(true)
                .reply(container(
                    textDisplay(
                        """
                        ### :warning: Error Occurred
                        Please provide valid amount of structures that will be an ``Integer``.
                        """
                    )
                ));
        }

    }

    static final Integer HEADER_COMPONENT_ID = 69;
    static final Integer STRUCTURE_COMPONENT_ID = 420;
    static final Integer EXPLOSIVES_COMPONENT_ID = 2137;
    static final Integer PROVIDE_AMOUNT_COMPONENT_ID = 4096;

}
