package pl.mrstudios.essential.wrapper;

import kong.unirest.core.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static kong.unirest.core.Unirest.get;

public class RustMapsAPI {

    public static @Nullable String mapImage(
            @NotNull Integer size,
            @NotNull Long seed
    ) {
        return ofNullable(
                get(format(API_ENDPOINT_MAP_BY_SEED, size, seed))
                        .header("X-API-Key", API_KEY)
                        .header("User-Agent", "Rust Maps API Wrapper/1.0.0 (in: '{project}')")
                        .asJson().getBody()
        ).map(JsonNode::getObject)

                /* Data */
                .filter((object) -> object.has("data"))
                .map((object) -> object.getJSONObject("data"))

                /* Image */
                .filter((data) -> data.has("imageUrl"))
                .map((data) -> data.getString("imageUrl"))

                /* Or Else */
                .orElse(null);
    }

    protected static final String API_BASE_URL = "https://api.rustmaps.com/v4/";
    protected static final String API_ENDPOINT_MAP_BY_SEED = API_BASE_URL + "maps/%d/%d?staging=false";

    protected static String API_KEY = "ENTER_API_KEY_HERE";

    public static void provideRustMapsApiKey(
            @NotNull String apiKey
    ) {
        API_KEY = apiKey;
    }

}
