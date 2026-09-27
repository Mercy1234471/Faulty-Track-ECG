package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.MeterRequestEntity
import com.example.ui.theme.EcgCardBorder
import com.example.ui.theme.EcgDarkSurface
import com.example.ui.theme.EcgDarkSurfaceVariant
import com.example.ui.theme.EcgOnYellow
import com.example.ui.theme.EcgStatusResolved
import com.example.ui.theme.EcgTextMuted
import com.example.ui.theme.EcgTextPrimary
import com.example.ui.theme.EcgTextSecondary
import com.example.ui.theme.EcgWhite
import com.example.ui.theme.EcgYellow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MeterRequestScreen(
    meterRequests: List<MeterRequestEntity> = emptyList(),
    onSubmitRequest: (
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
    onExportExcel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val categories = listOf("Domestic", "Residential", "Non Residential")
    val regions = listOf("Accra East", "Accra West", "Ashanti", "Tema", "Eastern", "Western", "Central", "Volta")

    // Form states
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var gpsAddress by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf(regions[0]) }
    var regionDropdownExpanded by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    // Upload image states & Gallery photo pickers
    var ghanaCardFrontUri by remember { mutableStateOf<Uri?>(null) }
    var ghanaCardBackUri by remember { mutableStateOf<Uri?>(null) }
    var energyCommissionUri by remember { mutableStateOf<Uri?>(null) }
    var sitePlanUri by remember { mutableStateOf<Uri?>(null) }

    var ghanaCardFrontUploaded by remember { mutableStateOf(false) }
    var ghanaCardBackUploaded by remember { mutableStateOf(false) }
    var energyCommissionUploaded by remember { mutableStateOf(false) }
    var sitePlanUploaded by remember { mutableStateOf(false) }

    val frontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            ghanaCardFrontUri = uri
            ghanaCardFrontUploaded = true
        }
    }
    val backLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            ghanaCardBackUri = uri
            ghanaCardBackUploaded = true
        }
    }
    val ecLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            energyCommissionUri = uri
            energyCommissionUploaded = true
        }
    }
    val siteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            sitePlanUri = uri
            sitePlanUploaded = true
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    var formError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("meter_request_screen")
    ) {
        // Module Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                        imageVector = Icons.Default.ElectricMeter,
                        contentDescription = null,
                        tint = EcgOnYellow,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "ECG Meter Portal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EcgWhite
                )
            }
        }

        // Meter Request Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
                // Name
                Text(
                    text = "APPLICANT FULL NAME *",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EcgYellow
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = { Text("As indicated on Ghana Card", color = EcgTextMuted) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EcgYellow) },
                    modifier = Modifier.fillMaxWidth().testTag("meter_fullname_input"),
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

                // Phone
                Text(
                    text = "PHONE NUMBER *",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EcgYellow
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = { Text("+233 24 000 0000", color = EcgTextMuted) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = EcgYellow) },
                    modifier = Modifier.fillMaxWidth().testTag("meter_phone_input"),
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

                // GPS Address
                Text(
                    text = "GHANA POST GPS ADDRESS *",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EcgYellow
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = gpsAddress,
                    onValueChange = { gpsAddress = it },
                    placeholder = { Text("e.g. GA-183-9022", color = EcgTextMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("meter_gps_input"),
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

                // Location & Region
                Text(
                    text = "LOCATION / STREET / COMMUNITY *",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EcgYellow
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("e.g. Spintex Road, near Baatsona Total", color = EcgTextMuted) },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = EcgYellow) },
                    modifier = Modifier.fillMaxWidth().testTag("meter_location_input"),
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

                // Category Dropdown (Domestic, Residential, Non Residential)
                Text(
                    text = "METER CATEGORY *",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EcgYellow
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryDropdownExpanded = true }
                            .testTag("meter_category_dropdown"),
                        trailingIcon = {
                            IconButton(onClick = { categoryDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = EcgYellow)
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
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                        modifier = Modifier
                            .background(EcgDarkSurface)
                            .border(1.dp, EcgCardBorder, RoundedCornerShape(8.dp))
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = EcgWhite) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Document Upload Sections (Mandatory per specification)
                Text(
                    text = "REQUIRED VERIFICATION DOCUMENTS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EcgYellow
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 1. Ghana Card Front
                UploadCard(
                    title = "Ghana Card (Front Page)",
                    subtitle = "Tap to upload front photo from phone gallery",
                    isUploaded = ghanaCardFrontUploaded,
                    selectedUri = ghanaCardFrontUri,
                    onUploadClick = {
                        try {
                            frontLauncher.launch("image/*")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open phone gallery: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    tag = "upload_ghana_card_front"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Ghana Card Back
                UploadCard(
                    title = "Ghana Card (Back Page)",
                    subtitle = "Tap to upload back photo from phone gallery",
                    isUploaded = ghanaCardBackUploaded,
                    selectedUri = ghanaCardBackUri,
                    onUploadClick = {
                        try {
                            backLauncher.launch("image/*")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open phone gallery: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    tag = "upload_ghana_card_back"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Energy Commission Form
                UploadCard(
                    title = "Energy Commission Certified Form",
                    subtitle = "Wiring installation completion certificate",
                    isUploaded = energyCommissionUploaded,
                    selectedUri = energyCommissionUri,
                    onUploadClick = {
                        try {
                            ecLauncher.launch("*/*")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open file picker: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    tag = "upload_energy_commission"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Photo of Site Plan
                UploadCard(
                    title = "Photo of Site Plan",
                    subtitle = "Tap to upload site plan photo from phone gallery",
                    isUploaded = sitePlanUploaded,
                    selectedUri = sitePlanUri,
                    onUploadClick = {
                        try {
                            siteLauncher.launch("image/*")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open phone gallery: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    tag = "upload_site_plan"
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Quick helper for emulator demo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            ghanaCardFrontUri = Uri.parse("android.resource://${context.packageName}/${R.drawable.ghana_card_sample_front}")
                            ghanaCardFrontUploaded = true
                            ghanaCardBackUri = Uri.parse("android.resource://${context.packageName}/${R.drawable.ghana_card_sample_back}")
                            ghanaCardBackUploaded = true
                            Toast.makeText(context, "Sample Ghana Card attached for preview", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(
                            text = "Auto-fill Sample Card (Demo)",
                            style = MaterialTheme.typography.labelSmall,
                            color = EcgTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (formError != null) {
                    Text(
                        text = formError ?: "",
                        color = Color(0xFFFF5252),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Submit Button
                Button(
                    onClick = {
                        if (fullName.isBlank()) {
                            formError = "Please enter applicant full name."
                            return@Button
                        }
                        if (phoneNumber.isBlank()) {
                            formError = "Please enter contact phone number."
                            return@Button
                        }
                        if (location.isBlank()) {
                            formError = "Please provide the installation location."
                            return@Button
                        }
                        formError = null

                        onSubmitRequest(
                            fullName,
                            phoneNumber,
                            gpsAddress.ifBlank { "GA-000-0000" },
                            location,
                            selectedRegion,
                            selectedCategory,
                            ghanaCardFrontUri?.toString() ?: if (ghanaCardFrontUploaded) "uploaded://ghana_front.jpg" else null,
                            ghanaCardBackUri?.toString() ?: if (ghanaCardBackUploaded) "uploaded://ghana_back.jpg" else null,
                            energyCommissionUri?.toString() ?: if (energyCommissionUploaded) "uploaded://energy_cert.pdf" else null,
                            sitePlanUri?.toString() ?: if (sitePlanUploaded) "uploaded://site_plan.jpg" else null
                        )

                        // Clear form
                        fullName = ""
                        phoneNumber = ""
                        gpsAddress = ""
                        location = ""
                        ghanaCardFrontUri = null
                        ghanaCardBackUri = null
                        energyCommissionUri = null
                        sitePlanUri = null
                        ghanaCardFrontUploaded = false
                        ghanaCardBackUploaded = false
                        energyCommissionUploaded = false
                        sitePlanUploaded = false
                        formError = null
                        android.widget.Toast.makeText(context, "Meter application submitted successfully!", android.widget.Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcgYellow,
                        contentColor = EcgOnYellow
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_meter_request_button")
                ) {
                    Icon(Icons.Default.PostAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Meter Application", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

@Composable
fun UploadCard(
    title: String,
    subtitle: String,
    isUploaded: Boolean,
    selectedUri: Uri? = null,
    onUploadClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onUploadClick)
            .testTag(tag),
        colors = CardDefaults.cardColors(
            containerColor = if (isUploaded) Color(0xFF1D281D) else EcgDarkSurfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUploaded) EcgStatusResolved else EcgCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedUri != null) {
                    AsyncImage(
                        model = selectedUri,
                        contentDescription = title,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, EcgStatusResolved, RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isUploaded) EcgStatusResolved.copy(alpha = 0.2f) else Color(0xFF333333),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isUploaded) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = if (isUploaded) EcgStatusResolved else EcgYellow,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = EcgWhite
                    )
                    Text(
                        text = if (isUploaded) "Photo uploaded from phone gallery ✓" else subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isUploaded) EcgStatusResolved else EcgTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onUploadClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUploaded) Color(0xFF263A26) else EcgYellow,
                    contentColor = if (isUploaded) EcgStatusResolved else EcgOnYellow
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (isUploaded) Icons.Default.Check else Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isUploaded) "Change" else "Upload",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
