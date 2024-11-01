package pl.mrstudios.essential.command.suggestion;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.suggester.Suggester;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static dev.rollczi.litecommands.suggestion.SuggestionResult.empty;
import static java.util.Arrays.asList;

public class StringArgumentSuggester implements Suggester<User, String> {

    @Override
    public @NotNull SuggestionResult suggest(
        @NotNull Invocation<User> invocation,
        @NotNull Argument<String> argument,
        @NotNull SuggestionContext context
    ) {

        SuggestionResult suggestionResult = empty();
        String original = context.getCurrent().firstLevel()
            .toLowerCase(),
            beforeLastDot = (original.lastIndexOf('.') == -1) ?
                original : original.substring(0, original.lastIndexOf('.')),
            afterLastDot = (original.lastIndexOf('.') == -1) ?
                original : original.substring(original.lastIndexOf('.'));

        if (context.getCurrent().firstLevel().isBlank())
            return suggestionResult;

        suggestionResult.add(context.getCurrent());
        this.domainTldCollection.stream()
            .filter((tld) -> tld.startsWith(afterLastDot))
            .map((tld) -> beforeLastDot + tld)
            .map(Suggestion::of)
            .forEach(suggestionResult::add);

        this.domainTldCollection.stream()
            .filter((tld) -> tld.startsWith(afterLastDot))
            .findFirst()
            .ifPresentOrElse(
                (tld) -> {}, () -> this.domainTldCollection.stream()
                    .map((tld) -> original + tld)
                    .map(Suggestion::of)
                    .forEach(suggestionResult::add)
            );

        return suggestionResult;
    }

    private final @NotNull Collection<String> domainTldCollection = asList(
        ".com", ".co", ".net", ".org", ".gg", ".uk", ".eu", ".de"
    );

}
