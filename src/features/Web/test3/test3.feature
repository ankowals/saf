@WebAndDb
Feature: test3
  Use selenium java with cucumber-jvm and navigate to website

 Scenario: Print title, url
   Given open browser
    When I open seleniumframework website
    Then I validate title and URL
    And open db
    And select query TestData.simpleQuery is executed and results stored as sqlSelectQueryResult