Feature: Parse and send the EMIS report to RCS

  In order to process the Report
  As the Tax Platform
  I want to download it from WorldPay and send the payment transactions to RCS

  Background: RCS is available
    Given the RCS Initiate Payment Transmission through HodsApi is available on "/payments/worldpay/.*" returning a 200 status on success
    And the RCS Send Transaction Information through HodsApi is available on "/payments/worldpay/.*/chunks/[0-9]+" returning a 202 status on success
    And the RCS Release through HodsApi is available on "/payments/worldpay/.*/release" returning a 200 status on success
    And I have valid private and public key to the WorldPay SFTP site

  Scenario: Successful RCS payments transmission
    Given a Report "MA.PISCESSW.#M.RECON.HMRE.D010114" is available for download on WorldPay containing
    """
         |000000000100000000009210000006000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002
         |050000000200000001550650000001000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |10000000030000011010000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |1500000004465935******7108   1506000000025000101141158000E041920A                            ADE000
         |160000000580729310000000000000K1234567890K-1234560
         |1500000006465935******7108   1506000001234560101141158000E041920A                            ADE000
         |160000000780729310000000000000V1234567891014-Y5RY0"""
    When I trigger the download for the date "2014/01/01"
    Then a POST to the RCS Initiate Payment Transmission through HodsApi endpoint "/payments/worldpay/[^/]+" is sent exactly once
    And a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/.*" is sent exactly once
    And a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/1" is sent with payload:
    """
	  {
        "payments": [
          {
            "consecNumber": 1,
            "date": "14001",
            "paymentProviderId": "1234567890KK1234567890K-1234560",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 2500,
                  "reference": "1234567890K"
              }
            ]
          },
          {
            "consecNumber": 2,
            "date": "14001",
            "paymentProviderId": "1234567891014V1234567891014-Y5RY0",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 123456,
                  "reference": "1234567891014"
              }
            ]
          }
        ]
      }
	  """
    And a POST to the RCS Release Data Set through HodsApi endpoint "/payments/worldpay/.*/release" is sent with payload:
    """
        {
          "chunks":
          [
            {
              "chunkId":"1",
              "transactionTypes":
              [
                {
                  "transactionType":"26",
                  "totalValue":125956,
                  "numberOfPayments":2
                }
              ]
            }
          ]
        }
        """


  Scenario: Only 'Accepted' payments are sent to RCS
    Given a Report "MA.PISCESSW.#M.RECON.HMRE.D010114" is available for download on WorldPay containing
    """
         |000000000100000000009210000006000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002
         |050000000200000001550650000001000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |10000000030000011010000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |1500000004465935******7108   1506000000025000101141158000E041920A                            ADE000
         |160000000580729310000000000000K1234567890K-1234560
         |1500000006465935******7108   1506000001234560101141158000E041920R                            ADE000
         |160000000780729310000000000000K1234567890K-9999990"""
    When I trigger the download for the date "2014/01/01"
    Then a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/1" is sent with payload:
    """
	  {
        "payments": [
          {
            "consecNumber": 1,
            "date": "14001",
            "paymentProviderId": "1234567890KK1234567890K-1234560",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 2500,
                  "reference": "1234567890K"
              }
            ]
          }
        ]
      }
	  """


  Scenario: Only 'Purchase' transaction type payments are sent to RCS
    Given a Report "MA.PISCESSW.#M.RECON.HMRE.D010114" is available for download on WorldPay containing
    """
         |000000000100000000009210000006000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002
         |050000000200000001550650000001000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |10000000030000011010000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |1500000004465935******7108   1506000000025000101141158000E041920A                            ADE000
         |160000000580729310000000000000K1234567890K-1234560
         |1500000006465935******7108   1506000001234560101141158005E041920A                            ADE000
         |160000000780729310000000000000K9999999999K-1234560"""
    When I trigger the download for the date "2014/01/01"
    Then a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/1" is sent with payload:
    """
	  {
        "payments": [
          {
            "consecNumber": 1,
            "date": "14001",
            "paymentProviderId": "1234567890KK1234567890K-1234560",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 2500,
                  "reference": "1234567890K"
              }
            ]
          }
        ]
      }
	  """
    And I see 'Transaction is not a Purchase, but a Refund: K9999999999K-1234560' in the logs at WARN level

  Scenario: Only transactions with expected MID's are sent to RCS
    Given a Report "MA.PISCESSW.#M.RECON.HMRE.D010114" is available for download on WorldPay containing
    """
         |000000000100000000009210000006000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002
         |050000000200000001550650000001000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |10000000030000006116702140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |1500000004465935******7108   1506000000025000101141158000E041920A                            ADE000
         |160000000580729310000000000000K9988776655K-1234560
         |10000000030000011010000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |1500000004465935******7108   1506000000025000101141158000E041920A                            ADE000
         |160000000580729310000000000000K1234567890K-9999990"""
    When I trigger the download for the date "2014/01/01"
    Then a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/1" is sent with payload:
    """
	  {
        "payments": [
          {
            "consecNumber": 1,
            "date": "14001",
            "paymentProviderId": "1234567890KK1234567890K-9999990",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 2500,
                  "reference": "1234567890K"
              }
            ]
          }
        ]
      }
	  """


  Scenario Outline: Correct bank account is sent to RCS for Debit Cards
    Given a Report "MA.PISCESSW.#M.RECON.HMRE.D010114" is available for download on WorldPay containing
    """
         |000000000100000000009210000006000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002
         |050000000200000001550650000001000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |100000000300000<merchant id>140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |1500000004465935******7108   1506000000025000101141158000E041920A                            ADE000
         |160000000580729310000000000000K1234567890K-1234560"""
    When I trigger the download for the date "2014/01/01"
    Then a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/1" is sent with payload:
    """
	  {
        "payments": [
          {
            "consecNumber": 1,
            "date": "14001",
            "paymentProviderId": "1234567890KK1234567890K-1234560",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "<bank account>",
                  "amount": 2500,
                  "reference": "1234567890K"
              }
            ]
          }
        ]
      }
	  """
  Examples:
    | merchant id | bank account | tax             |
    | 11010000    | 33333333     | Self Assessment |
    | 12010000    | 55555555     | VAT             |

  @logs @YTA-1122 @YTA-1132
  Scenario: Successful RCS payments transmission for multiple chunks
    Given a Report "MA.PISCESSW.#M.RECON.HMRE.D021115" is available for download on WorldPay containing
    """
         |000000000100000000009210000001000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002
         |050000000200000001550650000001000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |10000000030000011010000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000400000000000000
         |1500000004465935******7108   1506000000025000211151158000E041920A                            ADE000
         |160000000580729310000000000000K1234567890K-1234560
         |1500000006465935******7108   1506000000026000211151158000E041920A                            ADE000
         |160000000780729310000000000000K1234567890K-1234560
         |1500000006465935******7108   1506000000027000211151158000E041920A                            ADE000
         |160000000780729310000000000000K1234567890K-1234560
         |1500000006465935******7108   1506000000028000211151158000E041920A                            ADE000
         |160000000780729310000000000000K1234567890K-1234560
         |10000000030000012010000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000100000000000000
         |1500000006465935******7108   1506000000030000211151158000E041920A                            ADE000
         |160000000780729310000000000000V1234567891014-Y5RY0
         |10000000030000013010000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000100000000000000
         |1500000006465935******7108   1506000000044440211151158000E041920A                            ADE000
         |160000000780729310000000000000A109717256408A-TTTT0
         |10000000030000014010000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000100000000000000
         |1500000006465935******7108   1506000000135790211151158000E041920A                            ADE000
         |160000000780729310000000000000PRPR823ILT16C-7AYSA0
         |1500000006465935******7108   1506000000125790211151158000E041920A                            ADE000
         |160000000780729310000000000000PFBO0T5QKG2RC-YAYSA0
         |10000000000000015010000140411000000000000000000000000000000000000000000479295360000000000000000000000000000000000000000000000001000000000000000
         |1500000000000000******0000   00000000639521102111500000000000000A                            000000
         |160000000000000000000000000000XC000843562584-8YSB0
         |1500000000000000******0000   00000000498261402111500000000000000A                            000000
         |160000000000000000000000000000XG001445528149-8YSG0
         |10000000000000015020000140411000000000000000000000000000000000000000000047353320000000000000000000000000000000000000000000000000200000000000000
         |1500000000000000******0000   00000000317334902111500000000000000A                            000000
         |160000000000000000000000000000XC000843562584-B2YS0
         |1500000000000000******0000   00000000156198302111500000000000000A                            000000
         |160000000000000000000000000000XG001445528149-B2YZ0
         |10000000000000016010000140411000000000000000000000000000000000000000000479295360000000000000000000000000000000000000000000000001000000000000000
         |1500000000000000******0000   00000000440521102111500000000000000A                            000000
         |160000000000000000000000000000M123456789MA-ABB2YZ0
         |1500000000000000******0000   00000000228661402111500000000000000A                            000000
         |160000000000000000000000000000M680686481MW-ABB2YZ0
         |10000000000000016020000140411000000000000000000000000000000000000000000479295360000000000000000000000000000000000000000000000001000000000000000
         |1500000000000000******0000   00000000240561102111500000000000000A                            000000
         |160000000000000000000000000000M345726781MA-XYB2YZ0
         |1500000000000000******0000   00000000428641402111500000000000000A                            000000
         |160000000000000000000000000000M680686481MW-OOD2YZ0
         """


    When I trigger the download with chunk size "2" for the date "2015/11/02"
    Then a POST to the RCS Initiate Payment Transmission through HodsApi endpoint "/payments/worldpay/[^/]+" is sent exactly once
    And a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/.*" is sent exactly 8 times
    And a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/1" is sent with payload:
    """
	  {
        "payments": [
          {
            "consecNumber": 1,
            "date": "15306",
            "paymentProviderId": "1234567890KK1234567890K-1234560",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 2500,
                  "reference": "1234567890K"
              }
            ]
          },
          {
            "consecNumber": 2,
            "date": "15306",
            "paymentProviderId": "1234567890KK1234567890K-1234560",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 2600,
                  "reference": "1234567890K"
              }
            ]
          }
        ]
      }
	  """
    And a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/2" is sent with payload:
    """
	  {
        "payments": [
          {
            "consecNumber": 3,
            "date": "15306",
            "paymentProviderId": "1234567890KK1234567890K-1234560",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 2700,
                  "reference": "1234567890K"
              }
            ]
          },
          {
            "consecNumber": 4,
            "date": "15306",
            "paymentProviderId": "1234567890KK1234567890K-1234560",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 2800,
                  "reference": "1234567890K"
              }
            ]
          }
        ]
      }
	  """
    And a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/3" is sent with payload:
    """
	  {
        "payments": [
          {
            "consecNumber": 5,
            "date": "15306",
            "paymentProviderId": "1234567891014V1234567891014-Y5RY0",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "55555555",
                  "amount": 3000,
                  "reference": "1234567891014"
              }
            ]
          },
          {
            "consecNumber": 6,
            "date": "15306",
            "paymentProviderId": "1097172564A00108AA109717256408A-TTTT0",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 4444,
                  "reference": "1097172564A00108A"
              }
            ]
          }
        ]
      }
	  """
    And a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/4" is sent with payload:
    """
	  {
        "payments": [
          {
            "consecNumber": 7,
            "date": "15306",
            "paymentProviderId": "997PR375966LT4212PRPR823ILT16C-7AYSA0",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 13579,
                  "reference": "997PR375966LT4212"
              }
            ]
          },
          {
            "consecNumber": 8,
            "date": "15306",
            "paymentProviderId": "551PO037790KG9912PFBO0T5QKG2RC-YAYSA0",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 12579,
                  "reference": "551PO037790KG9912"
              }
            ]
          }
        ]
      }
	  """
    And a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/5" is sent with payload:
    """
	  {
        "payments": [
          {
            "consecNumber": 9,
            "date": "15306",
            "paymentProviderId": "XC000843562584XC000843562584-8YSB0",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 6395211,
                  "reference": "XC000843562584"
              }
            ]
          },
          {
            "consecNumber": 10,
            "date": "15306",
            "paymentProviderId": "XG001445528149XG001445528149-8YSG0",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 4982614,
                  "reference": "XG001445528149"
              }
            ]
          }
        ]
      }
	  """
    And a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/6" is sent with payload:
    """
	  {
        "payments": [
          {
            "consecNumber": 11,
            "date": "15306",
            "paymentProviderId": "XC000843562584XC000843562584-B2YS0",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 3126452,
                  "reference": "XC000843562584"
              },
               {
                  "transactionType": "27",
                  "destinationAccountNumber": "33333333",
                  "amount": 46897,
                  "reference": "XC000843562584"
              }
            ]
          },
          {
            "consecNumber": 12,
            "date": "15306",
            "paymentProviderId": "XG001445528149XG001445528149-B2YZ0",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 1538900,
                  "reference": "XG001445528149"
              },
              {
                  "transactionType": "27",
                  "destinationAccountNumber": "33333333",
                  "amount": 23083,
                  "reference": "XG001445528149"
              }
            ]
          }
        ]
      }
	  """
    And a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/7" is sent with payload:
    """
	  {
        "payments": [
          {
            "consecNumber": 13,
            "date": "15306",
            "paymentProviderId": "123456789MAM123456789MA-ABB2YZ0",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 4405211,
                  "reference": "123456789MA"
              }
            ]
          },
          {
            "consecNumber": 14,
            "date": "15306",
            "paymentProviderId": "680686481MWM680686481MW-ABB2YZ0",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 2286614,
                  "reference": "680686481MW"
              }
            ]
          }
        ]
      }
	  """
    And a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/8" is sent with payload:
    """
	  {
        "payments": [
          {
            "consecNumber": 15,
            "date": "15306",
            "paymentProviderId": "345726781MAM345726781MA-XYB2YZ0",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 2370060,
                  "reference": "345726781MA"
              },
               {
                  "transactionType": "27",
                  "destinationAccountNumber": "33333333",
                  "amount": 35551,
                  "reference": "345726781MA"
              }
            ]
          },
          {
            "consecNumber": 16,
            "date": "15306",
            "paymentProviderId": "680686481MWM680686481MW-OOD2YZ0",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 4223068,
                  "reference": "680686481MW"
              },
              {
                  "transactionType": "27",
                  "destinationAccountNumber": "33333333",
                  "amount": 63346,
                  "reference": "680686481MW"
              }
            ]
          }
        ]
      }
	  """
    And a POST to the RCS Release Data Set through HodsApi endpoint "/payments/worldpay/.*/release" is sent with payload:
    """
        {
          "chunks":
          [
            {
              "chunkId":"1",
              "transactionTypes":
              [
                {
                  "transactionType":"26",
                  "totalValue":5100,
                  "numberOfPayments":2
                }
              ]
            },
            {
              "chunkId":"2",
              "transactionTypes":
              [
                {
                  "transactionType":"26",
                  "totalValue":5500,
                  "numberOfPayments":2
                }
              ]
            },
            {
              "chunkId":"3",
              "transactionTypes":
              [
                {
                  "transactionType":"26",
                  "totalValue":7444,
                  "numberOfPayments":2
                }
              ]
            },
            {
              "chunkId":"4",
              "transactionTypes":
              [
                {
                  "transactionType":"26",
                  "totalValue":26158,
                  "numberOfPayments":2
                }
              ]
            },
            {
              "chunkId":"5",
              "transactionTypes":
              [
                {
                  "transactionType":"26",
                  "totalValue":11377825,
                  "numberOfPayments":2
                }
              ]
            },
            {
              "chunkId":"6",
              "transactionTypes":
              [
                {
                  "transactionType":"26",
                  "totalValue":4665352,
                  "numberOfPayments":2
                }
              ]
            },
            {
              "chunkId":"7",
              "transactionTypes":
              [
                {
                  "transactionType":"26",
                  "totalValue":6691825,
                  "numberOfPayments":2
                }
              ]
            },
            {
              "chunkId":"8",
              "transactionTypes":
              [
                {
                  "transactionType":"26",
                  "totalValue":6593128,
                  "numberOfPayments":2
                }
              ]
            }
          ]
        }
        """

  Scenario: Empty downloaded EMIS reports cause no interaction with RCS
    Given an empty Report "MA.PISCESSW.#M.RECON.HMRE.D010114" is available for download on WorldPay
    When I trigger the download for the date "2014/01/01"
    Then a POST to the endpoint "/payments/worldpay/.*" is NOT sent


  @PAYM-369 @YTA-1132
  Scenario: Commission is separated when sent to RCS
  Given a Report "MA.PISCESSW.#M.RECON.HMRE.D121215" is available for download on WorldPay containing
  """
         |000000000100000000009210000001000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002
         |050000000200000001550650000001000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
         |10000000030000011020000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000500000000000000
         |1500000004465935******7108   1506000000100591212151158000E041920A                            ADE000
         |160000000580729310000000000000K2234567890K-1234560
         |10000000060000012020000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000100000000000000
         |1500000007465935******7108   1506000099879001212151158000E041920A                            ADE000
         |160000000880729310000000000000V1234567891014-Y5RY0"""
    When I trigger the download with chunk size "2" for the date "2015/12/12"
    Then a POST to the RCS Initiate Payment Transmission through HodsApi endpoint "/payments/worldpay/[^/]+" is sent exactly once
    And a POST to the RCS Send Transaction Information through HodsApi endpoint "/payments/worldpay/.*/chunks/1" is sent with payload:
    """
	  {
        "payments": [
          {
            "consecNumber": 1,
            "date": "15346",
            "paymentProviderId": "2234567890KK2234567890K-1234560",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "33333333",
                  "amount": 9910,
                  "reference": "2234567890K"
              },
              {
                  "transactionType": "27",
                  "destinationAccountNumber": "33333333",
                  "amount": 149,
                  "reference": "2234567890K"
              }
            ]
          },
          {
            "consecNumber": 2,
            "date": "15346",
            "paymentProviderId": "1234567891014V1234567891014-Y5RY0",
            "paymentItems": [
              {
                  "transactionType": "26",
                  "destinationAccountNumber": "55555555",
                  "amount": 9840296,
                  "reference": "1234567891014"
              },
              {
                  "transactionType": "27",
                  "destinationAccountNumber": "55555555",
                  "amount": 147604,
                  "reference": "1234567891014"
              }
            ]
          }
        ]
      }
	  """

    @logs
    Scenario: Payments processed are logged
      Given a Report "MA.PISCESSW.#M.RECON.HMRE.D150411" is available for download on WorldPay containing
      """
      |000000000100000000009210000001000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002
      |050000000200000001550650000001000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
      |10000000030000011010000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000200000000000000
      |1500000004465935******7108   1506000000025000101141158000E041920A                            ADE000
      |160000000580729310000000000000K1234567890K-1234560
      |1500000004465935******7108   1506000000025000101141158000E041920A                            ADE000
      |160000000580729310000000000000K1234567890K-1234560
      |10000000030000011020000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000100000000000000
      |1500000004465935******7108   1506000000025000101141158000E041920A                            ADE000
      |160000000580729310000000000000K1234567890K-1234560
      |10000000030000012010000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000200000000000000
      |1500000006465935******7108   1506000000030000101141158000E041920A                            ADE000
      |160000000780729310000000000000V1234567891014-Y5RY0
      |1500000006465935******7108   1506000000030000101141158000E041920A                            ADE000
      |160000000780729310000000000000V1234567891014-Y5RY0
      |10000000030000012020000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000100000000000000
      |1500000006465935******7108   1506000000030000101141158000E041920A                            ADE000
      |160000000780729310000000000000V1234567891014-Y5RY0
      |10000000030000013010000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000200000000000000
      |1500000006465935******7108   1506000000044440101141158000E041920A                            ADE000
      |160000000780729310000000000000A109717256408A-TTTT0
      |1500000006465935******7108   1506000000044440101141158000E041920A                            ADE000
      |160000000780729310000000000000A109717256408A-TTTT0
      |10000000030000013020000140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000100000000000000
      |1500000006465935******7108   1506000000042440101141158000E041920A                            ADE000
      |160000000780729310000000000000A106717256408A-TTTT0
      |10000000000000015010000140411000000000000000000000000000000000000000000479295360000000000000000000000000000000000000000000000000200000000000000
      |1500000000000000******0000   00000000639521101011400000000000000A                            000000
      |160000000000000000000000000000XC000843562584-8YSB0
      |1500000000000000******0000   00000000498261401011400000000000000A                            000000
      |160000000000000000000000000000XG001445528149-8YSG0
      |10000000000000015020000140411000000000000000000000000000000000000000000047353320000000000000000000000000000000000000000000000000100000000000000
      |1500000000000000******0000   00000000156198301011400000000000000A                            000000
      |160000000000000000000000000000XG001445528149-B2YZ0"""
      When I trigger the download for the date "2011/04/15"
      Then I see 'processing EMIS report with header 0000000001' in the logs at INFO level
      And I see 'processing merchant ID 11010000 (SA - Debit Card) with 2 payments for 14/04/2011' in the logs at INFO level
      And I see 'processing merchant ID 11020000 (SA - Credit Card) with 1 payments for 14/04/2011' in the logs at INFO level
      And I see 'processing merchant ID 12010000 (VAT - Debit Card) with 2 payments for 14/04/2011' in the logs at INFO level
      And I see 'processing merchant ID 12020000 (VAT - Credit Card) with 1 payments for 14/04/2011' in the logs at INFO level
      And I see 'processing merchant ID 13010000 (CT - Debit Card) with 2 payments for 14/04/2011' in the logs at INFO level
      And I see 'processing merchant ID 13020000 (CT - Credit Card) with 1 payments for 14/04/2011' in the logs at INFO level
      And I see 'processing merchant ID 15010000 (OTHER - Debit Card) with 2 payments for 14/04/2011' in the logs at INFO level
      And I see 'processing merchant ID 15020000 (OTHER - Credit Card) with 1 payments for 14/04/2011' in the logs at INFO level
      And I see 'processed 12 payments' in the logs at INFO level


