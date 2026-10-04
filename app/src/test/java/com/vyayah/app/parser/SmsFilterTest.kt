package com.vyayah.app.parser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsFilterTest {

    @Test
    fun testVmSbiUpiOtpExclusion() {
        val sender = "VM-SBIUPI"
        val body = "842910 is your OTP for transaction of Rs. 1,450.00 at SWIGGY. Do not share OTP with anyone - State Bank of India"
        assertFalse("OTPs from VM-SBIUPI must NEVER enter the ledger or adjust balances", SmsFilter.shouldProcess(sender, body))
    }

    @Test
    fun testHdfcBankOtpExclusion() {
        val sender = "AD-HDFCBK"
        val body = "482910 is your secret code for purchase of Rs. 4,500.00 at AMAZON. Valid for 10 mins. Never share your password/OTP."
        assertFalse("OTPs from HDFC Bank must NEVER enter the ledger", SmsFilter.shouldProcess(sender, body))
    }

    @Test
    fun testLoginAlertExclusion() {
        val sender = "AD-ICICIB"
        val body = "Login alert: NetBanking logged in on 04-10-2026 14:20 from Chrome. If not you, call customer care."
        assertFalse("Login alerts must be rejected", SmsFilter.shouldProcess(sender, body))
    }

    @Test
    fun testPersonalPhoneNumberExclusion() {
        val sender = "+919876543210"
        val body = "Hey bro, sent you Rs 500 on GPay check it out."
        assertFalse("SMS from personal numbers must be ignored", SmsFilter.shouldProcess(sender, body))
    }

    @Test
    fun testVmSbiUpiAuthenticDebitAccepted() {
        val sender = "VM-SBIUPI"
        val body = "Dear UPI user A/C 4567 debited by 320.0 on 04Oct26 by transfer to VPA blinkit@icici (Ref no 427811902831)."
        assertTrue("Legitimate debit SMS from VM-SBIUPI must be accepted", SmsFilter.shouldProcess(sender, body))
    }
}
