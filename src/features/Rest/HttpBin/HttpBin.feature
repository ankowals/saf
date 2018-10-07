@httpBin
Feature: HttpBin

  Scenario: Tigger Post request

    Given json post request post1 is sent
    When the status code is Expected.statusOK
    Then verify that rest response body has
      | key                     | action                   | expected   |
      | data                    | containsString           | Expected.a |