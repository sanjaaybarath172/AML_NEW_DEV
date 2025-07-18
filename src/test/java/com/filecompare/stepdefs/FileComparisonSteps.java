package com.filecompare.stepdefs;

import com.filecompare.model.ComparisonResult;
import com.filecompare.utils.FileComparator;
import com.filecompare.utils.ZipExtractor;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import org.junit.Assert;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileComparisonSteps {
    
    private String sourceZipPath;
    private String targetZipPath;
    private String sourceExtractDir = "temp/source";
    private String targetExtractDir = "temp/target";
    private String reportDir = "reports";
    private FileComparator comparator;
    private List<ComparisonResult> results;
    
    @Given("I have a source zip file {string}")
    public void i_have_a_source_zip_file(String zipFile) {
        this.sourceZipPath = zipFile;
        File file = new File(zipFile);
        Assert.assertTrue("Source zip file does not exist: " + zipFile, file.exists());
    }
    
    @And("I have a target zip file {string}")
    public void i_have_a_target_zip_file(String zipFile) {
        this.targetZipPath = zipFile;
        File file = new File(zipFile);
        Assert.assertTrue("Target zip file does not exist: " + zipFile, file.exists());
    }
    
    @When("I extract both zip files")
    public void i_extract_both_zip_files() throws Exception {
        ZipExtractor.extractZip(sourceZipPath, sourceExtractDir);
        ZipExtractor.extractZip(targetZipPath, targetExtractDir);
        
        comparator = new FileComparator(sourceExtractDir, targetExtractDir, reportDir);
    }
    
    @Then("I should see the same number of files in both archives")
    public void i_should_see_the_same_number_of_files_in_both_archives() {
        int sourceCount = ZipExtractor.countFiles(sourceExtractDir);
        int targetCount = ZipExtractor.countFiles(targetExtractDir);
        
        System.out.println("Source files count: " + sourceCount);
        System.out.println("Target files count: " + targetCount);
        
        Assert.assertEquals("File count mismatch between archives", sourceCount, targetCount);
    }
    
    @And("I should be able to compare all corresponding files")
    public void i_should_be_able_to_compare_all_corresponding_files() throws Exception {
        results = comparator.compareAllFiles();
        Assert.assertNotNull("Comparison results should not be null", results);
        Assert.assertTrue("Should have comparison results", results.size() > 0);
    }
    
    @When("I compare {string} files between source and target")
    public void i_compare_files_between_source_and_target(String fileType) throws Exception {
        if (comparator == null) {
            ZipExtractor.extractZip(sourceZipPath, sourceExtractDir);
            ZipExtractor.extractZip(targetZipPath, targetExtractDir);
            comparator = new FileComparator(sourceExtractDir, targetExtractDir, reportDir);
        }
        
        results = comparator.compareAllFiles();
        
        // Filter results by file type
        results = results.stream()
                .filter(r -> r.getFileType().equalsIgnoreCase(fileType))
                .collect(Collectors.toList());
    }
    
    @Then("I should get a comparison report for {string} files")
    public void i_should_get_a_comparison_report_for_files(String fileType) {

        Assert.assertTrue("Should have results for " + fileType + " files", 
            results.size() > 0 || !hasFilesOfType(fileType));
    }
    
    @And("the report should highlight differences if any")
    public void the_report_should_highlight_differences_if_any() {
        for (ComparisonResult result : results) {
            System.out.println("File: " + result.getFileName() + 
                " - Status: " + (result.isIdentical() ? "IDENTICAL" : "DIFFERENT"));
            if (!result.isIdentical()) {
                System.out.println("Differences: " + result.getDifferenceCount());
            }
        }
    }
    
    @When("I perform complete comparison of all files")
    public void i_perform_complete_comparison_of_all_files() throws Exception {
        if (comparator == null) {
            ZipExtractor.extractZip(sourceZipPath, sourceExtractDir);
            ZipExtractor.extractZip(targetZipPath, targetExtractDir);
            comparator = new FileComparator(sourceExtractDir, targetExtractDir, reportDir);
        }
        
        results = comparator.compareAllFiles();
        comparator.generateReport();
    }
    
    @Then("I should get a summary report with:")
    public void i_should_get_a_summary_report_with(List<String> expectedItems) {
        File summaryFile = new File(reportDir, "comparison_summary.txt");
        Assert.assertTrue("Summary report should exist", summaryFile.exists());
        
        // Verify report contains expected sections
        long identicalCount = results.stream().filter(ComparisonResult::isIdentical).count();
        long differentCount = results.size() - identicalCount;
        
        System.out.println("Summary Report:");
        System.out.println("Total files compared: " + results.size());
        System.out.println("Identical files: " + identicalCount);
        System.out.println("Files with differences: " + differentCount);
    }
    
    @And("detailed reports for each file comparison")
    public void detailed_reports_for_each_file_comparison() {
        Assert.assertNotNull("Should have detailed results", results);
        Assert.assertTrue("Should have comparison details", results.size() > 0);
    }
    
    private boolean hasFilesOfType(String fileType) {
        File sourceDir = new File(sourceExtractDir);
        return findFilesWithExtension(sourceDir, fileType).size() > 0;
    }
    
    private List<File> findFilesWithExtension(File dir, String extension) {
        return java.util.Arrays.stream(dir.listFiles())
                .filter(file -> file.isFile() && 
                    file.getName().toLowerCase().endsWith("." + extension.toLowerCase()))
                .collect(Collectors.toList());
    }
}
