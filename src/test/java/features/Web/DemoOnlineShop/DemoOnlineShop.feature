@demoOnline
Feature: DemoOnlineShop

  Scenario: Verify sum of 2 items equals total price
    Given open browser
    When open main page
      And navigate to all products page
      And add product TestData.product1 to cart
      And add product TestData.product2 to cart
      And navigate to checkout page
    Then verify that SubTotal value equals sum of totals per product type