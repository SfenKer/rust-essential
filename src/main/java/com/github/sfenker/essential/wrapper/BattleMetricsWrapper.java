package com.github.sfenker.essential.wrapper;

import kong.unirest.core.JsonNode;
import kong.unirest.core.json.JSONObject;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.lang.System.getenv;
import static kong.unirest.core.HeaderNames.*;
import static kong.unirest.core.MimeTypes.JSON;
import static kong.unirest.core.Unirest.get;

public class BattleMetricsWrapper {

    public static @NotNull CompletableFuture<JSONObject> serverInfo(
        @NotNull Map<String, Object> queryParameters
    ) {
        return get(BMW_API_ENDPOINT_SERVERS)
            .header(AUTHORIZATION, format("Bearer %s", getenv("BATTLEMETRICS_API_KEY")))
            .header(ACCEPT, JSON)
            .header(USER_AGENT, BMW_API_USER_AGENT)
            .queryString(queryParameters)
            .asJsonAsync()
            .thenApply(
                (response) ->
                    response.mapBody(JsonNode::getObject)
            );
    }

    static final @NotNull String BMW_API_BASE_URL = "https://api.battlemetrics.com/";
    static final @NotNull String BMW_API_USER_AGENT = "Battle Metrics API Wrapper/1.0.0 (in: 'rust-essential')";

    static final @NotNull String BMW_API_ENDPOINT_SERVERS =
        BMW_API_BASE_URL + "servers";

}
