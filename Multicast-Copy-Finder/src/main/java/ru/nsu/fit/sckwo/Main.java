package ru.nsu.fit.sckwo;

import org.jetbrains.annotations.NotNull;
import ru.nsu.fit.sckwo.core.Multicast;
import ru.nsu.fit.sckwo.exceptions.CopyFinderException;
import ru.nsu.fit.sckwo.options.CopyFinderOptions;
import ru.nsu.fit.sckwo.options.OptionsParser;

public class Main {
    public static void main(@NotNull String[] args) {
        try {
            CopyFinderOptions copyFinderOptions = OptionsParser.parse(args);
            System.out.println(copyFinderOptions);
            Multicast.cast(copyFinderOptions);
        } catch (CopyFinderException e) {
            System.err.println(e.getMessage());
        }
    }
}
