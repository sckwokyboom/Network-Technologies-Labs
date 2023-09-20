package ru.nsu.fit.sckwo.core;

import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.exceptions.CopyFinderRuntimeException;
import ru.nsu.fit.sckwo.options.CopyFinderOptions;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Multicast {
    private final static int PUBLISH_PERIOD = 100;
    private final static int RECEIVE_CHECK_PERIOD = 500;

    public static void cast(@NotNull CopyFinderOptions options) {
        switch (options.mode()) {
            case RECEIVER_MODE -> receive(options);
            case PUBLISHER_MODE -> publish(options);
        }
    }

    private static void publish(@NotNull CopyFinderOptions options) {
        try (DatagramSocket socket = new DatagramSocket()) {
            TimerTask task = sendSecurityKey(options, socket);
            Timer timer = new Timer();
            timer.schedule(task, 0, PUBLISH_PERIOD);
            while (true) ;
        } catch (SocketException e) {
            throw new CopyFinderRuntimeException(e);
        }
    }

    private static void receive(@NotNull CopyFinderOptions options) {
        byte[] securityKey = options.securityKey().getBytes(StandardCharsets.UTF_8);
        byte[] buf = new byte[securityKey.length];
        try (MulticastSocket socket = new MulticastSocket(options.port())) {
            Set<InetAddress> verifiedAddresses = new HashSet<>();
            SocketAddress multicastAddress = new InetSocketAddress(options.ip(), 0);
            socket.joinGroup(multicastAddress, null);

            Thread thread = new Thread(() -> {
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(packet);
                    } catch (IOException e) {
                        throw new CopyFinderRuntimeException(e);
                    }
                    if (Arrays.equals(buf, securityKey)) {
                        verifiedAddresses.add(packet.getAddress());
                    }
                }
            });
            TimerTask task = printInfo(verifiedAddresses);
            thread.start();
            Timer timer = new Timer();
            timer.schedule(task, 0, RECEIVE_CHECK_PERIOD);
            thread.join();
        } catch (IOException | InterruptedException e) {
            throw new CopyFinderRuntimeException(e);
        }
    }

    @NotNull
    private static TimerTask printInfo(@NotNull Set<@NotNull InetAddress> verifiedAddresses) {
        Set<InetAddress> liveAddresses = new HashSet<>();
        return new TimerTask() {
            @Override
            public void run() {
                if (!verifiedAddresses.equals(liveAddresses)) {
                    liveAddresses.clear();
                    liveAddresses.addAll(verifiedAddresses);
                    System.out.println("Connected verified members of the multicast group: " + verifiedAddresses);
                }
                verifiedAddresses.clear();
            }
        };
    }

    @NotNull
    private static TimerTask sendSecurityKey(CopyFinderOptions options, DatagramSocket socket) {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    byte[] securityKey = options.securityKey().getBytes(StandardCharsets.UTF_8);
                    DatagramPacket packet = new DatagramPacket(securityKey, securityKey.length, options.ip(), options.port());
                    socket.send(packet);
                } catch (IOException e) {
                    throw new CopyFinderRuntimeException(e);
                }
            }
        };
    }
}
