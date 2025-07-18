package com.filecompare.utils;

import com.filecompare.model.ComparisonResult;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import java.io.File;

public class XmlComparator {
    
    public static ComparisonResult compare(File sourceFile, File targetFile, String fileName) {
        ComparisonResult result = new ComparisonResult(fileName, "XML");
        
        try {
            Diff diff = DiffBuilder.compare(sourceFile)
                    .withTest(targetFile)
                    .ignoreWhitespace()
                    .ignoreComments()
                    .checkForIdentical()
                    .build();
            
            if (diff.hasDifferences()) {
                result.setIdentical(false);
                for (Difference difference : diff.getDifferences()) {
                    result.addDifference(difference.toString());
                }
            }
            
        } catch (Exception e) {
            result.setIdentical(false);
            result.setErrorMessage("Error comparing XML files: " + e.getMessage());
        }
        
        return result;
    }
}
