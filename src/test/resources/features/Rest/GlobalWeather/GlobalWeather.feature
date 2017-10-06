@globalWeather
Feature: GlobalWeather

  Scenario: Tigger Post request

    Given simple XML request post1
    When the status code is Expected.statusOK
    Then verify that response has
      | GetCitiesByCountryResponse.GetCitiesByCountryResult.NewDataSet.table[1].city, equals | Expected.CityName     |
     # | items.volumeInfo.title,	contains          | Expected.title       |
      #| items.volumeInfo.pageCount, isGreaterThan | Expected.pageCount	 |