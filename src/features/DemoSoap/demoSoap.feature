@demoSoap
Feature: demo soap

  Scenario: Trigger Post request

    Given service is available
    When get country info via soap api
    Then verify that status code is Expected.statusOK
    And verify that rest response body has
      | key                                                                                    | action  | expected             |
      | Envelope.Body.FullCountryInfoResponse.FullCountryInfoResult.sCapitalCity               | equalTo | Expected.CapitalCity |
      | Envelope.Body.FullCountryInfoResponse.FullCountryInfoResult.Languages.tLanguage.sName  | equalTo | Expected.Language    |