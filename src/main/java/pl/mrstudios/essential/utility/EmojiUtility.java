package pl.mrstudios.essential.utility;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import kotlin.Pair;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static java.lang.String.format;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static java.util.Optional.ofNullable;
import static net.dv8tion.jda.api.entities.emoji.Emoji.fromCustom;
import static pl.mrstudios.essential.utility.StreamUtility.readResource;

@SuppressWarnings("unchecked")
public class EmojiUtility {

    public static @NotNull Emoji customEmoji(
            @NotNull String name
    ) {
        return ofNullable(customEmoji.get(format("rust_%s", name)))
                .orElseThrow();
    }

    protected static final Gson gson = new Gson();
    protected static final Map<String, Emoji> customEmoji = ofEntries(
            gson.fromJson(readResource("data/general/custom_emoji.json"), JsonArray.class)
                    .asList().stream()
                    .map(JsonElement::getAsJsonObject)
                    .map((object) -> new Pair<>(
                            object.get("identifier").getAsLong(),
                            object.get("name").getAsString()
                    )).map((pair) -> entry(pair.getSecond(), fromCustom(pair.getSecond(), pair.getFirst(), false)))
                    .toArray(Map.Entry[]::new)
    );

}
