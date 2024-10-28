package pl.mrstudios.essential.listener;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class UserInteractionListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(
        @NotNull ButtonInteractionEvent event
    ) {
        buttonInteractions.keySet()
            .stream().filter(event.getComponentId()::equals)
            .map(buttonInteractions::get).forEach(
                (consumer) -> consumer.accept(event.getUser(), event)
            );
    }

    @Override
    public void onStringSelectInteraction(
        @NotNull StringSelectInteractionEvent event
    ) {
        menuInteractions.keySet()
            .stream().filter(event.getComponentId()::equals)
            .map(menuInteractions::get).forEach(
                (consumer) -> consumer.accept(event.getUser(), event)
            );
    }

    @Override
    public void onModalInteraction(
        @NotNull ModalInteractionEvent event
    ) {
        modalInteractions.keySet()
            .stream().filter(event.getModalId()::equals)
            .map(modalInteractions::get).forEach(
                (consumer) -> consumer.accept(event.getUser(), event)
            );
    }

    public final static Map<String, BiConsumer<User, ModalInteractionEvent>> modalInteractions = new ConcurrentHashMap<>();
    public final static Map<String, BiConsumer<User, ButtonInteractionEvent>> buttonInteractions = new ConcurrentHashMap<>();
    public final static Map<String, BiConsumer<User, StringSelectInteractionEvent>> menuInteractions = new ConcurrentHashMap<>();

}
