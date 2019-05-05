@demoUi
Feature: demo Ui

  Scenario: test ui automation

    Given open browser
      And in the browser, navigate to a demo shop web page
      And in the browser, extract page title
      And create file TestData.TmpFile.path
    When write page title to a file TestData.TmpFile.path
    Then open an app Environment.Active.App.path with additional arguments TestData.TmpFile.path
      And in notepad, verify that page title is Expected.PageTitle
      And pause execution