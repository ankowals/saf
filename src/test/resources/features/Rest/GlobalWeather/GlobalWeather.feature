@globalWeather
Feature: GlobalWeather

  Scenario: Tigger Post request

    Given xml post request post1 with soap action header TestData.soapActionHeader is sent
    When the status code is Expected.statusOK
    Then verify that rest response body has
      | GetCitiesByCountryResponse.GetCitiesByCountryResult.NewDataSet.table[1].city | equalTo | Expected.CityName |