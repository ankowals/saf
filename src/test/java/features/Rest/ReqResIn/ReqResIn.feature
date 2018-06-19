@reqResIn
Feature: Req Res-In

  Scenario: Tigger Post request to create single user

    Given service is available
      And write storage TestData with id ReqRestInScenario1 to file
      And read storage TestData with id ReqRestInScenario1 from file
    When json post request createUser is sent
    Then extract user id as userId of type String

  Scenario: Trigger a Get request to get a single user and validate the response

    Given service is available
    When json get single user with id userId of type String request is sent
    Then verify that rest response body has
      | key                        | action            | expected        |
      | data.id                    | equalTo           | Expected.userId |

  Scenario: Tigger Put request to modify single user

    Given service is available
    When json put request modifyUser to modify single user with id 2 is sent
    Then verify that rest response body has
      | key                        | action            | expected        |
      | name                       | equalTo           | Expected.name   |
      | job                        | equalTo           | janitor         |

  Scenario: Tigger Delete request to remove a single user

    Given service is available
    When json delete single user request with id 2 is sent
    Then verify that status code is 204