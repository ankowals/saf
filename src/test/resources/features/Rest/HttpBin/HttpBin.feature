@httpBin
Feature: HttpBin

  Scenario: Tigger Post request

    Given simple POST request post1
    When the status code is Expected.statusOK
    Then verify that response has
      | data, containsString | Expected.a |