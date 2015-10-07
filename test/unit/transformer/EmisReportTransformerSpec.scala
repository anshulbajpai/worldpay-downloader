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

package unit.transformer

import model.PaymentStatus._
import model._
import org.joda.time.LocalDate
import parsers.StreamingEmisReportParser.MerchantTransaction
import support.steps.LogFinder
import transformer.EmisReportTransformer
import uk.gov.hmrc.play.test.UnitSpec
import unit.MerchantBuilder

class EmisReportTransformerSpec extends UnitSpec with EmisReportGenerator with LogFinder{

  val emisReportTransformer =  new EmisReportTransformer(merchants)

  val November_1_2015 = new LocalDate(2015, 11, 1)
  val November_2_2015 = new LocalDate(2015, 11, 2)

  "Transforming a report with a single payment" should {

    "convert to a single payment" in {
      // given
      val merchantTransactions = createTransactions(mid -> Seq(transaction(ref = "V1234567891014-Y5RY0")))

      //when
      val rcsPayments = emisReportTransformer.toRcsChunks(merchantTransactions, 400).next()._1.payments

      //then
      rcsPayments should contain theSameElementsInOrderAs Seq(rcsPayment(1, paymentProviderId = "1234567891014V1234567891014-Y5RY0", reference = "1234567891014"))
    }

    "contain no RCS payments when the order id is invalid" in {
      // given
      val merchantTransactions = createTransactions(mid -> Seq(transaction(ref = "")))

      //when
      val rcsChunks = emisReportTransformer.toRcsChunks(merchantTransactions.toIterator, 400)

      //then
      rcsChunks should be (empty)
    }
  }

  "Transforming a report with multiple payments" should {

    "convert to multiple payments" in {
      // given
      val merchantTransactions = createTransactions(mid -> Seq(
        transaction(ref = "V1234567891014-Y5RY0"),
        transaction(amount = 350076, ref = "K0987654321K-9999990")
      ))

      //when
      val rcsPayments = emisReportTransformer.toRcsChunks(merchantTransactions, 400).next()._1.payments

      //then
      rcsPayments should contain theSameElementsInOrderAs Seq(
        rcsPayment(1, paymentProviderId = "1234567891014V1234567891014-Y5RY0",reference = "1234567891014"),
        rcsPayment(2, paymentProviderId = "0987654321KK0987654321K-9999990", amount = 350076, reference = "0987654321K"))
    }


    "only include Accepted payments" in {
      // given
      val merchantTransactions = createTransactions(mid -> Seq(
          transaction(status = Accepted, ref = "V1234567891014-Y5RY0"),
          transaction(status = Rejected, ref = "V1234567891014-99990"),
          transaction(status = Pending, ref = "V1234567891014-88880")
        ))

      //when
      val rcsPayments = emisReportTransformer.toRcsChunks(merchantTransactions, 400).next()._1.payments

      //then
      rcsPayments should contain theSameElementsInOrderAs Seq(
        rcsPayment(1, paymentProviderId = "1234567891014V1234567891014-Y5RY0", reference = "1234567891014")
      )
    }

    "only include purchased transaction type payments" in{
      // given
      val merchantTransactions = createTransactions(mid -> Seq(
        transaction(transactionType = TransactionType.Refund, ref = "V1234567891014-11110"),
        transaction(transactionType = TransactionType.Purchase, ref = "V1234567891014-22220"),
        transaction(transactionType = TransactionType.CashAdvance, ref = "V1234567891014-33330"),
        transaction(transactionType = TransactionType.CashBack, ref = "V1234567891014-44440")
      ))

      //when
      val rcsPayments = emisReportTransformer.toRcsChunks(merchantTransactions, 400).next()._1.payments

      //then
      rcsPayments should contain theSameElementsInOrderAs Seq(
        rcsPayment(1, paymentProviderId = "1234567891014V1234567891014-22220", reference = "1234567891014")
      )
    }

    "include release chunks indexed from '1'" in {
      // given
      val merchantTransactions = createTransactions(mid -> Seq(
        transaction(ref = "V1234567891014-Y5RY0"),
        transaction(ref = "K0987654321K-9999990")
      ))

      // when
      val releaseChunks = emisReportTransformer.toRcsChunks(merchantTransactions, 400).next()._2

      //then
      releaseChunks.chunkId shouldBe "1"

    }
  }

