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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.FaultEntity
import com.example.data.model.TechnicianEntity
import com.example.ui.theme.EcgCardBorder
import com.example.ui.theme.EcgDarkSurface
import com.example.ui.theme.EcgDarkSurfaceVariant
import com.example.ui.theme.EcgOnYellow
import com.example.ui.theme.EcgStatusInProgress
import com.example.ui.theme.EcgStatusPending
import com.example.ui.theme.EcgStatusResolved
import com.example.ui.theme.EcgTextMuted
import com.example.ui.theme.EcgTextPrimary
import com.example.ui.theme.EcgTextSecondary
import com.example.ui.theme.EcgWhite
import com.example.ui.theme.EcgYellow

@Composable
fun FaultListScreen(
    faults: List<FaultEntity>,
    technicians: List<TechnicianEntity>,
    onUpdateStatus: (faultId: Long, newStatus: String, notes: String?) -> Unit,
    onAssignTechnician: (faultId: Long, tech: TechnicianEntity) -> Unit,
    onDeleteFault: (faultId: Long) -> Unit,
    onExportExcel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filterTabs = listOf("All", "Pending", "In Progress", "Resolved")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFaultForDetail by remember { mutableStateOf<FaultEntity?>(null) }

    val filteredFaults = remember(faults, selectedTabIndex, searchQuery) {
        val tabFiltered = when (selectedTabIndex) {
            1 -> faults.filter { it.status.equals("Pending", ignoreCase = true) }
            2 -> faults.filter { it.status.equals("In Progress", ignoreCase = true) }
            3 -> faults.filter { it.status.equals("Resolved", ignoreCase = true) }
            else -> faults
        }

        if (searchQuery.isBlank()) tabFiltered
        else tabFiltered.filter {
            it.faultReference.contains(searchQuery, ignoreCase = true) ||
            it.faultType.contains(searchQuery, ignoreCase = true) ||
            it.locationAddress.contains(searchQuery, ignoreCase = true) ||
            it.district.contains(searchQuery, ignoreCase = true) ||
            it.region.contains(searchQuery, ignoreCase = true) ||
            (it.assignedTechnicianName?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("fault_list_screen")
    ) {
        // Header with Excel Download Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ECG Fault Reports",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EcgWhite
                )
                Text(
                    text = "${filteredFaults.size} records matching filter",
                    style = MaterialTheme.typography.bodySmall,
                    color = EcgTextMuted
                )
            }

            Button(
                onClick = onExportExcel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcgYellow,
                    contentColor = EcgOnYellow
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("download_excel_button")
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Excel Report", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by reference (POF-...), type, area...", color = EcgTextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = EcgYellow, modifier = Modifier.size(18.dp))
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fault_search_input"),
            singleLine = true,
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

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Tabs: All, Pending, In Progress, Resolved
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = EcgDarkSurface,
            contentColor = EcgYellow,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = EcgYellow,
                    height = 3.dp
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(EcgCardBorder)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .testTag("fault_filter_tabs")
        ) {
            filterTabs.forEachIndexed { index, title ->
                val count = when (index) {
                    1 -> faults.count { it.status.equals("Pending", ignoreCase = true) }
                    2 -> faults.count { it.status.equals("In Progress", ignoreCase = true) }
                    3 -> faults.count { it.status.equals("Resolved", ignoreCase = true) }
                    else -> faults.size
                }
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = "$title ($count)",
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) EcgYellow else EcgTextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_$title")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Faults List / Table Cards
        if (filteredFaults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = EcgTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No faults found for this filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EcgTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("faults_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredFaults) { fault ->
                    FaultRowCard(
                        fault = fault,
                        onClick = { selectedFaultForDetail = fault }
                    )
                }
            }
        }
    }

    // Detail Dialog
    selectedFaultForDetail?.let { fault ->
        FaultDetailDialog(
            fault = fault,
            technicians = technicians,
            onDismiss = { selectedFaultForDetail = null },
            onUpdateStatus = { newStatus, notes ->
                onUpdateStatus(fault.id, newStatus, notes)
                selectedFaultForDetail = fault.copy(status = newStatus, resolutionNotes = notes)
            },
            onReassignTechnician = { tech ->
                onAssignTechnician(fault.id, tech)
                selectedFaultForDetail = fault.copy(
                    assignedTechnicianId = tech.id,
                    assignedTechnicianName = tech.name,
                    assignedTechnicianPhone = tech.phoneNumber,
                    assignedTechnicianLocation = tech.location,
                    status = "In Progress"
                )
            },
            onDelete = {
                onDeleteFault(fault.id)
                selectedFaultForDetail = null
            }
        )
    }
}

@Composable
fun FaultRowCard(
    fault: FaultEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (fault.status.lowercase()) {
        "resolved" -> EcgStatusResolved
        "in progress" -> EcgStatusInProgress
        else -> EcgStatusPending
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("fault_card_${fault.faultReference}"),
        colors = CardDefaults.cardColors(containerColor = EcgDarkSurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EcgCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Ref ID + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = fault.faultReference,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = EcgYellow
                    )
                    Text(
                        text = fault.faultType,
                        style = MaterialTheme.typography.bodySmall,
                        color = EcgWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, statusColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = fault.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = EcgTextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${fault.locationAddress} (${fault.district})",
                    style = MaterialTheme.typography.bodySmall,
                    color = EcgTextSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Row: Date & Time of Report + Assigned Technician
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reported Date and Time (Explicit requirement #4)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time Reported",
                        tint = EcgYellow,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${fault.reportedDate} at ${fault.reportedTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EcgTextMuted,
                        fontSize = 11.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = fault.assignedTechnicianName ?: "Unassigned",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (fault.assignedTechnicianName != null) EcgYellow else EcgTextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (fault.assignedTechnicianName != null) FontWeight.Bold else FontWeight.Normal
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = EcgTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