#  The following is for documentation only, and it not executable.


#  Scenario: Describe the content of the File
#    Given an EMIS Streamline Reconciliation file containing:
#    """
#       |000000000100000000009210000006000000010000000000000000000000000000003334520000000000000000000000000000100000000000000000008000000000000000111050002002
#       |050000016200000001550650000001000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
#       |10000001630000051833083140411110415000000000000000000000000000000000000000096000000000000000000000000000000000000000000000000000600000000000000
#       |1500000164465935******7108   1506000000025001404111158000E041920A                            ADE000
#       |1600000165807293100000000000001234567890K-123456-0"""
#    Then the merchant outlet line begins with "10"
#    And the transaction data line begins with "15"
#    And the transaction supplementary data line begins with "16"
#    And the merchant id "51833083" should be found in the merchant outlet line position 11 with lenght 13 and without the leading zeroes
#    And the transaction date "140411" should be found in transaction data line position 45 with length 6
#    And the transaction amount in pence is "00000002500" should be found in transaction data line position 34 with length 11
#    And the payment status "A" should be found in transaction data line position 65 with length 1
#    And the Originators Transaction Reference is "1234567890K-123456-0" should be found in transaction supplementary data position 31 with length 20
#    And the Transaction Type '0' should be found in the Transaction data line position 57 with length 1