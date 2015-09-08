Feature: Session Id and Request Id
  In order to trace the request
  As the Tax Platform
  I want session id and request id added to request headers


  Scenario: Session Id and Request Id is added to headers
    Given RCS is available
    And I have valid private and public key to the WorldPay SFTP site
    And an EMIS report is available for the date "2014/01/01"
    And a Report "MA.PISCESSW.#M.RECON.HMRE.D010114" is available for download on WorldPay containing
    """
         |000000000100000000009210000006000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002
         |050000000200000001550650000001000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |10000000030000011010000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |1500000004465935******7108   1506000000025000101141158000E041920A                            ADE000
         |160000000580729310000000000000K1234567890K-1234560
         |1500000006465935******7108   1506000001234560101141158000E041920A                            ADE000
         |160000000780729310000000000000V1234567891014-Y5RY0"""
    When I trigger the download for the date "2014/01/01"
    Then a GET with requestId and sessionId in header is sent to endpoint "/payments/worldpay[^/]+"
    And a POST with requestId and sessionId in header is sent to endpoint "/payments/worldpay/[^/]+"
    And a POST with requestId and sessionId in header is sent to endpoint "/payments/worldpay/.*/chunks/[0-9]+"
    And a POST with requestId and sessionId in header is sent to endpoint "/payments/worldpay/[^/]+/release"