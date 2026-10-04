package com.vyayah.app.parser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsFilterTest {

    @Test
    fun testOtpExclusion() {
        val sender = "VM-HDFCBK"
        val body = "482910 is your OTP for purchase of Rs. 4,500.00 at AMAZON. Do not share OTP with anyone."
        assertFalse("OTPs must be strictly rejected", SmsFilter.shouldProcess(sender, body))
    }

    @Test
    fun testLoginAlertExclusion() {
        val sender = "AD-ICICIB"
        val body = "Login alert: NetBanking logged in on 04-10-2026 14:20 from Chrome. If not you, call customer care."
        assertFalse("Login alerts must be rejected", SmsFilter.shouldProcess(sender, body))
    }

    @Test
    fun testPromotionalLoanOfferExclusion() {
        val sender = "BZ-AXISBK"
        val body = "Congratulations! You are eligible for pre-approved personal loan of Rs 5,00,000. Apply now."
        assertFalse("Loan and promotional offers must be rejected", SmsFilter.shouldProcess(sender, body))
    }

    @Test
    fun testPersonalPhoneNumberExclusion() {
        val sender = "+919876543210"
        val body = "Hey bro, sent you Rs 500 on GPay check it out."
        assertFalse("SMS from personal numbers must be ignored", SmsFilter.shouldProcess(sender, body))
    }

    @Test
    fun testValidTransactionAccepted() {
        val sender = "VM-HDFCBK"
        val body = "Debited Rs. 450.00 from A/C **1234 to SWIGGY on 04-10-26. Avl Bal Rs. 14,050.00."
        assertTrue("Legitimate bank transaction must pass filter", SmsFilter.shouldProcess(sender, body))
    }
}
