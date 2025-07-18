package com.filecompare.utils;

import com.filecompare.model.ComparisonResult;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TextComparator {
    
    public static ComparisonResult compare(File sourceFile, File targetFile, String fileName) {
        ComparisonResult result = new ComparisonResult(fileName, "txt");
        
        try {
            List<String> sourceLines = FileUtils.readLines(sourceFile, StandardCharsets.UTF_8);
            List<String> targetLines = FileUtils.readLines(targetFile, StandardCharsets.UTF_8);
            
            // Compare line counts
            if (sourceLines.size() != targetLines.size()) {
                result.setIdentical(false);
                result.addDifference(String.format("Line count mismatch: source=%d, target=%d",
                    sourceLines.size(), targetLines.size()));
            }
            
            // Compare line by line
            int maxLines = Math.min(sourceLines.size(), targetLines.size());
            for (int i = 0; i < maxLines; i++) {
                if (!sourceLines.get(i).equals(targetLines.get(i))) {
                    result.setIdentical(false);
                    result.addDifference(String.format("Line %d differs:\n  Source: %s\n  Target: %s",
                        i + 1, sourceLines.get(i), targetLines.get(i)));
                }
            }
            
        } catch (Exception e) {
            result.setIdentical(false);
            result.setErrorMessage("Error comparing text files: " + e.getMessage());
        }
        
        return result;
    }
}
