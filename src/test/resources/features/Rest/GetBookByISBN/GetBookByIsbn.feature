@bookByIsbn
Feature: Get book by ISBN
  Scenario: User calls web service to get a book by its ISBN
    Given a book exists with an isbn
    When a user retrieves the book by isbn
    Then the status code is Expected.statusOK
      And response includes the following
        | totalItems 	 		                    | Expected.totalItems   |
        | kind					                    | Expected.kind         |
      And response includes the following in any order
        | items.volumeInfo.title 					| Expected.title		|
        | items.volumeInfo.publisher 				| Expected.publisher	|
        | items.volumeInfo.pageCount 				| Expected.pageCount	|