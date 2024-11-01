package pl.mrstudios.essential.utility.builder;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static net.dv8tion.jda.api.interactions.components.ActionRow.partitionOf;
import static pl.mrstudios.essential.listener.UserInteractionListener.buttonInteractions;
import static pl.mrstudios.essential.listener.UserInteractionListener.menuInteractions;

@SuppressWarnings("RegExpDuplicateCharacterInClass")
public class EmbedResponseBuilder {

    private ModalInteraction modalInteraction;
    private CommandInteraction commandInteraction;
    private ComponentInteraction componentInteraction;

    private boolean edit;
    private boolean ephemeral;
    private boolean editComponents;

    private final EmbedBuilder embedBuilder;
    private final Collection<ActionComponent> components;

    private EmbedResponseBuilder() {

        /* Default Values */
        this.edit = false;
        this.ephemeral = false;
        this.editComponents = false;
        this.components = new ArrayList<>();
        this.embedBuilder = new EmbedBuilder();

    }

    public @NotNull EmbedResponseBuilder embed(
        @NotNull Consumer<EmbedBuilder> consumer
    ) {
        consumer.accept(this.embedBuilder);
        this.embedBuilder.setDescription(
            this.embedBuilder.getDescriptionBuilder().toString()
                .replaceAll("(?m)^\\s*$[\n\n]+", "")
        );
        return this;
    }

    public @NotNull EmbedResponseBuilder component(
        @NotNull Button component,
        @NotNull BiConsumer<User, ButtonInteractionEvent> consumer
    ) {
        this.components.add(component);
        buttonInteractions.put(component.getId(), consumer);
        return this;
    }

    public @NotNull EmbedResponseBuilder component(
        @NotNull StringSelectMenu component,
        @NotNull BiConsumer<User, StringSelectInteractionEvent> consumer
    ) {
        this.components.add(component);
        menuInteractions.put(component.getId(), consumer);
        return this;
    }

    public @NotNull EmbedResponseBuilder ephemeral() {
        this.ephemeral = !this.ephemeral;
        return this;
    }

    public @NotNull EmbedResponseBuilder deferEdit() {
        this.edit = !this.edit;
        return this;
    }

    public @NotNull EmbedResponseBuilder editComponents() {
        this.editComponents = !this.editComponents;
        return this;
    }

    public @NotNull EmbedResponseBuilder applyEvent(
        @NotNull CommandInteraction event
    ) {
        this.commandInteraction = event;
        return this;
    }

    public @NotNull EmbedResponseBuilder applyEvent(
        @NotNull ComponentInteraction event
    ) {
        this.componentInteraction = event;
        return this;
    }

    public @NotNull EmbedResponseBuilder applyEvent(
        @NotNull ModalInteraction event
    ) {
        this.modalInteraction = event;
        return this;
    }

    public void build() {

        if (!isNull(this.commandInteraction))
            this.commandInteraction.deferReply(this.ephemeral)
                .setEmbeds(this.embedBuilder.build())
                .setComponents(partitionOf(this.components))
                .queue();

        /* Component Interaction */
        else if (!isNull(this.componentInteraction) && this.edit)
            this.componentInteraction.deferEdit()
                .setEmbeds(this.embedBuilder.build())
                .setComponents(
                    (this.editComponents) ? partitionOf(this.components) : this.componentInteraction.getMessage()
                        .getComponents()
                ).queue();

        else if (!isNull(this.componentInteraction))
            this.componentInteraction.deferReply(this.ephemeral)
                .setEmbeds(this.embedBuilder.build())
                .setComponents(
                    (this.editComponents) ? partitionOf(this.components) : this.componentInteraction.getMessage()
                        .getComponents()
                ).queue();

        /* Modal Interaction */
        else if (!isNull(this.modalInteraction) && this.edit)
            this.modalInteraction.deferEdit()
                .setEmbeds(this.embedBuilder.build())
                .setComponents(
                    (this.editComponents) ? partitionOf(this.components) : requireNonNull(this.modalInteraction.getMessage())
                        .getComponents()
                ).queue();

        else if (!isNull(this.modalInteraction))
            this.modalInteraction.deferReply(this.ephemeral)
                .setEmbeds(this.embedBuilder.build())
                .queue();

    }

    public static @NotNull EmbedResponseBuilder embedResponse() {
        return new EmbedResponseBuilder();
    }

}
