@demoRemote
Feature: demo remote

  Scenario: demo remote

    Given host node1 is alive
      And list files in users home directory
    When switch user to root
      And execute sample command in an interactive shell
    Then download file from remote host
