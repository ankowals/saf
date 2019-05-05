@demoRemote
Feature: demo remote

  Scenario: demo remote

    Given host node1 is alive
      And list files in users home directory
    When switch user to root
      And execute sample command in an interactive shell
    Then download file from remote host

  Scenario: demo remote winRS

    Given remote host node1 is accessible via winRM
    When on remote host node1 get service TermService status
    Then verify that service TermService status is Running

  Scenario: start notepad on remote host

    Given remote host node1 is accessible via winRM
    When on remote host node1, open an app from Environment.Active.App.path
    Then wait for 5 seconds