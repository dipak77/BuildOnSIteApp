package com.aistudio.constructpro.kgrmqd.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.constructpro.kgrmqd.data.Project
import com.aistudio.constructpro.kgrmqd.data.Worker
import com.aistudio.constructpro.kgrmqd.data.Transaction
import com.aistudio.constructpro.kgrmqd.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.absoluteValue

// ─────────────────────────────────────────────
// PREMIUM THEME COMPATIBILITY GETTERS
// ─────────────────────────────────────────────
private val AquaGlow @Composable get() = MaterialTheme.colorScheme.ext.accentPrimary
private val VioletGlow @Composable get() = MaterialTheme.colorScheme.ext.accentSecondary
private val EmeraldGlow @Composable get() = MaterialTheme.colorScheme.ext.accentSuccess
private val RoseGlow @Composable get() = MaterialTheme.colorScheme.ext.accentDanger

private val GradientAqua @Composable get() = Brush.linearGradient(
    if (MaterialTheme.colorScheme.ext.isDark) listOf(NeonCyan, NeonCyanDim)
    else listOf(LightCyan, Color(0xFF0284C7))
)


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
    onSave: (type: String, amount: Double, category: String, description: String, party: Worker?, reference: String, paymentMethod: String, date: String) -> Unit,
    transactionToEdit: Transaction? = null,
    viewModel: MainViewModel
) {
    var type by remember { mutableStateOf("Money Out") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Material") }
    var description by remember { mutableStateOf("") }
    var selectedParty by remember { mutableStateOf<Worker?>(null) }
    var reference by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var date by remember { mutableStateOf("") }

    LaunchedEffect(visible) {
        if (visible) {
            if (transactionToEdit != null) {
                type = transactionToEdit.type
                amountStr = if (transactionToEdit.amount == 0.0) "" else transactionToEdit.amount.toString()
                category = transactionToEdit.category
                description = transactionToEdit.description
                selectedParty = allWorkers.find { it.id == transactionToEdit.partyId || it.name == transactionToEdit.partyName }
                reference = transactionToEdit.reference
                paymentMethod = transactionToEdit.paymentMethod
                date = transactionToEdit.date
            } else {
                type = presetType
                amountStr = ""
                category = "Material"
                description = ""
                selectedParty = null
                reference = ""
                paymentMethod = "Cash"
                date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            }
        }
    }

    GlassModalDialog(
        visible = visible,
        onDismiss = onDismiss,
        title = if (transactionToEdit != null) "Update Cash Ledger" else "Register Cash Ledgers",
        darkTheme = darkTheme,
        glowColor = if (type == "Money In") NeonGreen else NeonPink,
        scrollable = true
    ) {
        UnifiedTransactionFormContent(
            dark = darkTheme,
            type = type,
            onTypeChange = { type = it },
            allWorkers = allWorkers,
            selectedParty = selectedParty,
            onPartySelected = { selectedParty = it },
            amountStr = amountStr,
            onAmountChange = { amountStr = it },
            category = category,
            onCategoryChange = { category = it },
            description = description,
            onDescriptionChange = { description = it },
            reference = reference,
            onReferenceChange = { reference = it },
            paymentMethod = paymentMethod,
            onPaymentMethodChange = { paymentMethod = it },
            date = date,
            onDateChange = { date = it },
            onSave = {
                val amt = amountStr.toDoubleOrNull() ?: 0.0
                onSave(type, amt, category, description, selectedParty, reference, paymentMethod, date)
                onDismiss()
            },
            onCancel = onDismiss,
            isPartyLocked = false,
            viewModel = viewModel
        )
    }
}

@Composable
fun UnifiedTransactionFormContent(
    dark: Boolean,
    type: String,
    onTypeChange: (String) -> Unit,
    allWorkers: List<Worker>,
    selectedParty: Worker?,
    onPartySelected: (Worker?) -> Unit,
    amountStr: String,
    onAmountChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    reference: String,
    onReferenceChange: (String) -> Unit,
    paymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    date: String,
    onDateChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isPartyLocked: Boolean = false,
    viewModel: MainViewModel
) {
    var partySearchQuery by remember { mutableStateOf("") }
    var isSearchingParty by remember { mutableStateOf(false) }
    var showInlinePartyForm by remember { mutableStateOf(false) }

    var amountError by remember { mutableStateOf<String?>(null) }
    var descError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedParty) {
        if (selectedParty != null) {
            partySearchQuery = selectedParty.name
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TogglePill(
                text = "Cash In",
                selected = type == "Money In",
                selectedColor = NeonGreen,
                darkTheme = dark,
                modifier = Modifier.weight(1f)
            ) { onTypeChange("Money In") }
            TogglePill(
                text = "Cash Out",
                selected = type == "Money Out",
                selectedColor = NeonPink,
                darkTheme = dark,
                modifier = Modifier.weight(1f)
            ) { onTypeChange("Money Out") }
        }

        Text(
            "Party Name Mapping / Account",
            color = if (dark) TextSecondary else TextSecondaryLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        if (isPartyLocked) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (dark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                    .border(1.dp, if (dark) GlassBorderDark else GlassBorderLight, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Person, null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Text(
                        selectedParty?.name ?: partySearchQuery.ifBlank { "No mapped party" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) TextPrimary else TextPrimaryLight
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GlassTextField(
                        value = selectedParty?.name ?: partySearchQuery,
                        onValueChange = {
                            if (selectedParty != null) onPartySelected(null)
                            partySearchQuery = it
                            isSearchingParty = true
                        },
                        label = "Search or Select Party",
                        placeholder = "Type to search party...",
                        darkTheme = dark,
                        icon = Icons.Default.Search
                    )
                }
                if (selectedParty != null || partySearchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        onPartySelected(null)
                        partySearchQuery = ""
                        isSearchingParty = false
                    }) {
                        Icon(Icons.Default.Clear, "Clear mapping", tint = NeonPink)
                    }
                }
            }

            if (showInlinePartyForm) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (dark) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Create New Party Inline", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        var inlineName by remember { mutableStateOf(partySearchQuery) }
                        var inlinePhone by remember { mutableStateOf("") }
                        var inlineWageStr by remember { mutableStateOf("") }
                        var inlineType by remember { mutableStateOf("Worker") }

                        GlassTextField(value = inlineName, onValueChange = { inlineName = it }, label = "Full Name", placeholder = "e.g. Tejas Contractors", darkTheme = dark)
                        GlassTextField(value = inlinePhone, onValueChange = { inlinePhone = it }, label = "Phone Number", placeholder = "e.g. 9876543210", darkTheme = dark)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                GlassTextField(value = inlineWageStr, onValueChange = { inlineWageStr = it }, label = "Daily Wage / Rate (₹)", isNumeric = true, placeholder = "e.g. 350.0", darkTheme = dark)
                            }
                        }

                        Text("Party Type", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Client", "Staff", "Vendor", "Worker", "Investor").forEach { typeOption ->
                                TogglePill(typeOption, inlineType == typeOption, NeonCyan, dark, Modifier.weight(1f), fontSize = 9.sp) { inlineType = typeOption }
                            }
                        }

                        val isInlineValid = inlineName.isNotBlank()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassButton(
                                onClick = { showInlinePartyForm = false },
                                darkTheme = dark,
                                outlineMode = true,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            GlassButton(
                                onClick = {
                                    val rate = inlineWageStr.toDoubleOrNull() ?: 0.0
                                    viewModel.addWorkerInline(
                                        name = inlineName,
                                        role = inlineType,
                                        shift = "Day",
                                        wageRate = rate,
                                        color = 0xFF10B981.toInt(),
                                        phone = inlinePhone,
                                        partyType = inlineType,
                                        partyId = "PID-${System.currentTimeMillis() % 100000}",
                                        dateOfJoining = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()),
                                        onSuccess = { newWorker ->
                                            onPartySelected(newWorker)
                                            showInlinePartyForm = false
                                            isSearchingParty = false
                                        }
                                    )
                                },
                                enabled = isInlineValid,
                                darkTheme = dark,
                                glowColor = NeonCyan,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save & Select", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            } else if (isSearchingParty || (partySearchQuery.isNotEmpty() && selectedParty == null)) {
                val matched = allWorkers.filter {
                    it.name.contains(partySearchQuery, ignoreCase = true) || it.partyType.contains(partySearchQuery, ignoreCase = true)
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (dark) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        if (matched.isEmpty()) {
                            Text(
                                "No matching parties found.",
                                color = if (dark) TextSecondary else TextSecondaryLight,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            matched.take(5).forEach { party ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onPartySelected(party)
                                            isSearchingParty = false
                                        }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(party.name, color = if (dark) TextPrimary else TextPrimaryLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            party.partyType + (if (party.phone.isNotEmpty()) " • ${party.phone}" else ""),
                                            color = if (dark) TextSecondary else TextSecondaryLight,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text("Select", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = if (dark) Color(0x33FFFFFF) else Color(0x33000000))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showInlinePartyForm = true
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Create New Party Inline", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (selectedParty != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (dark) Color(0x3310B981) else Color(0x2210B981))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Mapped to: ${selectedParty.name} [${selectedParty.partyType}]",
                        color = if (dark) NeonGreen else Color(0xFF047857),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column {
            GlassTextField(
                value = amountStr,
                onValueChange = {
                    onAmountChange(it)
                    amountError = FormValidator.validateAmount(it).errorMessage
                },
                label = "Transaction Amount (₹)",
                isNumeric = true,
                placeholder = "45000.0",
                darkTheme = dark,
                focusedStroke = if (amountError != null) Color.Red else (if (type == "Money In") NeonGreen else NeonPink)
            )
            if (amountError != null) {
                Text(amountError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
            }
        }

        Column {
            GlassDatePickerField(
                value = date,
                onValueChange = onDateChange,
                label = "Transaction Date",
                darkTheme = dark,
                focusedStroke = if (type == "Money In") NeonGreen else NeonPink
            )
        }

        Text("Payment Method", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PAYMENT_METHODS.forEach { method ->
                val sel = paymentMethod == method
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sel) NeonGreen.copy(alpha = 0.20f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (sel) NeonGreen else (if (dark) GlassBorderLight.copy(alpha = 0.20f) else GlassBorderLight),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onPaymentMethodChange(method) }
                        .padding(horizontal = 6.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = method,
                        color = if (sel) (if (dark) NeonGreen else Color(0xFF047857)) else (if (dark) TextSecondary else TextSecondaryLight),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        GlassTextField(
            value = reference,
            onValueChange = onReferenceChange,
            label = "Reference No. / Cheque / TxRef",
            placeholder = "REF-987293",
            darkTheme = dark
        )

        Text("Add Cost Code / Segment", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            COST_CODES.forEach { cat ->
                val sel = category == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sel) NeonPurple.copy(alpha = 0.20f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (sel) NeonPurple else (if (dark) GlassBorderLight.copy(alpha = 0.20f) else GlassBorderLight),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onCategoryChange(cat) }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat,
                        color = if (sel) (if (dark) NeonPurple else Color(0xFF6D28D9)) else (if (dark) TextSecondary else TextSecondaryLight),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Column {
            GlassTextField(
                value = description,
                onValueChange = {
                    onDescriptionChange(it)
                    descError = FormValidator.validateDescription(it).errorMessage
                },
                label = "Brief Expenditure Memo / More Details",
                placeholder = "Weekly worker payout session",
                darkTheme = dark,
                focusedStroke = if (descError != null) Color.Red else NeonPurple
            )
            if (descError != null) {
                Text(descError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
            }
        }

        val isValid = amountStr.isNotBlank() && description.isNotBlank() && amountError == null && descError == null

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, NeonPink.copy(0.5f), RoundedCornerShape(12.dp))
                    .background(NeonPink.copy(alpha = 0.08f))
                    .clickable(onClick = onCancel)
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("CANCEL", color = NeonPink, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isValid) (if (type == "Money In") NeonGreen else NeonPink) else Color.Gray.copy(alpha = 0.5f))
                    .then(if (isValid) Modifier.clickable(onClick = onSave) else Modifier)
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SAVE RECORD",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
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

@Composable
fun PremiumReportPreviewDialog(
    dark: Boolean,
    selectedTxDetail: Transaction?,
    selectedPartyDetail: Worker?,
    projectTransactions: List<Transaction>,
    allWorkers: List<Worker>,
    currentProject: Project?,
    viewModel: MainViewModel,
    dateRangeText: String = "All Time",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var reportType by remember {
        mutableStateOf(
            if (selectedTxDetail != null) "Receipt"
            else if (selectedPartyDetail != null) "Party Ledger"
            else "Party Balance"
        )
    }

    val projectName = currentProject?.name ?: "Treasure Garden"
    val siteAddress = currentProject?.location ?: "Treasure Garden Road Site, India"
    val generatedBy = viewModel.userSession.value?.displayName ?: "Tejas Harane"

    val textNavy = Color(0xFF0F172A)
    val textGray = Color(0xFF64748B)
    val dividerColor = Color(0xFFE2E8F0)

    GlassModalDialog(
        visible = true,
        onDismiss = onDismiss,
        title = "", 
        darkTheme = dark,
        glowColor = NeonCyan,
        scrollable = false 
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with custom title/selector and close button
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NeonCyan))
                    
                    var showDropdown by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDropdown = true }
                            .background(if (dark) Color(0xFF1E2D4A) else Color(0xFFE2E8F4))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayText = when (reportType) {
                            "Receipt" -> "Payment Receipt"
                            "Party Ledger" -> "Party Ledger Report"
                            "Party Transactions" -> "Party Transactions Report"
                            "Party Balance" -> "Party Balance Report"
                            "Summary" -> "Payment Summary Report"
                            "Transactions" -> "Payment Transactions Report"
                            else -> "Report"
                        }
                        Text(
                            text = "$displayText ▼",
                            color = if (dark) Color.White else Color(0xFF1E293B),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            if (selectedTxDetail != null) {
                                DropdownMenuItem(
                                    text = { Text("Payment Receipt") },
                                    onClick = { reportType = "Receipt"; showDropdown = false }
                                )
                            }
                            if (selectedPartyDetail != null) {
                                DropdownMenuItem(
                                    text = { Text("Party Ledger Report") },
                                    onClick = { reportType = "Party Ledger"; showDropdown = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Party Transactions Report") },
                                    onClick = { reportType = "Party Transactions"; showDropdown = false }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Party Balance Report") },
                                onClick = { reportType = "Party Balance"; showDropdown = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Payment Summary Report") },
                                onClick = { reportType = "Summary"; showDropdown = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Payment Transactions Report") },
                                onClick = { reportType = "Transactions"; showDropdown = false }
                            )
                        }
                    }
                }
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(if (dark) Color(0x1AFFFFFF) else Color(0x0D000000))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = if (dark) TextSecondary else TextSecondaryLight, modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = "Page 1 of 1",
                fontSize = 11.sp,
                color = if (dark) TextSecondary else TextSecondaryLight,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Scrollable simulated A4 sheet preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (reportType) {
                    "Receipt" -> {
                        val rxAmount = selectedTxDetail?.amount ?: 1000.0
                        val rxName = selectedTxDetail?.partyName ?: selectedPartyDetail?.name ?: "Tejas Harane"
                        val rxDate = selectedTxDetail?.date ?: "2026-05-27"
                        val rxId = selectedTxDetail?.id ?: 1024
                        val rxMethod = selectedTxDetail?.paymentMethod ?: "Cash"
                        val rxRemark = selectedTxDetail?.description ?: ""
                        val rxIsMoneyIn = selectedTxDetail?.type == "Money In"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Header
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(projectName, fontSize = 10.sp, color = textNavy, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(siteAddress.replace("\n", " "), fontSize = 8.sp, color = textGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("GST : N/A", fontSize = 8.sp, color = textGray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Payment Receipt", fontSize = 12.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Payment Date:", fontSize = 8.sp, color = textGray)
                                        Text(rxDate, fontSize = 8.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor))
                            
                            // Project
                            Column {
                                Text("Project:", fontSize = 8.sp, color = textGray)
                                Text(projectName, fontSize = 9.sp, color = textNavy, fontWeight = FontWeight.Bold)
                            }
                            
                            // To
                            Column {
                                Text("To:", fontSize = 8.sp, color = textGray)
                                Text(rxName, fontSize = 9.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                Text("GST: NA", fontSize = 8.sp, color = textGray)
                            }
                            
                            // Subject
                            Column {
                                Text("Subject:", fontSize = 8.sp, color = textGray)
                                Text(if (rxIsMoneyIn) "Payment In Receipt" else "Payment Out Receipt", fontSize = 9.sp, color = textNavy, fontWeight = FontWeight.Bold)
                            }
                            
                            // Body
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Dear Sir/Madam,", fontSize = 9.sp, color = textNavy)
                                Text("We confirm ${if (rxIsMoneyIn) "receipt" else "disbursal"} of below payment on $rxDate.", fontSize = 9.sp, color = textNavy)
                            }
                            
                            // Table
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.5.dp, Color(0xFFCBD5E1))
                            ) {
                                val borderMod = Modifier.border(0.25.dp, Color(0xFFCBD5E1))
                                
                                // Amount Row
                                Row(Modifier.fillMaxWidth().height(24.dp)) {
                                    Box(Modifier.weight(1.2f).fillMaxHeight().then(borderMod).padding(horizontal = 6.dp, vertical = 4.dp)) {
                                        Text("Amount", fontSize = 8.sp, color = Color(0xFF475569))
                                    }
                                    Box(Modifier.weight(2.8f).fillMaxHeight().then(borderMod).padding(horizontal = 6.dp, vertical = 4.dp), contentAlignment = Alignment.CenterEnd) {
                                        Text(formatIndianRupees(rxAmount), fontSize = 8.sp, color = if (rxIsMoneyIn) Color(0xFF15803D) else Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                // Payment Date Row
                                Row(Modifier.fillMaxWidth().height(24.dp)) {
                                    Box(Modifier.weight(1.2f).fillMaxHeight().then(borderMod).padding(horizontal = 6.dp, vertical = 4.dp)) {
                                        Text("Payment Date", fontSize = 8.sp, color = Color(0xFF475569))
                                    }
                                    Box(Modifier.weight(2.8f).fillMaxHeight().then(borderMod).padding(horizontal = 6.dp, vertical = 4.dp)) {
                                        Text(rxDate, fontSize = 8.sp, color = textNavy)
                                    }
                                }
                                
                                // Payment Method Row
                                Row(Modifier.fillMaxWidth().height(24.dp)) {
                                    Box(Modifier.weight(1.2f).fillMaxHeight().then(borderMod).padding(horizontal = 6.dp, vertical = 4.dp)) {
                                        Text("Payment Method", fontSize = 8.sp, color = Color(0xFF475569))
                                    }
                                    Box(Modifier.weight(2.8f).fillMaxHeight().then(borderMod).padding(horizontal = 6.dp, vertical = 4.dp)) {
                                        Text(rxMethod, fontSize = 8.sp, color = textNavy)
                                    }
                                }
                                
                                // Remark Row
                                Row(Modifier.fillMaxWidth().height(24.dp)) {
                                    Box(Modifier.weight(1.2f).fillMaxHeight().then(borderMod).padding(horizontal = 6.dp, vertical = 4.dp)) {
                                        Text("Remark", fontSize = 8.sp, color = Color(0xFF475569))
                                    }
                                    Box(Modifier.weight(2.8f).fillMaxHeight().then(borderMod).padding(horizontal = 6.dp, vertical = 4.dp)) {
                                        Text(rxRemark, fontSize = 8.sp, color = textNavy)
                                    }
                                }
                                
                                // Attachment Row
                                Row(Modifier.fillMaxWidth().height(24.dp)) {
                                    Box(Modifier.weight(1.2f).fillMaxHeight().then(borderMod).padding(horizontal = 6.dp, vertical = 4.dp)) {
                                        Text("Attachment", fontSize = 8.sp, color = Color(0xFF475569))
                                    }
                                    Box(Modifier.weight(2.8f).fillMaxHeight().then(borderMod).padding(horizontal = 6.dp, vertical = 4.dp)) {
                                        Text("", fontSize = 8.sp, color = textNavy)
                                    }
                                }
                            }
                            
                            Spacer(Modifier.height(4.dp))
                            Text("Thank you for your services . Please contact us for any clarifications.", fontSize = 7.sp, color = textGray)
                            
                            Spacer(Modifier.height(16.dp))
                            Text("Authorised Signatory", fontSize = 8.sp, color = textNavy, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
                        }
                    }
                    "Party Ledger" -> {
                        val party = selectedPartyDetail ?: allWorkers.firstOrNull()
                        if (party != null) {
                            val matchedTxs = projectTransactions.filter { it.partyId == party.id || it.partyName == party.name }
                            val totalIn = matchedTxs.filter { it.type == "Money In" }.sumOf { it.amount }
                            val totalOut = matchedTxs.filter { it.type == "Money Out" }.sumOf { it.amount }
                            val balance = totalIn - totalOut

                            val isClient = party.partyType == "Client" || party.partyType == "Investor"
                            val payments = if (isClient) totalIn else totalOut
                            val salesExpenses = if (isClient) totalOut else totalIn

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Header
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(projectName, fontSize = 10.sp, color = textNavy, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(siteAddress.replace("\n", " "), fontSize = 8.sp, color = textGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("GST : N/A", fontSize = 8.sp, color = textGray)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Party Ledger Report", fontSize = 12.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                        Text("Generated By: $generatedBy", fontSize = 8.sp, color = textGray)
                                        Text("Date Range: $dateRangeText", fontSize = 8.sp, color = textGray)
                                    }
                                }
                                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor))

                                // Project Box
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Row {
                                        Text("Project: ", fontSize = 8.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                                        Text(projectName, fontSize = 8.sp, color = textNavy)
                                    }
                                    Row {
                                        Text("Site Address: ", fontSize = 8.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                                        Text(siteAddress.replace("\n", " "), fontSize = 8.sp, color = textNavy)
                                    }
                                }

                                // Party details row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(0.5.dp, dividerColor, RoundedCornerShape(4.dp))
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row {
                                        Text("Party Name: ", fontSize = 9.sp, color = textGray)
                                        Text(party.name, fontSize = 9.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                    }
                                    Row {
                                        Text("Type: ", fontSize = 9.sp, color = textGray)
                                        Text(party.partyType, fontSize = 9.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Summary cards row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                            .padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(if (isClient) "RECEIVED" else "PAID TO", fontSize = 7.sp, color = textGray, fontWeight = FontWeight.Bold)
                                        Text(formatIndianRupees(payments), fontSize = 10.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                    }
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                            .padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(if (isClient) "TOTAL BILLING" else "TOTAL WORK", fontSize = 7.sp, color = textGray, fontWeight = FontWeight.Bold)
                                        Text(formatIndianRupees(salesExpenses), fontSize = 10.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    val isReceived = balance >= 0
                                    val balStatus = if (isReceived) "Received" else "Paid"
                                    val balColor = if (isReceived) Color(0xFF15803D) else Color(0xFFB91C1C)
                                    val balBg = if (isReceived) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                    Column(
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .background(balBg, RoundedCornerShape(4.dp))
                                            .padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("NET BALANCE", fontSize = 7.sp, color = balColor, fontWeight = FontWeight.Bold)
                                        Text(formatIndianRupees(kotlin.math.abs(balance)), fontSize = 10.sp, color = balColor, fontWeight = FontWeight.Bold)
                                        Text(balStatus, fontSize = 6.sp, color = balColor)
                                    }
                                }

                                // Transactions Table
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E293B)).padding(4.dp)) {
                                        Text("S.No", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                                        Text("Date", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Text("Type", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Text("Method", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                                        Text("Amount", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                    }
                                    if (matchedTxs.isEmpty()) {
                                        Text("No transactions found.", fontSize = 8.sp, color = textGray, modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally))
                                    } else {
                                        matchedTxs.forEachIndexed { idx, tx ->
                                            val isIn = tx.type == "Money In"
                                            Row(
                                                modifier = Modifier.fillMaxWidth().background(if (idx % 2 == 0) Color(0xFFF8FAFC) else Color.White).padding(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text((idx + 1).toString(), fontSize = 8.sp, color = textNavy, modifier = Modifier.weight(0.5f))
                                                Text(tx.date, fontSize = 8.sp, color = textNavy, modifier = Modifier.weight(1f))
                                                Text(tx.type, fontSize = 8.sp, color = if (isIn) Color(0xFF15803D) else Color(0xFFB91C1C), modifier = Modifier.weight(1f))
                                                Text(tx.paymentMethod, fontSize = 8.sp, color = textNavy, modifier = Modifier.weight(1.2f))
                                                Text(String.format(Locale.US, "%,.2f", tx.amount), fontSize = 8.sp, color = if (isIn) Color(0xFF15803D) else Color(0xFFB91C1C), fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                            }
                                            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "Party Transactions" -> {
                        val party = selectedPartyDetail ?: allWorkers.firstOrNull()
                        if (party != null) {
                            val matchedTxs = projectTransactions.filter { it.partyId == party.id || it.partyName == party.name }
                            val totalIn = matchedTxs.filter { it.type == "Money In" }.sumOf { it.amount }
                            val totalOut = matchedTxs.filter { it.type == "Money Out" }.sumOf { it.amount }
                            val balance = totalIn - totalOut

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Header
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("Company", fontSize = 10.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                        Text("Pune", fontSize = 8.sp, color = textGray)
                                        Text("GST : N/A", fontSize = 8.sp, color = textGray)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Party Transactions", fontSize = 12.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                        Text("Generated By: $generatedBy", fontSize = 8.sp, color = textGray)
                                        Text("Date Range: $dateRangeText", fontSize = 8.sp, color = textGray)
                                    }
                                }
                                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor))

                                // Project Box
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Row {
                                        Text("Project: ", fontSize = 8.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                                        Text(projectName, fontSize = 8.sp, color = textNavy)
                                    }
                                    Row {
                                        Text("Site Address: ", fontSize = 8.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                                        Text(siteAddress.replace("\n", " "), fontSize = 8.sp, color = textNavy)
                                    }
                                }

                                // Party details row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(0.5.dp, dividerColor, RoundedCornerShape(4.dp))
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row {
                                        Text("Party Name: ", fontSize = 9.sp, color = textGray)
                                        Text(party.name, fontSize = 9.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                    }
                                    Row {
                                        Text("Type: ", fontSize = 9.sp, color = textGray)
                                        Text(party.partyType, fontSize = 9.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Stats row
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF1F5F9))
                                        .padding(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Total In: ${String.format(Locale.US, "%,.2f", totalIn)}", fontSize = 8.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                                    Text("Total Out: ${String.format(Locale.US, "%,.2f", totalOut)}", fontSize = 8.sp, color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
                                    Text("Balance: ${String.format(Locale.US, "%,.2f", balance)}", fontSize = 8.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                }

                                // Table
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E293B)).padding(4.dp)) {
                                        Text("Date", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Text("Sender", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                                        Text("Receiver", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                                        Text("Amount", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                        Text("Balance", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                    }
                                    
                                    val sorted = matchedTxs.sortedBy { it.date }
                                    var bal = 0.0
                                    if (sorted.isEmpty()) {
                                        Text("No transactions found.", fontSize = 8.sp, color = textGray, modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally))
                                    } else {
                                        sorted.forEachIndexed { idx, tx ->
                                            val isIn = tx.type == "Money In"
                                            bal += if (isIn) tx.amount else -tx.amount
                                            Row(
                                                modifier = Modifier.fillMaxWidth().background(if (idx % 2 == 0) Color(0xFFF8FAFC) else Color.White).padding(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(tx.date, fontSize = 8.sp, color = textNavy, modifier = Modifier.weight(1f))
                                                
                                                val sender = if (isIn) (tx.partyName ?: "Client") else "Company"
                                                Text(sender, fontSize = 8.sp, color = textNavy, modifier = Modifier.weight(1.2f))
                                                
                                                val receiver = if (!isIn) (tx.partyName ?: "Staff") else "Company"
                                                Text(receiver, fontSize = 8.sp, color = textNavy, modifier = Modifier.weight(1.2f))
                                                
                                                val amtColor = if (isIn) Color(0xFF15803D) else Color(0xFFB91C1C)
                                                Text(String.format(Locale.US, "%,.2f", tx.amount), fontSize = 8.sp, color = amtColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                                
                                                Text(String.format(Locale.US, "%,.2f", bal), fontSize = 8.sp, color = textNavy, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                            }
                                            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "Party Balance" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(projectName, fontSize = 10.sp, color = textNavy, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(siteAddress.replace("\n", " "), fontSize = 8.sp, color = textGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("GST : N/A", fontSize = 8.sp, color = textGray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Party Balance Report", fontSize = 12.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                    Text("Generated By: $generatedBy", fontSize = 8.sp, color = textGray)
                                    Text("Date Range: $dateRangeText", fontSize = 8.sp, color = textGray)
                                }
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor))
                            
                            // Project Box
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Row {
                                    Text("Project: ", fontSize = 8.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                                    Text(projectName, fontSize = 8.sp, color = textNavy)
                                }
                                Row {
                                    Text("Site Address: ", fontSize = 8.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                                    Text(siteAddress.replace("\n", " "), fontSize = 8.sp, color = textNavy)
                                }
                            }
                            
                            Spacer(Modifier.height(4.dp))
                            
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().background(Color(0xFF1E293B)).padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Party Name", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                                    Text("Sales & Exp", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                    Text("Payments", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                    Text("Net Balance", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
                                }
                                
                                if (allWorkers.isEmpty()) {
                                    Text("No parties registered.", fontSize = 10.sp, color = textGray, modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally))
                                } else {
                                    allWorkers.forEachIndexed { idx, worker ->
                                        val partyTx = projectTransactions.filter { it.partyId == worker.id || it.partyName == worker.name }
                                        val totalIn = partyTx.filter { it.type == "Money In" }.sumOf { it.amount }
                                        val totalOut = partyTx.filter { it.type == "Money Out" }.sumOf { it.amount }
                                        val netBalance = totalIn - totalOut

                                        val isClient = worker.partyType == "Client" || worker.partyType == "Investor"
                                        val payments = if (isClient) totalIn else totalOut
                                        val salesExpenses = if (isClient) totalOut else totalIn

                                        Row(
                                            modifier = Modifier.fillMaxWidth().background(if (idx % 2 == 0) Color(0xFFF8FAFC) else Color.White).padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(worker.name, fontSize = 9.sp, color = textNavy, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                                            
                                            val salesText = if (salesExpenses > 0) String.format(Locale.US, "%,.2f", salesExpenses) else ""
                                            Text(salesText, fontSize = 9.sp, color = textNavy, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                            
                                            val paymentsText = if (payments > 0) String.format(Locale.US, "%,.2f", payments) else ""
                                            Text(paymentsText, fontSize = 9.sp, color = textNavy, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                            
                                            val isReceived = netBalance >= 0
                                            val balColor = if (isReceived) Color(0xFF15803D) else Color(0xFFB91C1C)
                                            val balStatus = if (isReceived) "Received" else "Paid"
                                            val balText = String.format(Locale.US, "%,.2f", netBalance.absoluteValue) + " " + balStatus
                                            Text(balText, fontSize = 9.sp, color = balColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
                                        }
                                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor))
                                    }
                                }
                            }
                        }
                    }
                    "Summary" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(projectName, fontSize = 10.sp, color = textNavy, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(siteAddress.replace("\n", " "), fontSize = 8.sp, color = textGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("GST : N/A", fontSize = 8.sp, color = textGray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Payment Report", fontSize = 12.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                    Text("Date Range: $dateRangeText", fontSize = 8.sp, color = textGray)
                                }
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor))
                            
                            // Project Box
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Row {
                                    Text("Project: ", fontSize = 8.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                                    Text(projectName, fontSize = 8.sp, color = textNavy)
                                }
                                Row {
                                    Text("Site Address: ", fontSize = 8.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                                    Text(siteAddress.replace("\n", " "), fontSize = 8.sp, color = textNavy)
                                }
                            }
                            
                            Spacer(Modifier.height(4.dp))
                            Text("Summary", fontSize = 12.sp, color = textNavy, fontWeight = FontWeight.Bold)
                            
                            val tradeGroups = projectTransactions.groupBy { tx ->
                                val w = allWorkers.find { it.id == tx.partyId || it.name == tx.partyName }
                                w?.partyType ?: "-"
                            }
                            Text("Trade Summary", fontSize = 10.sp, color = textNavy, fontWeight = FontWeight.Bold)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E293B)).padding(4.dp)) {
                                    Text("Trade", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                                    Text("#Entries", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
                                    Text("In", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                    Text("Out", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                }
                                tradeGroups.toList().forEachIndexed { idx, (trade, txs) ->
                                    val totalIn = txs.filter { it.type == "Money In" }.sumOf { it.amount }
                                    val totalOut = txs.filter { it.type == "Money Out" }.sumOf { it.amount }
                                    Row(modifier = Modifier.fillMaxWidth().background(if (idx % 2 == 0) Color(0xFFF8FAFC) else Color.White).padding(4.dp)) {
                                        Text(trade, fontSize = 8.sp, color = textNavy, modifier = Modifier.weight(1.5f))
                                        Text(txs.size.toString(), fontSize = 8.sp, color = textNavy, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
                                        Text(String.format(Locale.US, "%,.2f", totalIn), fontSize = 8.sp, color = Color(0xFF15803D), textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                        Text(String.format(Locale.US, "%,.2f", totalOut), fontSize = 8.sp, color = Color(0xFFB91C1C), textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                    }
                                }
                            }
                            
                            Spacer(Modifier.height(4.dp))
                            
                            val catGroups = projectTransactions.groupBy { it.category }
                            Text("Category Summary", fontSize = 10.sp, color = textNavy, fontWeight = FontWeight.Bold)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E293B)).padding(4.dp)) {
                                    Text("Category", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                                    Text("#Entries", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
                                    Text("In", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                    Text("Out", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                }
                                catGroups.toList().forEachIndexed { idx, (cat, txs) ->
                                    val totalIn = txs.filter { it.type == "Money In" }.sumOf { it.amount }
                                    val totalOut = txs.filter { it.type == "Money Out" }.sumOf { it.amount }
                                    Row(modifier = Modifier.fillMaxWidth().background(if (idx % 2 == 0) Color(0xFFF8FAFC) else Color.White).padding(4.dp)) {
                                        Text(cat, fontSize = 8.sp, color = textNavy, modifier = Modifier.weight(1.5f))
                                        Text(txs.size.toString(), fontSize = 8.sp, color = textNavy, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
                                        Text(String.format(Locale.US, "%,.2f", totalIn), fontSize = 8.sp, color = Color(0xFF15803D), textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                        Text(String.format(Locale.US, "%,.2f", totalOut), fontSize = 8.sp, color = Color(0xFFB91C1C), textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                    }
                                }
                            }
                        }
                    }
                    "Transactions" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(projectName, fontSize = 10.sp, color = textNavy, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(siteAddress.replace("\n", " "), fontSize = 8.sp, color = textGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("GST : N/A", fontSize = 8.sp, color = textGray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Payment Report", fontSize = 12.sp, color = textNavy, fontWeight = FontWeight.Bold)
                                    Text("Generated By: $generatedBy", fontSize = 8.sp, color = textGray)
                                    Text("Date Range: $dateRangeText", fontSize = 8.sp, color = textGray)
                                }
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor))
                            
                            // Project Box
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Row {
                                    Text("Project: ", fontSize = 8.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                                    Text(projectName, fontSize = 8.sp, color = textNavy)
                                }
                                Row {
                                    Text("Site Address: ", fontSize = 8.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                                    Text(siteAddress.replace("\n", " "), fontSize = 8.sp, color = textNavy)
                                }
                            }
                            
                            val totalIn = projectTransactions.filter { it.type == "Money In" }.sumOf { it.amount }
                            val totalOut = projectTransactions.filter { it.type == "Money Out" }.sumOf { it.amount }
                            val totalBalance = totalIn - totalOut
                            Column(
                                modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9)).padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Total In: ${String.format(Locale.US, "%,.2f", totalIn)}", fontSize = 9.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                                Text("Total Out: ${String.format(Locale.US, "%,.2f", totalOut)}", fontSize = 9.sp, color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
                                Text("Balance: ${String.format(Locale.US, "%,.2f", totalBalance)}", fontSize = 9.sp, color = textNavy, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(Modifier.height(4.dp))
                            
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E293B)).padding(4.dp)) {
                                    Text("Date", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text("Sender", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                                    Text("Receiver", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                                    Text("Amount", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                    Text("Balance", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                }
                                
                                val sorted = projectTransactions.sortedBy { it.date }
                                var bal = 0.0
                                sorted.forEachIndexed { idx, tx ->
                                    val isIn = tx.type == "Money In"
                                    bal += if (isIn) tx.amount else -tx.amount
                                    Row(
                                        modifier = Modifier.fillMaxWidth().background(if (idx % 2 == 0) Color(0xFFF8FAFC) else Color.White).padding(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(tx.date, fontSize = 8.sp, color = textNavy, modifier = Modifier.weight(1f))
                                        
                                        val sender = if (isIn) (tx.partyName ?: "Client") else "Company"
                                        Text(sender, fontSize = 8.sp, color = textNavy, modifier = Modifier.weight(1.2f))
                                        
                                        val receiver = if (!isIn) (tx.partyName ?: "Staff") else "Company"
                                        Text(receiver, fontSize = 8.sp, color = textNavy, modifier = Modifier.weight(1.2f))
                                        
                                        val amtColor = if (isIn) Color(0xFF15803D) else Color(0xFFB91C1C)
                                        Text(String.format(Locale.US, "%,.2f", tx.amount), fontSize = 8.sp, color = amtColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                        
                                        Text(String.format(Locale.US, "%,.2f", bal), fontSize = 8.sp, color = textNavy, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                                    }
                                    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(dividerColor))
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Actions Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, RoseGlow.copy(0.4f), RoundedCornerShape(12.dp))
                        .background(RoseGlow.copy(0.08f))
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CLOSE", color = RoseGlow, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GradientAqua)
                        .clickable {
                            try {
                                val pdfFile = when (reportType) {
                                    "Receipt" -> {
                                        val rxAmount = selectedTxDetail?.amount ?: 1000.0
                                        val rxName = selectedTxDetail?.partyName ?: selectedPartyDetail?.name ?: "Tejas Harane"
                                        val rxDate = selectedTxDetail?.date ?: "2026-05-27"
                                        val rxId = selectedTxDetail?.id ?: 1024
                                        val rxMethod = selectedTxDetail?.paymentMethod ?: "Cash"
                                        val rxRemark = selectedTxDetail?.description ?: ""
                                        val rxIsMoneyIn = selectedTxDetail?.type == "Money In"
                                        PdfUtils.generateReceiptPdfFile(
                                            context = context,
                                            txId = rxId,
                                            name = rxName,
                                            amount = formatIndianRupees(rxAmount),
                                            date = rxDate,
                                            paymentMethod = rxMethod,
                                            remark = rxRemark,
                                            isMoneyIn = rxIsMoneyIn,
                                            projectName = projectName,
                                            siteAddress = siteAddress
                                        )
                                    }
                                    "Party Ledger" -> {
                                        val party = selectedPartyDetail ?: allWorkers.firstOrNull() ?: throw IllegalArgumentException("No party selected")
                                        val matchedTxs = projectTransactions.filter { it.partyId == party.id || it.partyName == party.name }
                                        val totalIn = matchedTxs.filter { it.type == "Money In" }.sumOf { it.amount }
                                        val totalOut = matchedTxs.filter { it.type == "Money Out" }.sumOf { it.amount }
                                        val balance = totalIn - totalOut

                                        val isClient = party.partyType == "Client" || party.partyType == "Investor"
                                        val payments = if (isClient) totalIn else totalOut
                                        val salesExpenses = if (isClient) totalOut else totalIn

                                        val statusText = if (balance >= 0) "Received" else "Paid"

                                        PdfUtils.generateBalanceReviewPdfFile(
                                            context = context,
                                            partyName = party.name,
                                            projectName = projectName,
                                            balance = formatIndianRupees(kotlin.math.abs(balance)),
                                            statusText = statusText,
                                            received = formatIndianRupees(payments),
                                            paid = formatIndianRupees(salesExpenses),
                                            transactions = matchedTxs,
                                            siteAddress = siteAddress,
                                            generatedBy = generatedBy,
                                            dateRange = dateRangeText
                                        )
                                    }
                                    "Party Transactions" -> {
                                        val party = selectedPartyDetail ?: allWorkers.firstOrNull() ?: throw IllegalArgumentException("No party selected")
                                        PdfUtils.generatePartyTransactionsReportPdfFile(
                                            context = context,
                                            projectName = projectName,
                                            siteAddress = siteAddress,
                                            generatedBy = generatedBy,
                                            party = party,
                                            transactions = projectTransactions,
                                            dateRange = dateRangeText
                                        )
                                    }
                                    "Party Balance" -> {
                                        PdfUtils.generatePartyBalanceReportPdfFile(
                                            context = context,
                                            projectName = projectName,
                                            siteAddress = siteAddress,
                                            generatedBy = generatedBy,
                                            parties = allWorkers,
                                            transactions = projectTransactions,
                                            dateRange = dateRangeText
                                        )
                                    }
                                    "Summary" -> {
                                        PdfUtils.generatePaymentSummaryReportPdfFile(
                                            context = context,
                                            projectName = projectName,
                                            siteAddress = siteAddress,
                                            generatedBy = generatedBy,
                                            transactions = projectTransactions,
                                            parties = allWorkers,
                                            dateRange = dateRangeText
                                        )
                                    }
                                    "Transactions" -> {
                                        PdfUtils.generatePaymentTransactionsReportPdfFile(
                                            context = context,
                                            projectName = projectName,
                                            siteAddress = siteAddress,
                                            generatedBy = generatedBy,
                                            transactions = projectTransactions,
                                            dateRange = dateRangeText
                                        )
                                    }
                                    else -> throw IllegalArgumentException("Unknown type")
                                }
                                val fileName = pdfFile.name
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                    val contentValues = android.content.ContentValues().apply {
                                        put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                                        put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf")
                                        put(android.provider.MediaStore.Downloads.RELATIVE_PATH,
                                            android.os.Environment.DIRECTORY_DOWNLOADS + "/ConstructPro")
                                    }
                                    val uri = context.contentResolver.insert(
                                        android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                        contentValues
                                    )
                                    if (uri != null) {
                                        context.contentResolver.openOutputStream(uri)?.use { out ->
                                            pdfFile.inputStream().use { input -> input.copyTo(out) }
                                        }
                                        Toast.makeText(context, "Saved to Downloads/ConstructPro/$fileName", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Download failed: could not create file", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    val dir = android.os.Environment.getExternalStoragePublicDirectory(
                                        android.os.Environment.DIRECTORY_DOWNLOADS
                                    )
                                    val folder = java.io.File(dir, "ConstructPro").also { it.mkdirs() }
                                    val dest = java.io.File(folder, fileName)
                                    pdfFile.copyTo(dest, overwrite = true)
                                    Toast.makeText(context, "Saved to Downloads/ConstructPro/$fileName", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text("DOWNLOAD", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// DATE FILTER AND RANGE HELPERS
// ─────────────────────────────────────────────
fun getPresetDateRange(preset: String): Pair<String, String>? {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()
    
    when (preset) {
        "Today" -> {
            val today = sdf.format(cal.time)
            return Pair(today, today)
        }
        "This Week" -> {
            val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val daysToSubtract = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY
            val end = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
            val start = sdf.format(cal.time)
            return Pair(start, end)
        }
        "Last Week" -> {
            val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val daysToSubtract = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY
            cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val end = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, -6)
            val start = sdf.format(cal.time)
            return Pair(start, end)
        }
        "This Month" -> {
            val end = sdf.format(cal.time)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val start = sdf.format(cal.time)
            return Pair(start, end)
        }
        "Last Month" -> {
            cal.add(Calendar.MONTH, -1)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val start = sdf.format(cal.time)
            val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, lastDay)
            val end = sdf.format(cal.time)
            return Pair(start, end)
        }
        else -> return null
    }
}

@Composable
fun ReportFilterDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    dark: Boolean,
    preset: String,
    onPresetChange: (String) -> Unit,
    startDate: String,
    onStartDateChange: (String) -> Unit,
    endDate: String,
    onEndDateChange: (String) -> Unit,
    onApply: () -> Unit
) {
    if (!visible) return

    val context = LocalContext.current

    val showDatePicker = { isStart: Boolean ->
        val calendar = java.util.Calendar.getInstance()
        val currentValue = if (isStart) startDate else endDate
        if (currentValue.isNotEmpty()) {
            try {
                val parts = currentValue.split("-")
                if (parts.size == 3) {
                    calendar.set(java.util.Calendar.YEAR, parts[0].toInt())
                    calendar.set(java.util.Calendar.MONTH, parts[1].toInt() - 1)
                    calendar.set(java.util.Calendar.DAY_OF_MONTH, parts[2].toInt())
                }
            } catch (e: Exception) {
                // ignore
            }
        }
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                if (isStart) onStartDateChange(formattedDate) else onEndDateChange(formattedDate)
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    GlassModalDialog(
        visible = visible,
        onDismiss = onDismiss,
        title = "Filter Transactions",
        darkTheme = dark,
        glowColor = EmeraldGlow
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "DATE RANGE PRESETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (dark) TextSecondary else TextSecondaryLight,
                letterSpacing = 1.sp
            )

            val presets = listOf("All", "Today", "This Week", "Last Week", "This Month", "Last Month", "Custom")
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.chunked(3).forEach { rowPresets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPresets.forEach { p ->
                            val isSelected = preset == p
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) EmeraldGlow.copy(alpha = 0.15f)
                                        else (if (dark) Color(0xFF1E2D4A).copy(alpha = 0.4f) else Color(0xFFF1F5FF))
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) EmeraldGlow else (if (dark) Color(0xFF2D3F5E) else Color(0xFFCBD5E1)),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        onPresetChange(p)
                                        if (p != "Custom") {
                                            val range = getPresetDateRange(p)
                                            if (range != null) {
                                                onStartDateChange(range.first)
                                                onEndDateChange(range.second)
                                            } else {
                                                onStartDateChange("")
                                                onEndDateChange("")
                                            }
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p,
                                    color = if (isSelected) (if (dark) NeonGreen else Color(0xFF047857))
                                            else (if (dark) TextSecondary else TextSecondaryLight),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        if (rowPresets.size < 3) {
                            repeat(3 - rowPresets.size) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (preset == "Custom") {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "START DATE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) TextSecondary else TextSecondaryLight,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (dark) Color(0xFF1E2D4A).copy(alpha = 0.4f) else Color.White)
                                .border(1.dp, if (dark) Color(0xFF2D3F5E) else Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                                .clickable { showDatePicker(true) }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (startDate.isEmpty()) "Select Date" else startDate,
                                    fontSize = 12.sp,
                                    color = if (startDate.isEmpty()) (if (dark) TextMuted else TextMutedLight)
                                            else (if (dark) Color.White else Color(0xFF1E293B)),
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select Start Date",
                                    tint = EmeraldGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "END DATE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) TextSecondary else TextSecondaryLight,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (dark) Color(0xFF1E2D4A).copy(alpha = 0.4f) else Color.White)
                                .border(1.dp, if (dark) Color(0xFF2D3F5E) else Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                                .clickable { showDatePicker(false) }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (endDate.isEmpty()) "Select Date" else endDate,
                                    fontSize = 12.sp,
                                    color = if (endDate.isEmpty()) (if (dark) TextMuted else TextMutedLight)
                                            else (if (dark) Color.White else Color(0xFF1E293B)),
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select End Date",
                                    tint = EmeraldGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, RoseGlow.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .background(RoseGlow.copy(alpha = 0.08f))
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CANCEL",
                        color = RoseGlow,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                val canApply = preset != "Custom" || (startDate.isNotEmpty() && endDate.isNotEmpty())
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (canApply) EmeraldGlow else Color.Gray.copy(alpha = 0.5f))
                        .then(if (canApply) Modifier.clickable(onClick = onApply) else Modifier)
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DONE",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}


