package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
fun TechnicianAssignDialog(
    technicians: List<TechnicianEntity>,
    currentlySelected: TechnicianEntity?,
    faultRefPlaceholder: String = "POF-2026-XXXXX",
    onDismiss: () -> Unit,
    onSelectTechnician: (TechnicianEntity) -> Unit
) {
    var selectedTech by remember { mutableStateOf(currentlySelected) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTechs = remember(technicians, searchQuery) {
        if (searchQuery.isBlank()) technicians
        else technicians.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.location.contains(searchQuery, ignoreCase = true) ||
            it.district.contains(searchQuery, ignoreCase = true) ||
            it.phoneNumber.contains(searchQuery)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("technician_selection_dialog"),
        containerColor = EcgDarkSurface,
        title = {
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
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Technicians",
                        tint = EcgOnYellow,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Select Technician",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EcgWhite
                    )
                    Text(
                        text = "Assign field personnel to fault",
                        style = MaterialTheme.typography.bodySmall,
                        color = EcgTextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, location, district...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = EcgTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("tech_search_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcgYellow,
                        unfocusedBorderColor = EcgCardBorder,
                        focusedTextColor = EcgTextPrimary,
                        unfocusedTextColor = EcgTextPrimary,
                        focusedContainerColor = EcgDarkSurfaceVariant,
                        unfocusedContainerColor = EcgDarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // SMS Alert info box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF232014), RoundedCornerShape(8.dp))
                        .border(1.dp, EcgYellow.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "SMS Alert",
                            tint = EcgYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Auto-SMS: \"A fault( $faultRefPlaceholder ) has been assigned to you. Please login to view the details\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = EcgYellow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List of Technicians
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTechs) { tech ->
                        val isSelected = selectedTech?.id == tech.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF2E2A14) else EcgDarkSurfaceVariant)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) EcgYellow else EcgCardBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTech = tech }
                                .padding(12.dp)
                                .testTag("tech_item_${tech.staffId}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedTech = tech },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = EcgYellow,
                                        unselectedColor = EcgTextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = tech.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = EcgWhite
                                        )
                                        Text(
                                            text = tech.staffId,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EcgYellow,
                                            fontSize = 10.sp,
                                            modifier = Modifier
                                                .background(Color(0xFF383000), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = "Phone",
                                            tint = EcgTextSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = tech.phoneNumber,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EcgTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Location",
                                            tint = EcgYellow,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "${tech.location} (${tech.district})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EcgTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedTech?.let { onSelectTechnician(it) }
                },
                enabled = selectedTech != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcgYellow,
                    contentColor = EcgOnYellow,
                    disabledContainerColor = Color(0xFF333333),
                    disabledContentColor = Color(0xFF777777)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("submit_technician_selection_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Submit Selection", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = EcgTextSecondary)
            ) {
                Text("Cancel")
            }
        }
    )
}
