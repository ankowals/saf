@httpBin
Feature: HttpBin

  Scenario: Tigger Post request

    Given send json post request post1
    When verify that status code is Expected.statusOK
    Then verify that rest response body has
      | key                     | action                   | expected   |
      | data                    | containsString           | Expected.a |