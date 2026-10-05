package com.vyayah.app.parser

import com.vyayah.app.data.model.PaymentInstrument
import com.vyayah.app.data.model.TransactionDirection
import com.vyayah.app.data.model.TransactionType
import org.junit.Assert.*
import org.junit.Test

class BankSmsParserTest {

    @Test
    fun testHdfcUpiDebit() {
        val sender = "VM-HDFCBK-S"
        val body = "Sent Rs.450.00 from HDFC Bank A/C **1234 to SWIGGY on 04-10-26. UPI Ref 427819283719. Bal Rs.14,500.00."

        val result = BankSmsParser.parse(sender, body)
        assertNotNull("Should parse HDFC UPI debit", result)
        assertEquals(45000L, result!!.amountMinor) // Rs 450.00 = 45000 paise
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals(TransactionType.PURCHASE, result.type)
        assertEquals(PaymentInstrument.UPI, result.instrument)
        assertEquals("1234", result.accountLast4)
        assertEquals("427819283719", result.upiRef)
        assertEquals(1450000L, result.availableBalanceMinor)
    }

    @Test
    fun testHdfcCreditCardSpend() {
        val sender = "AD-HDFCBK"
        val body = "Rs. 2,499.00 spent on your HDFC Bank Credit Card ending 9876 at AMAZON RETAIL on 03-Oct-26. Avl limit: Rs. 1,45,000.00."

        val result = BankSmsParser.parse(sender, body)
        assertNotNull(result)
        assertEquals(249900L, result!!.amountMinor)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals(PaymentInstrument.CARD, result.instrument)
        assertEquals("9876", result.accountLast4)
    }

    @Test
    fun testSbiUpiDebit() {
        val sender = "BZ-SBIUPI"
        val body = "Dear UPI user A/C 4567 debited by 320.0 on 04Oct26 by transfer to VPA blinkit@icici (Ref no 427811902831)."

        val result = BankSmsParser.parse(sender, body)
        assertNotNull(result)
        assertEquals(32000L, result!!.amountMinor)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals(PaymentInstrument.UPI, result.instrument)
        assertEquals("4567", result.accountLast4)
        assertEquals("blinkit@icici", result.upiVpa)
        assertEquals("427811902831", result.upiRef)
    }

    @Test
    fun testSbiCreditSalary() {
        val sender = "AD-SBINB"
        val body = "Your A/C 4567 is credited by Rs 1,15,000.00 on 01-10-26 by Salary Payroll Ref 98218731. Avl Bal Rs 1,42,300.00."

        val result = BankSmsParser.parse(sender, body)
        assertNotNull(result)
        assertEquals(11500000L, result!!.amountMinor) // 1,15,000.00
        assertEquals(TransactionDirection.CREDIT, result.direction)
        assertEquals(TransactionType.INCOME, result.type)
        assertEquals("4567", result.accountLast4)
        assertEquals(14230000L, result.availableBalanceMinor)
    }

    @Test
    fun testIciciRefund() {
        val sender = "VM-ICICIB"
        val body = "Refund of INR 1,999.00 credited to ICICI Bank Card XX4321 on 02-Oct-26 from ZOMATO. Avl Bal INR 85,000.00."

        val result = BankSmsParser.parse(sender, body)
        assertNotNull(result)
        assertEquals(199900L, result!!.amountMinor)
        assertEquals(TransactionDirection.CREDIT, result.direction)
        assertEquals(TransactionType.REFUND, result.type)
        assertEquals(PaymentInstrument.CARD, result.instrument)
        assertEquals("4321", result.accountLast4)
    }

    @Test
    fun testAtmWithdrawal() {
        val sender = "AD-AXISBK"
        val body = "Rs. 5,000.00 debited from Axis Bank A/C XX7890 via ATM cash withdrawal on 04-Oct-26. Avl Bal Rs 12,000.00."

        val result = BankSmsParser.parse(sender, body)
        assertNotNull(result)
        assertEquals(500000L, result!!.amountMinor)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals(TransactionType.ATM, result.type)
        assertEquals(PaymentInstrument.CASH, result.instrument)
        assertEquals("7890", result.accountLast4)
    }

