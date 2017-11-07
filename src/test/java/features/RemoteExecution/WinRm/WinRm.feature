@winRM
Feature: WinRM

  Scenario: check that node is alive

    #Given execute via WinRS on node node1
    Given windows host node1 is alive
