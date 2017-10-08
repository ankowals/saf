@reqResIn
Feature: ReqResIn

  Background: Sanity check
    Given service is available

  @storageTest
  Scenario: Trigger a GET request to get a single user and validate the response

   Given set TestData.a_to_czwarty_kluczyk.test4.testx2 in storage TOJESTTEST
    And set TestData.search_sentence in storage TOJESTTEST2
    And set TestData.DoubleMapa.second in storage TOJESTTEST3
    And set TestData.ostatni[1] in storage TOJESTTEST4
    And set TestData.Najgorsza.klucz2[1].klucz1 in storage TOJESTTEST5

  Scenario: Tigger Post request

    Given json post request post1 is sent