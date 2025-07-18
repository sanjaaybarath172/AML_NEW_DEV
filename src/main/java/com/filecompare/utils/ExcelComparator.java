package com.filecompare.utils;

import com.filecompare.model.ComparisonResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;

public class ExcelComparator {
    
    public static ComparisonResult compare(File sourceFile, File targetFile, String fileName) {
        ComparisonResult result = new ComparisonResult(fileName, "xlsx");
        
        try (FileInputStream sourceFis = new FileInputStream(sourceFile);
             FileInputStream targetFis = new FileInputStream(targetFile);
             Workbook sourceWorkbook = createWorkbook(sourceFile, sourceFis);
             Workbook targetWorkbook = createWorkbook(targetFile, targetFis)) {
            
            // Compare sheet count
            if (sourceWorkbook.getNumberOfSheets() != targetWorkbook.getNumberOfSheets()) {
                result.setIdentical(false);
                result.addDifference(String.format("Sheet count mismatch: source=%d, target=%d",
                    sourceWorkbook.getNumberOfSheets(), targetWorkbook.getNumberOfSheets()));
            }
            
            // Compare each sheet
            for (int i = 0; i < Math.min(sourceWorkbook.getNumberOfSheets(), 
                    targetWorkbook.getNumberOfSheets()); i++) {
                Sheet sourceSheet = sourceWorkbook.getSheetAt(i);
                Sheet targetSheet = targetWorkbook.getSheetAt(i);
                
                compareSheets(sourceSheet, targetSheet, result);
            }
            
        } catch (Exception e) {
            result.setIdentical(false);
            result.setErrorMessage("Error comparing Excel files: " + e.getMessage());
        }
        
        return result;
    }
    
    private static Workbook createWorkbook(File file, FileInputStream fis) throws Exception {
        if (file.getName().toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(fis);
        } else {
            return new HSSFWorkbook(fis);
        }
    }
    
    private static void compareSheets(Sheet sourceSheet, Sheet targetSheet, ComparisonResult result) {
        // Compare sheet names
        if (!sourceSheet.getSheetName().equals(targetSheet.getSheetName())) {
            result.addDifference(String.format("Sheet name mismatch: source='%s', target='%s'",
                sourceSheet.getSheetName(), targetSheet.getSheetName()));
        }
        
        // Compare row counts
        int sourceRows = sourceSheet.getLastRowNum() + 1;
        int targetRows = targetSheet.getLastRowNum() + 1;
        
        if (sourceRows != targetRows) {
            result.addDifference(String.format("Row count mismatch in sheet '%s': source=%d, target=%d",
                sourceSheet.getSheetName(), sourceRows, targetRows));
        }
        
        // Compare cell values
        for (int rowNum = 0; rowNum < Math.min(sourceRows, targetRows); rowNum++) {
            Row sourceRow = sourceSheet.getRow(rowNum);
            Row targetRow = targetSheet.getRow(rowNum);
            
            if (sourceRow == null && targetRow == null) continue;
            if (sourceRow == null || targetRow == null) {
                result.addDifference(String.format("Row %d missing in one file", rowNum + 1));
                continue;
            }
            
            int maxCells = Math.max(
                sourceRow.getLastCellNum(),
                targetRow.getLastCellNum()
            );
            
            for (int cellNum = 0; cellNum < maxCells; cellNum++) {
                Cell sourceCell = sourceRow.getCell(cellNum);
                Cell targetCell = targetRow.getCell(cellNum);
                
                String sourceValue = getCellValue(sourceCell);
                String targetValue = getCellValue(targetCell);
                
                if (!sourceValue.equals(targetValue)) {
                    result.addDifference(String.format(
                        "Cell [%s%d] differs: source='%s', target='%s'",
                        getColumnLetter(cellNum), rowNum + 1, sourceValue, targetValue));
                }
            }
        }
    }
    
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    private static String getColumnLetter(int columnIndex) {
        StringBuilder column = new StringBuilder();
        while (columnIndex >= 0) {
            column.insert(0, (char) ('A' + columnIndex % 26));
            columnIndex = (columnIndex / 26) - 1;
        }
        return column.toString();
    }
}
