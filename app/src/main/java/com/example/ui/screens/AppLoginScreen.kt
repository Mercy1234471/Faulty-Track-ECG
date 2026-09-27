package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.EcgCardBorder
import com.example.ui.theme.EcgDarkSurface
import com.example.ui.theme.EcgDarkSurfaceVariant
import com.example.ui.theme.EcgOnYellow
import com.example.ui.theme.EcgStatusRejected
import com.example.ui.theme.EcgTextMuted
import com.example.ui.theme.EcgTextPrimary
import com.example.ui.theme.EcgWhite
import com.example.ui.theme.EcgYellow
import com.example.util.SmsGatewayManager
import kotlinx.coroutines.launch

@Composable
fun AppLoginScreen(
    onLoginSuccess: (role: String, userIdentifier: String) -> Unit,
    modifier: Modifier = Modifier,
    onAdminLoginClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var expectedCode by remember { mutableStateOf<String?>(null) }
    var isVerificationStep by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSendingSms by remember { mutableStateOf(false) }

    fun doSendOtp() {
        val trimmed = phoneNumber.trim()
        val digitsOnly = trimmed.filter { it.isDigit() }
        if (digitsOnly.length < 9) {
            errorMessage = "Please enter a valid phone number (minimum 9 digits)."
            return
        }

        errorMessage = null
        val code = (100000..999999).random().toString()
        expectedCode = code
        verificationCode = ""
        isSendingSms = true

        coroutineScope.launch {
            val messageText = "Your ECG Faulty Track Report verification code is: $code. Do not share this code with anyone."
            val result = SmsGatewayManager.sendRealSms(context, trimmed, messageText)
            isSendingSms = false
            isVerificationStep = true

            if (!result.success && result.method != "SIM_NOT_AVAILABLE") {
                errorMessage = result.message
            }
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        doSendOtp()
    }

    fun initiateSendOtp() {
        val trimmed = phoneNumber.trim()
        val digitsOnly = trimmed.filter { it.isDigit() }
        if (digitsOnly.length < 9) {
            errorMessage = "Please enter a valid phone number (minimum 9 digits)."
            return
        }

        val requiredPermissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.SEND_SMS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (requiredPermissions.isNotEmpty()) {
            permissionsLauncher.launch(requiredPermissions.toTypedArray())
        } else {
            doSendOtp()
        }
    }

    fun verifyCodeAndLogin() {
        val entered = verificationCode.trim()
        if (entered.isEmpty()) {
            errorMessage = "Please enter the 6-digit verification code."
            return
        }
        // Accepts generated OTP or test code 123456
        if ((expectedCode != null && entered == expectedCode) || entered == "123456") {
            errorMessage = null
            onLoginSuccess("User", phoneNumber.trim())
        } else {
            errorMessage = "Invalid verification code. Please check your SMS and try again."
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("app_login_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(EcgYellow, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "ECG Logo",
                tint = EcgOnYellow,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ECG Faulty Track Report",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = EcgWhite
        )
        Text(
            text = "Electricity Company of Ghana",
            style = MaterialTheme.typography.bodySmall,
            color = EcgYellow
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Sign In / Verification Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EcgDarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, EcgCardBorder)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                if (!isVerificationStep) {
                    // STEP 1: PHONE NUMBER INPUT
                    Text(
                        text = "PHONE NUMBER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EcgYellow
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            if (errorMessage != null) errorMessage = null
                        },
                        placeholder = {
                            Text("+233 24 000 0000", color = EcgTextMuted, fontSize = 14.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone Number",
                                tint = EcgYellow
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { if (!isSendingSms) initiateSendOtp() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_number_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcgYellow,
                            unfocusedBorderColor = EcgCardBorder,
                            focusedTextColor = EcgTextPrimary,
                            unfocusedTextColor = EcgTextPrimary,
                            focusedContainerColor = EcgDarkSurfaceVariant,
                            unfocusedContainerColor = EcgDarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = EcgStatusRejected,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { initiateSendOtp() },
                        enabled = !isSendingSms,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EcgYellow,
                            contentColor = EcgOnYellow
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("app_login_button")
                    ) {
                        if (isSendingSms) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = EcgOnYellow,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sending SMS...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val userNumber = phoneNumber.trim().ifEmpty { "+233 24 123 4567" }
                            onLoginSuccess("Staff", userNumber)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("continue_to_dashboard_button"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EcgCardBorder)
                    ) {
                        Text(
                            text = "Continue to Dashboard",
                            color = EcgYellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // STEP 2: VERIFICATION CODE INPUT
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    isVerificationStep = false
                                    errorMessage = null
                                }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit phone",
                                tint = EcgYellow,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Change Number",
                                color = EcgYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = {
                            if (it.length <= 6) {
                                verificationCode = it.filter { char -> char.isDigit() }
                                if (errorMessage != null) errorMessage = null
                            }
                        },
                        placeholder = {
                            Text("Enter 6-digit code", color = EcgTextMuted, fontSize = 14.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verification Code",
                                tint = EcgYellow
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { verifyCodeAndLogin() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verification_code_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcgYellow,
                            unfocusedBorderColor = EcgCardBorder,
                            focusedTextColor = EcgTextPrimary,
                            unfocusedTextColor = EcgTextPrimary,
                            focusedContainerColor = EcgDarkSurfaceVariant,
                            unfocusedContainerColor = EcgDarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = EcgStatusRejected,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { verifyCodeAndLogin() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EcgYellow,
                            contentColor = EcgOnYellow
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("verify_code_button")
                    ) {
                        Text(
                            text = "Verify & Sign In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { if (!isSendingSms) initiateSendOtp() },
                        enabled = !isSendingSms,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EcgCardBorder)
                    ) {
                        if (isSendingSms) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = EcgYellow,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sending...",
                                color = EcgYellow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = EcgYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Resend SMS Code",
                                color = EcgYellow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
