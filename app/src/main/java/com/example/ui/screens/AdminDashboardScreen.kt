package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FaultEntity
import com.example.data.model.MeterRequestEntity
import com.example.data.model.TechnicianEntity
import com.example.ui.theme.EcgCardBorder
import com.example.ui.theme.EcgDarkSurface
import com.example.ui.theme.EcgDarkSurfaceVariant
import com.example.ui.theme.EcgOnYellow
import com.example.ui.theme.EcgStatusInProgress
import com.example.ui.theme.EcgStatusPending
import com.example.ui.theme.EcgStatusRejected
import com.example.ui.theme.EcgStatusResolved
import com.example.ui.theme.EcgStatusReviewed
import com.example.ui.theme.EcgTextMuted
import com.example.ui.theme.EcgTextPrimary
import com.example.ui.theme.EcgTextSecondary
import com.example.ui.theme.EcgWhite
import com.example.ui.theme.EcgYellow

@Composable
fun AdminDashboardScreen(
    isAdminLoggedIn: Boolean,
    faults: List<FaultEntity>,
    meterRequests: List<MeterRequestEntity>,
    technicians: List<TechnicianEntity>,
    serverIp: String,
    onLoginAdmin: (password: String) -> Boolean,
    onLogoutAdmin: () -> Unit,
    onUpdateMeterStatus: (id: Long, status: String, tech: String?, remarks: String?) -> Unit,
    onExportFaultsExcel: () -> Unit,
    onExportMeterRequestsExcel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var adminPasswordInput by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    var editingRequest by remember { mutableStateOf<MeterRequestEntity?>(null) }

    val serverUrl = "http://$serverIp:8080"

    if (!isAdminLoggedIn) {
        // Admin Portal Login Screen
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("admin_login_box"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EcgYellow, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = EcgDarkSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(EcgYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = EcgOnYellow,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "ECG Administrator Console",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = EcgWhite
                    )
                    Text(
                        text = "Authorized Personnel Access Only",
                        style = MaterialTheme.typography.bodySmall,
                        color = EcgYellow
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = adminPasswordInput,
                        onValueChange = { adminPasswordInput = it },
                        label = { Text("Admin Master Key / Password", color = EcgTextSecondary) },
                        placeholder = { Text("ecg2026", color = EcgTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EcgYellow) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().testTag("admin_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcgYellow,
                            unfocusedBorderColor = EcgCardBorder,
                            focusedTextColor = EcgTextPrimary,
                            unfocusedTextColor = EcgTextPrimary,
                            focusedContainerColor = EcgDarkSurfaceVariant,
                            unfocusedContainerColor = EcgDarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    if (loginError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = loginError ?: "",
                            color = Color(0xFFFF5252),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Default Admin Password: ecg2026 (or 1234)",
                        style = MaterialTheme.typography.labelSmall,
                        color = EcgTextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val success = onLoginAdmin(adminPasswordInput)
                            if (!success) {
                                loginError = "Invalid credential. Use 'ecg2026'"
                            } else {
                                loginError = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EcgYellow,
                            contentColor = EcgOnYellow
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_login_submit_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unlock Admin Portal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        // Authenticated Admin Dashboard
        val scrollState = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .testTag("admin_authenticated_dashboard")
        ) {
            // Header with Logout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(EcgYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = EcgOnYellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Admin Command Center",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EcgWhite
                        )
                        Text(
                            text = "Master Control & Export Station",
                            style = MaterialTheme.typography.bodySmall,
                            color = EcgYellow
                        )
                    }
                }

                IconButton(
                    onClick = onLogoutAdmin,
                    modifier = Modifier.testTag("admin_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = EcgStatusRejected
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Web Gateway Server Card (Requirement #12, #13, #14)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_web_gateway_card"),
                colors = CardDefaults.cardColors(containerColor = EcgDarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EcgYellow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = EcgYellow)
                            Text(
                                text = "Web Browser Access IP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EcgWhite
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xFF00E676).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "OFFLINE READY",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Open this default IP on any browser (laptop, phone or tablet on the same Wi-Fi/Hotspot) to view the web dashboard and download reports:",
                        style = MaterialTheme.typography.bodySmall,
                        color = EcgTextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // IP URL Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161616), RoundedCornerShape(8.dp))
                            .border(1.dp, EcgCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = serverUrl,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = EcgYellow
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Copy button
                            IconButton(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("ECG Web Gateway", serverUrl))
                                    Toast.makeText(context, "Copied IP URL: $serverUrl", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy IP", tint = EcgYellow, modifier = Modifier.size(16.dp))
                            }

                            // Open in browser button
                            IconButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(serverUrl))
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = "Open Browser", tint = EcgYellow, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Excel Report Center (Requirement #10)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_excel_center_card"),
                colors = CardDefaults.cardColors(containerColor = EcgDarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EcgCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXCEL REPORT EXPORT CENTER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EcgYellow
                    )
                    Text(
                        text = "Automatic download of formatted Excel spreadsheets (.csv) for laptop & offline analysis",
                        style = MaterialTheme.typography.bodySmall,
                        color = EcgTextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onExportFaultsExcel,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EcgYellow,
                                contentColor = EcgOnYellow
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_faults_excel_btn")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Faults Excel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onExportMeterRequestsExcel,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2C2C2C),
                                contentColor = EcgYellow
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EcgYellow),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_meter_excel_btn")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Meters Excel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Meter Request Management List (Admin only can view, assign, update, and track)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Track & Manage Meter Requests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EcgWhite
                )
                Text(
                    text = "${meterRequests.size} Applications",
                    style = MaterialTheme.typography.bodySmall,
                    color = EcgYellow
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                meterRequests.forEach { req ->
                    AdminMeterRequestCard(
                        request = req,
                        onEdit = { editingRequest = req }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Edit Meter Request Status & Technician Dialog
    editingRequest?.let { req ->
        var editStatus by remember { mutableStateOf(req.status) }
        var editTech by remember { mutableStateOf(req.assignedTechnician ?: "") }
        var editRemarks by remember { mutableStateOf(req.adminRemarks ?: "") }
        var statusExpanded by remember { mutableStateOf(false) }

        val statuses = listOf("Submitted", "Reviewed", "Assigned", "In Progress", "Completed", "Rejected")

        AlertDialog(
            onDismissRequest = { editingRequest = null },
            containerColor = EcgDarkSurface,
            title = {
                Text(
                    text = "Update ${req.requestReference}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EcgWhite
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Applicant: ${req.fullName} (${req.category})",
                        style = MaterialTheme.typography.bodySmall,
                        color = EcgYellow
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Status", style = MaterialTheme.typography.labelSmall, color = EcgTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editStatus,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { statusExpanded = true },
                            trailingIcon = {
                                IconButton(onClick = { statusExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = EcgYellow)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EcgYellow,
                                unfocusedBorderColor = EcgCardBorder,
                                focusedTextColor = EcgTextPrimary,
                                unfocusedTextColor = EcgTextPrimary
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )

                        DropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false },
                            modifier = Modifier.background(EcgDarkSurface)
                        ) {
                            statuses.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st, color = EcgWhite) },
                                    onClick = {
                                        editStatus = st
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Assigned Technician / Contractor", style = MaterialTheme.typography.labelSmall, color = EcgTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = editTech,
                        onValueChange = { editTech = it },
                        placeholder = { Text("Name of assigned technician", color = EcgTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcgYellow,
                            unfocusedBorderColor = EcgCardBorder,
                            focusedTextColor = EcgTextPrimary,
                            unfocusedTextColor = EcgTextPrimary
                        ),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Admin Remarks / Instructions", style = MaterialTheme.typography.labelSmall, color = EcgTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = editRemarks,
                        onValueChange = { editRemarks = it },
                        placeholder = { Text("Remarks, site inspection outcome...", color = EcgTextMuted) },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcgYellow,
                            unfocusedBorderColor = EcgCardBorder,
                            focusedTextColor = EcgTextPrimary,
                            unfocusedTextColor = EcgTextPrimary
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateMeterStatus(req.id, editStatus, editTech.ifBlank { null }, editRemarks.ifBlank { null })
                        editingRequest = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcgYellow, contentColor = EcgOnYellow),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { editingRequest = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = EcgTextSecondary)
                }
            }
        )
    }
}

@Composable
fun AdminMeterRequestCard(
    request: MeterRequestEntity,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (request.status.lowercase()) {
        "completed" -> EcgStatusResolved
        "in progress" -> EcgStatusInProgress
        "rejected" -> EcgStatusRejected
        "reviewed" -> EcgStatusReviewed
        "assigned" -> EcgYellow
        else -> EcgStatusPending
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = EcgDarkSurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EcgCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.requestReference,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EcgYellow
                )

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, statusColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = request.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${request.fullName} • ${request.phoneNumber}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = EcgWhite
            )

            Text(
                text = "${request.location} (${request.region}) • GPS: ${request.gpsAddress}",
                style = MaterialTheme.typography.bodySmall,
                color = EcgTextSecondary,
                fontSize = 11.sp
            )

            if (!request.adminRemarks.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Remarks: ${request.adminRemarks}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EcgStatusInProgress,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tech: ${request.assignedTechnician ?: "None assigned"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (request.assignedTechnician != null) EcgYellow else EcgTextMuted,
                    fontSize = 11.sp
                )

                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C2C2C),
                        contentColor = EcgYellow
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Update Status", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
