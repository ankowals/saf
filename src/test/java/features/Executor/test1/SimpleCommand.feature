@exec
Feature: SimpleCommand

  Scenario: Execute simple command on local host

    Given execute sample command
    And pause execution
    And execute sample command