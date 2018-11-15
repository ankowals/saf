@parallelWebTest
Feature: ParallelWebTest

  Scenario: First thread scenario

    Given open browser
    When navigate to google search
      And enter first thread into search input
    Then verify that first result text is Clojure - Threading Macros Guide

  Scenario: Second thread scenario

    Given open browser
    When navigate to google search
      And enter second thread into search input
    Then verify that element //IDoNotExist is present on the page
      And navigate to google search

  Scenario: Third thread scenario
    
    Given open browser
    When navigate to google search
      And enter third thread into search input
    Then verify that first result text is Unknown result
      And navigate to google search