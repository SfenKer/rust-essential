package pl.mrstudios.essential.command.result;

import dev.rollczi.litecommands.handler.result.ResultHandler;
import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invocation.Invocation;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.builder.EmbedResponseBuilder;

public class EmbedResponseResult implements ResultHandler<User, EmbedResponseBuilder> {

    @Override
    public void handle(
        @NotNull Invocation<User> invocation,
        @NotNull EmbedResponseBuilder response,
        @NotNull ResultHandlerChain<User> resultChain
    ) {

        invocation.context()
            .get(SlashCommandInteractionEvent.class)
            .map(response::applyEvent)
            .ifPresent(EmbedResponseBuilder::build);

    }

}
