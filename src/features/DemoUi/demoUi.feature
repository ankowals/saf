Feature: demo Ui

  @demoUi
  Scenario: test ui automation

    Given open browser
      And in the browser, navigate to a demo shop web page
      And in the browser, extract page title
      And create file TestData.TmpFile.path
    When write page title to a file TestData.TmpFile.path
    Then open an app Environment.Active.App.path with additional arguments TestData.TmpFile.path
      And in notepad, verify that page title is Expected.PageTitle
      And pause execution
    
  @demoParamTransformation
  Scenario: test step input parameters transformation
    
    Given open browser
    When in the browser, search for product with name TestData.Product1
    Then in the browser, verify that Expected.SearchResultsNumber results were returned
      And in the browser, verify that 7 results were returned