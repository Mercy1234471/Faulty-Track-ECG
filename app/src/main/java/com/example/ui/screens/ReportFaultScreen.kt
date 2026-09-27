package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TechnicianEntity
import com.example.ui.components.TechnicianAssignDialog
import com.example.ui.theme.EcgCardBorder
import com.example.ui.theme.EcgDarkSurface
import com.example.ui.theme.EcgDarkSurfaceVariant
import com.example.ui.theme.EcgOnYellow
import com.example.ui.theme.EcgTextMuted
import com.example.ui.theme.EcgTextPrimary
import com.example.ui.theme.EcgTextSecondary
import com.example.ui.theme.EcgWhite
import com.example.ui.theme.EcgYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportFaultScreen(
    technicians: List<TechnicianEntity>,
    onSubmitFault: (
        type: String,
        region: String,
        district: String,
        location: String,
        gps: String,
        description: String,
        date: String,
        time: String,
        assignedTech: TechnicianEntity?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val faultTypes = listOf(
        "Transformer Outage",
        "Line Drop / Cable Cut",
        "Low Voltage / Fluctuation",
        "Burnt / Broken Pole",
        "Meter Fault / Sparks",
        "Phase Failure",
        "Street Light Outage",
        "Substation Trip",
        "Illegal Connection Report"
    )

    val regions = listOf(
        "Accra East",
        "Accra West",
        "Ashanti",
        "Tema",
        "Eastern",
        "Western",
        "Central",
        "Volta"
    )

    var selectedType by remember { mutableStateOf(faultTypes[0]) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    var selectedRegion by remember { mutableStateOf(regions[0]) }
    var regionDropdownExpanded by remember { mutableStateOf(false) }

    var district by remember { mutableStateOf("Makola District") }
    var locationAddress by remember { mutableStateOf("") }
    var gpsAddress by remember { mutableStateOf("") }

    val currentDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val currentTime = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }

    var reportedDate by remember { mutableStateOf(currentDate) }
    var reportedTime by remember { mutableStateOf(currentTime) }
    var description by remember { mutableStateOf("") }

    var assignedTechnician by remember { mutableStateOf<TechnicianEntity?>(null) }
    var showAssignDialog by remember { mutableStateOf(false) }

    var formError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("report_fault_screen")
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(EcgYellow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = EcgOnYellow,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = "Report / Log a New Fault",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EcgWhite
                )
                Text(
                    text = "ECG Emergency Dispatch & Outage Tracking",
                    style = MaterialTheme.typography.bodySmall,
                    color = EcgTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fault Type Dropdown
        Text(
            text = "FAULT TYPE *",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = EcgYellow
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { typeDropdownExpanded = true }
                    .testTag("fault_type_dropdown"),
                trailingIcon = {
                    IconButton(onClick = { typeDropdownExpanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Type", tint = EcgYellow)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EcgYellow,
                    unfocusedBorderColor = EcgCardBorder,
                    focusedTextColor = EcgTextPrimary,
                    unfocusedTextColor = EcgTextPrimary,
                    focusedContainerColor = EcgDarkSurface,
                    unfocusedContainerColor = EcgDarkSurface
                ),
                shape = RoundedCornerShape(8.dp)
            )

            DropdownMenu(
                expanded = typeDropdownExpanded,
                onDismissRequest = { typeDropdownExpanded = false },
                modifier = Modifier
                    .background(EcgDarkSurface)
                    .border(1.dp, EcgCardBorder, RoundedCornerShape(8.dp))
            ) {
                faultTypes.forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = type,
                                color = if (selectedType == type) EcgYellow else EcgTextPrimary,
                                fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            selectedType = type
                            typeDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Region Dropdown
        Text(
            text = "REGION *",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = EcgYellow
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedRegion,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { regionDropdownExpanded = true }
                    .testTag("fault_region_dropdown"),
                trailingIcon = {
                    IconButton(onClick = { regionDropdownExpanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Region", tint = EcgYellow)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EcgYellow,
                    unfocusedBorderColor = EcgCardBorder,
                    focusedTextColor = EcgTextPrimary,
                    unfocusedTextColor = EcgTextPrimary,
                    focusedContainerColor = EcgDarkSurface,
                    unfocusedContainerColor = EcgDarkSurface
                ),
                shape = RoundedCornerShape(8.dp)
            )

            DropdownMenu(
                expanded = regionDropdownExpanded,
                onDismissRequest = { regionDropdownExpanded = false },
                modifier = Modifier
                    .background(EcgDarkSurface)
                    .border(1.dp, EcgCardBorder, RoundedCornerShape(8.dp))
            ) {
                regions.forEach { region ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = region,
                                color = if (selectedRegion == region) EcgYellow else EcgTextPrimary,
                                fontWeight = if (selectedRegion == region) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            selectedRegion = region
                            regionDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // District Field
        Text(
            text = "DISTRICT / OPERATIONAL ZONE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = EcgYellow
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = district,
            onValueChange = { district = it },
            placeholder = { Text("e.g. Makola, Dansoman, Adum...", color = EcgTextMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fault_district_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EcgYellow,
                unfocusedBorderColor = EcgCardBorder,
                focusedTextColor = EcgTextPrimary,
                unfocusedTextColor = EcgTextPrimary,
                focusedContainerColor = EcgDarkSurface,
                unfocusedContainerColor = EcgDarkSurface
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Location Address & GPS Address in two columns or rows
        Text(
            text = "LOCATION & PHYSICAL ADDRESS *",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = EcgYellow
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = locationAddress,
            onValueChange = { locationAddress = it },
            placeholder = { Text("Street name, landmark, building number...", color = EcgTextMuted) },
            leadingIcon = {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = EcgYellow)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fault_location_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EcgYellow,
                unfocusedBorderColor = EcgCardBorder,
                focusedTextColor = EcgTextPrimary,
                unfocusedTextColor = EcgTextPrimary,
                focusedContainerColor = EcgDarkSurface,
                unfocusedContainerColor = EcgDarkSurface
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "GHANA POST GPS ADDRESS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = EcgYellow
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = gpsAddress,
            onValueChange = { gpsAddress = it },
            placeholder = { Text("e.g. GA-183-9022", color = EcgTextMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fault_gps_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EcgYellow,
                unfocusedBorderColor = EcgCardBorder,
                focusedTextColor = EcgTextPrimary,
                unfocusedTextColor = EcgTextPrimary,
                focusedContainerColor = EcgDarkSurface,
                unfocusedContainerColor = EcgDarkSurface
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Date and Time fields ("add date to it")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DATE REPORTED *",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EcgYellow
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = reportedDate,
                    onValueChange = { reportedDate = it },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = EcgYellow, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fault_date_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcgYellow,
                        unfocusedBorderColor = EcgCardBorder,
                        focusedTextColor = EcgTextPrimary,
                        unfocusedTextColor = EcgTextPrimary,
                        focusedContainerColor = EcgDarkSurface,
                        unfocusedContainerColor = EcgDarkSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TIME REPORTED *",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EcgYellow
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = reportedTime,
                    onValueChange = { reportedTime = it },
                    leadingIcon = {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = EcgYellow, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fault_time_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcgYellow,
                        unfocusedBorderColor = EcgCardBorder,
                        focusedTextColor = EcgTextPrimary,
                        unfocusedTextColor = EcgTextPrimary,
                        focusedContainerColor = EcgDarkSurface,
                        unfocusedContainerColor = EcgDarkSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Description
        Text(
            text = "FAULT DESCRIPTION & DETAILS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = EcgYellow
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Describe spark, outage extent, danger factors, transformer number...", color = EcgTextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Description, contentDescription = null, tint = EcgYellow)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .testTag("fault_description_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EcgYellow,
                unfocusedBorderColor = EcgCardBorder,
                focusedTextColor = EcgTextPrimary,
                unfocusedTextColor = EcgTextPrimary,
                focusedContainerColor = EcgDarkSurface,
                unfocusedContainerColor = EcgDarkSurface
            ),
            shape = RoundedCornerShape(8.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Selected Technician Status Card
        if (assignedTechnician != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("assigned_tech_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF282414)),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EcgYellow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(EcgYellow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = EcgOnYellow, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                text = "Assigned: ${assignedTechnician?.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = EcgWhite
                            )
                            Text(
                                text = "${assignedTechnician?.phoneNumber} • ${assignedTechnician?.location}",
                                style = MaterialTheme.typography.bodySmall,
                                color = EcgYellow,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = { assignedTechnician = null }) {
                        Icon(Icons.Default.Clear, contentDescription = "Remove Tech", tint = EcgTextMuted)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Form Error
        if (formError != null) {
            Text(
                text = formError ?: "",
                color = Color(0xFFFF5252),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Downright corner assign button + Submit Row
        // Requirement: "There should be an assign to a technician at the downright corner of the log a new fault form.
        // When you click on assign, all technicians name, phone number and location should popup on your screen to select your technician then you click submit at where you selected your technician"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Submit Button
            Button(
                onClick = {
                    if (locationAddress.isBlank()) {
                        formError = "Please specify the location address."
                        return@Button
                    }
                    formError = null
                    onSubmitFault(
                        selectedType,
                        selectedRegion,
                        district,
                        locationAddress,
                        gpsAddress.ifBlank { "GA-000-0000" },
                        description.ifBlank { "Fault reported at $locationAddress" },
                        reportedDate,
                        reportedTime,
                        assignedTechnician
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcgYellow,
                    contentColor = EcgOnYellow
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("submit_fault_report_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (assignedTechnician != null) "Submit & Send SMS" else "Submit Fault Report",
                    fontWeight = FontWeight.Bold
                )
            }

            // Downright Corner: "Assign to a Technician" button
            Button(
                onClick = { showAssignDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C2C2C),
                    contentColor = EcgYellow
                ),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EcgYellow),
                modifier = Modifier.testTag("assign_to_technician_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Assign Technician",
                    tint = EcgYellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (assignedTechnician == null) "Assign Technician" else "Change Tech",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Technician Selection Popup Dialog
    if (showAssignDialog) {
        TechnicianAssignDialog(
            technicians = technicians,
            currentlySelected = assignedTechnician,
            onDismiss = { showAssignDialog = false },
            onSelectTechnician = { tech ->
                assignedTechnician = tech
                showAssignDialog = false
            }
        )
    }
}
