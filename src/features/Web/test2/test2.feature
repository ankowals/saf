@Web
Feature: test2
  Second search for a specific sentence using google

  Background:
    Given load configuration data from "Web\test2\test2.config"
    And evaluate macros

  Scenario: Test search 2
    Given open browser
    When I open google page
    Then I check for input element
    Then I search for text

  Scenario: Test search 3
    Given open browser
    When I open seleniumframework website
    Then I validate title and URL