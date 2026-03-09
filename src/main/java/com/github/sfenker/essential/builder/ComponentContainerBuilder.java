package com.github.sfenker.essential.builder;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.join;

public class ComponentContainerBuilder {

    final Collection<ContainerChildComponent> components =
        newArrayList();

    public @NotNull ComponentContainerBuilder section(
        @NotNull Thumbnail thumbnail,
        @NotNull String title,
        @NotNull String description
    ) {
        this.components.add(Section.of(
            thumbnail,
            TextDisplay.of(title),
            TextDisplay.of(description)
        ));
        return this;
    }

    public @NotNull ComponentContainerBuilder textDisplay(
        @NotNull String string,
        @NotNull Object... format
    ) {
        this.components.add(TextDisplay.of(string.formatted(format)));
        return this;
    }

    public @NotNull ComponentContainerBuilder textDisplayList(
        @NotNull String... string
    ) {
        return textDisplay(join(string, "\n"));
    }

    public @NotNull ComponentContainerBuilder gallery(
        @NotNull String... url
    ) {
        this.components.add(
            MediaGallery.of(
                stream(url)
                    .map(MediaGalleryItem::fromUrl)
                    .toList()
            )
        ); return this;
    }

    public @NotNull ComponentContainerBuilder actionRow(
        @NotNull ActionRowChildComponent... components
    ) {
        this.components.add(
            ActionRow.of(
                stream(components)
                    .toList()
            )
        ); return this;
    }

    public @NotNull Collection<ContainerChildComponent> componentCollection() {
        return this.components;
    }

    public @NotNull Container build() {
        return Container.of(this.components);
    }

    public static @NotNull ComponentContainerBuilder componentContainerBuilder() {
        return new ComponentContainerBuilder();
    }

}
