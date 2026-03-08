package com.github.sfenker.essential.command.internal;

import io.github.kaktushose.jdac.embeds.error.ErrorMessageFactory;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;

import static com.github.sfenker.essential.builder.ComponentContainerBuilder.componentContainerBuilder;
import static com.github.sfenker.essential.utility.StringUtility.throwableToString;
import static io.github.kaktushose.proteus.conversion.ConversionResult.Failure;

public class ErrorMessageFactoryImpl implements ErrorMessageFactory {

    @Override
    public @NotNull MessageTopLevelComponent getTypeAdaptingFailedMessage(
        @NotNull ErrorContext context,
        @NotNull Failure<?> failure
    ) {
        return componentContainerBuilder()
            .textDisplay("### :warning: Error Occurred")
            .textDisplay("Invalid argument type was provided.")
            .build();
    }

    @Override
    public @NotNull MessageTopLevelComponent getInsufficientPermissionsMessage(
        @NotNull ErrorContext context
    ) {
        return componentContainerBuilder()
            .textDisplay("### :warning: Error Occurred")
            .textDisplay("You don't have permissions to execute that interaction.")
            .build();
    }

    @Override
    public @NotNull MessageTopLevelComponent getConstraintFailedMessage(
        @NotNull ErrorContext context,
        @NotNull String message
    ) {
        return componentContainerBuilder()
            .textDisplay("### :warning: Error Occurred")
            .textDisplay(message)
            .build();
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
        return componentContainerBuilder()
            .textDisplay("### :warning: Error Occurred")
            .textDisplay("This interaction has expired and can no longer be used.")
            .build();
    }

    public static @NotNull MessageTopLevelComponent exceptionMessageTemplate(
        @NotNull Throwable throwable
    ) {
        return componentContainerBuilder()
            .textDisplay("### :warning: Error Occurred")
            .textDisplay("An unexpected exception was thrown while executing that interaction.")
            .textDisplay("### :scroll: Stacktrace")
            .textDisplay(
                "```\n%s\n```",
                throwableToString(throwable)
            )
            .build();
    }

}