  "Transforming a report with multiple merchants" should {
    "only accept transaction from TaxPlatform Live Proving Transactions" in {

      val emisReport = createTransactions(
        merchants.SelfAssessmentForDebitCard.merchantId -> Seq(
          transaction(status = Accepted, ref = "V1234567891014-Y5RY0"),
          transaction(status = Rejected, ref = "V1234567891014-XXXX0")),
        "72484484" -> Seq(
          transaction(status = Accepted, ref = "V1234567891014-Y5RY0"),
          transaction(status = Rejected, ref = "V1234567891014-XXXX0"))
      )

      //when
      val rcsPayments = emisReportTransformer.toRcsChunks(emisReport, 400).next()._1.payments

      //then
      rcsPayments should contain theSameElementsInOrderAs Seq(
        rcsPayment(1, paymentProviderId = "1234567891014V1234567891014-Y5RY0",reference = "1234567891014")
      )
    }

    "have correct bank account numbers for each Mid" in {


      val emisReport = createTransactions(
        merchants.SelfAssessmentForDebitCard.merchantId -> Seq(transaction(ref = "K9876543210K-9999990")),
        merchants.VatForDebitCard.merchantId -> Seq(transaction(ref = "V1234567891014-Y5RY0"))
        )

      //when
      val rcsPayments = emisReportTransformer.toRcsChunks(emisReport, 400).next()._1.payments

      //then
      rcsPayments should contain theSameElementsInOrderAs Seq(
        rcsPayment(1, paymentProviderId = "9876543210KK9876543210K-9999990", destinationAccountNumber = merchantMap("sa").account, reference = "9876543210K"),
        rcsPayment(2, paymentProviderId = "1234567891014V1234567891014-Y5RY0", destinationAccountNumber = merchantMap("vat").account, reference = "1234567891014")
      )
    }
  }


