package pl.mrstudios.essential.command.result;

import dev.rollczi.litecommands.handler.result.ResultHandler;
import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invocation.Invocation;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import pl.mrstudios.essential.utility.EmbedResponseUtility;

public class EmbedResponseResult implements ResultHandler<User, EmbedResponseUtility> {

    @Override
    public void handle(
            @NotNull Invocation<User> invocation,
            @NotNull EmbedResponseUtility response,
            @NotNull ResultHandlerChain<User> resultChain
    ) {
        response.build();
    }

}
