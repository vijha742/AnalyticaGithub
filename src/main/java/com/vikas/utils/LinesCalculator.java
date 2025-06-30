package com.vikas.utils;

public class LinesCalculator {

    public static int calculateLinesOfCode(String language, int sizeInBytes) {
        int bytesPerLine = switch (language.toLowerCase()) {
            case "java" -> 50;
            case "typescript", "tsx", "jsx", "javascript" -> 40;
            case "css", "scss", "less" -> 33;
            case "json", "yaml", "yml", "xml", "toml", "properties" -> 35;
            case "html" -> 45;
            case "python" -> 40;
            case "c", "cpp", "c++", "c#", "csharp" -> 50;
            case "go" -> 45;
            case "ruby", "php" -> 40;
            case "rust" -> 55;
            case "kotlin" -> 48;
            case "swift" -> 48;
            default -> 45;
        };

        return Math.max(1, sizeInBytes / bytesPerLine);
    }
}
