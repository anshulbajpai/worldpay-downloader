Feature: Failures

  In order to avoid sending bad data to RCS
  As the Tax Platform
  I want to handle some communication failures.

  Background:
    Given an EMIS report is available for the date "2014/01/01"


   Scenario Outline: A report has been released so a new one is not sent
     Given RCS has a report for the date "2014/01/01" with id "123-456-789-0" with status "<status>"
     When I trigger the download for the date "2014/01/01"
     Then a GET to the RCS List Data Set through HodsApi endpoint "/payments/worldpay" is queried from "2014-01-01" to "2014-01-01"
     And a POST to the RCS Initiate Payment Transmission through HodsApi endpoint "/payments/worldpay/[^/]+" is NOT sent
     And a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/1" is NOT sent
     And a POST to the RCS Release Data Set through HodsApi endpoint "/payments/worldpay/.*/release" is NOT sent
     And I see 'Not getting EMIS report as RCS told us data set for 2014-01-01 has already been released' in the logs at ERROR level
   Examples:
   | status       |
   | Released     |
   | Failed Recon |
   | Sending      |
   | Tech Failure |
   | Sent         |


  @PAYM-152
  Scenario: An unexpected dataset status is received so the report is not sent
    Given RCS has a report for the date "2014/01/01" with id "123-456-789-0" with status "Surprise"
    When I trigger the download for the date "2014/01/01"
    Then a GET to the RCS List Data Set through HodsApi endpoint "/payments/worldpay" is queried from "2014-01-01" to "2014-01-01"
    And a POST to the RCS Initiate Payment Transmission through HodsApi endpoint "/payments/worldpay/.*" is NOT sent
    And I see 'Not processing EMIS report due to unexpected status 'Surprise' returned from existing dataset: 123-456-789-0' in the logs at ERROR level


  Scenario: RCS fails and HodsApi returns a 502 error code during RCS initiation
    Given the RCS Initiate Payment Transmission through HodsApi on "/payments/worldpay/[^/]+" fails returning a 502 status
    When I trigger the download for the date "2014/01/01"
    Then I see 'Emis report processing failed' in the logs at ERROR level
    And a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/1" is NOT sent