  "Transforming a report" should {

    "create multiple chunks to send to Rcs" in {
      val emisReport = createTransactions(
        merchants.SelfAssessmentForDebitCard.merchantId -> Seq(transaction(ref = "K9876543210K-9999990"),
                                              transaction(ref = "K9876543210K-9999990"),
                                              transaction(ref = "K9876543210K-9999990"),
                                              transaction(ref = "K9876543210K-9999990"),
                                              transaction(ref = "K9876543210K-9999990"),
                                              transaction(ref = "K9876543210K-9999990")),
        merchants.VatForDebitCard.merchantId -> Seq(transaction(ref = "V1234567891014-Y5RY0"))
        )

      //when
      val rcsChunks = emisReportTransformer.toRcsChunks(emisReport, chunkSize = 3).map(_._1).toSeq

      //then
      rcsChunks(0).payments.size should be(3)
      rcsChunks(1).payments.size should be(3)
      rcsChunks(2).payments.size should be(1)
      rcsChunks.size should be(3)

      val saAccountNumber = merchantMap("sa").account

      rcsChunks(0).payments should contain theSameElementsInOrderAs Seq(
        rcsPayment(1, paymentProviderId = "9876543210KK9876543210K-9999990", destinationAccountNumber = saAccountNumber, reference = "9876543210K"),
        rcsPayment(2, paymentProviderId = "9876543210KK9876543210K-9999990", destinationAccountNumber = saAccountNumber, reference = "9876543210K"),
        rcsPayment(3, paymentProviderId = "9876543210KK9876543210K-9999990", destinationAccountNumber = saAccountNumber, reference = "9876543210K")
      )

      rcsChunks(1).payments should contain theSameElementsInOrderAs Seq(
        rcsPayment(4, paymentProviderId = "9876543210KK9876543210K-9999990", destinationAccountNumber = saAccountNumber, reference = "9876543210K"),
        rcsPayment(5, paymentProviderId = "9876543210KK9876543210K-9999990", destinationAccountNumber = saAccountNumber, reference = "9876543210K"),
        rcsPayment(6, paymentProviderId = "9876543210KK9876543210K-9999990", destinationAccountNumber = saAccountNumber, reference = "9876543210K")
      )

      val vatAccountNumber = merchantMap("vat").account

      rcsChunks(2).payments should contain theSameElementsInOrderAs Seq(
        rcsPayment(7, paymentProviderId = "1234567891014V1234567891014-Y5RY0", destinationAccountNumber = vatAccountNumber, reference = "1234567891014")
      )
    }

    "when the transaction dates are all before 2 November, 2015, create multiple payments items for a credit card transaction, ensuring the right commission (1.4%)" in {
      val emisReport = createTransactions(
        merchants.SelfAssessmentForCreditCard.merchantId -> Seq(transaction(amount = 37, ref = "K9876543210K-9999990", date = November_1_2015)),
        merchants.VatForCreditCard.merchantId            -> Seq(transaction(amount = 2341691, ref = "V1234567891014-Y5RY0", date = November_1_2015))
      )

      val rcsChunks = emisReportTransformer.toRcsChunks(emisReport, chunkSize = 400).map(_._1).toSeq

      rcsChunks(0).payments.size should be(2)

      val saAccountNumber = merchantMap("sa").account

      rcsChunks(0).payments(0).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = saAccountNumber, amount = Pence(36), reference = "9876543210K"),
        RcsPaymentItem(transactionType = "27", destinationAccountNumber = saAccountNumber, amount = Pence(1), reference = "9876543210K")
      )

      val vatAccountNumber = merchantMap("vat").account

