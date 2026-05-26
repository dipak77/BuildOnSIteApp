package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun MoreScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val dark = viewModel.darkThemeEnabled
    val allProjects by viewModel.projects.collectAsState()
    val allWorkers by viewModel.workers.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val allMOMs by viewModel.moms.collectAsState()
    val allPayroll by viewModel.payroll.collectAsState()
    val allEstimates by viewModel.estimates.collectAsState()

    val currentProject by viewModel.activeProject.collectAsState()

    val context = LocalContext.current
    val cFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    // Navigation sub-model trigger selectors
    var activeSubModal by remember { mutableStateOf<String?>(null) } // "Parties", "Estimates", "Payroll", "Reports", "Minutes"

    // Estimates Input States
    var inputEstName by remember { mutableStateOf("") }
    var inputEstQty by remember { mutableStateOf("") }
    var inputEstUnit by remember { mutableStateOf("") }
    var inputEstRate by remember { mutableStateOf("") }

    // MOM Input States
    var inputMOMTitle by remember { mutableStateOf("") }
    var inputMOMContent by remember { mutableStateOf("") }

    // Payroll Input States
    var selectedWorkerForPayroll by remember { mutableStateOf<Worker?>(null) }
    var inputPayrollAmount by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Title: Admin Workspace
        item {
            Column {
                Text(
                    text = "Control Center",
                    color = if (dark) NeonCyan else Color(0xFF0284C7),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Construction administrative modules",
                    color = if (dark) TextSecondary else TextSecondaryLight,
                    fontSize = 12.sp
                )
            }
        }

        // Module Grid
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Parties (Workers)
                    ModuleGridCell(
                        title = "Parties",
                        icon = Icons.Default.Groups,
                        desc = "Manage workers",
                        gradient = Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.5f), Color.Transparent)),
                        darkTheme = dark,
                        modifier = Modifier.weight(1f),
                        onClick = { activeSubModal = "Parties" }
                    )

                    // Estimates
                    ModuleGridCell(
                        title = "Estimates",
                        icon = Icons.Default.Construction,
                        desc = "Project bills & materials",
                        gradient = Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.5f), Color.Transparent)),
                        darkTheme = dark,
                        modifier = Modifier.weight(1f),
                        onClick = { activeSubModal = "Estimates" }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Payroll
                    ModuleGridCell(
                        title = "Payroll",
                        icon = Icons.Default.Receipt,
                        desc = "Disbursements log",
                        gradient = Brush.linearGradient(listOf(NeonGreen.copy(alpha = 0.5f), Color.Transparent)),
                        darkTheme = dark,
                        modifier = Modifier.weight(1f),
                        onClick = { activeSubModal = "Payroll" }
                    )

                    // Reports
                    ModuleGridCell(
                        title = "Reports",
                        icon = Icons.Default.Analytics,
                        desc = "Project cost audits",
                        gradient = Brush.linearGradient(listOf(NeonAmber.copy(alpha = 0.5f), Color.Transparent)),
                        darkTheme = dark,
                        modifier = Modifier.weight(1f),
                        onClick = { activeSubModal = "Reports" }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // MOM Minutes
                    ModuleGridCell(
                        title = "Minutes",
                        icon = Icons.Default.FilePresent,
                        desc = "Meeting digests",
                        gradient = Brush.linearGradient(listOf(NeonPink.copy(alpha = 0.5f), Color.Transparent)),
                        darkTheme = dark,
                        modifier = Modifier.weight(0.5f),
                        onClick = { activeSubModal = "Minutes" }
                    )
                }
            }
        }

        // Theme and Backup Control Row Title
        item {
            Text(
                text = "Preferences",
                color = if (dark) TextPrimary else TextPrimaryLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Appearance Selector & File Backups Box
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                darkTheme = dark
            ) {
                // Dark theme toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (dark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Theme Toggle",
                            tint = if (dark) NeonCyan else Color(0xFF0284C7)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Dark Glass Neon theme",
                                color = if (dark) TextPrimary else TextPrimaryLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Frosted backgrounds & glowing indicators",
                                color = if (dark) TextSecondary else TextSecondaryLight,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = dark,
                        onCheckedChange = { viewModel.darkThemeEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }

                Divider(color = if (dark) GlassBorderDark else GlassBorderLight, modifier = Modifier.padding(vertical = 12.dp))

                // Database operation trigger buttons
                Text(
                    text = "Backup & Shares",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (dark) NeonCyan else Color(0xFF0284C7),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Export JSON Backup
                    GlassButton(
                        onClick = { viewModel.exportFullBackup(context) },
                        darkTheme = dark,
                        glowColor = NeonPurple,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("BACKUP DATA", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Restore JSON Backup (Launches sample text payload to simulate import, or restores directly)
                    GlassButton(
                        onClick = {
                            val seedJson = """
                            {
                              "projects": [
                                {"id":1, "name":"Emerald Plaza Restore", "location":"High Street Segment 2", "budget":800000.0, "status":"Active"}
                              ],
                              "workers": [
                                {"id":1, "name":"Jason Becker", "role":"Architect Designer", "shift":"Day", "wageRate":750.0, "avatarColor":-16724321}
                              ],
                              "tasks": [],
                              "transactions": [],
                              "attendance": [],
                              "moms": [],
                              "payroll": [],
                              "estimates": []
                            }
                            """.trimIndent()
                            viewModel.importFullBackup(context, seedJson)
                        },
                        darkTheme = dark,
                        glowColor = NeonCyan,
                        outlineMode = true,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("IMPORT BACKUP", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Project list with Dot Status indicators
        item {
            Text(
                text = "Construction Projects",
                color = if (dark) TextPrimary else TextPrimaryLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(allProjects) { proj ->
            val statusColor = when (proj.status) {
                "Active" -> NeonGreen
                "On Hold" -> NeonAmber
                else -> NeonPink
            }

            val isActiveFocus = currentProject?.id == proj.id

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                darkTheme = dark,
                borderColor = if (isActiveFocus) GlassesActiveGlowBorder(dark) else null,
                padding = 12.dp,
                onClick = { viewModel.selectedProjectId = proj.id }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Dot project status indicator
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = proj.name,
                                color = if (dark) TextPrimary else TextPrimaryLight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${proj.location} • Budget: ${cFormatter.format(proj.budget)}",
                                color = if (dark) TextSecondary else TextSecondaryLight,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Delete options
                    IconButton(onClick = { viewModel.deleteProject(proj, context) }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove Project",
                            tint = if (dark) TextMuted else TextSecondaryLight
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // MODULE WINDOW MODALS (ESTIMATES, PAYROLL, MOM)
    // ==========================================

    // 1. Workers MODAL (Parties)
    GlassModalDialog(
        visible = activeSubModal == "Parties",
        onDismiss = { activeSubModal = null },
        title = "Site Roster (Workers)",
        darkTheme = dark,
        glowColor = NeonCyan
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allWorkers) { worker ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (dark) Color(0x1F293780) else Color(0x1E000000)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(worker.name, color = if (dark) TextPrimary else TextPrimaryLight, fontWeight = FontWeight.Bold)
                            Text("${worker.role} • ${worker.shift} shift", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 11.sp)
                        }
                        IconButton(onClick = { viewModel.deleteWorker(worker, context) }) {
                            Icon(Icons.Default.DeleteOutline, null, tint = NeonPink, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    // 2. Estimates MODAL
    GlassModalDialog(
        visible = activeSubModal == "Estimates",
        onDismiss = { activeSubModal = null },
        title = "Estimates & Materials",
        darkTheme = dark,
        glowColor = NeonPurple
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Add estimate input form
            Text("Add Material Estimate", fontWeight = FontWeight.Bold, color = if (dark) TextPrimary else TextPrimaryLight, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassTextField(value = inputEstName, onValueChange = { inputEstName = it }, label = "Cement...", modifier = Modifier.weight(0.5f), darkTheme = dark)
                GlassTextField(value = inputEstQty, onValueChange = { inputEstQty = it }, label = "Qty", isNumeric = true, modifier = Modifier.weight(0.25f), darkTheme = dark)
                GlassTextField(value = inputEstRate, onValueChange = { inputEstRate = it }, label = "Rate", isNumeric = true, modifier = Modifier.weight(0.25f), darkTheme = dark)
            }
            GlassButton(
                onClick = {
                    val qty = inputEstQty.toDoubleOrNull() ?: 1.0
                    val rate = inputEstRate.toDoubleOrNull() ?: 1.0
                    if (inputEstName.isNotBlank() && currentProject != null) {
                        viewModel.addEstimate(currentProject!!.id, inputEstName, qty, "Bag", rate)
                        inputEstName = ""
                        inputEstQty = ""
                        inputEstRate = ""
                    }
                },
                darkTheme = dark,
                glowColor = NeonPurple,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ADD ESTIMATE", fontWeight = FontWeight.Bold)
            }

            Divider(color = if (dark) GlassBorderDark else GlassBorderLight)

            // Current lists
            val filteredEstimates = allEstimates.filter { it.projectId == currentProject?.id }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredEstimates) { est ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(est.itemName, color = if (dark) TextPrimary else TextPrimaryLight, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${est.quantity} bags at $${est.rate}/bag", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 11.sp)
                        }
                        Text(cFormatter.format(est.totalCost), color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // 3. MOM Minutes MODAL
    GlassModalDialog(
        visible = activeSubModal == "Minutes",
        onDismiss = { activeSubModal = null },
        title = "Meeting Minutes (MOM)",
        darkTheme = dark,
        glowColor = NeonPink
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Log Site Minutes", fontWeight = FontWeight.Bold, color = if (dark) TextPrimary else TextPrimaryLight, fontSize = 12.sp)
            GlassTextField(value = inputMOMTitle, onValueChange = { inputMOMTitle = it }, label = "Architecture Layout Specs Alignment", darkTheme = dark)
            GlassTextField(value = inputMOMContent, onValueChange = { inputMOMContent = it }, label = "Meeting minutes details...", darkTheme = dark)
            GlassButton(
                onClick = {
                    if (inputMOMTitle.isNotBlank() && currentProject != null) {
                        viewModel.addMOM(currentProject!!.id, inputMOMTitle, inputMOMContent, "2026-05-26")
                        inputMOMTitle = ""
                        inputMOMContent = ""
                    }
                },
                darkTheme = dark,
                glowColor = NeonPink,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ADD MEETING RECORD", fontWeight = FontWeight.Bold)
            }

            Divider(color = if (dark) GlassBorderDark else GlassBorderLight)

            val projMOMs = allMOMs.filter { it.projectId == currentProject?.id }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(projMOMs) { mom ->
                    Column(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                        Text(mom.title, color = if (dark) TextPrimary else TextPrimaryLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(mom.content, color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 11.sp)
                        Text("Date: ${mom.date}", color = if (dark) TextMuted else TextSecondaryLight, fontSize = 9.sp)
                    }
                }
            }
        }
    }

    // 4. Payroll MODAL
    GlassModalDialog(
        visible = activeSubModal == "Payroll",
        onDismiss = { activeSubModal = null },
        title = "Wages Disbursements Ledger",
        darkTheme = dark,
        glowColor = NeonGreen
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Issue Wage Payment", fontWeight = FontWeight.Bold, color = if (dark) TextPrimary else TextPrimaryLight, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Dropdown or Quick picker
                Text(
                    text = selectedWorkerForPayroll?.name ?: "Select Worker",
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (allWorkers.isNotEmpty()) {
                                selectedWorkerForPayroll = allWorkers.random()
                            }
                        }
                        .background(if (dark) Color(0x1F293780) else Color(0x33000000), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    color = if (dark) TextPrimary else TextPrimaryLight,
                    fontSize = 13.sp
                )
                GlassTextField(value = inputPayrollAmount, onValueChange = { inputPayrollAmount = it }, label = "Amount", isNumeric = true, modifier = Modifier.weight(1f), darkTheme = dark)
            }
            GlassButton(
                onClick = {
                    val amt = inputPayrollAmount.toDoubleOrNull() ?: 100.0
                    if (selectedWorkerForPayroll != null && currentProject != null) {
                        viewModel.addPayroll(selectedWorkerForPayroll!!.id, currentProject!!.id, "2026-05-26", amt, "Paid")
                        inputPayrollAmount = ""
                        selectedWorkerForPayroll = null
                    }
                },
                darkTheme = dark,
                glowColor = NeonGreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("PROCESS PAYROLL", fontWeight = FontWeight.Bold)
            }

            Divider(color = if (dark) GlassBorderDark else GlassBorderLight)

            val projPayroll = allPayroll.filter { it.projectId == currentProject?.id }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(projPayroll) { py ->
                    val worker = allWorkers.find { it.id == py.workerId }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(worker?.name ?: "Unknown Crew", color = if (dark) TextPrimary else TextPrimaryLight, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Disbursement: ${py.date}", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 11.sp)
                        }
                        Text(cFormatter.format(py.wagesPaid), color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // 5. Reports FinAudits MODAL
    GlassModalDialog(
        visible = activeSubModal == "Reports",
        onDismiss = { activeSubModal = null },
        title = "Financial Audit Report Details",
        darkTheme = dark,
        glowColor = NeonAmber
    ) {
        val projTx = allTransactions.filter { it.projectId == currentProject?.id }
        val categoryTotals = projTx.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }

        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Expenditures by Category Tag", color = if (dark) TextPrimary else TextPrimaryLight, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            val totalTxSum = projTx.sumOf { it.amount }
            categoryTotals.forEach { (cat, tot) ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(cat, color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 13.sp)
                        Text(cFormatter.format(tot), color = if (dark) TextPrimary else TextPrimaryLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val progressRatio = if (totalTxSum > 0.0) (tot / totalTxSum).toFloat() else 0f
                    GlassProgressBar(progress = progressRatio, darkTheme = dark, glowColor = NeonAmber)
                }
            }
        }
    }
}

// Reusable Grid Cell Card with fine gradients
@Composable
fun ModuleGridCell(
    title: String,
    icon: ImageVector,
    desc: String,
    gradient: Brush,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (darkTheme) GlassBorderDark else GlassBorderLight),
        colors = CardDefaults.cardColors(containerColor = if (darkTheme) Color(0x35111827) else Color(0xCCFFFFFF))
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(14.dp)
                .fillMaxWidth()
                .height(90.dp)
        ) {
            Column {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (darkTheme) Color.White else Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    title,
                    color = if (darkTheme) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    desc,
                    color = if (darkTheme) TextSecondary else TextSecondaryLight,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Active focusing color mapping function
private fun GlassesActiveGlowBorder(darkTheme: Boolean): Color {
    return if (darkTheme) GlassBorderNeonCyan else NeonCyan
}
