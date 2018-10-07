@01_installation
Feature: test5

  @storageTest
  Scenario: test for set
   Given set TestData.a_to_czwarty_kluczyk.test4.testx2 to TOJESTTEST
    And set TestData.search_sentence to TOJESTTEST2
    And set TestData.DoubleMapa.second to TOJESTTEST3
    And set TestData.ostatni[1] to TOJESTTEST4
    And set TestData.Najgorsza.klucz2[1].klucz1 to TOJESTTEST5
    And set TestData.nieMaMnie.brakujacyKlucz to TOJESTTEST6
    And set TestData.nieMaMnie2[0].brakujacyKlucz to TOJESTTEST7
    And set TestData.nieMaMnie3[0] to TOJESTTEST8
    And set TestData.Najgorsza.klucz2[2].klucz3 to TOJESTTEST8

  Scenario: test for set2

    And set TestData.nieMaMnie4[5] to TOJESTTEST9

  Scenario: test for set3

    And set TestData.nieMaMnie4[5].klucz1 to TOJESTTEST9