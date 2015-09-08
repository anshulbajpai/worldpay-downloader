/*
 * Copyright 2015 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unit.config

import config.Base64Decoder
import sun.misc.{BASE64Encoder, BASE64Decoder}
import uk.gov.hmrc.play.test.UnitSpec

class Base64DecoderSpec extends UnitSpec {

  val valueToDecode64 = """LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlKS1FJQkFBS0NBZ0VBcklCNk1PdHJydm1qOC8yNTd3Ni9ZdUNkWnF5SmozakNsbHV0cmdFUjlJYVNOQkxTClhOcmt0emxHVkU1ZmZQdDFiWWRGTldWUzB1ZXpFZmVlcGFHTUtyVUs4U1NjMTFNZktyNHlyVWQ3Z01TSVdpeEoKS2FrM0RQdHljNjdaRDltQXRtZnQ5NkFDYXMyMUlpTSt1blVFazZEcGkyNmFWeUpGZ0pITEZpUngrQS9tdVF0bAo4YjdpdFFaYmQ5QXVZSVJyQTZaRzF0SVJEbEk3Y21heW13RHRnL0Y5WUZ2R3c3dWZIOWdKdkhCWG9ZSDZuMHAyCjFsVmRRNXRlcUpzbktMTFRtc29wdlR0OVRqbkQxeTJPVTYvbHhnWEhnb0xCeG5xa3BNVGRQM09hbVFZa2h3UVkKTkd6U2IxUzRRYU8rei9mbHNWUS9MYVNlbkN2YVFoUkFscDhLQTNkL2U0eHI0aDNRUHNRWnBzaDdkNWZ4bzViTQpCamxxZ2o2UkcwVk52NHFMTytoY0QzbDBGRXNLOHZyK2RLTkQ0NDloZ3pZWEtsU0dUR05ZUG1DYVQ0M3Y5cDFpClN6Wmwvb1dOSE04Ujk1Tzk2TzA2Ym9UUVcwNk9GVU82Z1lPbnBwSHlyTzBVNEk4SjgzR0pZR1N1UDRDMXlzSk8KWlJRbk9zcWdFcUVLSS82YkZRMDRqNUFFOEdyV25UbWk0WmZUblJUQ1llTGg2ZW95MG1jWW50aU9SNTlDcVVjUgpVWkg1WG9iRGNWV0tpQjF3QzlXWWEvTlRUclZmL1pWR0JCN0FiNFRsYVkvQzR1NkY3alhwN1lHWU5MeTQrWGFaCmtXN2VBMXQ2TXUxbTdkd3B6UUNaL3J1UzRySXpKQzVERGdxaGNpRUxvR09CYSs2OU1kMWlWaUpOQ0RVQ0F3RUEKQVFLQ0FnQjBGYldsREJpWVZLa1B2aFNOR3FpUzJzTXg0RFJtVjRiamRtaW5xMUt3OG9xYS9udVFQMEloUnlyYgoreTNwUnJOY2RPYVBLMU9VV0pZNkppQWVLRnZTTmFsYVpDQWtDMnp5NzdIY3lEa0ZpN3lTcWpER0YzalRoTGNkCndIR1VaL05sVFFWb1djYW1weTRZL2d6dG4yZm9EU1o5VFg0UjJ4OVlpVVJZNzBlKy9yeHVsSXdUSmprY1IxM3cKZ0NsbGpGY1R5cWJyVTh4a2pkUXhoWWoySktheDN5WlhYUXVpbHJWYjBaK2pyeHBadHNXNHdueThlZy9jT2tkMgpRWXNWcVZvYlZhYkhFS3lmb3VIM0FWSkJZU0xJb3dVREFBbDFEdjJUWUdQZm1tVlVTenp6bXo3S3NOemdUT3YrClNEcFZhK2FuOGlEZUQyVXBoWjFJVUtheWRlNWZFUnBRQzRDL3RScWpLVm9qZVF3OW5UczJmRkFHZVduMWc3TDkKdWxsTXBFVFozZFZwVS9TZUkyRHdCcytlSktTemRvZGp0Nlg1NkxGaDlXMVZBZ245QXNTdnJTTXpGdytrTkR4RwpaWkl0WUFEeFRDUVB6WHd0WlF3UTVtQ1NxR3NFblZLY2F6a3AzbkZGVlJkaDBRdEc0Rjh1bHMzczl3MWlZQTA4Cm93c0xLMVQ2d1Z6VEpvZDJxdDQvVnJhb2xMQjVRZHdmTWpqcVVEOXQxamZBaVNaeE9heVRueE9RbWJtOUJBMk0KNkNlTFB6THk4R0NVd1NqMk5taUsrVTNFUEl5cllGYUFtcTBabm4zL1hyeDBTTURaM25xWXJtVExZNXJYcUY3bQpBYXd5N0I0RUpHbEhiWGJqbzRXZWZWUWN1MG03Z3FlY3pkVE45bko5Wi8rN1B2VTFRUUtDQVFFQTQvcEJoSEZrClhJQmt4Nmg3RER3clI0TTl4MnB2SDUxNzdYQk5nZGlOOGpwbEwwM2pnREZCWHloMjYwc3FnNXdSaXpwaHkyWm8KSmFyZkc1SXVoQWRDUEZKK3ZadnNFQ3FkNlYva2R5Qnh5Mkx2UU5lbURYdkwyT2pkM1V5Q2pYUlhmQXBRN0hEcgpXOFQzOHdBSEt4K01uQVdqeEtKL1R4aGlZSmdvUTBIVTBLRmx3Q2tzRGovdDYyNVJaQnlxUEFvZWdmMXlTa1Q2Ck9xZU9DQXU4TzFRcWxBV1NpM0MrYUlGSFpZV1JheHkwdXBHbmlNeS9RN01ncTIyNWJBMzlBaGJXTTFKSi9aK2IKWkNHNjk4eTFZTzlpdEVDUytyR2FtTUNLUjRRU1VOaGVKZExtVlFIMU1lN3hBcVJhZlRadkcxR1dHdVhaT3dCcQpPUDdvTCs0MTNsNXlSUUtDQVFFQXdiU1M2d0RPTHpiUEYwZmd3eHFwVmZzL2NFeUxSTHFveTVjc296c0RxK28vCllhdmdOejRvRHJOQmp1djBTaVBwRGFab0MvMkQ1dE9FYWF1NjAvWGVnQmdnMjhVN2oxTmZQYXRWTkhIdEkyQVgKMjFaUE1BVmY5QldNRmlrM2xHeUdUWDI3QWRzbHVTRGVGZ3M3ajVZMTNVbHlNSUxMNm8yN051eDVJbTZ0Nkl1MApZa1JJY1orWkZ4Tjlhc29YN0xYR3lzbUc2MzhGcDdpTjFKbi9EcWVhYzVQTlNoYWJlbkR6dzRBQnN6OTN5UkF4CnhPTysxU3ZrN1lkS2lvak5Kcmp2WUtUbHR5Wk5TNWw3T0p6R2E1RWp6K1ZpN29EaHRnNFZYWXlCTzBQUDZrQVQKYlZIY1U4RW9jeUZrK0hpcFVTRFdicFBLTGdtNU5ZZGdqKzQvV2N1Vk1RS0NBUUFoWEI5Mk4vbU5wNlFYcTBuRwpNby9LdVNPektKbjJNRjlRRVZ6bUlVQTBMK05hbHJVeHJ3TjMzejMzbEZvMlJBSTFNMmZZRWJZdnZOOUU4NUVtCjlNRDJmc1BaRHhYdUQ2NW4xZ1ppZ0RibW9pV01nWUhrK3phTEdSaE9KOEg2M1Fsb04wSThOeEhacmVyeWZoZEl2QWVPenRJR3BycjkKN0I4S0g1TC80YkZoZXpEclJBcmVvQ2g1WG1GWmRINWxIZHpMZkpCTU9DMFp3cWlhUGFHZk1UMDRtL0l1SWlxYwpCWU1DanI0TTJOVUtRSlRnNVZoWVoxR3lNcEhWdzgvT0l6UjJ4bG4xbS91MmdESlg1eGZYYlUvN3NXWk0KLS0tLS1FTkQgUlNBIFBSSVZBVEUgS0VZLS0tLS0K"""

  val valueDecoded64 = """-----BEGIN RSA PRIVATE KEY-----
                         |MIIJKQIBAAKCAgEArIB6MOtrrvmj8/257w6/YuCdZqyJj3jCllutrgER9IaSNBLS
                         |XNrktzlGVE5ffPt1bYdFNWVS0uezEfeepaGMKrUK8SSc11MfKr4yrUd7gMSIWixJ
                         |Kak3DPtyc67ZD9mAtmft96ACas21IiM+unUEk6Dpi26aVyJFgJHLFiRx+A/muQtl
                         |8b7itQZbd9AuYIRrA6ZG1tIRDlI7cmaymwDtg/F9YFvGw7ufH9gJvHBXoYH6n0p2
                         |1lVdQ5teqJsnKLLTmsopvTt9TjnD1y2OU6/lxgXHgoLBxnqkpMTdP3OamQYkhwQY
                         |NGzSb1S4QaO+z/flsVQ/LaSenCvaQhRAlp8KA3d/e4xr4h3QPsQZpsh7d5fxo5bM
                         |Bjlqgj6RG0VNv4qLO+hcD3l0FEsK8vr+dKND449hgzYXKlSGTGNYPmCaT43v9p1i
                         |SzZl/oWNHM8R95O96O06boTQW06OFUO6gYOnppHyrO0U4I8J83GJYGSuP4C1ysJO
                         |ZRQnOsqgEqEKI/6bFQ04j5AE8GrWnTmi4ZfTnRTCYeLh6eoy0mcYntiOR59CqUcR
                         |UZH5XobDcVWKiB1wC9WYa/NTTrVf/ZVGBB7Ab4TlaY/C4u6F7jXp7YGYNLy4+XaZ
                         |kW7eA1t6Mu1m7dwpzQCZ/ruS4rIzJC5DDgqhciELoGOBa+69Md1iViJNCDUCAwEA
                         |AQKCAgB0FbWlDBiYVKkPvhSNGqiS2sMx4DRmV4bjdminq1Kw8oqa/nuQP0IhRyrb
                         |+y3pRrNcdOaPK1OUWJY6JiAeKFvSNalaZCAkC2zy77HcyDkFi7ySqjDGF3jThLcd
                         |wHGUZ/NlTQVoWcampy4Y/gztn2foDSZ9TX4R2x9YiURY70e+/rxulIwTJjkcR13w
                         |gClljFcTyqbrU8xkjdQxhYj2JKax3yZXXQuilrVb0Z+jrxpZtsW4wny8eg/cOkd2
                         |QYsVqVobVabHEKyfouH3AVJBYSLIowUDAAl1Dv2TYGPfmmVUSzzzmz7KsNzgTOv+
                         |SDpVa+an8iDeD2UphZ1IUKayde5fERpQC4C/tRqjKVojeQw9nTs2fFAGeWn1g7L9
                         |ullMpETZ3dVpU/SeI2DwBs+eJKSzdodjt6X56LFh9W1VAgn9AsSvrSMzFw+kNDxG
                         |ZZItYADxTCQPzXwtZQwQ5mCSqGsEnVKcazkp3nFFVRdh0QtG4F8uls3s9w1iYA08
                         |owsLK1T6wVzTJod2qt4/VraolLB5QdwfMjjqUD9t1jfAiSZxOayTnxOQmbm9BA2M
                         |6CeLPzLy8GCUwSj2NmiK+U3EPIyrYFaAmq0Znn3/Xrx0SMDZ3nqYrmTLY5rXqF7m
                         |Aawy7B4EJGlHbXbjo4WefVQcu0m7gqeczdTN9nJ9Z/+7PvU1QQKCAQEA4/pBhHFk
                         |XIBkx6h7DDwrR4M9x2pvH5177XBNgdiN8jplL03jgDFBXyh260sqg5wRizphy2Zo
                         |JarfG5IuhAdCPFJ+vZvsECqd6V/kdyBxy2LvQNemDXvL2Ojd3UyCjXRXfApQ7HDr
                         |W8T38wAHKx+MnAWjxKJ/TxhiYJgoQ0HU0KFlwCksDj/t625RZByqPAoegf1ySkT6
                         |OqeOCAu8O1QqlAWSi3C+aIFHZYWRaxy0upGniMy/Q7Mgq225bA39AhbWM1JJ/Z+b
                         |ZCG698y1YO9itECS+rGamMCKR4QSUNheJdLmVQH1Me7xAqRafTZvG1GWGuXZOwBq
                         |OP7oL+413l5yRQKCAQEAwbSS6wDOLzbPF0fgwxqpVfs/cEyLRLqoy5csozsDq+o/
                         |YavgNz4oDrNBjuv0SiPpDaZoC/2D5tOEaau60/XegBgg28U7j1NfPatVNHHtI2AX
                         |21ZPMAVf9BWMFik3lGyGTX27AdsluSDeFgs7j5Y13UlyMILL6o27Nux5Im6t6Iu0
                         |YkRIcZ+ZFxN9asoX7LXGysmG638Fp7iN1Jn/Dqeac5PNShabenDzw4ABsz93yRAx
                         |xOO+1Svk7YdKiojNJrjvYKTltyZNS5l7OJzGa5Ejz+Vi7oDhtg4VXYyBO0PP6kAT
                         |bVHcU8EocyFk+HipUSDWbpPKLgm5NYdgj+4/WcuVMQKCAQAhXB92N/mNp6QXq0nG
                         |Mo/KuSOzKJn2MF9QEVzmIUA0L+NalrUxrwN33z33lFo2RAI1M2fYEbYvvN9E85Em
                         |9MD2fsPZDxXuD65n1gZigDbmoiWMgYHk+zaLGRhOJ8H63QloN0I8NxHZreryfhdIvAeOztIGprr9
                         |7B8KH5L/4bFhezDrRAreoCh5XmFZdH5lHdzLfJBMOC0ZwqiaPaGfMT04m/IuIiqc
                         |BYMCjr4M2NUKQJTg5VhYZ1GyMpHVw8/OIzR2xln1m/u2gDJX5xfXbU/7sWZM
                         |-----END RSA PRIVATE KEY-----
                         |""".stripMargin

  "Base64Decoder" should {
    "decode the value " in {
      Base64Decoder.decode(valueToDecode64) shouldBe valueDecoded64
    }
  }


}
