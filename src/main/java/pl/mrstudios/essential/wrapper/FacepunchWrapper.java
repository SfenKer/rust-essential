package pl.mrstudios.essential.wrapper;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static java.nio.charset.StandardCharsets.UTF_8;
import static kong.unirest.core.Unirest.get;
import static pl.mrstudios.essential.utility.StreamUtility.byteArrayInputStream;

public class FacepunchWrapper {

    public static @NotNull Collection<SyndEntry> rssFeedEntries() throws Exception {
        return new SyndFeedInput()
            .build(new XmlReader(byteArrayInputStream(
                get(FEED_NEWS_URL)
                    .header("User-Agent", FEED_USER_AGENT)
                    .asString().getBody()
                    .replace("&lt;img src=\"", "")
                    .replace("\"&gt;&lt;br/&gt;", "|")
                    .getBytes(UTF_8)
            ))).getEntries();
    }

    private static final @NotNull String FEED_NEWS_URL = "https://rust.facepunch.com/rss/news";
    private static final @NotNull String FEED_USER_AGENT = "News Reader/1.0.0 (in: '{project}')";

}
