package pl.mrstudios.essential.utility.builder;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

import static net.dv8tion.jda.api.interactions.components.ActionRow.partitionOf;
import static net.dv8tion.jda.api.interactions.modals.Modal.Builder;
import static net.dv8tion.jda.api.interactions.modals.Modal.create;
import static pl.mrstudios.essential.listener.UserInteractionListener.modalInteractions;

public class ModalResponseBuilder {

    private final Builder modalBuilder;
    private final ComponentInteraction componentInteraction;

    public ModalResponseBuilder(
        @NotNull ComponentInteraction componentInteraction
    ) {
        this.componentInteraction = componentInteraction;
        this.modalBuilder = create("a", "b");
    }

    public @NotNull ModalResponseBuilder id(
        @NotNull String id
    ) {
        this.modalBuilder.setId(id);
        return this;
    }

    public @NotNull ModalResponseBuilder title(
        @NotNull String title
    ) {
        this.modalBuilder.setTitle(title);
        return this;
    }

    public @NotNull ModalResponseBuilder handler(
        @NotNull BiConsumer<User, ModalInteractionEvent> consumer
    ) {
        modalInteractions.put(this.modalBuilder.getId(), consumer);
        return this;
    }

    public @NotNull ModalResponseBuilder components(
        @NotNull TextInput... inputs
    ) {
        this.modalBuilder.addComponents(partitionOf(inputs));
        return this;
    }

    public void build() {
        this.componentInteraction.replyModal(this.modalBuilder.build()).queue();
    }

    public static @NotNull ModalResponseBuilder modalResponseBuilder(
        @NotNull ComponentInteraction event
    ) {
        return new ModalResponseBuilder(event);
    }

}
