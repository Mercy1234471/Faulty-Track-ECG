package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AppLoginScreen
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.theme.EcgDarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EcgViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: EcgViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("ecg_prefs", Context.MODE_PRIVATE)

        setContent {
            MyApplicationTheme {
                val faults by viewModel.faults.collectAsStateWithLifecycle()
                val recentFaults by viewModel.recentFaults.collectAsStateWithLifecycle()
                val meterRequests by viewModel.meterRequests.collectAsStateWithLifecycle()
                val technicians by viewModel.technicians.collectAsStateWithLifecycle()
                val districts by viewModel.districts.collectAsStateWithLifecycle()
                val notifications by viewModel.notifications.collectAsStateWithLifecycle()
                val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
                val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
                val serverIp by viewModel.serverIp.collectAsStateWithLifecycle()
                val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

                var isAppLoggedIn by remember {
                    mutableStateOf(prefs.getBoolean("is_logged_in", true))
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = EcgDarkBackground
                ) {
                    if (!isAppLoggedIn) {
                        AppLoginScreen(
                            onLoginSuccess = { role, user ->
                                prefs.edit().putBoolean("is_logged_in", true).apply()
                                viewModel.setUserRole(role)
                                isAppLoggedIn = true
                            }
                        )
                    } else {
                        MainDashboardScreen(
                            faults = faults,
                            recentFaults = recentFaults,
                            meterRequests = meterRequests,
                            technicians = technicians,
                            districts = districts,
                            notifications = notifications,
                            unreadCount = unreadCount,
                            isAdminLoggedIn = isAdminLoggedIn,
                            serverIp = serverIp,
                            snackbarMessage = snackbarMessage,
                            onClearSnackbar = { viewModel.clearSnackbar() },
                            onLoginAdmin = { pwd -> viewModel.loginAdmin(pwd) },
                            onLogoutAdmin = {
                                viewModel.logoutAdmin()
                            },
                            onSubmitFault = { type, reg, dist, loc, gps, desc, dt, tm, tech ->
                                viewModel.logNewFault(type, reg, dist, loc, gps, desc, dt, tm, tech)
                            },
                            onUpdateFaultStatus = { id, st, notes ->
                                viewModel.updateFaultStatus(id, st, notes)
                            },
                            onAssignTechnician = { id, tech ->
                                viewModel.assignTechnician(id, tech)
                            },
                            onDeleteFault = { id ->
                                viewModel.deleteFault(id)
                            },
                            onSubmitMeterRequest = { name, ph, gps, loc, reg, cat, f, b, ec, sp ->
                                viewModel.submitMeterRequest(name, ph, gps, loc, reg, cat, f, b, ec, sp)
                            },
                            onUpdateMeterStatus = { id, st, tech, rem ->
                                viewModel.updateMeterRequestStatus(id, st, tech, rem)
                            },
                            onMarkAllNotificationsRead = {
                                viewModel.markAllNotificationsAsRead()
                            },
                            onNotificationClick = { notif ->
                                viewModel.markNotificationAsRead(notif.id)
                            },
                            onExportFaultsExcel = {
                                viewModel.exportFaultsExcel()
                            },
                            onExportMeterRequestsExcel = {
                                viewModel.exportMeterRequestsExcel()
                            }
                        )
                    }
                }
            }
        }
    }
}
