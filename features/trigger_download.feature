Feature: Triggering a download of an EMIS report

  Scenario: Manually triggered download for today
    Given an EMIS report is available for today's date
    And the RCS system is available
    When I trigger the download for today
    Then the RCS system receives a report
    And I see 'Completed the processing of the report' in the logs at INFO level


  Scenario: The next download is scheduled when the application starts
    Given the application is running
    Then I see 'Next download scheduled for ' in the logs since the start of the application at INFO level
