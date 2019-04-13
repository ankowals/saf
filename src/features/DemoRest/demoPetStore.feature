@demoPetStore
Feature: demo pet store

  Scenario: Trigger Post request to create a pet

    Given add pet
    When get pet
    Then verify that pet name is Lena

  Scenario: verify post response

    Given add pet with validation filter