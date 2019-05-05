@demoDb
Feature: demo db

  Scenario: Load data from csv file to dB

    Given load into a table in Environment.Active.Jdbc.url database
      | file  | input.csv |
      | table | Dept      |

    When edit in Environment.Active.Jdbc.url database
      |UPDATE Dept SET DNAME = 'New RESEARCH', LOC = 'Barcelona' WHERE DEPTNO = 20|

    Then verify in Environment.Active.Jdbc.url database
      |query        |TestData.simpleQuery |
      |template     |dept                 |