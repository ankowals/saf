Feature: DemoOnlineShop

  @demoOnline
  Scenario: Verify sum of 2 items equals total price Thread 1

    Given open browser
    When open main page
      And navigate to all products page
      And add product TestData.product1 to cart
      And add product TestData.product2 to cart
      And navigate to checkout page
    Then verify that SubTotal value equals sum of totals per product type

  Scenario: Verify sum of 2 items equals total price Thread 2

    Given open browser
    When open main page
    And navigate to all products page
    And add product TestData.product1 to cart
    And add product TestData.product2 to cart
    And navigate to checkout page
    Then verify that SubTotal value equals sum of totals per product type

  Scenario: Verify sum of 2 items equals total price Thread 3

    Given open browser
    When open main page
    And navigate to all products page
    And add product TestData.product1 to cart
    And add product TestData.product2 to cart
    And navigate to checkout page
    Then verify that SubTotal value equals sum of totals per product type