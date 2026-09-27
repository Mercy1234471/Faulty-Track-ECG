package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FaultEntity
import com.example.data.model.TechnicianEntity
import com.example.ui.components.BarChartFaultsByType
import com.example.ui.components.DoughnutChartFaultsByRegion
import com.example.ui.components.StatCardsGrid
import com.example.ui.theme.EcgCardBorder
import com.example.ui.theme.EcgDarkSurface
import com.example.ui.theme.EcgDarkSurfaceVariant
import com.example.ui.theme.EcgOnYellow
import com.example.ui.theme.EcgTextMuted
import com.example.ui.theme.EcgTextPrimary
import com.example.ui.theme.EcgTextSecondary
import com.example.ui.theme.EcgWhite
import com.example.ui.theme.EcgYellow

@Composable
fun FaultLogDashboardScreen(
    faults: List<FaultEntity>,
    recentFaults: List<FaultEntity>,
    technicians: List<TechnicianEntity>,
    onNavigateToReport: () -> Unit,
    onNavigateToAllFaults: () -> Unit,
    onExportExcel: () -> Unit,
    onUpdateStatus: (faultId: Long, newStatus: String, notes: String?) -> Unit,
    onAssignTechnician: (faultId: Long, tech: TechnicianEntity) -> Unit,
    onDeleteFault: (faultId: Long) -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    isAdminLoggedIn: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedFaultForDetail by remember { mutableStateOf<FaultEntity?>(null) }
    var showMenuWindow by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Full Page Menu Window
    if (showMenuWindow) {
        BackHandler { showMenuWindow = false }
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(com.example.ui.theme.EcgDarkBackground)
                .padding(20.dp)
                .testTag("full_page_menu_window")
        ) {
            // Header with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showMenuWindow = false },
                    modifier = Modifier
                        .size(42.dp)
                        .background(EcgDarkSurfaceVariant, RoundedCornerShape(10.dp))
                        .border(1.dp, EcgCardBorder, RoundedCornerShape(10.dp))
                        .testTag("menu_window_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = EcgYellow,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Menu Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = EcgWhite,
                    fontSize = 20.sp
                )
            }

            // 1. Report Fault
            FullPageMenuItem(
                title = "Report Fault",
                icon = Icons.Default.Warning,
                iconBgColor = EcgYellow,
                iconColor = EcgOnYellow,
                testTag = "menu_item_report_fault",
                onClick = {
                    showMenuWindow = false
                    onNavigateToReport()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Recent Fault
            FullPageMenuItem(
                title = "Recent Fault",
                icon = Icons.Default.History,
                iconBgColor = EcgDarkSurfaceVariant,
                iconColor = EcgYellow,
                testTag = "menu_item_recent_fault",
                onClick = {
                    showMenuWindow = false
                    onNavigateToAllFaults()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Export Excel Sheet
            FullPageMenuItem(
                title = "Export Excel Sheet",
                icon = Icons.Default.FileDownload,
                iconBgColor = EcgDarkSurfaceVariant,
                iconColor = EcgYellow,
                testTag = "menu_item_export_excel",
                onClick = {
                    showMenuWindow = false
                    onExportExcel()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Admin Only
            FullPageMenuItem(
                title = "Admin Only",
                icon = Icons.Default.AdminPanelSettings,
                iconBgColor = if (isAdminLoggedIn) EcgYellow else EcgDarkSurfaceVariant,
                iconColor = if (isAdminLoggedIn) EcgOnYellow else EcgYellow,
                testTag = "menu_item_admin_only",
                onClick = {
                    showMenuWindow = false
                    onNavigateToAdmin()
                }
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("fault_log_dashboard_screen")
    ) {
        // Dashboard Title & Quick Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(EcgYellow, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "ECG Logo",
                        tint = EcgOnYellow,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "ECG Log Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EcgWhite
                    )
                    Text(
                        text = "National Distribution Grid Monitoring",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = EcgYellow
                    )
                }
            }

            // Three lines menu button
            IconButton(
                onClick = { showMenuWindow = true },
                modifier = Modifier
                    .size(38.dp)
                    .background(EcgDarkSurfaceVariant, RoundedCornerShape(8.dp))
                    .border(1.dp, EcgCardBorder, RoundedCornerShape(8.dp))
                    .testTag("dashboard_three_lines_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Dashboard Menu",
                    tint = EcgYellow,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Stat Cards: Total, Pending, In Progress, Resolved
        StatCardsGrid(faults = faults)

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Bar Chart of faults by type
        BarChartFaultsByType(faults = faults)

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Doughnut Chart of faults by region
        DoughnutChartFaultsByRegion(faults = faults)

        Spacer(modifier = Modifier.height(24.dp))
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
private fun FullPageMenuItem(
    title: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(EcgDarkSurface)
            .border(1.dp, EcgCardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 20.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(iconBgColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = EcgWhite,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = EcgYellow,
            modifier = Modifier.size(22.dp)
        )
    }
}
