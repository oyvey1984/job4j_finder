package ru.job4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Search {

    private static Path path;
    private static String fileName;
    private static Predicate<Path> predicate;

    public static List<Path> search(Path root, Predicate<Path> condition) throws IOException {
        SearchFiles searcher = new SearchFiles(condition);
        Files.walkFileTree(root, searcher);
        return searcher.getPaths();
    }

    public static void validate(String[] args) {
        ArgsName arguments = ArgsName.of(args);

        path = Path.of(arguments.get("d"));
        if (!Files.exists(path)) {
            throw new IllegalArgumentException(String.format("Not exist %s", path.toAbsolutePath()));
        }
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException(String.format("Not directory %s", path.toAbsolutePath()));
        }
        fileName = arguments.get("n");
        String searchType = arguments.get("t");
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File is not specified.");
        }
        if (searchType == null || searchType.isEmpty()) {
            throw new IllegalArgumentException("Search type is not specified.");
        }
        switch (searchType) {
            case "name":
                Pattern pattern = Pattern.compile(fileName);
                predicate = path -> {
                    Matcher matcher = pattern.matcher(path.toFile().getName());
                    return matcher.matches();
                };
                if (!fileName.matches("[^\\\\/:*?\"<>|]+")) {
                    throw new IllegalArgumentException("Invalid file name");
                }
                break;
            case "mask":
                predicate = path -> {
                    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + fileName);
                    return  matcher.matches(path.getFileName());
                };
                if (!fileName.matches(".*\\*.*|.*\\?.*")) {
                    throw new IllegalArgumentException("Invalid mask");
                }
                break;
            case "regex":
                try {
                    Pattern patternReg = Pattern.compile(fileName);
                    predicate = path -> {
                        Matcher matcher = patternReg.matcher(path.toFile().getName());
                        return matcher.matches();
                    };
                } catch (PatternSyntaxException e) {
                    throw new IllegalArgumentException("Invalid regular expression", e);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid search type");
        }
        File result = new File(arguments.get("o"));
        String resultToString = result.toString();
        if (resultToString == null || resultToString.isEmpty()) {
            throw new IllegalArgumentException("File for writing the result is not specified.");
        }
        if (!result.exists()) {
            throw new IllegalArgumentException(String.format("Not exist %s", result.getAbsoluteFile()));
        }
    }

    public static void main(String[] args) throws IOException {
        validate(args);
        search(path, predicate)
               .forEach(System.out::println);

    }
}