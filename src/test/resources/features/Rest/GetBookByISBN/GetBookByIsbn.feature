@bookByIsbn
Feature: Get book by ISBN
  Scenario: User calls web service to get a book by its ISBN
    Given a book exists with an isbn
    When a user retrieves the book by isbn
    Then the status code is TestData.statusOK
      And response includes the following
        | totalItems 	 		                    | 1 	                |
        | kind					                    | books#volumes	        |
      And response includes the following in any order
        | items.volumeInfo.title 					| Steve Jobs			|
        | items.volumeInfo.publisher 				| Simon and Schuster	|
        | items.volumeInfo.pageCount 				| 630					|