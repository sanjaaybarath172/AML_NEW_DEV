package com.filecompare.utils;

import com.filecompare.model.ComparisonResult;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

public class CsvComparator {
    
    public static ComparisonResult compare(File sourceFile, File targetFile, String fileName) {
        ComparisonResult result = new ComparisonResult(fileName, "CSV");
        
        try (CSVReader sourceReader = new CSVReaderBuilder(new FileReader(sourceFile)).build();
             CSVReader targetReader = new CSVReaderBuilder(new FileReader(targetFile)).build()) {
            
            List<String[]> sourceData = sourceReader.readAll();
            List<String[]> targetData = targetReader.readAll();
            
            // Compare row counts
            if (sourceData.size() != targetData.size()) {
                result.setIdentical(false);
                result.addDifference(String.format("Row count mismatch: source=%d, target=%d", 
                    sourceData.size(), targetData.size()));
            }
            
            // Compare data row by row
            int maxRows = Math.min(sourceData.size(), targetData.size());
            for (int i = 0; i < maxRows; i++) {
                String[] sourceRow = sourceData.get(i);
                String[] targetRow = targetData.get(i);
                
                if (!Arrays.equals(sourceRow, targetRow)) {
                    result.setIdentical(false);
                    result.addDifference(String.format("Row %d differs: source=%s, target=%s",
                        i + 1, Arrays.toString(sourceRow), Arrays.toString(targetRow)));
                }
            }
            
        } catch (Exception e) {
            result.setIdentical(false);
            result.setErrorMessage("Error comparing CSV files: " + e.getMessage());
        }
        
        return result;
    }
}
