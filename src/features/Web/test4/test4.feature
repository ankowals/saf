@Web @02_etl
Feature: test4
  Next Search for a specific sentence using google

  @smoke
  @SeverityLevel.BLOCKER @TestCaseId("example-99")
  Scenario: Test search 4
    Given open browser
      And load configuration data from "Web\test4\test4.config"
      And evaluate macros
    When I open google page
    Then I check for input element
       And I search for text TestData.wartosc_dla_wyszukiwarki
       And attach sample file to report

  @smoke
  Scenario: Test search 5
    Given open browser
      And load configuration data from "Web\test4\test45.config"
      And evaluate macros
    When I open google page
    Then I check for input element
      And I search for text