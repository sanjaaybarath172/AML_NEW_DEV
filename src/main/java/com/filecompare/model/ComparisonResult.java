package com.filecompare.model;

import java.util.ArrayList;
import java.util.List;

public class ComparisonResult {
    private String fileName;
    private String fileType;
    private boolean identical;
    private int differenceCount;
    private List<String> differences;
    private String errorMessage;

    public ComparisonResult(String fileName, String fileType) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.differences = new ArrayList<>();
        this.identical = true;
    }

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public boolean isIdentical() {
        return identical;
    }

    public void setIdentical(boolean identical) {
        this.identical = identical;
    }

    public int getDifferenceCount() {
        return differenceCount;
    }

    public void setDifferenceCount(int differenceCount) {
        this.differenceCount = differenceCount;
    }

    public List<String> getDifferences() {
        return differences;
    }

    public void setDifferences(List<String> differences) {
        this.differences = differences;
    }

    public void addDifference(String difference) {
        this.differences.add(difference);
        this.differenceCount++;
        this.identical = false;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "ComparisonResult{" +
                "fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", identical=" + identical +
                ", differenceCount=" + differenceCount +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
