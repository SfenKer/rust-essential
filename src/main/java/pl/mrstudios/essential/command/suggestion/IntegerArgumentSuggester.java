package pl.mrstudios.essential.command.suggestion;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.suggester.Suggester;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import static dev.rollczi.litecommands.suggestion.SuggestionResult.from;

public class IntegerArgumentSuggester implements Suggester<User, Integer> {

    @Override
    public @NotNull SuggestionResult suggest(
        @NotNull Invocation<User> invocation,
        @NotNull Argument<Integer> argument,
        @NotNull SuggestionContext context
    ) {
        return from(context.getCurrent());
    }

}
