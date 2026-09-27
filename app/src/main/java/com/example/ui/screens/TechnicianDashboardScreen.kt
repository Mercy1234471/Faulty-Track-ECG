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
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TechnicianEntity
import com.example.ui.theme.EcgCardBorder
import com.example.ui.theme.EcgDarkSurface
import com.example.ui.theme.EcgDarkSurfaceVariant
import com.example.ui.theme.EcgOnYellow
import com.example.ui.theme.EcgTextMuted
import com.example.ui.theme.EcgTextPrimary
import com.example.ui.theme.EcgTextSecondary
import com.example.ui.theme.EcgWhite
import com.example.ui.theme.EcgYellow
import com.example.util.SmsNotificationHelper

@Composable
fun TechnicianDashboardScreen(
    technicians: List<TechnicianEntity>,
    districts: List<String>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Technicians, 1: Districts
    var searchQuery by remember { mutableStateOf("") }

    val filteredTechs = remember(technicians, searchQuery) {
        if (searchQuery.isBlank()) technicians
        else technicians.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.staffId.contains(searchQuery, ignoreCase = true) ||
            it.location.contains(searchQuery, ignoreCase = true) ||
            it.district.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("technician_dashboard_screen")
    ) {
        // Header
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
                    imageVector = Icons.Default.Engineering,
                    contentDescription = null,
                    tint = EcgOnYellow,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = "ECG Technician Directory",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EcgWhite
                )
                Text(
                    text = "${technicians.size} Field Engineers Across Districts",
                    style = MaterialTheme.typography.bodySmall,
                    color = EcgYellow
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Switcher Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcgDarkSurface, RoundedCornerShape(8.dp))
                .border(1.dp, EcgCardBorder, RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selectedTab == 0) EcgYellow else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 10.dp)
                    .testTag("tech_tab_personnel"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Technicians (${technicians.size})",
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTab == 0) EcgOnYellow else EcgTextSecondary,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selectedTab == 1) EcgYellow else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 10.dp)
                    .testTag("tech_tab_districts"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Districts Directory",
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTab == 1) EcgOnYellow else EcgTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, staff ID, district...", color = EcgTextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EcgYellow, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth().testTag("tech_search_input"),
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

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Technicians List
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTechs) { tech ->
                    TechnicianCard(
                        tech = tech,
                        onCall = { SmsNotificationHelper.launchPhoneCall(context, tech.phoneNumber) },
                        onSms = {
                            val msg = "Hello ${tech.name}, ECG Dispatcher message regarding assigned field tasks."
                            SmsNotificationHelper.launchSmsComposer(context, tech.phoneNumber, msg)
                        }
                    )
                }
            }
        } else {
            // Districts and their technicians
            val allDistrictsList = remember(technicians) {
                technicians.map { it.district }.distinct().sorted()
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allDistrictsList) { district ->
                    val districtTechs = technicians.filter { it.district == district }
                    val activeFaults = districtTechs.sumOf { it.activeFaultCount }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.LocationCity, contentDescription = null, tint = EcgYellow)
                                    Text(
                                        text = district,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EcgWhite
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF2E2A14), RoundedCornerShape(12.dp))
                                        .border(1.dp, EcgYellow, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$activeFaults Active Tasks",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EcgYellow,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Assigned Technicians (${districtTechs.size}):",
                                style = MaterialTheme.typography.labelSmall,
                                color = EcgTextMuted,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            districtTechs.forEach { t ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${t.name} (${t.staffId})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = EcgWhite
                                        )
                                        Text(
                                            text = "${t.designation} • ${t.phoneNumber}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EcgTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        IconButton(
                                            onClick = { SmsNotificationHelper.launchPhoneCall(context, t.phoneNumber) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = EcgYellow, modifier = Modifier.size(15.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                SmsNotificationHelper.launchSmsComposer(
                                                    context,
                                                    t.phoneNumber,
                                                    "ECG Alert: Please check assigned duties for $district"
                                                )
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Sms, contentDescription = "SMS", tint = EcgYellow, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TechnicianCard(
    tech: TechnicianEntity,
    onCall: () -> Unit,
    onSms: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = EcgDarkSurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EcgCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(EcgDarkSurfaceVariant, CircleShape)
                        .border(1.dp, EcgYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = EcgYellow,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tech.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EcgWhite
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF383000), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = tech.staffId,
                                style = MaterialTheme.typography.labelSmall,
                                color = EcgYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${tech.designation} • ${tech.phoneNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EcgTextSecondary,
                        fontSize = 12.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = EcgYellow, modifier = Modifier.size(12.dp))
                        Text(
                            text = "${tech.location} (${tech.district})",
                            style = MaterialTheme.typography.bodySmall,
                            color = EcgTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Call & SMS Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF282828), CircleShape)
                        .clickable(onClick = onCall),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        tint = EcgYellow,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF282828), CircleShape)
                        .clickable(onClick = onSms),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "SMS",
                        tint = EcgYellow,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
