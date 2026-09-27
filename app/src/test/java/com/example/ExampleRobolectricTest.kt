package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ECG Faulty track report", appName)
  }

  @Test
  fun `verify automatic SMS assignment message format`() {
    val message = com.example.util.SmsNotificationHelper.generateAssignmentMessage("POF-2026-00001")
    assertEquals(
      "A fault( POF-2026-00001 ) has been assigned to you. Please login to view the details",
      message
    )
  }

  @Test
  fun `verify SMS verification code dispatch`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val result = com.example.util.SmsNotificationHelper.sendVerificationCodeSms(
      context,
      "+233240001122",
      "582910"
    )
    org.junit.Assert.assertTrue(result.success)
  }
}
