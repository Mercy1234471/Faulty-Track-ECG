package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FaultEntity
import com.example.data.model.MeterRequestEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TechnicianEntity
import com.example.ui.components.EcgTopBar
import com.example.ui.theme.EcgDarkBackground
import com.example.ui.theme.EcgDarkSurface
import com.example.ui.theme.EcgOnYellow
import com.example.ui.theme.EcgStatusRejected
import com.example.ui.theme.EcgTextMuted
import com.example.ui.theme.EcgTextSecondary
import com.example.ui.theme.EcgYellow

sealed class EcgScreenNav(val route: String, val title: String) {
    object FaultDashboard : EcgScreenNav("fault_dashboard", "Faults")
    object ReportFault : EcgScreenNav("report_fault", "Report")
    object FaultList : EcgScreenNav("fault_list", "Fault List")
    object MeterRequests : EcgScreenNav("meter_requests", "Meters")
    object Technicians : EcgScreenNav("technicians", "Techs")
    object Notifications : EcgScreenNav("notifications", "Alerts")
    object Admin : EcgScreenNav("admin", "Admin Only")
}

@Composable
fun MainDashboardScreen(
    faults: List<FaultEntity>,
    recentFaults: List<FaultEntity>,
    meterRequests: List<MeterRequestEntity>,
    technicians: List<TechnicianEntity>,
    districts: List<String>,
    notifications: List<NotificationEntity>,
    unreadCount: Int,
    isAdminLoggedIn: Boolean,
    serverIp: String,
    snackbarMessage: String?,
    onClearSnackbar: () -> Unit,
    onLoginAdmin: (password: String) -> Boolean,
    onLogoutAdmin: () -> Unit,
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
    onUpdateFaultStatus: (faultId: Long, newStatus: String, notes: String?) -> Unit,
    onAssignTechnician: (faultId: Long, tech: TechnicianEntity) -> Unit,
    onDeleteFault: (faultId: Long) -> Unit,
    onSubmitMeterRequest: (
        fullName: String,
        phoneNumber: String,
        gpsAddress: String,
        location: String,
        region: String,
        category: String,
        ghanaCardFront: String?,
        ghanaCardBack: String?,
        energyCommissionForm: String?,
        sitePlanPhoto: String?
    ) -> Unit,
    onUpdateMeterStatus: (id: Long, status: String, tech: String?, remarks: String?) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    onNotificationClick: (NotificationEntity) -> Unit,
    onExportFaultsExcel: () -> Unit,
    onExportMeterRequestsExcel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<EcgScreenNav>(EcgScreenNav.FaultDashboard) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onClearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EcgDarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EcgTopBar(
                serverIp = serverIp,
                unreadNotificationsCount = unreadCount,
                isAdminLoggedIn = isAdminLoggedIn,
                onNotificationsClick = {
                    currentScreen = EcgScreenNav.Notifications
                },
                onAdminClick = {
                    currentScreen = EcgScreenNav.Admin
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = EcgDarkSurface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("ecg_bottom_navigation")
            ) {
                // 1. ECG Faults Hub
                NavigationBarItem(
                    selected = currentScreen is EcgScreenNav.FaultDashboard ||
                               currentScreen is EcgScreenNav.ReportFault ||
                               currentScreen is EcgScreenNav.FaultList,
                    onClick = { currentScreen = EcgScreenNav.FaultDashboard },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Faults Hub",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Faults", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EcgOnYellow,
                        selectedTextColor = EcgYellow,
                        indicatorColor = EcgYellow,
                        unselectedIconColor = EcgTextMuted,
                        unselectedTextColor = EcgTextMuted
                    ),
                    modifier = Modifier.testTag("nav_faults")
                )

                // 2. Meter Requests
                NavigationBarItem(
                    selected = currentScreen is EcgScreenNav.MeterRequests,
                    onClick = { currentScreen = EcgScreenNav.MeterRequests },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ElectricMeter,
                            contentDescription = "Meter Requests",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Meters", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EcgOnYellow,
                        selectedTextColor = EcgYellow,
                        indicatorColor = EcgYellow,
                        unselectedIconColor = EcgTextMuted,
                        unselectedTextColor = EcgTextMuted
                    ),
                    modifier = Modifier.testTag("nav_meters")
                )

                // 3. Technicians Directory
                NavigationBarItem(
                    selected = currentScreen is EcgScreenNav.Technicians,
                    onClick = { currentScreen = EcgScreenNav.Technicians },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = "Technicians",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Technicians", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EcgOnYellow,
                        selectedTextColor = EcgYellow,
                        indicatorColor = EcgYellow,
                        unselectedIconColor = EcgTextMuted,
                        unselectedTextColor = EcgTextMuted
                    ),
                    modifier = Modifier.testTag("nav_technicians")
                )

                // 4. Admin Only
                NavigationBarItem(
                    selected = currentScreen is EcgScreenNav.Admin,
                    onClick = { currentScreen = EcgScreenNav.Admin },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Only",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Admin Only", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EcgOnYellow,
                        selectedTextColor = EcgYellow,
                        indicatorColor = EcgYellow,
                        unselectedIconColor = EcgTextMuted,
                        unselectedTextColor = EcgTextMuted
                    ),
                    modifier = Modifier.testTag("nav_admin")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is EcgScreenNav.FaultDashboard -> {
                    FaultLogDashboardScreen(
                        faults = faults,
                        recentFaults = recentFaults,
                        technicians = technicians,
                        onNavigateToReport = { currentScreen = EcgScreenNav.ReportFault },
                        onNavigateToAllFaults = { currentScreen = EcgScreenNav.FaultList },
                        onExportExcel = onExportFaultsExcel,
                        onUpdateStatus = onUpdateFaultStatus,
                        onAssignTechnician = onAssignTechnician,
                        onDeleteFault = onDeleteFault,
                        onNavigateToAdmin = { currentScreen = EcgScreenNav.Admin },
                        isAdminLoggedIn = isAdminLoggedIn
                    )
                }

                is EcgScreenNav.ReportFault -> {
                    ReportFaultScreen(
                        technicians = technicians,
                        onSubmitFault = { type, region, district, location, gps, description, date, time, tech ->
                            onSubmitFault(type, region, district, location, gps, description, date, time, tech)
                            currentScreen = EcgScreenNav.FaultList
                        }
                    )
                }

                is EcgScreenNav.FaultList -> {
                    FaultListScreen(
                        faults = faults,
                        technicians = technicians,
                        onUpdateStatus = onUpdateFaultStatus,
                        onAssignTechnician = onAssignTechnician,
                        onDeleteFault = onDeleteFault,
                        onExportExcel = onExportFaultsExcel
                    )
                }

                is EcgScreenNav.MeterRequests -> {
                    MeterRequestScreen(
                        meterRequests = meterRequests,
                        onSubmitRequest = onSubmitMeterRequest,
                        onExportExcel = onExportMeterRequestsExcel
                    )
                }

                is EcgScreenNav.Technicians -> {
                    TechnicianDashboardScreen(
                        technicians = technicians,
                        districts = districts
                    )
                }

                is EcgScreenNav.Notifications -> {
                    NotificationScreen(
                        notifications = notifications,
                        onMarkAllAsRead = onMarkAllNotificationsRead,
                        onNotificationClick = onNotificationClick
                    )
                }

                is EcgScreenNav.Admin -> {
                    AdminDashboardScreen(
                        isAdminLoggedIn = isAdminLoggedIn,
                        faults = faults,
                        meterRequests = meterRequests,
                        technicians = technicians,
                        serverIp = serverIp,
                        onLoginAdmin = onLoginAdmin,
                        onLogoutAdmin = onLogoutAdmin,
                        onUpdateMeterStatus = onUpdateMeterStatus,
                        onExportFaultsExcel = onExportFaultsExcel,
                        onExportMeterRequestsExcel = onExportMeterRequestsExcel
                    )
                }
            }
        }
    }
}
