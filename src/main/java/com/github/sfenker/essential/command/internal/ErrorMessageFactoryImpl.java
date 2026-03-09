package com.github.sfenker.essential.command.internal;

import io.github.kaktushose.jdac.embeds.error.ErrorMessageFactory;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;

import static com.github.sfenker.essential.utility.DiscordUtility.container;
import static com.github.sfenker.essential.utility.DiscordUtility.textDisplay;
import static com.github.sfenker.essential.utility.StringUtility.throwableToString;
import static io.github.kaktushose.proteus.conversion.ConversionResult.Failure;

public class ErrorMessageFactoryImpl implements ErrorMessageFactory {

    @Override
    public @NotNull MessageTopLevelComponent getTypeAdaptingFailedMessage(
        @NotNull ErrorContext context,
        @NotNull Failure<?> failure
    ) {
        return container(
            textDisplay(
                """
                ### :warning: Error Occurred
                An error occurred while converting the interaction's arguments.
                """
            )
        );
    }

    @Override
    public @NotNull MessageTopLevelComponent getInsufficientPermissionsMessage(
        @NotNull ErrorContext context
    ) {
        return container(
            textDisplay(
                """
                ### :warning: Error Occurred
                You don't have permission to execute that interaction.
                """
            )
        );
    }

    @Override
    public @NotNull MessageTopLevelComponent getConstraintFailedMessage(
        @NotNull ErrorContext context,
        @NotNull String message
    ) {
        return container(
            textDisplay(
                """
                ### :warning: Error Occurred
                %s
                """, message
            )
        );
    }

    @Override
    public @NotNull MessageTopLevelComponent getInteractionExecutionFailedMessage(
        @NotNull ErrorContext context,
        @NotNull Throwable throwable
    ) {
        return exceptionMessageTemplate(throwable);
    }

    @Override
    public @NotNull MessageTopLevelComponent getTimedOutComponentMessage(
        @NotNull GenericInteractionCreateEvent event
    ) {
        return container(
            textDisplay(
                """
                ### :warning: Error Occurred
                That interaction has expired and can no longer be used.
                """
            )
        );
    }

    public static @NotNull MessageTopLevelComponent exceptionMessageTemplate(
        @NotNull Throwable throwable
    ) {
        return container(
            textDisplay(
                """
                ### :warning: Error Occurred
                An unexpected exception was thrown while executing that interaction.
                """
            ),
                textDisplay(
                    """
                    ### :scroll: Stacktrace
                    ```
                    %s
                    ```
                    """, throwableToString(throwable)
                )
        );
    }

}
