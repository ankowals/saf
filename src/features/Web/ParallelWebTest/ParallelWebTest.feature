@parallelWebTest
Feature: ParallelWebTest

  Scenario: First thread scenario

    Given open browser
    When navigate to google search
    Then enter first thread into search input

  Scenario: Second thread scenario

    Given open browser
    When navigate to google search
    Then enter second thread into search input

  Scenario: Third thread scenario

    Given open browser
    When navigate to google search
    Then enter third thread into search input