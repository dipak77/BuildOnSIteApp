package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Project
import com.example.data.Worker
import com.example.ui.theme.*

@Composable
fun QuickAddDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    darkTheme: Boolean,
    onAddTransaction: () -> Unit,
    onAddTask: () -> Unit,
    onAddWorker: () -> Unit
) {
    GlassModalDialog(
        visible = visible,
        onDismiss = onDismiss,
        title = "Operations Quick Actions",
        darkTheme = darkTheme,
        glowColor = NeonCyan
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                "Register a quick site ledger transaction or assignment slot.",
                color = if (darkTheme) TextSecondary else TextSecondaryLight,
                fontSize = 12.sp
            )
            GlassButton(onClick = { onDismiss(); onAddTransaction() }, darkTheme = darkTheme, glowColor = NeonCyan, modifier = Modifier.fillMaxWidth()) {
                Text("Register Cash Transaction", fontWeight = FontWeight.Bold)
            }
            GlassButton(onClick = { onDismiss(); onAddTask() }, darkTheme = darkTheme, glowColor = NeonPurple, modifier = Modifier.fillMaxWidth()) {
                Text("Assign Crew Task", fontWeight = FontWeight.Bold)
            }
            GlassButton(onClick = { onDismiss(); onAddWorker() }, darkTheme = darkTheme, glowColor = NeonGreen, modifier = Modifier.fillMaxWidth()) {
                Text("Create Worker Profile", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProjectFormDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    darkTheme: Boolean,
    onSave: (name: String, location: String, budget: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var budgetStr by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var budgetError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(visible) {
        if (visible) {
            name = ""
            location = ""
            budgetStr = ""
            nameError = null
            locationError = null
            budgetError = null
        }
    }

    GlassModalDialog(
        visible = visible,
        onDismiss = onDismiss,
        title = "Initialize Construction Site Project",
        darkTheme = darkTheme,
        glowColor = NeonPurple
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                GlassTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = FormValidator.validateProjectName(it).errorMessage
                    },
                    label = "Site / Project Class Name",
                    placeholder = "Skyline Corporate Tower",
                    darkTheme = darkTheme,
                    focusedStroke = if (nameError != null) Color.Red else NeonPurple
                )
                if (nameError != null) {
                    Text(nameError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                }
            }

            Column {
                GlassTextField(
                    value = location,
                    onValueChange = {
                        location = it
                        locationError = FormValidator.validateLocation(it).errorMessage
                    },
                    label = "Geological Location Block",
                    placeholder = "Sector 62, City Center",
                    darkTheme = darkTheme,
                    focusedStroke = if (locationError != null) Color.Red else NeonPurple
                )
                if (locationError != null) {
                    Text(locationError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                }
            }

            Column {
                GlassTextField(
                    value = budgetStr,
                    onValueChange = {
                        budgetStr = it
                        budgetError = FormValidator.validateBudget(it).errorMessage
                    },
                    label = "Fiscal Budget (₹)",
                    isNumeric = true,
                    placeholder = "1250000.0",
                    darkTheme = darkTheme,
                    focusedStroke = if (budgetError != null) Color.Red else NeonPurple
                )
                if (budgetError != null) {
                    Text(budgetError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                }
            }

            val isValid = name.isNotBlank() && location.isNotBlank() && budgetStr.isNotBlank() &&
                    nameError == null && locationError == null && budgetError == null

            GlassButton(
                onClick = {
                    val budget = budgetStr.toDoubleOrNull() ?: 0.0
                    onSave(name, location, budget)
                    onDismiss()
                },
                enabled = isValid,
                glowColor = NeonPurple,
                darkTheme = darkTheme,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("LAUNCH SITE OPERATIONS", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TransactionFormDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    darkTheme: Boolean,
    presetType: String,
    allWorkers: List<Worker>,
    onCreateNewParty: () -> Unit,
    onSave: (type: String, amount: Double, category: String, description: String, party: Worker?, reference: String, paymentMethod: String, date: String) -> Unit
) {
    var type by remember { mutableStateOf("Money Out") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Material") }
    var description by remember { mutableStateOf("") }
    var selectedParty by remember { mutableStateOf<Worker?>(null) }
    var reference by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var partySearchQuery by remember { mutableStateOf("") }
    var isSearchingParty by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf("") }

    var amountError by remember { mutableStateOf<String?>(null) }
    var descError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(visible) {
        if (visible) {
            type = presetType
            amountStr = ""
            category = "Material"
            description = ""
            selectedParty = null
            reference = ""
            paymentMethod = "Cash"
            partySearchQuery = ""
            isSearchingParty = false
            amountError = null
            descError = null
            date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        }
    }

    GlassModalDialog(
        visible = visible,
        onDismiss = onDismiss,
        title = "Register Cash Ledgers",
        darkTheme = darkTheme,
        glowColor = if (type == "Money In") NeonGreen else NeonPink,
        scrollable = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TogglePill(
                    text = "Cash In",
                    selected = type == "Money In",
                    selectedColor = NeonGreen,
                    darkTheme = darkTheme,
                    modifier = Modifier.weight(1f)
                ) { type = "Money In" }
                TogglePill(
                    text = "Cash Out",
                    selected = type == "Money Out",
                    selectedColor = NeonPink,
                    darkTheme = darkTheme,
                    modifier = Modifier.weight(1f)
                ) { type = "Money Out" }
            }

            Text(
                "Party Name Mapping / Account",
                color = if (darkTheme) TextSecondary else TextSecondaryLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GlassTextField(
                        value = selectedParty?.name ?: partySearchQuery,
                        onValueChange = {
                            if (selectedParty != null) selectedParty = null
                            partySearchQuery = it
                            isSearchingParty = true
                        },
                        label = "Search or Select Party",
                        placeholder = "Type to search party...",
                        darkTheme = darkTheme,
                        icon = Icons.Default.Search
                    )
                }
                if (selectedParty != null) {
                    IconButton(onClick = { selectedParty = null; partySearchQuery = "" }) {
                        Icon(Icons.Default.Clear, "Clear mapping", tint = NeonPink)
                    }
                }
            }

            if (isSearchingParty || (partySearchQuery.isNotEmpty() && selectedParty == null)) {
                val matched = allWorkers.filter {
                    it.name.contains(partySearchQuery, ignoreCase = true) || it.partyType.contains(partySearchQuery, ignoreCase = true)
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (darkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        if (matched.isEmpty()) {
                            Text(
                                "No matching parties found.",
                                color = if (darkTheme) TextSecondary else TextSecondaryLight,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            matched.take(5).forEach { party ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedParty = party
                                            partySearchQuery = party.name
                                            isSearchingParty = false
                                        }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(party.name, color = if (darkTheme) TextPrimary else TextPrimaryLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            party.partyType + (if (party.phone.isNotEmpty()) " • ${party.phone}" else ""),
                                            color = if (darkTheme) TextSecondary else TextSecondaryLight,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text("Select", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = if (darkTheme) Color(0x33FFFFFF) else Color(0x33000000))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isSearchingParty = false
                                    onDismiss()
                                    onCreateNewParty()
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Create New Party", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (selectedParty != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (darkTheme) Color(0x3310B981) else Color(0x2210B981))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Mapped to: ${selectedParty?.name} [${selectedParty?.partyType}]",
                        color = if (darkTheme) NeonGreen else Color(0xFF047857),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                GlassTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        amountError = FormValidator.validateAmount(it).errorMessage
                    },
                    label = "Transaction Amount (₹)",
                    isNumeric = true,
                    placeholder = "45000.0",
                    darkTheme = darkTheme,
                    focusedStroke = if (amountError != null) Color.Red else (if (type == "Money In") NeonGreen else NeonPink)
                )
                if (amountError != null) {
                    Text(amountError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                }
            }

            Column {
                GlassDatePickerField(
                    value = date,
                    onValueChange = { date = it },
                    label = "Transaction Date",
                    darkTheme = darkTheme,
                    focusedStroke = if (type == "Money In") NeonGreen else NeonPink
                )
            }

            Text("Payment Method", color = if (darkTheme) TextSecondary else TextSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Cash", "Bank Transfer", "Cheque").forEach { method ->
                    TogglePill(method, paymentMethod == method, NeonGreen, darkTheme, Modifier.weight(1f)) { paymentMethod = method }
                }
            }

            GlassTextField(value = reference, onValueChange = { reference = it }, label = "Reference No. / Cheque / TxRef", placeholder = "REF-987293", darkTheme = darkTheme)

            Text("Add Cost Code / Segment", color = if (darkTheme) TextSecondary else TextSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Material", "Labor", "Equipment", "Other").forEach { cat ->
                    TogglePill(cat, category == cat, NeonPurple, darkTheme, Modifier.weight(1f)) { category = cat }
                }
            }

            Column {
                GlassTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        descError = FormValidator.validateDescription(it).errorMessage
                    },
                    label = "Brief Expenditure Memo / More Details",
                    placeholder = "Weekly worker payout session",
                    darkTheme = darkTheme,
                    focusedStroke = if (descError != null) Color.Red else NeonPurple
                )
                if (descError != null) {
                    Text(descError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                }
            }

            val isValid = amountStr.isNotBlank() && description.isNotBlank() && amountError == null && descError == null

            GlassButton(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    onSave(type, amt, category, description, selectedParty, reference, paymentMethod, date)
                    onDismiss()
                },
                enabled = isValid,
                glowColor = if (type == "Money In") NeonGreen else NeonPink,
                darkTheme = darkTheme,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("PROCESS TRANSACTION RECORD", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TaskFormDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    darkTheme: Boolean,
    onSave: (title: String, priority: String, assignee: String, dueDate: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var assignee by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(visible) {
        if (visible) {
            title = ""
            priority = "Medium"
            assignee = ""
            dueDate = ""
            titleError = null
            dateError = null
        }
    }

    GlassModalDialog(
        visible = visible,
        onDismiss = onDismiss,
        title = "Deploy Site Task Assignment",
        darkTheme = darkTheme,
        glowColor = NeonCyan
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                GlassTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = FormValidator.validateTaskTitle(it).errorMessage
                    },
                    label = "Task Description Title",
                    placeholder = "Conduct structural welding integration",
                    darkTheme = darkTheme,
                    focusedStroke = if (titleError != null) Color.Red else NeonCyan
                )
                if (titleError != null) {
                    Text(titleError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                }
            }

            GlassTextField(value = assignee, onValueChange = { assignee = it }, label = "Select/Type Assignee Name", placeholder = "Supervisor / Crew", darkTheme = darkTheme)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("High", "Medium", "Low").forEach { p ->
                    TogglePill(p, priority == p, NeonCyan, darkTheme, Modifier.weight(1f)) { priority = p }
                }
            }

            Column {
                GlassTextField(
                    value = dueDate,
                    onValueChange = {
                        dueDate = it
                        dateError = FormValidator.validateDate(it).errorMessage
                    },
                    label = "Task Due Date Deadline (YYYY-MM-DD)",
                    placeholder = "2026-05-30",
                    darkTheme = darkTheme,
                    focusedStroke = if (dateError != null) Color.Red else NeonCyan
                )
                if (dateError != null) {
                    Text(dateError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                }
            }

            val isValid = title.isNotBlank() && dueDate.isNotBlank() && titleError == null && dateError == null

            GlassButton(
                onClick = {
                    onSave(title, priority, if (assignee.isBlank()) "Crew" else assignee, dueDate)
                    onDismiss()
                },
                enabled = isValid,
                glowColor = NeonCyan,
                darkTheme = darkTheme,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("DELEGATE SITE TASK", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WorkerFormDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    darkTheme: Boolean,
    workerCount: Int,
    onSave: (
        name: String, role: String, shift: String, wageRate: Double, phone: String, email: String,
        partyType: String, address: String, partyId: String, dateOfJoining: String,
        aadhaar: String, pan: String, reference: String
    ) -> Unit
) {
    var partyId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var partyType by remember { mutableStateOf("Worker") }
    var address by remember { mutableStateOf("") }
    var dateOfJoining by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var pan by remember { mutableStateOf("") }
    var referenceStr by remember { mutableStateOf("") }
    var wageStr by remember { mutableStateOf("") }
    var shift by remember { mutableStateOf("Day") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var aadhaarError by remember { mutableStateOf<String?>(null) }
    var panError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var wageError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(visible) {
        if (visible) {
            partyId = "PID-${workerCount + 1}"
            name = ""
            phone = ""
            email = ""
            partyType = "Worker"
            address = ""
            dateOfJoining = "2026-05-30"
            role = ""
            aadhaar = ""
            pan = ""
            referenceStr = ""
            wageStr = ""
            shift = "Day"

            nameError = null
            phoneError = null
            emailError = null
            aadhaarError = null
            panError = null
            dateError = null
            wageError = null
        }
    }

    GlassModalDialog(
        visible = visible,
        onDismiss = onDismiss,
        title = "Add New Party / Worker Profile",
        darkTheme = darkTheme,
        glowColor = NeonGreen,
        scrollable = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassTextField(value = partyId, onValueChange = { partyId = it }, label = "Party ID", placeholder = "PID-1", darkTheme = darkTheme)

            Column {
                GlassTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = FormValidator.validateWorkerName(it).errorMessage
                    },
                    label = "Party / Worker Full Name",
                    placeholder = "John Doe / Tejas Contractors",
                    darkTheme = darkTheme,
                    focusedStroke = if (nameError != null) Color.Red else NeonGreen
                )
                if (nameError != null) {
                    Text(nameError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                }
            }

            Column {
                GlassTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = FormValidator.validatePhone(it).errorMessage
                    },
                    label = "Phone Number",
                    placeholder = "9876543210",
                    darkTheme = darkTheme,
                    focusedStroke = if (phoneError != null) Color.Red else NeonGreen
                )
                if (phoneError != null) {
                    Text(phoneError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                }
            }

            Column {
                GlassTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = FormValidator.validateEmail(it).errorMessage
                    },
                    label = "Email Address",
                    placeholder = "client@example.com",
                    darkTheme = darkTheme,
                    focusedStroke = if (emailError != null) Color.Red else NeonGreen
                )
                if (emailError != null) {
                    Text(emailError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                }
            }

            Text("Party Type Category", color = if (darkTheme) TextSecondary else TextSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Client", "Staff", "Vendor", "Worker", "Investor").forEach { type ->
                    TogglePill(type, partyType == type, NeonGreen, darkTheme, Modifier.weight(1f), fontSize = 10.sp) { partyType = type }
                }
            }

            GlassTextField(value = address, onValueChange = { address = it }, label = "Address / Location", placeholder = "Enter home or office address", darkTheme = darkTheme)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    Column {
                        GlassTextField(
                            value = dateOfJoining,
                            onValueChange = {
                                dateOfJoining = it
                                dateError = FormValidator.validateDate(it).errorMessage
                            },
                            label = "Date of Joining",
                            placeholder = "2026-05-30",
                            darkTheme = darkTheme,
                            focusedStroke = if (dateError != null) Color.Red else NeonGreen
                        )
                        if (dateError != null) {
                            Text(dateError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    GlassTextField(value = role, onValueChange = { role = it }, label = "Designation / Role", placeholder = "Foreman", darkTheme = darkTheme)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    Column {
                        GlassTextField(
                            value = aadhaar,
                            onValueChange = {
                                aadhaar = it
                                aadhaarError = FormValidator.validateAadhaar(it).errorMessage
                            },
                            label = "Aadhaar Card No.",
                            placeholder = "12-digit",
                            darkTheme = darkTheme,
                            focusedStroke = if (aadhaarError != null) Color.Red else NeonGreen
                        )
                        if (aadhaarError != null) {
                            Text(aadhaarError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    Column {
                        GlassTextField(
                            value = pan,
                            onValueChange = {
                                pan = it.uppercase()
                                panError = FormValidator.validatePan(it.uppercase()).errorMessage
                            },
                            label = "PAN Card No.",
                            placeholder = "ABCDE1234F",
                            darkTheme = darkTheme,
                            focusedStroke = if (panError != null) Color.Red else NeonGreen
                        )
                        if (panError != null) {
                            Text(panError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                        }
                    }
                }
            }

            GlassTextField(value = referenceStr, onValueChange = { referenceStr = it }, label = "Referred By / Given Reference", placeholder = "Partner X", darkTheme = darkTheme)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1.2f)) {
                    Column {
                        GlassTextField(
                            value = wageStr,
                            onValueChange = {
                                wageStr = it
                                wageError = FormValidator.validateBudget(it).errorMessage // standard non-negative double validation
                            },
                            label = "Daily Wage / Rate (₹)",
                            isNumeric = true,
                            placeholder = "350.0",
                            darkTheme = darkTheme,
                            focusedStroke = if (wageError != null) Color.Red else NeonGreen
                        )
                        if (wageError != null) {
                            Text(wageError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                        }
                    }
                }
                Column(modifier = Modifier.weight(0.8f)) {
                    Text(
                        "Standard Shift",
                        color = if (darkTheme) TextSecondary else TextSecondaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Day", "Night").forEach { sh ->
                            TogglePill(sh, shift == sh, NeonGreen, darkTheme, Modifier.weight(1f), fontSize = 11.sp) { shift = sh }
                        }
                    }
                }
            }

            val isValid = name.isNotBlank() && nameError == null && phoneError == null &&
                    emailError == null && aadhaarError == null && panError == null &&
                    dateError == null && wageError == null

            GlassButton(
                onClick = {
                    val rate = wageStr.toDoubleOrNull() ?: 0.0
                    onSave(
                        name, if (role.isNotBlank()) role else partyType, shift, rate, phone, email,
                        partyType, address, partyId, dateOfJoining, aadhaar, pan, referenceStr
                    )
                    onDismiss()
                },
                enabled = isValid,
                glowColor = NeonGreen,
                darkTheme = darkTheme,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("PROCESS PARTY PROFILE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TogglePill(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    onClick: () -> Unit
) {
    val resolvedColor = if (!darkTheme) {
        when (selectedColor) {
            NeonCyan -> Color(0xFF0284C7)
            NeonPurple -> Color(0xFF6D28D9)
            NeonGreen -> Color(0xFF047857)
            NeonPink -> Color(0xFFBE123C)
            else -> selectedColor
        }
    } else selectedColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) resolvedColor.copy(alpha = 0.20f) else Color.Transparent)
            .border(
                1.dp,
                if (selected) resolvedColor else (if (darkTheme) GlassBorderLight.copy(alpha = 0.20f) else GlassBorderLight),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) resolvedColor else (if (darkTheme) TextSecondary else TextSecondaryLight),
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
