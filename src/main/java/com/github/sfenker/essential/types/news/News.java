package com.github.sfenker.essential.types.news;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.NodeList;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.regex.Pattern.compile;

public class News {

    public String title;
    public String description;
    public String thumbnail;
    public String url;

    public static @NotNull News parseFromXml(
        @NotNull NodeList node
    ) {

        var news = new News();

        news.url = node.item(1).getTextContent();
        news.title = node.item(2).getTextContent();

        var description = node.item(3)
            .getTextContent();

        var matcher = DESCRIPTION_PATTERN.matcher(description);

        checkArgument(
            matcher.matches(),
            "Invalid News Decription: %s",
            description
        );

        news.thumbnail = matcher.group(1);
        news.description = matcher.group(2);

        return news;

    }

    static final Pattern DESCRIPTION_PATTERN =
        compile("(?s)<img src=\"([^\"]+)\"><br/>(.*)");

}
