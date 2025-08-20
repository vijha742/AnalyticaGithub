package com.vikas.model;

import lombok.Data;

@Data
public class LanguageStats {
    private String language;
    private int linesOfCode;
    private float percentage;
    private int fileCount; // Calculating data but it is just an approximate guess.

}
