package pl.mrstudios.essential.command.internal;

import com.github.kaktushose.jda.commands.embeds.error.ErrorMessageFactory;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.kaktushose.jda.commands.definitions.interactions.command.OptionDataDefinition.ConstraintDefinition;
import static java.awt.Color.RED;
import static java.lang.String.format;
import static pl.mrstudios.essential.utility.EmbedUtility.embedBuilder;
import static pl.mrstudios.essential.utility.StringUtility.throwableToString;

public class ErrorMessageFactoryImpl implements ErrorMessageFactory {

    @Override
    public @NotNull MessageCreateData getTypeAdaptingFailedMessage(
        @NotNull ErrorContext context,
        @NotNull List<String> userInput
    ) {
        return new MessageCreateBuilder()
            .addEmbeds(
                embedBuilder()
                    .setColor(RED)
                    .setDescription(
                        """
                        ### :warning: ‌ Error Occurred
                        Invalid argument type was provided, please contact with support.
                        """
                    ).build()
            ).build();
    }

    @Override
    public @NotNull MessageCreateData getInsufficientPermissionsMessage(
        @NotNull ErrorContext context
    ) {
        return new MessageCreateBuilder()
            .addEmbeds(
                embedBuilder()
                    .setColor(RED)
                    .setDescription(
                        """
                        ### :warning: ‌ Error Occurred
                        You don't have enough permissions to execute that command.
                        """
                    ).build()
            ).build();
    }

    @Override
    public @NotNull MessageCreateData getConstraintFailedMessage(
        @NotNull ErrorContext context,
        @NotNull ConstraintDefinition constraint
    ) {
        return new MessageCreateBuilder()
            .addEmbeds(
                embedBuilder()
                    .setColor(RED)
                    .setDescription(
                        """
                        ### :warning: ‌ Error Occurred
                        Invalid parameter was provided, please contact with support.
                        """
                    ).build()
            ).build();
    }

    @Override
    public @NotNull MessageCreateData getCooldownMessage(
        @NotNull ErrorContext context,
        long ms
    ) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public @NotNull MessageCreateData getWrongChannelTypeMessage(
        @NotNull ErrorContext context
    ) {
        return new MessageCreateBuilder()
            .addEmbeds(
                embedBuilder()
                    .setColor(RED)
                    .setDescription(
                        """
                        ### :warning: ‌ Error Occurred
                        This command can't be executed on that channel.
                        """
                    ).build()
            ).build();
    }

    @Override
    public @NotNull MessageCreateData getCommandExecutionFailedMessage(
        @NotNull ErrorContext context,
        @NotNull Throwable exception
    ) {
        return new MessageCreateBuilder()
            .addEmbeds(
                embedBuilder()
                    .setColor(RED)
                    .setDescription(format(
                        """
                        ### :warning: ‌ Error Occurred
                        An unexpected exception was thrown while executing that interaction.
                        ### :scroll: ‌ Stacktrace
                        ```
                        %s
                        ```
                        """, throwableToString(exception)
                    )).build()
            ).build();
    }

    @Override
    public @NotNull MessageCreateData getTimedOutComponentMessage(
        @NotNull GenericInteractionCreateEvent event
    ) {
        return new MessageCreateBuilder()
            .addEmbeds(
                embedBuilder()
                    .setColor(RED)
                    .setDescription(
                        """
                        ### :warning: ‌ Error Occurred
                        This interaction expired, please create new interaction.
                        """
                    ).build()
            ).build();
    }

}
