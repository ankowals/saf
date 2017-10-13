@db
Feature: Basic

  Scenario: Verify that connection to DB can be established

    Given open db

  Scenario: Load data from csv file to dB

    Given open db
      And data from input csv file is loaded to table Dept
    When simple select is executed
    Then validate that result is like expectedOutput