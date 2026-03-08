package com.github.sfenker.essential.wrapper;

import kong.unirest.core.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static kong.unirest.core.Unirest.get;

public class RustMapsWrapper {

    public static @Nullable String mapImage(
        @NotNull Integer size,
        @NotNull Long seed
    ) {
        return get(format(API_ENDPOINT_MAP_BY_SEED, size, seed))
            .header("X-API-Key", getenv("RUST_MAPS_API_KEY"))
            .header("User-Agent", API_USER_AGENT)
            .asJson()
            .map(JsonNode::getObject)
            .map(
                (object) ->
                    object.getJSONObject("data")
            )
            .mapBody(
                (data) ->
                    data.getString("imageUrl")
            );
    }

    public static @NotNull CompletableFuture<String> mapImageAsync(
        @NotNull Integer size,
        @NotNull Long seed
    ) {
        return supplyAsync(() -> mapImage(size, seed));
    }

    static final @NotNull String API_BASE_URL = "https://api.rustmaps.com/v4/";
    static final @NotNull String API_USER_AGENT = "Rust Maps API Wrapper/1.0.0 (in: '{project}')";

    static final @NotNull String API_ENDPOINT_MAP_BY_SEED =
        API_BASE_URL + "maps/%d/%d?staging=false";

}
