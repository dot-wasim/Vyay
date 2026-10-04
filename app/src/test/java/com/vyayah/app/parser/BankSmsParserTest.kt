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
}
