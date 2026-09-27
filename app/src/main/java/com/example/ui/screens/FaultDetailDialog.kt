package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sms
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FaultEntity
import com.example.data.model.TechnicianEntity
import com.example.ui.components.TechnicianAssignDialog
import com.example.ui.theme.EcgCardBorder
import com.example.ui.theme.EcgDarkSurface
import com.example.ui.theme.EcgDarkSurfaceVariant
import com.example.ui.theme.EcgOnYellow
import com.example.ui.theme.EcgStatusInProgress
import com.example.ui.theme.EcgStatusPending
import com.example.ui.theme.EcgStatusRejected
import com.example.ui.theme.EcgStatusResolved
import com.example.ui.theme.EcgTextMuted
import com.example.ui.theme.EcgTextPrimary
import com.example.ui.theme.EcgTextSecondary
import com.example.ui.theme.EcgWhite
import com.example.ui.theme.EcgYellow
import com.example.util.SmsNotificationHelper

@Composable
fun FaultDetailDialog(
    fault: FaultEntity,
    technicians: List<TechnicianEntity>,
    onDismiss: () -> Unit,
    onUpdateStatus: (newStatus: String, notes: String?) -> Unit,
    onReassignTechnician: (TechnicianEntity) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var isEditingStatus by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf(fault.status) }
    var resolutionNotes by remember { mutableStateOf(fault.resolutionNotes ?: "") }
    var showAssignDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    val statusOptions = listOf("Pending", "In Progress", "Resolved")

    val statusColor = when (fault.status.lowercase()) {
        "resolved" -> EcgStatusResolved
        "in progress" -> EcgStatusInProgress
        else -> EcgStatusPending
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fault_detail_dialog"),
        containerColor = EcgDarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(statusColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = fault.faultReference,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EcgWhite
                        )
                        Text(
                            text = fault.faultType,
                            style = MaterialTheme.typography.bodySmall,
                            color = EcgYellow
                        )
                    }
                }

                // Delete Fault Action
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.testTag("delete_fault_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Fault",
                        tint = EcgStatusRejected
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Status Badge & Timing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, statusColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = fault.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = EcgTextMuted, modifier = Modifier.size(12.dp))
                        Text(
                            text = "${fault.reportedDate} ${fault.reportedTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EcgTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Location Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EcgDarkSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EcgCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = EcgYellow, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Location Details",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EcgYellow
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = fault.locationAddress,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = EcgWhite
                        )
                        Text(
                            text = "${fault.region} • ${fault.district} • GPS: ${fault.gpsAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EcgTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fault Description
                Text(
                    text = "DESCRIPTION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EcgTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fault.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EcgTextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Assigned Technician Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (fault.assignedTechnicianName != null) Color(0xFF242214) else EcgDarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (fault.assignedTechnicianName != null) EcgYellow.copy(alpha = 0.6f) else EcgCardBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ASSIGNED TECHNICIAN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EcgYellow
                            )
                            Text(
                                text = if (fault.assignedTechnicianName != null) "Reassign" else "Assign",
                                style = MaterialTheme.typography.labelSmall,
                                color = EcgYellow,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { showAssignDialog = true }
                                    .testTag("reassign_tech_link")
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (fault.assignedTechnicianName != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = fault.assignedTechnicianName ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EcgWhite
                                    )
                                    Text(
                                        text = "${fault.assignedTechnicianPhone ?: ""} • ${fault.assignedTechnicianLocation ?: ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EcgTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                // Quick Call & SMS
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            fault.assignedTechnicianPhone?.let {
                                                SmsNotificationHelper.launchPhoneCall(context, it)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = "Call Tech", tint = EcgYellow, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            fault.assignedTechnicianPhone?.let {
                                                val msg = SmsNotificationHelper.generateAssignmentMessage(fault.faultReference)
                                                SmsNotificationHelper.launchSmsComposer(context, it, msg)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Sms, contentDescription = "SMS Tech", tint = EcgYellow, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No technician assigned yet. Tap 'Assign' above to dispatch a linesman via SMS.",
                                style = MaterialTheme.typography.bodySmall,
                                color = EcgTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Resolution Notes / Edit Status Area
                if (!isEditingStatus) {
                    if (!fault.resolutionNotes.isNullOrBlank()) {
                        Text(
                            text = "RESOLUTION NOTES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EcgTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = fault.resolutionNotes ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EcgTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedButton(
                        onClick = { isEditingStatus = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_fault_status_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EcgYellow),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EcgYellow),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Update Status & Resolution Notes")
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EcgDarkSurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, EcgCardBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "UPDATE STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EcgYellow
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedStatus,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { statusDropdownExpanded = true }
                                    .testTag("status_select_field"),
                                trailingIcon = {
                                    IconButton(onClick = { statusDropdownExpanded = true }) {
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
                                expanded = statusDropdownExpanded,
                                onDismissRequest = { statusDropdownExpanded = false },
                                modifier = Modifier
                                    .background(EcgDarkSurface)
                                    .border(1.dp, EcgCardBorder, RoundedCornerShape(6.dp))
                            ) {
                                statusOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt, color = EcgWhite) },
                                        onClick = {
                                            selectedStatus = opt
                                            statusDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = resolutionNotes,
                            onValueChange = { resolutionNotes = it },
                            placeholder = { Text("Resolution notes, parts replaced, crew report...", color = EcgTextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EcgYellow,
                                unfocusedBorderColor = EcgCardBorder,
                                focusedTextColor = EcgTextPrimary,
                                unfocusedTextColor = EcgTextPrimary
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { isEditingStatus = false },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Cancel", color = EcgTextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onUpdateStatus(selectedStatus, resolutionNotes)
                                    isEditingStatus = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EcgYellow, contentColor = EcgOnYellow),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("save_status_button")
                            ) {
                                Text("Save Update", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EcgYellow, contentColor = EcgOnYellow),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )

    // Reassign Technician Dialog
    if (showAssignDialog) {
        TechnicianAssignDialog(
            technicians = technicians,
            currentlySelected = null,
            faultRefPlaceholder = fault.faultReference,
            onDismiss = { showAssignDialog = false },
            onSelectTechnician = { tech ->
                onReassignTechnician(tech)
                showAssignDialog = false
            }
        )
    }

    // Delete Confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = EcgDarkSurface,
            title = {
                Text("Delete Fault Record?", color = EcgWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete ${fault.faultReference}? This action cannot be undone.",
                    color = EcgTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcgStatusRejected,
                        contentColor = EcgWhite
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete Record", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirm = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = EcgTextSecondary)
                }
            }
        )
    }
}
