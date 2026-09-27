package com.example.util

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class SmsGatewayProvider(val displayName: String) {
    DEVICE_SIM("Device SIM (SmsManager)"),
    ARKESEL("Arkesel SMS (Ghana)"),
    MNOTIFY("mNotify SMS (Ghana)"),
    HUBTEL("Hubtel SMS (Ghana)"),
    TWILIO("Twilio SMS (Global)")
}

data class GatewayConfig(
    val provider: SmsGatewayProvider = SmsGatewayProvider.DEVICE_SIM,
    val apiKey: String = "",
    val senderId: String = "ECG-GH",
    val apiSecret: String = "", // Hubtel client secret or Twilio Auth Token
    val accountSid: String = "" // Twilio Account SID
)

data class SmsDispatchResult(
    val success: Boolean,
    val method: String,
    val message: String
)

object SmsGatewayManager {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private const val PREFS_NAME = "ecg_sms_gateway_prefs"
    private const val KEY_PROVIDER = "gateway_provider"
    private const val KEY_API_KEY = "gateway_api_key"
    private const val KEY_SENDER_ID = "gateway_sender_id"
    private const val KEY_API_SECRET = "gateway_api_secret"
    private const val KEY_ACCOUNT_SID = "gateway_account_sid"

    fun loadConfig(context: Context): GatewayConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val providerStr = prefs.getString(KEY_PROVIDER, SmsGatewayProvider.DEVICE_SIM.name)
        val provider = try {
            SmsGatewayProvider.valueOf(providerStr ?: SmsGatewayProvider.DEVICE_SIM.name)
        } catch (_: Exception) {
            SmsGatewayProvider.DEVICE_SIM
        }
        return GatewayConfig(
            provider = provider,
            apiKey = prefs.getString(KEY_API_KEY, "") ?: "",
            senderId = prefs.getString(KEY_SENDER_ID, "ECG-GH") ?: "ECG-GH",
            apiSecret = prefs.getString(KEY_API_SECRET, "") ?: "",
            accountSid = prefs.getString(KEY_ACCOUNT_SID, "") ?: ""
        )
    }

    fun saveConfig(context: Context, config: GatewayConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_PROVIDER, config.provider.name)
            .putString(KEY_API_KEY, config.apiKey.trim())
            .putString(KEY_SENDER_ID, config.senderId.trim().ifEmpty { "ECG-GH" })
            .putString(KEY_API_SECRET, config.apiSecret.trim())
            .putString(KEY_ACCOUNT_SID, config.accountSid.trim())
            .apply()
    }

    /**
     * Sends a real SMS to the destination phone number.
     * If a cloud gateway is configured, sends via HTTP API.
     * Otherwise, attempts device SIM via Android SmsManager.
     */
    suspend fun sendRealSms(
        context: Context,
        phoneNumber: String,
        messageText: String
    ): SmsDispatchResult = withContext(Dispatchers.IO) {
        val config = loadConfig(context)
        val cleanPhone = phoneNumber.replace(" ", "").replace("-", "").trim()

        // 1. If cloud gateway is configured with an API key, use it
        if (config.apiKey.isNotBlank()) {
            when (config.provider) {
                SmsGatewayProvider.ARKESEL -> {
                    return@withContext sendViaArkesel(cleanPhone, messageText, config)
                }
                SmsGatewayProvider.MNOTIFY -> {
                    return@withContext sendViaMNotify(cleanPhone, messageText, config)
                }
                SmsGatewayProvider.HUBTEL -> {
                    return@withContext sendViaHubtel(cleanPhone, messageText, config)
                }
                SmsGatewayProvider.TWILIO -> {
                    return@withContext sendViaTwilio(cleanPhone, messageText, config)
                }
                SmsGatewayProvider.DEVICE_SIM -> {
                    // fallthrough to SIM
                }
            }
        }

        // 2. Otherwise fallback to device SIM (SmsManager)
        val simResult = SmsNotificationHelper.sendVerificationCodeSms(context, cleanPhone, extractCode(messageText))
        if (simResult.success && simResult.method == "DIRECT_SMS") {
            SmsDispatchResult(
                success = true,
                method = "DEVICE_SIM",
                message = "SMS dispatched via phone SIM card to $cleanPhone"
            )
        } else {
            SmsDispatchResult(
                success = false,
                method = "SIM_NOT_AVAILABLE",
                message = "The browser preview runs in a cloud emulator without a physical SIM card. To send real SMS to $cleanPhone, enter your SMS Gateway API Key (Arkesel, mNotify, Hubtel) in SMS Gateway Settings, or install the app APK on a physical phone."
            )
        }
    }

    private fun extractCode(message: String): String {
        val regex = Regex("""\b\d{6}\b""")
        return regex.find(message)?.value ?: "123456"
    }

    private fun formatPhoneForGhana(phone: String): String {
        var p = phone.replace("+", "").trim()
        if (p.startsWith("0") && p.length == 10) {
            p = "233" + p.substring(1)
        }
        return p
    }

    // Arkesel SMS API (Ghana)
    private fun sendViaArkesel(phone: String, message: String, config: GatewayConfig): SmsDispatchResult {
        return try {
            val formatted = formatPhoneForGhana(phone)
            val json = JSONObject().apply {
                put("sender", config.senderId.ifEmpty { "ECG-GH" })
                put("message", message)
                put("recipients", JSONArray().put(formatted))
            }
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://sms.arkesel.com/api/v2/sms/send")
                .addHeader("api-key", config.apiKey)
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            val respBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                SmsDispatchResult(true, "ARKESEL", "SMS successfully sent to $phone via Arkesel")
            } else {
                SmsDispatchResult(false, "ARKESEL_ERROR", "Arkesel error: $respBody")
            }
        } catch (e: Exception) {
            SmsDispatchResult(false, "ARKESEL_FAIL", "Failed to connect to Arkesel: ${e.message}")
        }
    }

    // mNotify SMS API (Ghana)
    private fun sendViaMNotify(phone: String, message: String, config: GatewayConfig): SmsDispatchResult {
        return try {
            val formatted = formatPhoneForGhana(phone)
            val json = JSONObject().apply {
                put("recipient", JSONArray().put(formatted))
                put("sender", config.senderId.ifEmpty { "ECG-GH" })
                put("message", message)
            }
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://api.mnotify.com/api/sms/quick?key=${config.apiKey}")
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            val respBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                SmsDispatchResult(true, "MNOTIFY", "SMS successfully sent to $phone via mNotify")
            } else {
                SmsDispatchResult(false, "MNOTIFY_ERROR", "mNotify error: $respBody")
            }
        } catch (e: Exception) {
            SmsDispatchResult(false, "MNOTIFY_FAIL", "Failed to connect to mNotify: ${e.message}")
        }
    }

    // Hubtel SMS API (Ghana)
    private fun sendViaHubtel(phone: String, message: String, config: GatewayConfig): SmsDispatchResult {
        return try {
            val formatted = formatPhoneForGhana(phone)
            val json = JSONObject().apply {
                put("From", config.senderId.ifEmpty { "ECG-GH" })
                put("To", formatted)
                put("Content", message)
            }
            val credentials = okhttp3.Credentials.basic(config.apiKey, config.apiSecret)
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://smsc.hubtel.com/v1/messages/send")
                .addHeader("Authorization", credentials)
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            val respBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                SmsDispatchResult(true, "HUBTEL", "SMS successfully sent to $phone via Hubtel")
            } else {
                SmsDispatchResult(false, "HUBTEL_ERROR", "Hubtel error: $respBody")
            }
        } catch (e: Exception) {
            SmsDispatchResult(false, "HUBTEL_FAIL", "Failed to connect to Hubtel: ${e.message}")
        }
    }

    // Twilio SMS API
    private fun sendViaTwilio(phone: String, message: String, config: GatewayConfig): SmsDispatchResult {
        return try {
            val credentials = okhttp3.Credentials.basic(config.accountSid, config.apiSecret)
            val formBody = okhttp3.FormBody.Builder()
                .add("To", phone)
                .add("From", config.senderId)
                .add("Body", message)
                .build()

            val url = "https://api.twilio.com/2010-04-01/Accounts/${config.accountSid}/Messages.json"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", credentials)
                .post(formBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val respBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                SmsDispatchResult(true, "TWILIO", "SMS successfully sent to $phone via Twilio")
            } else {
                SmsDispatchResult(false, "TWILIO_ERROR", "Twilio error: $respBody")
            }
        } catch (e: Exception) {
            SmsDispatchResult(false, "TWILIO_FAIL", "Failed to connect to Twilio: ${e.message}")
        }
    }
}
