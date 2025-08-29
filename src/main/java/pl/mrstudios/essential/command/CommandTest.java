package pl.mrstudios.essential.command;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.CommandConfig;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import org.jetbrains.annotations.NotNull;

import static net.dv8tion.jda.api.interactions.IntegrationType.GUILD_INSTALL;
import static net.dv8tion.jda.api.interactions.IntegrationType.USER_INSTALL;

@Interaction
public class CommandTest {

    @CommandConfig(integration = { GUILD_INSTALL, USER_INSTALL })
    @Command("test")
    public void executeDefault(
        @NotNull CommandEvent event
    ) {
        throw new RuntimeException("test");
    }

}

