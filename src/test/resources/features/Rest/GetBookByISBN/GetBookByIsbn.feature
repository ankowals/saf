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

    Then verify that response has
      | totalItems, lessThan                              | Expected.highAmountOfTotalItems   |
      | kind, containsString                              | Expected.partOfKind               |
      | items.volumeInfo.title, containsInAnyOrder  	  | Expected.title		              |
      | items[0].volumeInfo.pageCount, greaterThan 	      | Expected.lowPageCount	          |
      | items[0].volumeInfo.authors, containsInAnyOrder   | Expected.author                   |