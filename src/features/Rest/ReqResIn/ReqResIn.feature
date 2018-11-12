@reqResIn
Feature: Req Res-In

  Scenario: Tigger Post request to create single user

    Given service is available
      And write storage TestData with id ReqRestInScenario1 to file
      And read storage TestData with id ReqRestInScenario1 from file
    When send json post request createUser
    Then extract user id as userId

  Scenario: Trigger a Get request to get a single user and validate the response

    Given service is available
    When send json get request single user with id userId
    Then verify that rest response body has
      | key                        | action            | expected        |
      | data.id                    | equalTo           | Expected.userId |

  Scenario: Tigger Put request to modify single user

    Given service is available
    When send json put request modifyUser to modify single user with id 2
    Then verify that rest response body has
      | key                        | action            | expected        |
      | name                       | equalTo           | Expected.name   |
      | job                        | equalTo           | janitor         |

  Scenario: Tigger Delete request to remove a single user

    Given service is available
    When send json delete request single user request with id 2
    Then verify that status code is 204