@reqResIn
Feature: ReqResIn

  Background: Sanity check
    Given service is available

  Scenario: Tigger Post request to create single user

    Given json post request createUser is sent

  Scenario: Trigger a Get request to get a single user and validate the response

