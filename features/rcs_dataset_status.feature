Feature: Reports sent to RCS have a status

  In order to send a report to RCS
  As the Tax Platform
  I want to know what is the status of any other reports sent before on the same day

  Background: RCS is available
    Given RCS is available
    And I have valid private and public key to the WorldPay SFTP site
    And an EMIS report is available for the date "2014/01/01"


  @PAYM-190
  Scenario Outline: Transactions are sent to RCS when an incomplete transmission exists
    Given RCS has a report for the date "2014/01/01" with id "123-456-789-0" with status "<status>"
    When I trigger the download for the date "2014/01/01"
    Then a GET to the RCS List Data Set through HodsApi endpoint "/payments/worldpay" is queried from "2014-01-01" to "2014-01-01"
    And a POST to the RCS Initiate Payment Transmission through HodsApi endpoint "/payments/worldpay/[^/]+" is sent
  Examples:
    | status       |
    | Open         |
    | Deleted      |


  @PAYM-142 @PAYM-152 @PAYM-190
  Scenario Outline: Force process of a report even if one has been released or contains an unexpected status for today to facilitate testing
    Given an EMIS report is available for today's date
    And RCS already has a report for today with status "<status>"
    When I force another download for today
    Then a POST to the RCS Initiate Payment Transmission through HodsApi endpoint "/payments/worldpay/[^/]+" is sent
    And I see 'EMIS report processing was forced' in the logs at WARN level
  Examples:
    | status   | notes                                                                |
    | Released | A dataset has already been released today                            |
    | Surprise | A completely unexpected status for a dataset already submitted today |


  @PAYM-190
  Scenario: Delete as a courtesy any existing Open dataset when we re-submit
    Given RCS has a report for the date "2014/01/01" with id "123-456-789-0" with status "Open"
    When I trigger the download for the date "2014/01/01"
    Then a DELETE to the RCS Delete Data Set through HodsApi endpoint "/payments/worldpay/123-456-789-0" is sent exactly once
    And a POST to the RCS Initiate Payment Transmission through HodsApi endpoint "/payments/worldpay/[^/]+" is sent


  @PAYM-190
  Scenario: Courtesy dataset delete is ignored and logged if it fails
    Given a DELETE to the RCS Delete Data Set through HodsApi endpoint "/payments/worldpay/123-456-789-0" will fail with status "403"
    And RCS has a report for the date "2014/01/01" with id "123-456-789-0" with status "Open"
    When I trigger the download for the date "2014/01/01"
    Then a DELETE to the RCS Delete Data Set through HodsApi endpoint "/payments/worldpay/123-456-789-0" is sent exactly once
    And a POST to the RCS Initiate Payment Transmission through HodsApi endpoint "/payments/worldpay/[^/]+" is sent
    And I see 'Could not delete dataset: 123-456-789-0' in the logs at WARN level