    @Test
    fun testCreditCardBillPayment() {
        val sender = "AD-HDFCBK"
        val body = "Payment of Rs 18,500.00 received towards your HDFC Bank Credit Card ending 9876 on 03-Oct-26. Thank you."

        val result = BankSmsParser.parse(sender, body)
        assertNotNull(result)
        assertEquals(1850000L, result!!.amountMinor)
        assertEquals(TransactionType.BILL_PAYMENT, result.type)
        assertEquals("9876", result.accountLast4)
    }

    @Test
    fun testKotakDebit() {
        val sender = "BZ-KOTAKB"
        val body = "Sent Rs. 650.00 from Kotak Bank acct XX3322 to UBER INDIA on 04-10-26. Bal: Rs. 8,200.00."

        val result = BankSmsParser.parse(sender, body)
        assertNotNull(result)
        assertEquals(65000L, result!!.amountMinor)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals("3322", result.accountLast4)
    }

    @Test
    fun testPnbDebit() {
        val sender = "BP-PNBSMS"
        val body = "Dear Customer, A/C *5544 debited by Rs.1200.00 on 04/10/2026. Avl Bal Rs.25400.00 - PNB"

        val result = BankSmsParser.parse(sender, body)
        assertNotNull(result)
        assertEquals(120000L, result!!.amountMinor)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals("5544", result.accountLast4)
        assertEquals(2540000L, result.availableBalanceMinor)
    }

    @Test
    fun testIdfcFirstBank() {
        val sender = "VM-IDFCFB"
        val body = "Paid Rs 350.00 at STARBUCKS using IDFC FIRST Bank A/C XX1122 on 04-Oct-26. Avl Bal Rs. 35,400.00."

        val result = BankSmsParser.parse(sender, body)
        assertNotNull(result)
        assertEquals(35000L, result!!.amountMinor)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals("1122", result.accountLast4)
    }

    @Test
    fun testReversal() {
        val sender = "BZ-SBIUPI"
        val body = "UPI transaction of Rs. 450.00 reversed to A/C 4567 on 04Oct26. Ref no 427819283719. Avl Bal Rs 14,950.00."

        val result = BankSmsParser.parse(sender, body)
        assertNotNull(result)
        assertEquals(45000L, result!!.amountMinor)
        assertEquals(TransactionDirection.CREDIT, result.direction)
        assertEquals(TransactionType.REVERSAL, result.type)
        assertEquals("427819283719", result.upiRef)
    }

    @Test
    fun testAxisBankDebit() {
        val sender = "AD-AXISBK"
        val body = "INR 720.00 debited from Axis Bank A/c no. XX1234 on 04-10-2026 14:30 towards UBER INDIA. Avl Bal INR 18,340.00."

        val result = BankSmsParser.parse(sender, body)
        assertNotNull("Should parse Axis debit", result)
        assertEquals(72000L, result!!.amountMinor)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals("1234", result.accountLast4)
        assertEquals("Axis Bank", result.bankName)
        assertEquals(1834000L, result.availableBalanceMinor)
    }

    @Test
    fun testPaytmUpiPayment() {
        val sender = "VM-PAYTM"
        val body = "Paid Rs. 45 to Sharma Kirana (UPI Ref 427811002233) from Paytm Payments Bank A/c XX5678 on 04-10-2026. Total Bal: Rs. 1,250.00."

        val result = BankSmsParser.parse(sender, body)
        assertNotNull("Should parse Paytm payment", result)
        assertEquals(4500L, result!!.amountMinor)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals(PaymentInstrument.UPI, result.instrument)
        assertEquals("5678", result.accountLast4)
        assertEquals("Paytm Payments Bank", result.bankName)
        assertEquals("427811002233", result.upiRef)
        assertEquals(125000L, result.availableBalanceMinor)
    }

