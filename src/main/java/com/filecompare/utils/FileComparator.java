package com.filecompare.utils;

import com.filecompare.model.ComparisonResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileComparator {
    private final String sourceDir;
    private final String targetDir;
    private final String reportDir;
    private final List<ComparisonResult> results;
    
    public FileComparator(String sourceDir, String targetDir, String reportDir) {
        this.sourceDir = sourceDir;
        this.targetDir = targetDir;
        this.reportDir = reportDir;
        this.results = new ArrayList<>();
        
        // Create report directory
        new File(reportDir).mkdirs();
    }
    
    public List<ComparisonResult> compareAllFiles() throws IOException {
        Map<String, File> sourceFiles = getFilesMap(sourceDir);
        Map<String, File> targetFiles = getFilesMap(targetDir);
        
        // Compare files present in both directories
        for (Map.Entry<String, File> entry : sourceFiles.entrySet()) {
            String relativePath = entry.getKey();
            File sourceFile = entry.getValue();
            File targetFile = targetFiles.get(relativePath);
            
            if (targetFile != null) {
                ComparisonResult result = compareFiles(sourceFile, targetFile, relativePath);
                results.add(result);
            } else {
                ComparisonResult result = new ComparisonResult(relativePath, getFileType(sourceFile));
                result.setIdentical(false);
                result.addDifference("File missing in target directory");
                results.add(result);
            }
        }
        
        // Check for files only in target
        for (Map.Entry<String, File> entry : targetFiles.entrySet()) {
            String relativePath = entry.getKey();
            if (!sourceFiles.containsKey(relativePath)) {
                ComparisonResult result = new ComparisonResult(relativePath, getFileType(entry.getValue()));
                result.setIdentical(false);
                result.addDifference("File missing in source directory");
                results.add(result);
            }
        }
        
        return results;
    }
    
    private Map<String, File> getFilesMap(String directory) throws IOException {
        Path basePath = Paths.get(directory);
        
        try (Stream<Path> paths = Files.walk(basePath)) {
            return paths
                .filter(Files::isRegularFile)
                .collect(Collectors.toMap(
                    path -> basePath.relativize(path).toString().replace("\\", "/"),
                    Path::toFile
                ));
        }
    }
    
    private ComparisonResult compareFiles(File sourceFile, File targetFile, String relativePath) {
        String fileType = getFileType(sourceFile);
        ComparisonResult result = new ComparisonResult(relativePath, fileType);

        
        try {
            switch (fileType.toUpperCase()) {
                case "XML":
                    return XmlComparator.compare(sourceFile, targetFile, relativePath);
                case "CSV":
                    return CsvComparator.compare(sourceFile, targetFile, relativePath);
                case "XLSX":
                case "XLS":
                    return ExcelComparator.compare(sourceFile, targetFile, relativePath);
                case "TXT":
                default:
                    return TextComparator.compare(sourceFile, targetFile, relativePath);
            }
        } catch (Exception e) {
            result.setIdentical(false);
            result.setErrorMessage("Error comparing files: " + e.getMessage());
            return result;
        }
    }
    
    private String getFileType(File file) {
        String fileName = file.getName();
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1).toUpperCase();
        }
        return "UNKNOWN";
    }
    
    public void generateReport() throws IOException {
        // Generate summary report
        StringBuilder summary = new StringBuilder();
        summary.append("File Comparison Summary\n");
        summary.append("======================\n\n");
        summary.append("Total files compared: ").append(results.size()).append("\n");
        
        long identicalCount = results.stream().filter(ComparisonResult::isIdentical).count();
        summary.append("Identical files: ").append(identicalCount).append("\n");
        summary.append("Files with differences: ").append(results.size() - identicalCount).append("\n\n");
        
        summary.append("Detailed Results:\n");
        summary.append("-----------------\n");
        
        for (ComparisonResult result : results) {
            summary.append("\nFile: ").append(result.getFileName()).append("\n");
            summary.append("Type: ").append(result.getFileType()).append("\n");
            summary.append("Status: ").append(result.isIdentical() ? "IDENTICAL" : "DIFFERENT").append("\n");
            
            if (!result.isIdentical()) {
                summary.append("Differences: ").append(result.getDifferenceCount()).append("\n");
                if (result.getErrorMessage() != null) {
                    summary.append("Error: ").append(result.getErrorMessage()).append("\n");
                }
            }
        }
        
        // Write summary to file
        Path summaryPath = Paths.get(reportDir, "comparison_summary.txt");
        Files.write(summaryPath, summary.toString().getBytes());
    }
    
    public List<ComparisonResult> getResults() {
        return results;
    }
}
