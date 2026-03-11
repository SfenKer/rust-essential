package com.github.sfenker.essential.service.server;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.sfenker.essential.types.server.ServerInfoResponse;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static com.github.sfenker.essential.utility.NetworkUtility.resolveHost;
import static com.github.sfenker.essential.wrapper.BattleMetricsWrapper.serverInfo;
import static com.google.common.collect.ImmutableMap.of;
import static java.lang.Long.parseLong;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class ServerInfoService {

    final AsyncCache<String, ServerInfoResponse> serverInfoResponseCache =
        newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(ofMinutes(15))
            .buildAsync();

    public @NotNull CompletableFuture<ServerInfoResponse> queryServerInfo(
        @NotNull String address
    ) {
        return supplyAsync(() -> resolveHost(address))
            .thenCompose(
                (resolvedAddress) ->
                    this.serverInfoResponseCache.get(
                        resolvedAddress, (string, _) ->
                            supplyServerInfo(string)
                    )
            );
    }

    @NotNull CompletableFuture<ServerInfoResponse> supplyServerInfo(
        @NotNull String address
    ) {
        return serverInfo(this.battleMetricsQueryParams.apply(address))
            .thenApply((object) -> {

                var dataObject =
                    object.getJSONArray("data")
                        .getJSONObject(0)
                        .getJSONObject("attributes");

                var detailsObject =
                    dataObject.getJSONObject("details");

                var mapObject =
                    (detailsObject.has("rust_maps")) ?
                        detailsObject.getJSONObject("rust_maps") : null;

                var response = new ServerInfoResponse();

                response.id = parseLong(dataObject.getString("id"));

                response.name = dataObject.getString("name");
                response.description = detailsObject.getString("rust_description")
                    .replace("\\n", "\n")
                    .replace("\\t", "\t");
                response.url = detailsObject.getString("rust_url");

                response.players = dataObject.getInt("players");
                response.maxPlayers = dataObject.getInt("maxPlayers");

                response.uptime = ofSeconds(detailsObject.getInt("rust_uptime"));

                response.mapName = detailsObject.getString("map");
                response.mapThumbnail = ofNullable(mapObject)
                    .map(
                        (jsonObject) ->
                            jsonObject.getString("thumbnailUrl")
                    )
                    .orElse(null);

                return response;

            });
    }

    final Function<String, Map<String, Object>> battleMetricsQueryParams =
        (address) -> of(
            "filter[game]", "rust",
            "filter[status]", "online",
            "filter[search]", address
        );

}
