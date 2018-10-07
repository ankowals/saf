@db
Feature: Basic

  Scenario: Verify that connection to DB can be established

    Given open db

  Scenario: Load data from csv file to dB

    Given open db
      And data from input csv file is loaded to table Dept
    When select query TestData.simpleQuery is executed and results stored as sqlSelectQueryResult
    Then validate that select query result sqlSelectQueryResult is like expectedOutput template