      rcsChunks(0).payments(1).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = vatAccountNumber, amount = Pence(2309360), reference = "1234567891014"),
        RcsPaymentItem(transactionType = "27", destinationAccountNumber = vatAccountNumber, amount = Pence(32331), reference = "1234567891014")
      )
    }

    "when the transaction date are all on 2 November, 2015, create multiple payments items for a credit card transaction, ensuring the right commission (1.5%)" in {
      val emisReport = createTransactions(
        merchants.SelfAssessmentForCreditCard.merchantId -> Seq(transaction(amount = 233621, ref = "K9876543210K-9999990", date = November_2_2015)),
        merchants.VatForCreditCard.merchantId            -> Seq(transaction(amount = 10007900, ref = "V1234567891014-Y5RY0", date = November_2_2015))
      )

      val rcsChunks = emisReportTransformer.toRcsChunks(emisReport, chunkSize = 400).map(_._1).toSeq

      rcsChunks(0).payments.size should be(2)

      val saAccountNumber = merchantMap("sa").account

      rcsChunks(0).payments(0).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = saAccountNumber, amount = Pence(233621 - 3453), reference = "9876543210K"),
        RcsPaymentItem(transactionType = "27", destinationAccountNumber = saAccountNumber, amount = Pence(3453), reference = "9876543210K")
      )

      val vatAccountNumber = merchantMap("vat").account

      rcsChunks(0).payments(1).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = vatAccountNumber, amount = Pence(10007900 - 147900), reference = "1234567891014"),
        RcsPaymentItem(transactionType = "27", destinationAccountNumber = vatAccountNumber, amount = Pence(147900), reference = "1234567891014")
      )
    }


    "when some of the transaction dates are before 2 November, 2015, and some after, create multiple payments items for a credit card transaction, ensuring the right commission" in {
      val emisReport = createTransactions(
        merchants.SelfAssessmentForCreditCard.merchantId -> Seq(transaction(amount = 216119, ref = "K9876543210K-9999990", date = November_1_2015)),
        merchants.SelfAssessmentForCreditCard.merchantId -> Seq(transaction(amount = 7735894, ref = "K9876543211K-9999990", date = November_1_2015)),
        merchants.VatForCreditCard.merchantId            -> Seq(transaction(amount = 10007900, ref = "V1234567891014-Y5RY0", date = November_2_2015))
      )

      val rcsChunks = emisReportTransformer.toRcsChunks(emisReport, chunkSize = 400).map(_._1).toSeq

      rcsChunks(0).payments.size should be(3)

      val saAccountNumber = merchantMap("sa").account

      rcsChunks(0).payments(0).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = saAccountNumber, amount = Pence(216119 - 2984), reference = "9876543210K"),
        RcsPaymentItem(transactionType = "27", destinationAccountNumber = saAccountNumber, amount = Pence(2984), reference = "9876543210K")
      )


      rcsChunks(0).payments(1).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = saAccountNumber, amount = Pence(7735894 - 106807), reference = "9876543211K"),
        RcsPaymentItem(transactionType = "27", destinationAccountNumber = saAccountNumber, amount = Pence(106807), reference = "9876543211K")
      )

      val vatAccountNumber = merchantMap("vat").account

      rcsChunks(0).payments(2).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = vatAccountNumber, amount = Pence(10007900 - 147900), reference = "1234567891014"),
        RcsPaymentItem(transactionType = "27", destinationAccountNumber = vatAccountNumber, amount = Pence(147900), reference = "1234567891014")
      )
    }


    "convert corporation tax payments" in {

      val emisReport = createTransactions(
        merchants.CorporationTaxForDebitCard.merchantId -> Seq(transaction(amount = 8888, ref = "A109717256408A-TTTT0")),
        merchants.CorporationTaxForCreditCard.merchantId -> Seq(transaction(amount = 1000, ref = "A106717256408A-TTTT0"))
      )

      val rcsChunks = emisReportTransformer.toRcsChunks(emisReport, chunkSize = 2).map(_._1).toSeq

      rcsChunks(0).payments.map(_.paymentProviderId) should contain theSameElementsInOrderAs Seq(
        "1097172564A00108AA109717256408A-TTTT0",
        "1067172564A00108AA106717256408A-TTTT0")

      val ctNumber = merchantMap("ct").account

      rcsChunks(0).payments(0).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = ctNumber, amount = Pence(8888), reference = "1097172564A00108A")
      )

      rcsChunks(0).payments(1).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = ctNumber, amount = Pence(986), reference = "1067172564A00108A"),
        RcsPaymentItem(transactionType = "27", destinationAccountNumber = ctNumber, amount = Pence(14), reference = "1067172564A00108A")
      )
    }

    "convert epaye payments" in {

      val emisReport = createTransactions(
        merchants.EPayeForDebitCard.merchantId -> Seq(transaction(amount = 8888, ref = "PFBO0T5QKG2RC-YAYSA0")),
        merchants.EPayeForCreditCard.merchantId -> Seq(transaction(amount = 1000, ref = "P5JJ8J29IN0A1-6AYSA0"))
      )

      val rcsChunks = emisReportTransformer.toRcsChunks(emisReport, chunkSize = 2).map(_._1).toSeq

      rcsChunks(0).payments.map(_.paymentProviderId) should contain theSameElementsInOrderAs Seq(
        "551PO037790KG9912PFBO0T5QKG2RC-YAYSA0",
        "199PJ397953IN1001P5JJ8J29IN0A1-6AYSA0")

      val epayeNumber = merchantMap("epaye").account

      rcsChunks(0).payments(0).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = epayeNumber, amount = Pence(8888), reference = "551PO037790KG9912")
      )

      rcsChunks(0).payments(1).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = epayeNumber, amount = Pence(986), reference = "199PJ397953IN1001"),
        RcsPaymentItem(transactionType = "27", destinationAccountNumber = epayeNumber, amount = Pence(14), reference = "199PJ397953IN1001")
      )
    }

    "convert other taxes payments" in {
      val emisReport = createTransactions(
        merchants.OtherTaxesForDebitCard.merchantId -> Seq(transaction(amount = 6543, ref = "XC000843562584-8YSB0")),
        merchants.OtherTaxesForCreditCard.merchantId -> Seq(transaction(amount = 1000, ref = "XAXBQF0KNI4K94J-3870"))
      )

      val rcsChunks = emisReportTransformer.toRcsChunks(emisReport, chunkSize = 2).map(_._1).toSeq
      rcsChunks(0).payments.map(_.paymentProviderId) should contain theSameElementsInOrderAs Seq(
        "XC000843562584XC000843562584-8YSB0",
        "XAXBQF0KNI4K94JXAXBQF0KNI4K94J-3870")

      val otherTaxesNumber = merchantMap("otherTaxes").account

      rcsChunks(0).payments(0).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = otherTaxesNumber, amount = Pence(6543), reference = "XC000843562584")
      )

      rcsChunks(0).payments(1).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = otherTaxesNumber, amount = Pence(986), reference = "XAXBQF0KNI4K94J"),
        RcsPaymentItem(transactionType = "27", destinationAccountNumber = otherTaxesNumber, amount = Pence(14), reference = "XAXBQF0KNI4K94J")
      )
    }

    "convert other stamp duty land tax payments" in {

      val emisReport = createTransactions(
          merchants.StampDutyLandTaxForDebitCard.merchantId -> Seq(transaction(amount = 8842, ref = "M123456789MA-ABB2YZ0")),
        merchants.StampDutyLandTaxForCreditCard.merchantId -> Seq(transaction(amount = 1000, ref = "M680686481MW-XYD4YZ0"))
      )

      val rcsChunks = emisReportTransformer.toRcsChunks(emisReport, chunkSize = 2).map(_._1).toSeq
      rcsChunks(0).payments.map(_.paymentProviderId) should contain theSameElementsInOrderAs
          Seq("123456789MAM123456789MA-ABB2YZ0","680686481MWM680686481MW-XYD4YZ0")

      val sdltNumber = merchantMap("sdlt").account

      rcsChunks(0).payments(0).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = sdltNumber, amount = Pence(8842), reference = "123456789MA")
      )

      rcsChunks(0).payments(1).paymentItems should contain theSameElementsInOrderAs Seq(
        RcsPaymentItem(transactionType = "26", destinationAccountNumber = sdltNumber, amount = Pence(986), reference = "680686481MW"),
        RcsPaymentItem(transactionType = "27", destinationAccountNumber = sdltNumber, amount = Pence(14), reference = "680686481MW")
      )
    }

  }
}


