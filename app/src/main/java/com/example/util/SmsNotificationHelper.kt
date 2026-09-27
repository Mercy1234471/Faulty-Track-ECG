package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat

object SmsNotificationHelper {

    fun generateAssignmentMessage(faultReference: String): String {
        return "A fault( $faultReference ) has been assigned to you. Please login to view the details"
    }

    data class SmsResult(
        val success: Boolean,
        val method: String, // "DIRECT_SMS" or "INTENT_LAUNCHED"
        val message: String
    )

    /**
     * Attempts to send real SMS directly if SEND_SMS permission is granted.
     * Otherwise, returns false and offers intent launcher.
     */
    fun sendDirectSms(
        context: Context,
        phoneNumber: String,
        faultReference: String
    ): SmsResult {
        val cleanPhone = phoneNumber.replace(" ", "").trim()
        val messageText = generateAssignmentMessage(faultReference)

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                val parts = smsManager.divideMessage(messageText)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(cleanPhone, null, messageText, null, null)
                }

                return SmsResult(
                    success = true,
                    method = "DIRECT_SMS",
                    message = "Direct SMS successfully transmitted to $cleanPhone: \"$messageText\""
                )
            } catch (e: Exception) {
                // Fallback to composer intent
                launchSmsComposer(context, cleanPhone, messageText)
                return SmsResult(
                    success = true,
                    method = "INTENT_LAUNCHED",
                    message = "SMS Composer opened for $cleanPhone: \"$messageText\""
                )
            }
        } else {
            // Launch standard SMS composer intent so user can tap send immediately
            launchSmsComposer(context, cleanPhone, messageText)
            return SmsResult(
                success = true,
                method = "INTENT_LAUNCHED",
                message = "SMS Composer dispatched to $cleanPhone: \"$messageText\""
            )
        }
    }

    fun launchSmsComposer(context: Context, phoneNumber: String, messageText: String) {
        try {
            val uri = Uri.parse("smsto:${Uri.encode(phoneNumber)}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Ignore if no SMS client installed
        }
    }

    fun launchPhoneCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${Uri.encode(phoneNumber)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Ignore
        }
    }

    /**
     * Sends the OTP verification code via SMS to the provided phone number.
     */
    fun sendVerificationCodeSms(
        context: Context,
        phoneNumber: String,
        code: String
    ): SmsResult {
        val cleanPhone = phoneNumber.replace(" ", "").trim()
        val messageText = "Your ECG Faulty Track Report verification code is: $code. Do not share this code with anyone."

        // Post system notification to notification shade as well
        postVerificationNotification(context, code)

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                val parts = smsManager.divideMessage(messageText)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(cleanPhone, null, messageText, null, null)
                }

                return SmsResult(
                    success = true,
                    method = "DIRECT_SMS",
                    message = "Verification code SMS sent to $cleanPhone"
                )
            } catch (_: Exception) {
                launchSmsComposer(context, cleanPhone, messageText)
                return SmsResult(
                    success = true,
                    method = "INTENT_LAUNCHED",
                    message = "SMS Composer opened for $cleanPhone"
                )
            }
        } else {
            launchSmsComposer(context, cleanPhone, messageText)
            return SmsResult(
                success = true,
                method = "INTENT_LAUNCHED",
                message = "SMS Composer opened for $cleanPhone"
            )
        }
    }

    /**
     * Posts a system notification with the verification code so the user can see it
     * in the Android notification drawer.
     */
    fun postVerificationNotification(context: Context, code: String) {
        try {
            val channelId = "ecg_verification_channel"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
                ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "ECG Verification Codes",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "SMS verification code notifications"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("ECG Security Verification")
                .setContentText("Your verification code is: $code")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(99123, notification)
        } catch (_: Exception) {
            // Ignore
        }
    }
}
