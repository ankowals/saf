@Web
Feature: test4
  Next Search for a specific sentence using google

  @smoke
  @SeverityLevel.BLOCKER @TestCaseId("example-99")
  Scenario: Test search 4
    Given open browser
      And test data from "Web\test4\test4.config" is loaded
      And macro evaluation is done
    When I open google page
    Then I check for input element
      #And I search for text
       And I search for text TestData.wartosc_dla_wyszukiwarki
       And attach sample file to report

  @smoke
  Scenario: Test search 5
    Given open browser
      And test data from "Web\test4\test45.config" is loaded
      And macro evaluation is done
    When I open google page
    Then I check for input element
      And I search for text