@demoDb
Feature: demo db
  
  Scenario: Load data from csv file to dB

    Given load into a table in DemoDb database
      | file  | input.csv |
      | table | Dept      |

    When edit in DemoDb database
      |UPDATE Dept SET DNAME = 'New RESEARCH', LOC = 'Barcelona' WHERE DEPTNO = 20|

    Then verify in DemoDb database
      |query        |TestData.simpleQuery |
      |template     |dept                 |