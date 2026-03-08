package com.github.sfenker.essential.wrapper;

import kong.unirest.core.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static kong.unirest.core.Unirest.get;

public class RustMapsWrapper {

    public static @Nullable String mapImage(
        @NotNull Integer size,
        @NotNull Long seed
    ) {
        return ofNullable(
            get(format(API_ENDPOINT_MAP_BY_SEED, size, seed))
                .header("X-API-Key", API_KEY)
                .header("User-Agent", API_USER_AGENT)
                .asJson().getBody()
        ).map(JsonNode::getObject)

            /* Status Code */
            .filter((object) -> object.getJSONObject("meta").getInt("statusCode") == 200)

            /* Data */
            .filter((object) -> object.has("data"))
            .map((object) -> object.getJSONObject("data"))

            /* Image */
            .filter((data) -> data.has("imageUrl"))
            .map((data) -> data.getString("imageUrl"))

            /* Or Else */
            .orElse(null);
    }

    private static @NotNull String API_KEY = "ENTER_API_KEY_HERE";
    private static final @NotNull String API_BASE_URL = "https://api.rustmaps.com/v4/";
    private static final @NotNull String API_USER_AGENT = "Rust Maps API Wrapper/1.0.0 (in: '{project}')";

    private static final @NotNull String API_ENDPOINT_MAP_BY_SEED = API_BASE_URL + "maps/%d/%d?staging=false";

    public static void provideRustMapsApiKey(
        @NotNull String apiKey
    ) {
        API_KEY = apiKey;
    }

}
