@ssh
Feature: Ssh

  Scenario: check that node is alive

    Given host node1 is alive

  Scenario: list files in users home directory

    Given list files in users home directory

  Scenario: switch user to root

    Given switch user to root

  Scenario: check command execution status when in shell

    Given check command exit status code when in shell

  Scenario: download file from users home dir

    Given check that file exists on remote node
