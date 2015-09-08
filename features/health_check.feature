Feature: The service health is monitored

  In order to know if my service is healthy
  As a support operator
  I want to be able to ping the service


  Scenario: Successful ping
    When I ping the microservice service using the endpoint '/ping/ping'
    Then I should get a successful response