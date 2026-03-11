package com.github.sfenker.essential.utility;

import org.jetbrains.annotations.NotNull;

import static java.net.InetAddress.getByName;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

public class NetworkUtility {

    public static @NotNull String resolveHost(
        @NotNull String addr
    ) {

        var split = addr.split(":");

        var host = tryResolveAddress(split[0]);
        var port = (split.length > 1) ?
            toInt(split[1], -1) : -1;

        return host + ((port != -1) ?
            (":" + port) : "");

    }

    public static @NotNull String tryResolveAddress(
        @NotNull String address
    ) {
        return resolveAddressOrElse(address, address);
    }

    public static @NotNull String resolveAddressOrElse(
        @NotNull String address,
        @NotNull String defaultValue
    ) {
        try {
            return getByName(address)
                .getHostAddress();
        } catch (Exception exception) {
            return defaultValue;
        }
    }

}
