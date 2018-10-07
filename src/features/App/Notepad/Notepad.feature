@notepad
Feature: Notepad

  Scenario: Open notepad and enter a text

    Given open an app from C:\\Windows\\System32\\notepad.exe with args ""
    And wait for 3 seconds