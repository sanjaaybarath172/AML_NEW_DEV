Feature: Compare files between source and target zip archives
  As a user
  I want to compare files between preshot and postshot zip files
  So that I can identify differences between corresponding files

  Background:
    Given I have a source zip file "preshot.zip"
    And I have a target zip file "postshot.zip"

  Scenario: Extract and compare all files from zip archives
    When I extract both zip files
    Then I should see the same number of files in both archives
    And I should be able to compare all corresponding files

  Scenario Outline: Compare different file types
    When I compare "<fileType>" files between source and target
    Then I should get a comparison report for "<fileType>" files
    And the report should highlight differences if any

    Examples:
      | fileType |
      | XML      |
      | TXT      |
      | CSV      |
      | XLSX     |

  Scenario: Generate comprehensive comparison report
    When I perform complete comparison of all files
    Then I should get a summary report with:
      | Total files compared     |
      | Identical files         |
      | Files with differences  |
      | Missing files           |
    And detailed reports for each file comparison
