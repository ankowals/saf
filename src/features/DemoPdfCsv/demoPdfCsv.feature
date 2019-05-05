@demoPdfCsv
Feature: demo Pdf and Csv

  Scenario: test Pdf and Csv automation

    Given read document to TestData.Document.Content
    When write content of TestData.Document.Content to csv file TestData.Temp.File
    Then verify csv file TestData.Temp.File content
        |key       |action  |expected       |
        |header1[1]|equals  |firstRowValue1 |
        |header2[1]|equals  |firstRowValue2 |
        |header3[1]|equals  |firstRowValue3 |
        |header1[2]|matches |second.*1      |
        |header2[2]|matches |second.*2      |
        |header3[2]|matches |second.*3      |