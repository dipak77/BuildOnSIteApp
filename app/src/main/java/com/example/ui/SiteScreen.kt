package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

@Composable
fun SiteScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val dark = viewModel.darkThemeEnabled
    val context = LocalContext.current
    val currentProject by viewModel.activeProject.collectAsState()
    val allWorkers by viewModel.workers.collectAsState()
    val allAttendance by viewModel.attendance.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val allTasks by viewModel.tasks.collectAsState()

    // Active navigational date for attendance tab (YYYY-MM-DD)
    val activeDate = viewModel.attendanceDate

    // Interactive dialog selection
    var selectedWorkerForAttendance by remember { mutableStateOf<Worker?>(null) }
    var inputOvertimeHours by remember { mutableStateOf("0.0") }

    val formatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val displayFormat = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US) }
    val cFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    // Multi-page state
    var selectedPartyDetail by remember { mutableStateOf<Worker?>(null) }
    var selectedTxDetail by remember { mutableStateOf<Transaction?>(null) }

    // Tab Selection state for Page 1
    var activeSiteTab by remember { mutableStateOf("Party") }
    val tabs = listOf("Party", "Transaction", "Site", "Task", "Attendance")

    // Sort Query and search for Party Tab
    var partySearchQuery by remember { mutableStateOf("") }
    var activeFilterSelected by remember { mutableStateOf(false) } // Active state toggle

    // Add transaction state for current active party
    var showAddPartyTxDialog by remember { mutableStateOf(false) }
    var partyTxType by remember { mutableStateOf("Money Out") } // "Money Out" or "Money In"
    var partyTxAmount by remember { mutableStateOf("") }
    var partyTxCategory by remember { mutableStateOf("Labor") }
    var partyTxDesc by remember { mutableStateOf("") }
    var partyTxMethod by remember { mutableStateOf("Cash") }
    var partyTxDate by remember { mutableStateOf("2026-05-27") }

    // PDF Preview dialog overlay
    var showPdfPreviewDialog by remember { mutableStateOf(false) }

    // Compute Project Transactions
    val activeProjId = currentProject?.id
    val projectTransactions = remember(allTransactions, activeProjId) {
        if (activeProjId == null) emptyList()
        else allTransactions.filter { it.projectId == activeProjId }
    }

    // Attendance calculation states
    val navigateDay = { days: Int ->
        val cal = Calendar.getInstance()
        cal.time = formatter.parse(activeDate) ?: Date()
        cal.add(Calendar.DATE, days)
        viewModel.attendanceDate = formatter.format(cal.time)
    }

    val parsedDateString = remember(activeDate) {
        try {
            val d = formatter.parse(activeDate) ?: Date()
            displayFormat.format(d)
        } catch (e: Exception) {
            activeDate
        }
    }

    val activeDateAttendance = remember(allAttendance, activeDate, currentProject) {
        val projId = currentProject?.id
        if (projId == null) emptyList()
        else allAttendance.filter { it.date == activeDate && it.projectId == projId }
    }

    // Core stats based on workers
    val presentCount = activeDateAttendance.count { it.status == "Present" || it.status == "Overtime" }
    val absentCount = activeDateAttendance.count { it.status == "Absent" }
    val totalOvertime = activeDateAttendance.sumOf { it.overtimeHours }

    val dailyWages = remember(activeDateAttendance, allWorkers) {
        activeDateAttendance.sumOf { att ->
            val w = allWorkers.find { it.id == att.workerId } ?: return@sumOf 0.0
            val base = if (att.status == "Absent") 0.0 else w.wageRate
            val overtimeMultiplier = (w.wageRate / 8.0) * 1.5
            val otCost = att.overtimeHours * overtimeMultiplier
            base + otCost
        }
    }

    // ================= PAGE NAVIGATION LAYER =================
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        if (selectedTxDetail != null) {
            // ================= PAGE 3: PAYMENT DETAILS ROUTE =================
            val tx = selectedTxDetail!!
            val amountStr = formatIndianRupees(tx.amount)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Page Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedTxDetail = null }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (dark) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Payment",
                                color = if (dark) TextPrimary else TextPrimaryLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "entry by ${viewModel.userSession.value?.displayName ?: "Tejas Harane"}",
                                color = if (dark) TextSecondary else TextSecondaryLight,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // PDF Icon & Share Custom Actions (Top Right)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // PDF Document Icon Badge
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x22FFFFFF))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .clickable { showPdfPreviewDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "PDF",
                                color = if (dark) NeonCyan else Color(0xFF0284C7),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Share Icon
                        IconButton(
                            onClick = {
                                val shareTxt = """
                                    Payment Receipt Detail:
                                    Project: ${currentProject?.name ?: "N/A"}
                                    To/Party: ${tx.partyName ?: "Company"}
                                    Amount: $amountStr
                                    Date: ${tx.date}
                                    Payment Method: ${tx.paymentMethod}
                                    Status: Transaction Authenticated
                                """.trimIndent()

                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Payment Receipt")
                                    putExtra(Intent.EXTRA_TEXT, shareTxt)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share payment record"))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = if (dark) Color.White else Color.Black
                            )
                        }

                        // Edit Icon Button
                        IconButton(onClick = {
                            Toast.makeText(context, "Interactive Edit details is active!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = if (dark) Color.White else Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                Divider(color = if (dark) GlassBorderDark else GlassBorderLight)
                Spacer(modifier = Modifier.height(18.dp))

                // Amount Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Amount",
                            color = if (dark) TextSecondary else TextSecondaryLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = amountStr,
                            color = if (tx.type == "Money In") NeonGreen else NeonPink,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Large dynamic thumbs up icon
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (tx.type == "Money In") NeonGreen.copy(alpha = 0.25f) else NeonCyan.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Success Status",
                            tint = if (tx.type == "Money In") NeonGreen else NeonCyan,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))
                Divider(color = if (dark) GlassBorderDark else GlassBorderLight)
                Spacer(modifier = Modifier.height(18.dp))

                // Detail Columns
                val toVal = if (tx.type == "Money Out") (tx.partyName ?: "Staff") else "Company"
                val fromVal = if (tx.type == "Money In") (tx.partyName ?: "Client") else "Company"

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailTextRow("To", toVal, dark)
                    DetailTextRow("From", fromVal, dark)
                    DetailTextRow("Paid At", tx.date, dark)
                    DetailTextRow("Project", currentProject?.name ?: "Treasure garden", dark)
                    DetailTextRow("Payment Method", tx.paymentMethod, dark)
                    DetailTextRow("Description", tx.description, dark)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Share PDF Bottom Trigger
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        onClick = { showPdfPreviewDialog = true },
                        darkTheme = dark,
                        glowColor = NeonCyan,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("VIEW PDF RECEIPT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    GlassButton(
                        onClick = {
                            val msg = "Paying: $amountStr to ${tx.partyName ?: "Tejas"}. Download receipt link: mockpdf.com/receipt-${tx.id}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, msg)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Receipt Link"))
                        },
                        darkTheme = dark,
                        glowColor = NeonPurple,
                        outlineMode = true,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SHARE AS LINK", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

        } else if (selectedPartyDetail != null) {
            // ================= PAGE 2: PARTY PROJECTS BALANCE ROUTE =================
            val worker = selectedPartyDetail!!
            val matchedTxs = remember(projectTransactions, worker) {
                projectTransactions.filter { it.partyId == worker.id || it.partyName == worker.name }
            }

            val totalReceived = matchedTxs.filter { it.type == "Money Out" }.sumOf { it.amount }
            val totalPaid = matchedTxs.filter { it.type == "Money In" }.sumOf { it.amount }
            val diff = totalReceived - totalPaid

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Page 2 Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedPartyDetail = null }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (dark) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Party Project Balance",
                            color = if (dark) TextPrimary else TextPrimaryLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { Toast.makeText(context, "Activity logged to database Sync!", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = if (dark) NeonCyan else Color(0xFF0284C7))
                        }
                        IconButton(onClick = { Toast.makeText(context, "Full site transactions backup exported!", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.GetApp, contentDescription = null, tint = if (dark) NeonGreen else Color(0xFF16A34A))
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = if (dark) Color.White else Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Party Card Banner
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    darkTheme = dark,
                    padding = 14.dp,
                    borderColor = if (dark) GlassBorderNeonCyan else null
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = worker.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dark) TextPrimary else TextPrimaryLight
                            )
                            Text(
                                text = currentProject?.name ?: "Treasure garden",
                                fontSize = 12.sp,
                                color = if (dark) TextSecondary else TextSecondaryLight
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatIndianRupees(diff.absoluteValue),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = if (diff >= 0) NeonGreen else NeonPink
                            )
                            Text(
                                text = if (diff >= 0) "Advance Paid" else "Pending to Pay",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (diff >= 0) NeonGreen else NeonPink
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Row (Party Received vs Party Paid)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        darkTheme = dark,
                        padding = 10.dp
                    ) {
                        Text(
                            text = "Party Received",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) TextMuted else TextSecondaryLight
                        )
                        Text(
                            text = formatIndianRupees(totalReceived),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (dark) TextPrimary else TextPrimaryLight
                        )
                    }

                    GlassCard(
                        modifier = Modifier.weight(1f),
                        darkTheme = dark,
                        padding = 10.dp
                    ) {
                        Text(
                            text = "Party Paid",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) TextMuted else TextSecondaryLight
                        )
                        Text(
                            text = formatIndianRupees(totalPaid),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (dark) TextPrimary else TextPrimaryLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filter & Sort Row Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (dark) NeonCyan else Color(0xFF0284C7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Filter",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) TextSecondary else TextSecondaryLight
                        )
                    }

                    Text(
                        text = "Amount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) TextMuted else TextSecondaryLight
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Transactions List
                if (matchedTxs.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recorded transactions for this site party yet.",
                            color = if (dark) TextMuted else TextSecondaryLight,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(matchedTxs) { tx ->
                            // Custom dates cards
                            val dateParts = tx.date.split("-")
                            val yearVal = dateParts.firstOrNull() ?: "2026"
                            val dayMonthStr = if (dateParts.size >= 3) {
                                val monthIndex = dateParts[1].toIntOrNull() ?: 1
                                val dayVal = dateParts[2].toIntOrNull() ?: 27
                                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                                val mStr = months.getOrElse(monthIndex - 1) { "May" }
                                "$dayVal $mStr"
                            } else "27 May"

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                darkTheme = dark,
                                padding = 8.dp,
                                onClick = { selectedTxDetail = tx }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Left Purple Calendar style date block
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Brush.verticalGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = yearVal,
                                                    fontSize = 9.sp,
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = dayMonthStr,
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Direction Text Description
                                        Column {
                                            val dirText = if (tx.type == "Money Out") "Company > ${worker.name}" else "${worker.name} > Company"
                                            Text(
                                                text = dirText,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (dark) TextPrimary else TextPrimaryLight,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = tx.paymentMethod + (if (tx.description.isNotBlank()) " • ${tx.description}" else ""),
                                                fontSize = 11.sp,
                                                color = if (dark) TextSecondary else TextSecondaryLight,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Amount Bold
                                    Text(
                                        text = formatIndianRupees(tx.amount),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (tx.type == "Money In") NeonGreen else NeonPink
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom actions row (I Paid, +, I Received) Custom styled
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left "I Paid" button
                    Button(
                        onClick = {
                            partyTxType = "Money Out"
                            partyTxCategory = "Labor"
                            partyTxAmount = ""
                            partyTxDesc = "Crew payment"
                            showAddPartyTxDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("I PAID", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Middle visual plus button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6200EE))
                            .clickable {
                                viewModel.showTransactionDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Tx",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right "I Received" button
                    Button(
                        onClick = {
                            partyTxType = "Money In"
                            partyTxCategory = "Client Advance"
                            partyTxAmount = ""
                            partyTxDesc = "Received funds"
                            showAddPartyTxDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("I RECEIVED", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }
                }
            }

        } else {
            // ================= PAGE 1:: MAIN MULTI-TAB WITH PARTY / SITES VIEW =================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Screen Headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = currentProject?.name ?: "Treasure garden",
                            color = if (dark) NeonCyan else Color(0xFF0284C7),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Site workspace operations console",
                            color = if (dark) TextSecondary else TextSecondaryLight,
                            fontSize = 12.sp
                        )
                    }

                    // Top action icon bar
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { Toast.makeText(context, "Logged thumbs up to site feed!", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = if (dark) NeonCyan else Color(0xFF0284C7))
                        }
                        IconButton(onClick = { Toast.makeText(context, "System checking notifications...", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = if (dark) NeonPurple else Color(0xFF8B5CF6))
                        }
                        IconButton(onClick = { showPdfPreviewDialog = true }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.LightGray)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = if (dark) Color.White else Color.Black)
                        }
                    }
                }

                // Custom Scrollable Horizontal Site-Level Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEach { tab ->
                        val selected = activeSiteTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) (if (dark) Color(0x3300FFFF) else Color(0x190284C7))
                                    else Color.Transparent
                                )
                                .border(
                                    1.dp,
                                    if (selected) (if (dark) NeonCyan else Color(0xFF0284C7))
                                    else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { activeSiteTab = tab }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                color = if (selected) {
                                    if (dark) NeonCyan else Color(0xFF0284C7)
                                } else {
                                    if (dark) TextSecondary else TextSecondaryLight
                                },
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content Switcher Routing
                when (activeSiteTab) {
                    "Party" -> {
                        // ================= PARTY SUB-TAB =================
                        // Count team members & totals
                        val teamCount = allWorkers.size
                        val totalAdvanceSum = allWorkers.sumOf { w ->
                            val txs = projectTransactions.filter { it.partyId == w.id || it.partyName == w.name }
                            val rec = txs.filter { it.type == "Money Out" }.sumOf { it.amount }
                            val paid = txs.filter { it.type == "Money In" }.sumOf { it.amount }
                            val d = rec - paid
                            if (d > 0) d else 0.0
                        }
                        val totalPendingSum = allWorkers.sumOf { w ->
                            val txs = projectTransactions.filter { it.partyId == w.id || it.partyName == w.name }
                            val rec = txs.filter { it.type == "Money Out" }.sumOf { it.amount }
                            val paid = txs.filter { it.type == "Money In" }.sumOf { it.amount }
                            val d = rec - paid
                            if (d < 0) -d else 0.0
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            // Crew member top details title line
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$teamCount Team Members",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (dark) TextPrimary else TextPrimaryLight
                                    )

                                    Text(
                                        text = "Manage Access >",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = NeonPurple,
                                        modifier = Modifier.clickable {
                                            Toast.makeText(context, "Opening crew profile management!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }

                            // Dynamic double grid totals (Advance Paid vs Pending to Pay)
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Total Advance Sum Card
                                    GlassCard(
                                        modifier = Modifier.weight(1f),
                                        darkTheme = dark,
                                        padding = 10.dp
                                    ) {
                                        Text(
                                            text = "ADVANCE PAID",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (dark) TextMuted else TextSecondaryLight
                                        )
                                        Text(
                                            text = formatIndianRupees(totalAdvanceSum),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = NeonGreen
                                        )
                                    }

                                    // Total Pending Sum Card
                                    GlassCard(
                                        modifier = Modifier.weight(1f),
                                        darkTheme = dark,
                                        padding = 10.dp
                                    ) {
                                        Text(
                                            text = "PENDING TO PAY",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (dark) TextMuted else TextSecondaryLight
                                        )
                                        Text(
                                            text = formatIndianRupees(totalPendingSum),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = NeonPink
                                        )
                                    }
                                }
                            }

                            // Rounded search field
                            item {
                                GlassTextField(
                                    value = partySearchQuery,
                                    onValueChange = { partySearchQuery = it },
                                    label = "Search Party",
                                    placeholder = "Type worker/party name...",
                                    darkTheme = dark,
                                    icon = Icons.Default.Search,
                                    focusedStroke = NeonCyan
                                )
                            }

                            // Filters active dropdown line & sorting arrows decoration
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Sorting clicker
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { activeFilterSelected = !activeFilterSelected }
                                    ) {
                                        Text(
                                            text = if (activeFilterSelected) "Sorting: Desc" else "Active",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (dark) TextSecondary else TextSecondaryLight
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = if (dark) TextSecondary else TextSecondaryLight,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = "Change Sort",
                                        tint = if (dark) TextSecondary else TextSecondaryLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Screen 3 worker/parties list items
                            val searchedWorkers = allWorkers.filter {
                                it.name.contains(partySearchQuery, ignoreCase = true) ||
                                it.role.contains(partySearchQuery, ignoreCase = true)
                            }

                            if (searchedWorkers.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No matching worker parties found.",
                                            color = if (dark) TextMuted else TextSecondaryLight,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            } else {
                                items(searchedWorkers) { worker ->
                                    // Calculate worker balances
                                    val txs = projectTransactions.filter { it.partyId == worker.id || it.partyName == worker.name }
                                    val rec = txs.filter { it.type == "Money Out" }.sumOf { it.amount }
                                    val paid = txs.filter { it.type == "Money In" }.sumOf { it.amount }
                                    val nodeDiff = rec - paid

                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        darkTheme = dark,
                                        padding = 10.dp,
                                        onClick = { selectedPartyDetail = worker }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Logo Initials initials badge
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .background(Color(worker.avatarColor), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = worker.name.take(2).uppercase(),
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                // Column details info
                                                Column {
                                                    Text(
                                                        text = worker.name,
                                                        color = if (dark) TextPrimary else TextPrimaryLight,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = worker.role,
                                                        color = if (dark) TextSecondary else TextSecondaryLight,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }

                                            // Column values balance
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = formatIndianRupees(nodeDiff.absoluteValue),
                                                    color = if (nodeDiff >= 0) NeonGreen else NeonPink,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = if (nodeDiff >= 0) "Advance Paid" else "Pending to Pay",
                                                    color = if (nodeDiff >= 0) NeonGreen else NeonPink,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Transaction" -> {
                        // ================= TRANSACTIONS LIST SUB-TAB =================
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Project Site Receipts (${projectTransactions.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dark) TextPrimary else TextPrimaryLight,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (projectTransactions.isEmpty()) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No financial transactions logged yet.",
                                        color = if (dark) TextMuted else TextSecondaryLight,
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(projectTransactions) { tx ->
                                        GlassCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            darkTheme = dark,
                                            padding = 10.dp,
                                            onClick = { selectedTxDetail = tx }
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = tx.partyName ?: "General Project",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (dark) TextPrimary else TextPrimaryLight
                                                    )
                                                    Text(
                                                        text = "${tx.category} • ${tx.date}",
                                                        fontSize = 11.sp,
                                                        color = if (dark) TextSecondary else TextSecondaryLight
                                                    )
                                                }

                                                Text(
                                                    text = (if (tx.type == "Money In") "+" else "-") + formatIndianRupees(tx.amount),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (tx.type == "Money In") NeonGreen else NeonPink
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Site" -> {
                        // ================= PROJECT METADATA SUB-TAB =================
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            GlassCard(modifier = Modifier.fillMaxWidth(), darkTheme = dark) {
                                Text(
                                    text = "Workspace Location",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dark) TextMuted else TextSecondaryLight
                                )
                                Text(
                                    text = currentProject?.location ?: "Treasure garden road site, India",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dark) TextPrimary else TextPrimaryLight
                                )
                            }

                            GlassCard(modifier = Modifier.fillMaxWidth(), darkTheme = dark) {
                                Text(
                                    text = "Estimated Budget",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dark) TextMuted else TextSecondaryLight
                                )
                                Text(
                                    text = formatIndianRupees(currentProject?.budget ?: 1500000.0),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGreen
                                )
                            }

                            GlassCard(modifier = Modifier.fillMaxWidth(), darkTheme = dark) {
                                Text(
                                    text = "Construction Phase Status",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dark) TextMuted else TextSecondaryLight
                                )
                                Text(
                                    text = currentProject?.status ?: "Active Phase 1",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonPurple
                                )
                            }
                        }
                    }

                    "Task" -> {
                        // ================= PROJECT TASKS SUB-TAB =================
                        val projTasks = remember(allTasks, activeProjId) {
                            if (activeProjId == null) emptyList()
                            else allTasks.filter { it.projectId == activeProjId }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Active Tasks (${projTasks.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dark) TextPrimary else TextPrimaryLight,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (projTasks.isEmpty()) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No crew tasks registered.",
                                        color = if (dark) TextMuted else TextSecondaryLight,
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(projTasks) { t ->
                                        GlassCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            darkTheme = dark,
                                            padding = 10.dp,
                                            onClick = { viewModel.cycleTaskStatus(t) }
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = t.title,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (dark) TextPrimary else TextPrimaryLight
                                                    )
                                                    Text(
                                                        text = "Assigned: ${t.assignee} • Priority: ${t.priority}",
                                                        fontSize = 11.sp,
                                                        color = if (dark) TextSecondary else TextSecondaryLight
                                                    )
                                                }

                                                GlassChip(
                                                    text = t.status,
                                                    selected = t.status == "Done",
                                                    onClick = { viewModel.cycleTaskStatus(t) },
                                                    darkTheme = dark,
                                                    activeColor = if (t.status == "Done") NeonGreen else NeonCyan
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Attendance" -> {
                        // ================= ORIGINAL ATTENDANCE LOGS SCREEN =================
                        // Original structured column perfectly preserved!
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            // Date navigator block
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    darkTheme = dark,
                                    padding = 10.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { navigateDay(-1) }) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack,
                                                contentDescription = "Previous Day",
                                                tint = if (dark) NeonCyan else Color(0xFF0284C7)
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable {
                                                viewModel.attendanceDate = "2026-05-26"
                                            }
                                        ) {
                                            Text(
                                                text = parsedDateString,
                                                color = if (dark) TextPrimary else TextPrimaryLight,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )

                                            if (activeDate != "2026-05-26") {
                                                Text(
                                                    text = "RETURN TO TODAY",
                                                    color = NeonPurple,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = "TODAY",
                                                    color = NeonCyan,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        }

                                        IconButton(onClick = { navigateDay(1) }) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = "Next Day",
                                                tint = if (dark) NeonCyan else Color(0xFF0284C7)
                                            )
                                        }
                                    }
                                }
                            }

                            // Live stats indicators
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    GlassCard(
                                        modifier = Modifier.weight(1f),
                                        darkTheme = dark,
                                        padding = 8.dp
                                    ) {
                                        Text("PRESENT", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("$presentCount Workers", color = if (dark) TextPrimary else TextPrimaryLight, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                    }

                                    GlassCard(
                                        modifier = Modifier.weight(1f),
                                        darkTheme = dark,
                                        padding = 8.dp
                                    ) {
                                        Text("ABSENT", color = NeonPink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("$absentCount Workers", color = if (dark) TextPrimary else TextPrimaryLight, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                    }

                                    GlassCard(
                                        modifier = Modifier.weight(1f),
                                        darkTheme = dark,
                                        padding = 8.dp
                                    ) {
                                        Text("OVERTIME", color = NeonPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("${totalOvertime} Hrs", color = if (dark) TextPrimary else TextPrimaryLight, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            // wages forecast block
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    darkTheme = dark,
                                    borderColor = if (dark) GlassBorderNeonCyan else null,
                                    padding = 10.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "ESTIMATED DAILY CREW PAYOUT",
                                                color = if (dark) TextMuted else TextSecondaryLight,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = cFormatter.format(dailyWages),
                                                color = NeonGreen,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.MonetizationOn,
                                            contentDescription = null,
                                            tint = NeonGreen,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }

                            // Crew log list
                            if (allWorkers.isEmpty()) {
                                item {
                                    GlassCard(modifier = Modifier.fillMaxWidth(), darkTheme = dark) {
                                        Text(
                                            text = "No workers registered. Click action button under sidebar to add.",
                                            color = if (dark) TextMuted else TextSecondaryLight,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                items(allWorkers) { worker ->
                                    val record = activeDateAttendance.find { it.workerId == worker.id }
                                    val statusText: String
                                    val statusColor: Color
                                    if (record != null) {
                                        statusText = when (record.status) {
                                            "Present" -> "Present"
                                            "Absent" -> "Absent"
                                            "Overtime" -> "OT: ${record.overtimeHours}h"
                                            else -> "Unmarked"
                                        }
                                        statusColor = when (record.status) {
                                            "Present" -> NeonGreen
                                            "Absent" -> NeonPink
                                            "Overtime" -> NeonPurple
                                            else -> if (dark) TextMuted else TextSecondaryLight
                                        }
                                    } else {
                                        statusText = "Unmarked"
                                        statusColor = if (dark) TextMuted else TextSecondaryLight
                                    }

                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        darkTheme = dark,
                                        padding = 10.dp,
                                        onClick = {
                                            selectedWorkerForAttendance = worker
                                            inputOvertimeHours = record?.overtimeHours?.toString() ?: "0.0"
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .background(Color(worker.avatarColor), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = worker.name.take(2).uppercase(),
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column {
                                                    Text(
                                                        text = worker.name,
                                                        color = if (dark) TextPrimary else TextPrimaryLight,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "${worker.role} • ${worker.shift} Shift • ${cFormatter.format(worker.wageRate)}/day",
                                                        color = if (dark) TextSecondary else TextSecondaryLight,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }

                                            GlassChip(
                                                text = statusText,
                                                selected = record != null,
                                                onClick = {
                                                    selectedWorkerForAttendance = worker
                                                    inputOvertimeHours = record?.overtimeHours?.toString() ?: "0.0"
                                                },
                                                darkTheme = dark,
                                                activeColor = statusColor
                                            )
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

    // ================= MODAL DIALOGS AND OVERLAYS =================

    // 1. Interactive Attendance Attendance Overtime Dialog
    val activeProj = currentProject
    val selectedWorker = selectedWorkerForAttendance
    if (selectedWorker != null && activeProj != null) {
        val record = activeDateAttendance.find { it.workerId == selectedWorker.id }

        GlassModalDialog(
            visible = true,
            onDismiss = { selectedWorkerForAttendance = null },
            title = "Mark Attendance: ${selectedWorker.name}",
            darkTheme = dark,
            glowColor = NeonPurple
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Logging status for $parsedDateString in ${activeProj.name}",
                    fontSize = 12.sp,
                    color = if (dark) TextSecondary else TextSecondaryLight
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(
                                if (record?.status == "Present") NeonGreen.copy(alpha = 0.25f)
                                else Color(0x1A9CA3AF)
                            )
                            .border(BorderStroke(1.dp, if (record?.status == "Present") NeonGreen else Color.Transparent), CircleShape)
                            .clickable {
                                viewModel.recordAttendance(selectedWorker.id, activeProj.id, activeDate, "Present")
                                selectedWorkerForAttendance = null
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("PRESENT", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(
                                if (record?.status == "Absent") NeonPink.copy(alpha = 0.25f)
                                else Color(0x1A9CA3AF)
                            )
                            .border(BorderStroke(1.dp, if (record?.status == "Absent") NeonPink else Color.Transparent), CircleShape)
                            .clickable {
                                viewModel.recordAttendance(selectedWorker.id, activeProj.id, activeDate, "Absent")
                                selectedWorkerForAttendance = null
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ABSENT", color = NeonPink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Divider(color = if (dark) GlassBorderDark else GlassBorderLight)

                Column {
                    Text(
                        text = "Or Log Overtime Shift (Hours)",
                        fontSize = 13.sp,
                        color = if (dark) TextSecondary else TextSecondaryLight,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            GlassTextField(
                                value = inputOvertimeHours,
                                onValueChange = { inputOvertimeHours = it },
                                label = "Overtime Hours",
                                isNumeric = true,
                                darkTheme = dark
                            )
                        }

                        GlassButton(
                            onClick = {
                                val hrs = inputOvertimeHours.toDoubleOrNull() ?: 0.0
                                viewModel.recordAttendance(
                                    selectedWorker.id,
                                    activeProj.id,
                                    activeDate,
                                    if (hrs > 0) "Overtime" else "Present",
                                    hrs
                                )
                                selectedWorkerForAttendance = null
                            },
                            darkTheme = dark,
                            glowColor = NeonPurple
                        ) {
                            Text("SAVE OT", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (record != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(Color(0x1AFF0000))
                            .clickable {
                                viewModel.recordAttendance(selectedWorker.id, activeProj.id, activeDate, "Clear")
                                selectedWorkerForAttendance = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CLEAR RECORD", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // 2. Add transaction record Dialog right in SiteScreen
    val activePartyForDialog = selectedPartyDetail
    if (showAddPartyTxDialog && activePartyForDialog != null && activeProj != null) {
        GlassModalDialog(
            visible = true,
            onDismiss = { showAddPartyTxDialog = false },
            title = "Record Payment from/to: ${activePartyForDialog.name}",
            darkTheme = dark,
            glowColor = if (partyTxType == "Money Out") NeonPink else NeonGreen
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Type selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Money Out", "Money In").forEach { type ->
                        val selected = partyTxType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) {
                                        if (type == "Money Out") NeonPink.copy(alpha = 0.25f) else NeonGreen.copy(alpha = 0.25f)
                                    } else Color.Transparent
                                )
                                .border(
                                    1.dp,
                                    if (selected) {
                                        if (type == "Money Out") NeonPink else NeonGreen
                                    } else Color.LightGray.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { partyTxType = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (type == "Money Out") "I PAID" else "I RECEIVED",
                                color = if (selected) {
                                    if (type == "Money Out") NeonPink else NeonGreen
                                } else {
                                    if (dark) TextSecondary else TextSecondaryLight
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                GlassTextField(
                    value = partyTxAmount,
                    onValueChange = { partyTxAmount = it },
                    label = "Transaction Amount (₹)",
                    isNumeric = true,
                    placeholder = "e.g. 5000",
                    darkTheme = dark
                )

                GlassTextField(
                    value = partyTxDesc,
                    onValueChange = { partyTxDesc = it },
                    label = "Purpose / Description",
                    placeholder = "e.g. Weekly advance settling",
                    darkTheme = dark
                )

                GlassTextField(
                    value = partyTxDate,
                    onValueChange = { partyTxDate = it },
                    label = "Transaction Date",
                    placeholder = "YYYY-MM-DD",
                    darkTheme = dark
                )

                // Category chips
                Text("Category Code", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Labor", "Material", "Equipment", "Client Advance", "Other").forEach { cat ->
                        val selected = partyTxCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) NeonPurple.copy(alpha = 0.2f) else Color.Transparent)
                                .border(1.dp, if (selected) NeonPurple else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { partyTxCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cat, color = if (selected) NeonPurple else if (dark) TextSecondary else TextSecondaryLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Payment Method chips
                Text("Payment Method", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Cash", "Bank Transfer", "Cheque").forEach { method ->
                        val selected = partyTxMethod == method
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .border(1.dp, if (selected) NeonCyan else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { partyTxMethod = method }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(method, color = if (selected) NeonCyan else if (dark) TextSecondary else TextSecondaryLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        onClick = { showAddPartyTxDialog = false },
                        darkTheme = dark,
                        glowColor = NeonPink,
                        outlineMode = true,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", fontWeight = FontWeight.Bold)
                    }

                    GlassButton(
                        onClick = {
                            val amt = partyTxAmount.toDoubleOrNull() ?: 0.0
                            if (amt > 0.0) {
                                viewModel.addTransaction(
                                    projectId = activeProj.id,
                                    type = partyTxType,
                                    amount = amt,
                                    category = partyTxCategory,
                                    description = partyTxDesc,
                                    date = partyTxDate,
                                    partyId = activePartyForDialog.id,
                                    partyName = activePartyForDialog.name,
                                    reference = "SiteScreen",
                                    paymentMethod = partyTxMethod
                                )
                                showAddPartyTxDialog = false
                                Toast.makeText(context, "Party transaction recorded successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please enter a valid amount!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        darkTheme = dark,
                        glowColor = NeonGreen,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SAVE RECORD", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 3. Mock PDF Document Overlay modal
    if (showPdfPreviewDialog) {
        val sampleAmt = selectedTxDetail?.amount ?: 1000.0
        val wordName = selectedTxDetail?.partyName ?: selectedPartyDetail?.name ?: "Tejas Harane"
        val wordDate = selectedTxDetail?.date ?: "2026-05-27"

        GlassModalDialog(
            visible = true,
            onDismiss = { showPdfPreviewDialog = false },
            title = "PDF Premium Viewer",
            darkTheme = dark,
            glowColor = NeonCyan
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Simulated paper sheet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("CONSTRUCT PRO INC.", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Black)
                        Text("OFFICIAL RECEIPT DEED", fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color.LightGray)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rec. ID: TX-${selectedTxDetail?.id ?: 1024}", fontSize = 9.sp, color = Color.DarkGray)
                            Text("Date: $wordDate", fontSize = 9.sp, color = Color.DarkGray)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text("Paid To/From:", fontSize = 9.sp, color = Color.Gray)
                        Text(wordName, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Principal Sum Received:", fontSize = 9.sp, color = Color.Gray)
                        Text(formatIndianRupees(sampleAmt), fontSize = 22.sp, color = Color.DarkGray, fontWeight = FontWeight.Black)

                        Spacer(modifier = Modifier.height(18.dp))
                        Divider(color = Color.LightGray)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Status: Digitally Sealed & Verified By Crew", fontSize = 9.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassButton(
                        onClick = { showPdfPreviewDialog = false },
                        darkTheme = dark,
                        glowColor = NeonPink,
                        outlineMode = true,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CLOSE DOCUMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    GlassButton(
                        onClick = {
                            showPdfPreviewDialog = false
                            Toast.makeText(context, "PDF Deed downloaded successfully to local files!", Toast.LENGTH_LONG).show()
                        },
                        darkTheme = dark,
                        glowColor = NeonGreen,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("DOWNLOAD PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailTextRow(
    label: String,
    value: String,
    darkTheme: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (darkTheme) TextSecondary else TextSecondaryLight,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = value,
            fontSize = 13.sp,
            color = if (darkTheme) TextPrimary else TextPrimaryLight,
            fontWeight = FontWeight.Bold
        )
    }
}
