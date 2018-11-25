@exec
Feature: SimpleCommand

  Scenario: Execute simple command on local host

    Given execute loop command
      And execute sample command
      And execute sample command in background
    When pause execution
    Then execute sample command
      And execute error command