    @Test
    fun testIciciCreditCardTransaction() {
        val sender = "AD-ICICIB"
        val body = "Your ICICI Bank Credit Card XX4002 has been used for a transaction of INR 3,199.00 at AMAZON INDIA on 04-OCT-2026. Avl Limit: INR 85,000.00."

        val result = BankSmsParser.parse(sender, body)
        assertNotNull("Should parse ICICI card transaction", result)
        assertEquals(319900L, result!!.amountMinor)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals(4002, result!!.accountLast4?.toIntOrNull())
        assertEquals("ICICI Bank", result.bankName)
        assertEquals(8500000L, result.availableBalanceMinor)
    }

    @Test
    fun testRailwayPaymentWithDecimals() {
        val sender = "BZ-SBIUPI"
        val body = "Dear SBI UPI user, A/C 1234 debited by Rs. 1336.05 on 01Oct26 by transfer to Indian Rail W Ref 427819283100. Avl Bal Rs 25000.00."

        val result = BankSmsParser.parse(sender, body)
        assertNotNull("Should parse Railway SMS with exact decimals", result)
        assertEquals(133605L, result!!.amountMinor) // Rs 1336.05 = 133605 paise, NOT 136 or 133
        assertEquals("427819283100", result.upiRef)

        val normalized = MerchantNormalizer.normalize(result.merchantRaw, result.upiVpa)
        assertEquals("Indian Railway 🚂", normalized)

        val formatted = AmountParser.formatPaiseToInr(result.amountMinor)
        assertEquals("₹1,336.05", formatted)
    }

    @Test
    fun testTwoConsecutivePaymentsOf1000() {
        val sender = "VM-HDFCBK"
        val sms1 = "Sent Rs. 1000.00 from HDFC Bank A/C **1234 to Grocery Store on 01-10-26. UPI Ref 427800000001. Bal Rs 5000.00."
        val sms2 = "Sent Rs 1000 from HDFC Bank A/C **1234 to Cafe Coffee on 01-10-26. UPI Ref 427800000002. Bal Rs 4000.00."

        val result1 = BankSmsParser.parse(sender, sms1)
        val result2 = BankSmsParser.parse(sender, sms2)

        assertNotNull(result1)
        assertNotNull(result2)

        assertEquals(100000L, result1!!.amountMinor) // Rs 1000.00 = 100000 paise, NOT 100
        assertEquals(100000L, result2!!.amountMinor) // Rs 1000 = 100000 paise, NOT 100

        assertEquals("427800000001", result1.upiRef)
        assertEquals("427800000002", result2.upiRef)
        assertNotEquals(result1.upiRef, result2.upiRef) // Distinct reference numbers ensure both are stored
    }

    @Test
    fun testFunModeMerchantEmojis() {
        assertEquals("Swiggy 🍕", MerchantNormalizer.normalize("SWIGGY BANGALORE IN"))
        assertEquals("Zomato 🍔", MerchantNormalizer.normalize("ZOMATO GURGAON"))
        assertEquals("BookMyShow 🎟️", MerchantNormalizer.normalize("BOOKMYSHOW MUMBAI"))
        assertEquals("PVR Cinemas 🍿", MerchantNormalizer.normalize("PVR CINEMAS FORUM"))
        assertEquals("Indian Railway 🚂", MerchantNormalizer.normalize("INDIAN RAIL W"))
    }

    @Test
    fun testSbiCreditCardBillPayment() {
        val sender = "AD-SBINB"
        val body = "Payment of Rs 10,000.00 received towards SBI Card ending in 4567 on 05-Oct-26 via CRED. Thank you."
        val result = BankSmsParser.parse(sender, body)
        assertNotNull(result)
        assertEquals(1000000L, result!!.amountMinor)
        assertEquals(TransactionType.BILL_PAYMENT, result.type)
    }

    @Test
    fun testIciciCreditCardBillPayment() {
        val sender = "VM-ICICIB"
        val body = "Payment of INR 10,000.00 received towards ICICI Bank Credit Card XX4321 on 05-Oct-26. Current outstanding updated."
        val result = BankSmsParser.parse(sender, body)
        assertNotNull(result)
        assertEquals(1000000L, result!!.amountMinor)
        assertEquals(TransactionType.BILL_PAYMENT, result.type)
    }
}

