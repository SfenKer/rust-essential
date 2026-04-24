package com.github.sfenker.essential.utility;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import oshi.ffm.SystemInfo;

import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class SystemUtility {

    @SneakyThrows
    public static @NotNull Double cpuUsage() {

        var operatingSystem = systemInfo.getOperatingSystem();

        var snapNow = operatingSystem.getCurrentProcess();
        sleep(500);
        var snapAfter = operatingSystem.getCurrentProcess();

        return (snapAfter.getProcessCpuLoadBetweenTicks(snapNow) * 100) /
            systemInfo.getHardware()
                .getProcessor()
                .getLogicalProcessorCount();

    }

    public static @NotNull CompletableFuture<Double> cpuUsageAsync() {
        return supplyAsync(SystemUtility::cpuUsage);
    }

    @SneakyThrows
    public static @NotNull Long memoryUsage() {
        return systemInfo.getOperatingSystem().getCurrentProcess()
            .getResidentMemory() / (1024 * 1024);
    }

    static final SystemInfo systemInfo =
        new SystemInfo();

}
