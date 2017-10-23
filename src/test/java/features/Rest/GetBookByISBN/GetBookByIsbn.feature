@bookByIsbn
Feature: Get book by ISBN
  Scenario: User calls web service to get a book by its ISBN
    Given a book exists with an isbn
    When a user retrieves the book by isbn
    Then verify that status code is Expected.statusOK

      And response includes the following
        | totalItems 	 		                    | Expected.totalItems   |
        | kind					                    | Expected.kind         |

      And response includes the following in any order
        | items.volumeInfo.title 					| Expected.title		|
        | items.volumeInfo.publisher 				| Expected.publisher	|
        | items.volumeInfo.pageCount 				| Expected.pageCount	|

      And response includes the following
        | totalItems 	        	                | 1                  |
        | kind					                    | books#volumes      |
      And response includes the following in any order
        | items.volumeInfo.title 					| Steve Jobs		 |
        | items.volumeInfo.publisher 				| Simon and Schuster |
        | items.volumeInfo.pageCount 				| 630	             |

      And verify that rest response body has
        | key                           | action             | expected                        |
        | totalItems                    | lessThan           | Expected.highAmountOfTotalItems |
        | kind                          | containsString     | Expected.partOfKind             |
        | items.volumeInfo.title        | containsInAnyOrder | Expected.title	               |
        | items[0].volumeInfo.pageCount | greaterThan        | Expected.lowPageCount           |
        | items[0].volumeInfo.authors   | containsInAnyOrder | Expected.author                 |

      And verify that rest response body has
        | key                           | action             | expected                |
        | totalItems                    | lessThan           | 99                      |
        | kind                          | containsString     | volumes                 |
        | items.volumeInfo.title        | containsInAnyOrder | Steve Jobs	           |
        | items[0].volumeInfo.pageCount | greaterThan 	     | 110                     |
        | items[0].volumeInfo.authors   | containsInAnyOrder | Walter Isaacson         |