Feature: Query for various information

  # Need to send one request, to ensure that the Scheduler has been initiated.
  Background:
    Given I ping the microservice service using the endpoint '/ping/ping'
    And I wait for a little bit while the app is initiated
    Then I should get a successful response

  Scenario: Asking for the next scheduled date
    When I ask for the next scheduled date
    Then I received a 200 response with a valid date

  Scenario: Asking for the current lock status, when no lock exists
    Given that the lock does not currently exist
    When I ask for the current lock status
    Then I receive a 404 response

  Scenario: Asking for the current lock status, when a lock does exist
    Given that the lock currently exists
    When I ask for the current lock status
    Then I receive a 200 response

  Scenario: Removing the current lock, when the lock service can remove it.
    Given we can release the lock
    When I request release of the current lock
    Then I receive a 200 response

  Scenario: Removing the current lock, when the lock service cannot remove it (or it never existed)
    Given we cannot release the lock
    When I request release of the current lock
    Then I receive a 500 response

  Scenario: Asking if the file for the given date has been processed, when it hasn't been
    Given that the file for 2015-04-01 has not been processed
    When I ask if the file for 2015-04-01 has been processed
    Then I receive a 404 response

  Scenario: Asking if the file for the given date has been processed, when it has been
    Given that the file for 2015-04-17 has already been processed
    When I ask if the file for 2015-04-17 has been processed
    Then I receive a 200 response

  Scenario: Requesting that the file for the given date be downloaded & processed, when we cannot create the lock
    Given that we cannot create the lock
    When I request that today's file be downloaded & processed
    Then I receive a 409 response

  Scenario: Requesting that the file for the given date be downloaded & processed, when we can create the lock but the file has already been processed.
    Given that we can create the lock
    But that today's file has already been processed
    When I request that today's file be downloaded & processed
    Then I receive a 400 response
    And we can release the lock
    Then we can verify that the lock was released

  Scenario: Requesting the re-initialisation of the Scheduler
    Given that we request a re-initialisation of the Scheduler
    Then I receive a 200 response