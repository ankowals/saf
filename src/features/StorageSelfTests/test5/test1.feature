Feature: test_5_6_7

  @storageTest
  Scenario: test for set
    Given set TestData.a_to_czwarty_kluczyk.test4.testx2 to TOJESTTEST

  @encoderTest
  Scenario: encoder test
    Given encode string ToMojeSuperTajneHaslo
    
  @decoderTest
  Scenario: decoder test
    Given decode string qgAo3qmFaTRKa7lGwLhwPgYnt8T0IuKa