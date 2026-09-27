package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EcgCardBorder
import com.example.ui.theme.EcgDarkSurface
import com.example.ui.theme.EcgOnYellow
import com.example.ui.theme.EcgStatusRejected
import com.example.ui.theme.EcgTextPrimary
import com.example.ui.theme.EcgTextSecondary
import com.example.ui.theme.EcgYellow

@Composable
fun EcgTopBar(
    serverIp: String,
    unreadNotificationsCount: Int,
    isAdminLoggedIn: Boolean = false,
    onNotificationsClick: () -> Unit,
    onAdminClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = EcgDarkSurface,
        tonalElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, color = EcgCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ECG Branding Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("ecg_brand_logo")
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(EcgYellow, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "ECG Lightning Logo",
                        tint = EcgOnYellow,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "ECG FAULT TRACK",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = EcgTextPrimary,
                        fontSize = 13.sp,
                        letterSpacing = 0.3.sp
                    )
                    Text(
                        text = "Electricity Company of Ghana",
                        style = MaterialTheme.typography.labelSmall,
                        color = EcgYellow,
                        fontSize = 10.sp
                    )
                }
            }

            // Action Icons (Admin & Notifications)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Admin Only Button
                if (onAdminClick != null) {
                    IconButton(
                        onClick = onAdminClick,
                        modifier = Modifier.testTag("admin_top_bar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Only",
                            tint = if (isAdminLoggedIn) EcgYellow else EcgTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Notifications Symbol with RED DOT
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("notification_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationsCount > 0) {
                                // Vivid Red Dot
                                Badge(
                                    containerColor = EcgStatusRejected,
                                    contentColor = Color.White,
                                    modifier = Modifier.testTag("notification_red_dot")
                                ) {
                                    Text(
                                        text = if (unreadNotificationsCount > 9) "9+" else "$unreadNotificationsCount",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (unreadNotificationsCount > 0) EcgYellow else EcgTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
