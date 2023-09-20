package ru.nsu.fit.sckwo.options;

import org.apache.commons.cli.*;
import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.exceptions.CopyFinderInvalidArgumentsException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OptionsParser {
    private static final Options options = new Options();

    static {
        options.addOption("ip", "ip", true, "IP address of multicast group");
        options.addOption("port", "port", true, "Server port");
        options.addOption("k", "key", true, "Safety key");
        OptionGroup appModeGroup = new OptionGroup();
        appModeGroup.addOption(new Option("p", "publisher", false, "Publishing mode"));
        appModeGroup.addOption(new Option("r", "receiver", false, "Receiver mode"));
        options.addOptionGroup(appModeGroup);
    }

    @NotNull
    public static CopyFinderOptions parse(@NotNull String[] args) {
        CommandLine cmd;
        try {
            cmd = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            throw new CopyFinderInvalidArgumentsException(e.getMessage());
        }

        Mode mode;
        int port;
        InetAddress ip;
        String key;
        if (cmd.hasOption("k")) {
            key = cmd.getOptionValue("k");
        } else {
            throw new CopyFinderInvalidArgumentsException("Security key not specified (-k).");
        }

        if (cmd.hasOption("r")) {
            mode = Mode.RECEIVER_MODE;
        } else if (cmd.hasOption("p")) {
            mode = Mode.PUBLISHER_MODE;
        } else {
            throw new CopyFinderInvalidArgumentsException("The application mod option is not selected (-p or -r).");
        }

        if (cmd.hasOption("port")) {
            port = Integer.parseInt(cmd.getOptionValue("port"));
            if (port <= 0) {
                throw new CopyFinderInvalidArgumentsException("Invalid port number");
            }
        } else {
            throw new CopyFinderInvalidArgumentsException("No required port option (-port)");
        }

        if (cmd.hasOption("ip")) {
            try {
                ip = InetAddress.getByName(cmd.getOptionValue("ip"));
                if (!ip.isMulticastAddress()) {
                    throw new CopyFinderInvalidArgumentsException("The specified IP address is not a Multicast group address.");
                }
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new CopyFinderInvalidArgumentsException("The IP address of the Multicast group is not specified.");
        }
        return new CopyFinderOptions(ip, mode, port, key);
    }
}
