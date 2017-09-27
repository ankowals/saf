@Web
Feature: test1
  Search for a specific sentence using google

  Background:
    Given configuration data from "Web\test1\test1.config" is loaded
      And macro evaluation is done

  @storageTest
  Scenario: Test search 1
    Given open browser
    When I open google page
    Then I check for input element
    Then I search for text