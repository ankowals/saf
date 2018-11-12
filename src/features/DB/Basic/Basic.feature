@db
Feature: Basic

  Scenario: Load data from csv file to dB

    Given load data from csv file input into a table Dept in Environment.Active.Jdbc.url database
    When edit in Environment.Active.Jdbc.url database
      |UPDATE Customers SET ContactName = 'Alfred Schmidt', City = 'Frankfurt' WHERE CustomerID = 1|

    Then verify in Environment.Active.Jdbc.url database
      |query        |TestData.simpleQuery |
      |template     |expectedOutput       |