@globalWeather
Feature: GlobalWeather

  Scenario: Tigger Post request

    Given service is available
    When send xml post request post1 with soap action header TestData.soapActionHeader
    Then verify that status code is Expected.statusOK
      And verify that rest response body has
        | key                               | action         | expected          |
        | GetCitiesByCountryResponse.text() | containsString | Expected.CityName |