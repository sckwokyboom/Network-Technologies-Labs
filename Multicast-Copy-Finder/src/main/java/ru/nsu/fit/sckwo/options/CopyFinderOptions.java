package ru.nsu.fit.sckwo.options;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

public record CopyFinderOptions(@NotNull InetAddress ip, @NotNull Mode mode, int port, @NotNull String securityKey) {
}
