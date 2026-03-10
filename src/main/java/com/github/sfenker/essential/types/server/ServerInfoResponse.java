package com.github.sfenker.essential.types.server;

import java.time.Duration;

public class ServerInfoResponse {

    public Long id;

    public String name;
    public String url;
    public String description;

    public Integer players;
    public Integer maxPlayers;

    public Duration uptime;

    public String mapName;
    public String mapThumbnail;

}
