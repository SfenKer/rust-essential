package com.github.sfenker.essential.utility;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent;
import net.dv8tion.jda.api.components.section.SectionContentComponent;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import static com.github.sfenker.essential.utility.StreamUtility.resourceStream;
import static net.dv8tion.jda.api.components.textdisplay.TextDisplay.ofFormat;
import static net.dv8tion.jda.api.utils.FileUpload.fromData;

public class DiscordUtility {

    /*
     * Currently it's not possible to use SelfUser#getAvatarUrl() in the thumbnail
     * because jda-commands have an issue with that and providing wrong json structure
     * without proxied url.
     */
    public static @NotNull FileUpload logoAsFileUpload() {
        return fromData(resourceStream("assets/image/logo.png"), "logo.png");
    }

    /*
     * Only for static imports.
     */
    public static @NotNull Section section(
        @NotNull SectionAccessoryComponent accessory,
        @NotNull SectionContentComponent component,
        @NotNull SectionContentComponent... components
    ) {
        return Section.of(accessory, component, components);
    }

    public static @NotNull TextDisplay textDisplay(
        @NotNull String string,
        @NotNull Object... format
    ) {
        return ofFormat(string, format);
    }

    public static @NotNull Container container(
        @NotNull ContainerChildComponent component,
        @NotNull ContainerChildComponent... components
    ) {
        return Container.of(component, components);
    }

    public static @NotNull ActionRow actionRow(
        @NotNull ActionRowChildComponent component,
        @NotNull ActionRowChildComponent... components
    ) {
        return ActionRow.of(component, components);
    }

    public static @NotNull SelectOption selectOption(
        @NotNull String label,
        @NotNull String value
    ) {
        return SelectOption.of(label, value);
    }

    public static @NotNull TextInput textInput(
        @NotNull String id,
        @NotNull TextInputStyle style
    ) {
        return TextInput.of(id, style);
    }

    public static @NotNull Label label(
        @NotNull String string,
        @NotNull LabelChildComponent labelChildComponent
    ) {
        return Label.of(string, labelChildComponent);
    }

}
