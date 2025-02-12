package ru.job4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ArgsName {
    private final Map<String, String> values = new HashMap<>();

    public String get(String key) {
        if (!values.containsKey(key)) {
            throw new IllegalArgumentException("This key: '" + key + "' is missing");
        }
        return values.get(key);
    }

    private void parse(String[] args) {
        Arrays.stream(args)
                .map(arg -> arg.split("=", 2))
                .forEach(array -> values.put(array[0].substring(1), array[1]));
    }

    public static ArgsName of(String[] args) {
        if (args.length != 4) {
            throw new IllegalArgumentException("""
                    Not all arguments are specified.
                    Enter search parameters, where\
                    -d is the directory to start the search in.
                    -n is the file name, mask, or regular expression.
                    -t is the search type: mask search by mask, name by full name match, regex by regular expression.
                    -o is the file to write the search result to.""");
        }
        Arrays.stream(args)
                .forEach(arg -> {
                    if (!arg.startsWith("-")) {
                        throw new IllegalArgumentException("Error: This argument '"
                                + arg
                                + "' does not start with a '-' character");
                    }
                    String strForSlit = arg.substring(1);
                    if (!strForSlit.contains("=")) {
                        throw new IllegalArgumentException("Error: This argument '"
                                + arg
                                + "' does not contain an equal sign");
                    }
                    String[] array = strForSlit.split("=", 2);
                    if (array.length < 2 || array[0].isBlank()) {
                        throw new IllegalArgumentException("Error: This argument '"
                                + arg
                                + "' does not contain a key");
                    }
                    if (array[1].isBlank()) {
                        throw new IllegalArgumentException("Error: This argument '"
                                + arg
                                + "' does not contain a value");
                    }
                });
        ArgsName names = new ArgsName();
        names.parse(args);
        return names;
    }
}