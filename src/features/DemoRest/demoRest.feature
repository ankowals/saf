@demoRest
Feature: demo rest

  Scenario: Trigger Post request to create a single user

    Given service is available
    When create user via http post
    Then extract user id as TestData.User.Id
      And write storage TestData with id scenario1 to file

  Scenario: Trigger Get request to get a single user details and validate the response

    Given service is available
      And read storage TestData with id scenario1 from file
    When retrieve details of a user with id TestData.User.Id
    Then verify that rest response body has
      | key                        | action            | expected        |
      | data.id                    | equalTo           | TestData.User.Id|

  Scenario: Trigger Put request to modify single user

    Given service is available
    When modify user with id TestData.User.Id
    Then verify that rest response body has
      | key                        | action            | expected             |
      | name                       | equalTo           | TestData.User.name   |
      | job                        | equalTo           | Expected.job         |
      | updatedAt                  | containsString    | Expected.date        |

  Scenario: Trigger Delete request to remove a single user

    Given service is available
    When delete user with id TestData.User.Id
    Then verify that status code is Expected.resultsCode