trait EmisReportGenerator extends MerchantBuilder{
  def transaction(
                   amount: Int = 2500,
                   date: LocalDate = new LocalDate(2011, 4, 14),
                   status: Status = Accepted,
                   transactionType:TransactionType.Value = TransactionType.Purchase,
                   ref: String) =
    Transaction(TransactionData(amount, date, status, transactionType),TransactionSuppData(ref))

  def rcsPayment(consecNumber: Int,
                 date: String = "11104",
                 paymentProviderId: String,
                 transactionType: String = RcsPaymentItem.HodPayment,
                 destinationAccountNumber: String = merchants.SelfAssessmentForDebitCard.accountNumber,
                 amount: Int = 2500,
                 reference: String) =
    RcsPayment(consecNumber, date, paymentProviderId, List(RcsPaymentItem(transactionType, destinationAccountNumber, Pence(amount),reference)))

  val mid = merchants.SelfAssessmentForDebitCard.merchantId

  def createTransactions(transactionsForMerchantIds: (String, Seq[Transaction])*): Iterator[MerchantTransaction] = {
    val map: Seq[MerchantTransaction] = transactionsForMerchantIds.flatMap {
      case (merchantId, transactions) =>
        transactions.map { transaction =>
          MerchantTransaction(MerchantOutlet(merchantId, -1, -1, LocalDate.now()), transaction)
        }
    }
    map.toIterator
  }
}
