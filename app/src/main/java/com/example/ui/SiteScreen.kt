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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

// ─────────────────────────────────────────────
// PREMIUM THEME COMPATIBILITY MAPPINGS
// ─────────────────────────────────────────────
private val PremiumNavy        = DarkBg0
private val PremiumDeepBlue    = DarkBg2
private val PremiumCard        = DarkBg1
private val PremiumCardLight   = LightBg2
private val PremiumBorder      = GlassBorderDark
private val PremiumBorderLight = GlassBorderLight

private val AquaGlow @Composable get() = MaterialTheme.colorScheme.ext.accentPrimary
private val VioletGlow @Composable get() = MaterialTheme.colorScheme.ext.accentSecondary
private val EmeraldGlow @Composable get() = MaterialTheme.colorScheme.ext.accentSuccess
private val RoseGlow @Composable get() = MaterialTheme.colorScheme.ext.accentDanger
private val AmberGlow @Composable get() = MaterialTheme.colorScheme.ext.accentWarning
private val IndigoGlow @Composable get() = if (MaterialTheme.colorScheme.ext.isDark) NeonBlue else LightBlue

private val GradientAqua @Composable get() = Brush.linearGradient(
    if (MaterialTheme.colorScheme.ext.isDark) listOf(NeonCyan, NeonCyanDim)
    else listOf(LightCyan, Color(0xFF0284C7))
)
private val GradientViolet @Composable get() = Brush.linearGradient(
    if (MaterialTheme.colorScheme.ext.isDark) listOf(NeonPurple, NeonPurpleDim)
    else listOf(LightPurple, Color(0xFF6D28D9))
)
private val GradientEmerald @Composable get() = Brush.linearGradient(
    if (MaterialTheme.colorScheme.ext.isDark) listOf(NeonGreen, NeonGreenDim)
    else listOf(LightGreen, Color(0xFF047857))
)
private val GradientRose @Composable get() = Brush.linearGradient(
    if (MaterialTheme.colorScheme.ext.isDark) listOf(NeonPink, NeonPinkDim)
    else listOf(LightPink, Color(0xFFBE185D))
)
private val GradientAmber @Composable get() = Brush.linearGradient(
    if (MaterialTheme.colorScheme.ext.isDark) listOf(NeonAmber, NeonAmberDim)
    else listOf(LightAmber, Color(0xFFB45309))
)
private val GradientPremium @Composable get() = Brush.linearGradient(
    if (MaterialTheme.colorScheme.ext.isDark) listOf(NeonCyan, NeonPurple, NeonPink)
    else listOf(LightCyan, LightPurple, LightPink)
)

// ─────────────────────────────────────────────
// MAIN COMPOSABLE
// ─────────────────────────────────────────────
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

    val activeDate = viewModel.attendanceDate
    var selectedWorkerForAttendance by remember { mutableStateOf<Worker?>(null) }
    var inputOvertimeHours by remember { mutableStateOf("0.0") }

    val formatter      = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val displayFormat  = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US) }
    val cFormatter     = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    var selectedPartyDetail by remember { mutableStateOf<Worker?>(null) }
    var selectedTxDetail    by remember { mutableStateOf<Transaction?>(null) }

    val activeSiteTab = viewModel.activeSiteTab
    val tabs = listOf("Party", "Transaction", "Site", "Task", "Attendance")

    var partySearchQuery     by remember { mutableStateOf("") }
    var activeFilterSelected by remember { mutableStateOf(false) }

    var txSearchQuery          by remember { mutableStateOf("") }
    var txDatePreset           by remember { mutableStateOf("All") }
    var txStartDate            by remember { mutableStateOf("") }
    var txEndDate              by remember { mutableStateOf("") }
    var showTxDateFilterDialog by remember { mutableStateOf(false) }

    var partyDetailTimeFilter  by remember { mutableStateOf("All Time") }
    var partyDetailHistoryTab  by remember { mutableStateOf("All") }

    var showAddPartyTxDialog by remember { mutableStateOf(false) }
    var partyTxType          by remember { mutableStateOf("Money Out") }
    var partyTxAmount        by remember { mutableStateOf("") }
    var partyTxCategory      by remember { mutableStateOf("Labour") }
    var partyTxDesc          by remember { mutableStateOf("") }
    var partyTxMethod        by remember { mutableStateOf("Cash") }
    var partyTxDate          by remember { mutableStateOf("2026-05-27") }
    var showPdfPreviewDialog by remember { mutableStateOf(false) }

    val activeProjId = currentProject?.id
    val projectTransactions = remember(allTransactions, activeProjId) {
        if (activeProjId == null) emptyList()
        else allTransactions.filter { it.projectId == activeProjId }
    }

    val filteredWorkers = remember(allWorkers, partySearchQuery) {
        allWorkers.filter {
            it.name.contains(partySearchQuery, ignoreCase = true) ||
            it.role.contains(partySearchQuery, ignoreCase = true)
        }
    }

    val filteredTransactions = remember(projectTransactions, txSearchQuery, txDatePreset, txStartDate, txEndDate) {
        var list = projectTransactions
        if (txSearchQuery.isNotEmpty()) {
            list = list.filter { tx ->
                (tx.description?.contains(txSearchQuery, ignoreCase = true) == true) ||
                (tx.category?.contains(txSearchQuery, ignoreCase = true) == true) ||
                (tx.partyName?.contains(txSearchQuery, ignoreCase = true) == true) ||
                (tx.reference?.contains(txSearchQuery, ignoreCase = true) == true) ||
                (tx.paymentMethod?.contains(txSearchQuery, ignoreCase = true) == true) ||
                tx.amount.toString().contains(txSearchQuery)
            }
        }
        val bounds = if (txDatePreset == "Custom") {
            if (txStartDate.isNotEmpty() && txEndDate.isNotEmpty()) Pair(txStartDate, txEndDate) else null
        } else {
            getPresetDateRange(txDatePreset)
        }
        if (bounds != null) {
            val (start, end) = bounds
            list = list.filter { it.date >= start && it.date <= end }
        }
        list
    }

    val filteredPartyTransactions = remember(projectTransactions, selectedPartyDetail, partyDetailHistoryTab, partyDetailTimeFilter) {
        val party = selectedPartyDetail
        if (party == null) emptyList()
        else {
            val matchedTxs = projectTransactions.filter { it.partyId == party.id || it.partyName == party.name }
            val typeFiltered = when (partyDetailHistoryTab) {
                "Received" -> matchedTxs.filter { it.type == "Money In" }
                "Paid"     -> matchedTxs.filter { it.type == "Money Out" }
                else       -> matchedTxs
            }
            if (partyDetailTimeFilter == "All Time") typeFiltered
            else {
                val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val cal = java.util.Calendar.getInstance()
                val today = cal.time
                cal.time = today
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                val startOfToday = cal.timeInMillis

                val filterLimitTime = when (partyDetailTimeFilter) {
                    "Today" -> startOfToday
                    "Weekly" -> { cal.add(java.util.Calendar.DAY_OF_YEAR, -7); cal.timeInMillis }
                    "2 Weeks" -> { cal.add(java.util.Calendar.DAY_OF_YEAR, -14); cal.timeInMillis }
                    "3 Weeks" -> { cal.add(java.util.Calendar.DAY_OF_YEAR, -21); cal.timeInMillis }
                    "Monthly" -> { cal.add(java.util.Calendar.MONTH, -1); cal.timeInMillis }
                    else -> 0L
                }

                typeFiltered.filter { tx ->
                    try {
                        val d = format.parse(tx.date)
                        if (d != null) {
                            if (partyDetailTimeFilter == "Today") {
                                d.time >= startOfToday
                            } else {
                                d.time >= filterLimitTime
                            }
                        } else true
                    } catch (e: Exception) { true }
                }
            }
        }
    }

    val activeDateRangeText = remember(txDatePreset, txStartDate, txEndDate) {
        if (txDatePreset == "Custom") {
            if (txStartDate.isNotEmpty() && txEndDate.isNotEmpty()) {
                "$txStartDate to $txEndDate"
            } else {
                "Custom Range"
            }
        } else {
            txDatePreset
        }
    }


    val navigateDay = { days: Int ->
        val cal = Calendar.getInstance()
        cal.time = formatter.parse(activeDate) ?: Date()
        cal.add(Calendar.DATE, days)
        viewModel.attendanceDate = formatter.format(cal.time)
    }

    val parsedDateString = remember(activeDate) {
        try { displayFormat.format(formatter.parse(activeDate) ?: Date()) }
        catch (e: Exception) { activeDate }
    }

    val activeDateAttendance = remember(allAttendance, activeDate, currentProject) {
        val projId = currentProject?.id ?: return@remember emptyList()
        allAttendance.filter { it.date == activeDate && it.projectId == projId }
    }

    val presentCount  = activeDateAttendance.count { it.status == "Present" || it.status == "Overtime" }
    val absentCount   = activeDateAttendance.count { it.status == "Absent" }
    val totalOvertime = activeDateAttendance.sumOf { it.overtimeHours }

    val dailyWages = remember(activeDateAttendance, allWorkers) {
        activeDateAttendance.sumOf { att ->
            val w = allWorkers.find { it.id == att.workerId } ?: return@sumOf 0.0
            val base = if (att.status == "Absent") 0.0 else w.wageRate
            val otCost = att.overtimeHours * (w.wageRate / 8.0) * 1.5
            base + otCost
        }
    }

    val bgBrush = if (dark)
        Brush.verticalGradient(listOf(PremiumNavy, Color(0xFF080C18), PremiumDeepBlue))
    else
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFFAFBFF), Color(0xFFEEF2FF)))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Decorative ambient glow blobs
        if (dark) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset((-60).dp, (-40).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(AquaGlow.copy(alpha = 0.06f), Color.Transparent)
                        ), CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.TopEnd)
                    .offset(60.dp, 80.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(VioletGlow.copy(alpha = 0.07f), Color.Transparent)
                        ), CircleShape
                    )
            )
        }

        // ── PAGE ROUTING ──
        AnimatedContent(
            targetState = Triple(selectedTxDetail, selectedPartyDetail, Unit),
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                slideOutHorizontally { -it } + fadeOut()
            },
            label = "pageRoute"
        ) { (txDetail, partyDetail, _) ->
            when {
                txDetail != null -> {
                    val freshTx = projectTransactions.find { it.id == txDetail.id } ?: txDetail
                    PremiumPaymentDetailPage(
                        tx = freshTx,
                        dark = dark,
                        currentProject = currentProject,
                        viewModel = viewModel,
                        context = context,
                        onBack = { selectedTxDetail = null },
                        onShowPdf = { showPdfPreviewDialog = true }
                    )
                }

                partyDetail != null -> PremiumPartyDetailPage(
                    worker = partyDetail,
                    dark = dark,
                    currentProject = currentProject,
                    projectTransactions = projectTransactions,
                    context = context,
                    onBack = { selectedPartyDetail = null },
                    onSelectTx = { selectedTxDetail = it },
                    onIPaid = {
                        partyTxType = "Money Out"; partyTxCategory = "Labour"
                        partyTxAmount = ""; partyTxDesc = "Crew payment"
                        showAddPartyTxDialog = true
                    },
                    onIReceived = {
                        partyTxType = "Money In"; partyTxCategory = "Client Advance"
                        partyTxAmount = ""; partyTxDesc = "Received funds"
                        showAddPartyTxDialog = true
                    },
                    onAddTx = { viewModel.showTransactionDialog = true },
                    onShowPdf = { showPdfPreviewDialog = true },
                    onHistoryTabChange = { partyDetailHistoryTab = it },
                    onTimeFilterChange = { partyDetailTimeFilter = it }
                )

                else -> PremiumMainPage(
                    dark = dark,
                    viewModel = viewModel,
                    currentProject = currentProject,
                    allWorkers = allWorkers,
                    projectTransactions = projectTransactions,
                    allTasks = allTasks,
                    activeProjId = activeProjId,
                    activeSiteTab = activeSiteTab,
                    tabs = tabs,
                    partySearchQuery = partySearchQuery,
                    onPartySearchChange = { partySearchQuery = it },
                    activeFilterSelected = activeFilterSelected,
                    onFilterToggle = { activeFilterSelected = !activeFilterSelected },
                    activeDateAttendance = activeDateAttendance,
                    presentCount = presentCount,
                    absentCount = absentCount,
                    totalOvertime = totalOvertime,
                    dailyWages = dailyWages,
                    parsedDateString = parsedDateString,
                    activeDate = activeDate,
                    navigateDay = navigateDay,
                    cFormatter = cFormatter,
                    context = context,
                    onSelectParty = { selectedPartyDetail = it },
                    onSelectTx = { selectedTxDetail = it },
                    onSelectWorkerAttendance = {
                        selectedWorkerForAttendance = it
                        val rec = activeDateAttendance.find { r -> r.workerId == it.id }
                        inputOvertimeHours = rec?.overtimeHours?.toString() ?: "0.0"
                    },
                    onShowPdf = { showPdfPreviewDialog = true },
                    filteredTransactions = filteredTransactions,
                    txSearchQuery = txSearchQuery,
                    onTxSearchQueryChange = { txSearchQuery = it },
                    txDatePreset = txDatePreset,
                    onOpenDateFilter = { showTxDateFilterDialog = true }
                )
            }
        }
    }

    // ── DIALOGS ──
    val activeProj   = currentProject
    val selectedWork = selectedWorkerForAttendance
    if (selectedWork != null && activeProj != null) {
        val record = activeDateAttendance.find { it.workerId == selectedWork.id }
        PremiumAttendanceDialog(
            worker = selectedWork,
            record = record,
            dark = dark,
            parsedDateString = parsedDateString,
            activeProj = activeProj,
            activeDate = activeDate,
            inputOvertimeHours = inputOvertimeHours,
            onOtChange = { inputOvertimeHours = it },
            onPresent = {
                viewModel.recordAttendance(selectedWork.id, activeProj.id, activeDate, "Present")
                selectedWorkerForAttendance = null
            },
            onAbsent = {
                viewModel.recordAttendance(selectedWork.id, activeProj.id, activeDate, "Absent")
                selectedWorkerForAttendance = null
            },
            onSaveOt = {
                val hrs = inputOvertimeHours.toDoubleOrNull() ?: 0.0
                viewModel.recordAttendance(
                    selectedWork.id, activeProj.id, activeDate,
                    if (hrs > 0) "Overtime" else "Present", hrs
                )
                selectedWorkerForAttendance = null
            },
            onClear = {
                viewModel.recordAttendance(selectedWork.id, activeProj.id, activeDate, "Clear")
                selectedWorkerForAttendance = null
            },
            onDismiss = { selectedWorkerForAttendance = null }
        )
    }

    val activePartyForDialog = selectedPartyDetail
    if (showAddPartyTxDialog && activePartyForDialog != null && activeProj != null) {
        PremiumAddTransactionDialog(
            dark = dark,
            partyTxType = partyTxType,
            partyTxAmount = partyTxAmount,
            partyTxDesc = partyTxDesc,
            partyTxDate = partyTxDate,
            partyTxCategory = partyTxCategory,
            partyTxMethod = partyTxMethod,
            onTypeChange = { partyTxType = it },
            onAmountChange = { partyTxAmount = it },
            onDescChange = { partyTxDesc = it },
            onDateChange = { partyTxDate = it },
            onCategoryChange = { partyTxCategory = it },
            onMethodChange = { partyTxMethod = it },
            onDismiss = { showAddPartyTxDialog = false },
            onSave = {
                val amt = partyTxAmount.toDoubleOrNull() ?: 0.0
                if (amt > 0.0) {
                    viewModel.addTransaction(
                        projectId = activeProj.id, type = partyTxType, amount = amt,
                        category = partyTxCategory, description = partyTxDesc,
                        date = partyTxDate, partyId = activePartyForDialog.id,
                        partyName = activePartyForDialog.name, reference = "SiteScreen",
                        paymentMethod = partyTxMethod
                    )
                    showAddPartyTxDialog = false
                    Toast.makeText(context, "Transaction recorded!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Enter a valid amount!", Toast.LENGTH_SHORT).show()
                }
            },
            allWorkers = allWorkers,
            selectedParty = activePartyForDialog,
            viewModel = viewModel
        )
    }

    if (showTxDateFilterDialog) {
        ReportFilterDialog(
            visible = showTxDateFilterDialog,
            onDismiss = { showTxDateFilterDialog = false },
            dark = dark,
            preset = txDatePreset,
            onPresetChange = { txDatePreset = it },
            startDate = txStartDate,
            onStartDateChange = { txStartDate = it },
            endDate = txEndDate,
            onEndDateChange = { txEndDate = it },
            onApply = { showTxDateFilterDialog = false }
        )
    }

    if (showPdfPreviewDialog) {
        val txsForReport = if (selectedPartyDetail != null) {
            filteredPartyTransactions
        } else if (activeSiteTab == "Transaction") {
            filteredTransactions
        } else {
            projectTransactions
        }

        val dateRangeTextForReport = if (selectedPartyDetail != null) {
            partyDetailTimeFilter
        } else if (activeSiteTab == "Transaction") {
            activeDateRangeText
        } else {
            "All Time"
        }

        val workersForReport = if (activeSiteTab == "Party") {
            filteredWorkers
        } else {
            allWorkers
        }

        PremiumReportPreviewDialog(
            dark = dark,
            selectedTxDetail = selectedTxDetail,
            selectedPartyDetail = selectedPartyDetail,
            projectTransactions = txsForReport,
            allWorkers = workersForReport,
            currentProject = currentProject,
            viewModel = viewModel,
            dateRangeText = dateRangeTextForReport,
            onDismiss = { showPdfPreviewDialog = false }
        )
    }
}

// ─────────────────────────────────────────────
// PAGE 1 — MAIN MULTI-TAB
// ─────────────────────────────────────────────
@Composable
private fun PremiumMainPage(
    dark: Boolean,
    viewModel: MainViewModel,
    currentProject: Project?,
    allWorkers: List<Worker>,
    projectTransactions: List<Transaction>,
    allTasks: List<Task>,
    activeProjId: Int?,
    activeSiteTab: String,
    tabs: List<String>,
    partySearchQuery: String,
    onPartySearchChange: (String) -> Unit,
    activeFilterSelected: Boolean,
    onFilterToggle: () -> Unit,
    activeDateAttendance: List<Attendance>,
    presentCount: Int,
    absentCount: Int,
    totalOvertime: Double,
    dailyWages: Double,
    parsedDateString: String,
    activeDate: String,
    navigateDay: (Int) -> Unit,
    cFormatter: NumberFormat,
    context: Context,
    onSelectParty: (Worker) -> Unit,
    onSelectTx: (Transaction) -> Unit,
    onSelectWorkerAttendance: (Worker) -> Unit,
    onShowPdf: () -> Unit,
    filteredTransactions: List<Transaction>,
    txSearchQuery: String,
    onTxSearchQueryChange: (String) -> Unit,
    txDatePreset: String,
    onOpenDateFilter: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // ── PREMIUM HEADER ──
        PremiumSiteHeader(
            dark = dark,
            currentProject = currentProject,
            viewModel = viewModel,
            context = context,
            onShowPdf = onShowPdf
        )

        // ── PREMIUM TAB BAR ──
        PremiumTabBar(
            dark = dark,
            tabs = tabs,
            activeSiteTab = activeSiteTab,
            onTabSelected = { viewModel.activeSiteTab = it }
        )

        // ── TAB CONTENT ──
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = activeSiteTab,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                },
                label = "tabContent"
            ) { tab ->
                when (tab) {
                    "Party" -> PartyTab(
                        dark = dark, allWorkers = allWorkers,
                        projectTransactions = projectTransactions,
                        partySearchQuery = partySearchQuery,
                        onPartySearchChange = onPartySearchChange,
                        activeFilterSelected = activeFilterSelected,
                        onFilterToggle = onFilterToggle,
                        context = context,
                        onSelectParty = onSelectParty
                    )
                    "Transaction" -> TransactionTab(
                        dark = dark,
                        viewModel = viewModel,
                        projectTransactions = filteredTransactions,
                        txSearchQuery = txSearchQuery,
                        onTxSearchQueryChange = onTxSearchQueryChange,
                        txDatePreset = txDatePreset,
                        onOpenDateFilter = onOpenDateFilter,
                        onSelectTx = onSelectTx
                    )
                    "Site" -> SiteInfoTab(
                        dark = dark,
                        currentProject = currentProject
                    )
                    "Task" -> TaskTab(
                        dark = dark, allTasks = allTasks,
                        activeProjId = activeProjId,
                        viewModel = viewModel
                    )
                    "Attendance" -> AttendanceTab(
                        dark = dark, allWorkers = allWorkers,
                        activeDateAttendance = activeDateAttendance,
                        presentCount = presentCount, absentCount = absentCount,
                        totalOvertime = totalOvertime, dailyWages = dailyWages,
                        parsedDateString = parsedDateString, activeDate = activeDate,
                        navigateDay = navigateDay, cFormatter = cFormatter,
                        onSelectWorker = onSelectWorkerAttendance
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// PREMIUM SITE HEADER
// ─────────────────────────────────────────────
@Composable
private fun PremiumSiteHeader(
    dark: Boolean,
    currentProject: Project?,
    viewModel: MainViewModel,
    context: Context,
    onShowPdf: () -> Unit
) {
    val headerBg = if (dark)
        Brush.horizontalGradient(listOf(Color(0xFF0D1B3E), Color(0xFF111827)))
    else
        Brush.horizontalGradient(listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFF)))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerBg)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Bottom border glow line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, AquaGlow.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = { viewModel.currentScreen = AppScreen.Dashboard },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to Dashboard",
                        tint = if (dark) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // Live indicator dot
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsatingDot(color = EmeraldGlow)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE SITE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = EmeraldGlow
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    // Project name – capped to 1 line with ellipsis
                    var dropdownExpanded by remember { mutableStateOf(false) }
                    val projects by viewModel.projects.collectAsState()

                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { dropdownExpanded = true }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = currentProject?.name ?: "Select Project",
                                style = TextStyle(
                                    brush = GradientAqua,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.3).sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch Project",
                                tint = if (dark) Color.White else Color(0xFF1E293B),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(if (dark) Color(0xFF0F172A) else Color.White)
                        ) {
                            projects.forEach { proj ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = proj.name,
                                            color = if (dark) Color.White else Color.Black,
                                            fontWeight = if (proj.id == currentProject?.id) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectedProjectId = proj.id
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Text(
                        text = "Site Operations",
                        color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp,
                        maxLines = 1
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PremiumIconBtn(
                    icon = if (viewModel.darkThemeEnabled) Icons.Default.LightMode else Icons.Default.DarkMode,
                    tint = if (dark) AquaGlow else VioletGlow,
                    dark = dark,
                    onClick = { viewModel.darkThemeEnabled = !viewModel.darkThemeEnabled }
                )
                PremiumIconBtn(
                    icon = Icons.Default.PictureAsPdf,
                    tint = RoseGlow,
                    dark = dark,
                    onClick = onShowPdf
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// PREMIUM TAB BAR
// ─────────────────────────────────────────────
@Composable
private fun PremiumTabBar(
    dark: Boolean,
    tabs: List<String>,
    activeSiteTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabIcons = mapOf(
        "Party"       to Icons.Default.Group,
        "Transaction" to Icons.Default.AccountBalance,
        "Site"        to Icons.Default.LocationOn,
        "Task"        to Icons.Default.Assignment,
        "Attendance"  to Icons.Default.CalendarToday
    )
    val tabColors = mapOf(
        "Party"       to VioletGlow,
        "Transaction" to EmeraldGlow,
        "Site"        to AquaGlow,
        "Task"        to AmberGlow,
        "Attendance"  to IndigoGlow
    )

    val scrollState = rememberScrollState()
    val tabBg = if (dark) Color(0xFF0D1B3E) else Color(0xFFEEF2FF)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(tabBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { tab ->
                val selected = activeSiteTab == tab
                val accentColor = tabColors[tab] ?: AquaGlow
                val icon = tabIcons[tab] ?: Icons.Default.Circle

                val animAlpha by animateFloatAsState(
                    targetValue = if (selected) 1f else 0f,
                    animationSpec = tween(250),
                    label = "tabAlpha"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selected)
                                Brush.linearGradient(
                                    listOf(accentColor.copy(alpha = 0.18f), accentColor.copy(alpha = 0.08f))
                                )
                            else
                                Brush.linearGradient(
                                    listOf(
                                        if (dark) Color(0xFF1A2744).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.7f),
                                        if (dark) Color(0xFF111827).copy(alpha = 0.4f) else Color(0xFFF1F5FF).copy(alpha = 0.5f)
                                    )
                                )
                        )
                        .border(
                            width = if (selected) 1.2.dp else 1.dp,
                            brush = if (selected)
                                Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.4f)))
                            else
                                Brush.linearGradient(
                                    listOf(
                                        if (dark) Color(0xFF1E2D4A) else Color(0xFFDDE4F0),
                                        if (dark) Color(0xFF1E2D4A) else Color(0xFFDDE4F0)
                                    )
                                ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab,
                            tint = if (selected) accentColor else
                                if (dark) Color(0xFF475569) else Color(0xFF94A3B8),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = tab,
                            color = if (selected) accentColor else
                                if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.2.sp
                        )
                    }
                }
            }
        }

        // Bottom gradient line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    if (dark)
                        Brush.horizontalGradient(listOf(Color(0xFF1E2D4A), Color(0xFF0D1B3E)))
                    else
                        Brush.horizontalGradient(listOf(Color(0xFFDDE4F0), Color(0xFFEEF2FF)))
                )
        )
    }
}

// ─────────────────────────────────────────────
// TAB: PARTY
// ─────────────────────────────────────────────
@Composable
private fun PartyTab(
    dark: Boolean,
    allWorkers: List<Worker>,
    projectTransactions: List<Transaction>,
    partySearchQuery: String,
    onPartySearchChange: (String) -> Unit,
    activeFilterSelected: Boolean,
    onFilterToggle: () -> Unit,
    context: Context,
    onSelectParty: (Worker) -> Unit
) {
    val totalAdvance = allWorkers.sumOf { w ->
        val txs = projectTransactions.filter { it.partyId == w.id || it.partyName == w.name }
        val d = txs.filter { it.type == "Money Out" }.sumOf { it.amount } -
                txs.filter { it.type == "Money In" }.sumOf { it.amount }
        if (d > 0) d else 0.0
    }
    val totalPending = allWorkers.sumOf { w ->
        val txs = projectTransactions.filter { it.partyId == w.id || it.partyName == w.name }
        val d = txs.filter { it.type == "Money Out" }.sumOf { it.amount } -
                txs.filter { it.type == "Money In" }.sumOf { it.amount }
        if (d < 0) -d else 0.0
    }
    val searchedWorkers = allWorkers.filter {
        it.name.contains(partySearchQuery, ignoreCase = true) ||
        it.role.contains(partySearchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PremiumStatCard(
                    modifier = Modifier.weight(1f),
                    dark = dark,
                    label = "PAID",
                    value = formatIndianRupees(totalAdvance),
                    valueColor = EmeraldGlow,
                    icon = Icons.Default.TrendingUp,
                    gradient = Brush.linearGradient(
                        listOf(EmeraldGlow.copy(alpha = 0.15f), EmeraldGlow.copy(alpha = 0.05f))
                    ),
                    borderColor = EmeraldGlow.copy(alpha = 0.3f)
                )
                PremiumStatCard(
                    modifier = Modifier.weight(1f),
                    dark = dark,
                    label = "Received",
                    value = formatIndianRupees(totalPending),
                    valueColor = RoseGlow,
                    icon = Icons.Default.TrendingDown,
                    gradient = Brush.linearGradient(
                        listOf(RoseGlow.copy(alpha = 0.15f), RoseGlow.copy(alpha = 0.05f))
                    ),
                    borderColor = RoseGlow.copy(alpha = 0.3f)
                )
            }
        }

        // Team Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${allWorkers.size} Team Members",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B)
                    )
                    Text(
                        text = "Tap member for balance details",
                        fontSize = 11.sp,
                        color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(VioletGlow.copy(alpha = 0.12f))
                        .border(1.dp, VioletGlow.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .clickable {
                            Toast.makeText(context, "Crew management opened!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Manage →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletGlow
                    )
                }
            }
        }

        // Search Bar
        item {
            PremiumSearchBar(
                value = partySearchQuery,
                onValueChange = onPartySearchChange,
                dark = dark,
                placeholder = "Search worker or role..."
            )
        }

        // Filter Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onFilterToggle)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = AquaGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (activeFilterSelected) "Sorted: Desc" else "Filter & Sort",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaGlow
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AquaGlow,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = "${searchedWorkers.size} results",
                    fontSize = 11.sp,
                    color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8)
                )
            }
        }

        if (searchedWorkers.isEmpty()) {
            item { PremiumEmptyState(dark = dark, message = "No matching workers found") }
        } else {
            items(searchedWorkers, key = { it.id }) { worker ->
                val txs = projectTransactions.filter { it.partyId == worker.id || it.partyName == worker.name }
                val diff = txs.filter { it.type == "Money Out" }.sumOf { it.amount } -
                           txs.filter { it.type == "Money In" }.sumOf { it.amount }
                PremiumPartyCard(
                    worker = worker,
                    diff = diff,
                    dark = dark,
                    onClick = { onSelectParty(worker) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ─────────────────────────────────────────────
// TAB: TRANSACTION
// ─────────────────────────────────────────────
@Composable
private fun TransactionTab(
    dark: Boolean,
    viewModel: MainViewModel,
    projectTransactions: List<Transaction>,
    txSearchQuery: String,
    onTxSearchQueryChange: (String) -> Unit,
    txDatePreset: String,
    onOpenDateFilter: () -> Unit,
    onSelectTx: (Transaction) -> Unit
) {
    val totalIn  = projectTransactions.filter { it.type == "Money In" }.sumOf { it.amount }
    val totalOut = projectTransactions.filter { it.type == "Money Out" }.sumOf { it.amount }
    val net      = totalIn - totalOut

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Summary Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (dark)
                          Brush.horizontalGradient(listOf(Color(0xFF0D1B3E), Color(0xFF111827)))
                        else
                          Brush.horizontalGradient(listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFF)))
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniFinanceStat(
                        dark = dark, label = "IN",
                        value = formatIndianRupees(totalIn),
                        color = EmeraldGlow, modifier = Modifier.weight(1f)
                    )
                    MiniFinanceStat(
                        dark = dark, label = "OUT",
                        value = formatIndianRupees(totalOut),
                        color = RoseGlow, modifier = Modifier.weight(1f)
                    )
                    MiniFinanceStat(
                        dark = dark, label = "NET",
                        value = formatIndianRupees(net.absoluteValue),
                        color = if (net >= 0) EmeraldGlow else RoseGlow,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search and filter row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    PremiumSearchBar(
                        value = txSearchQuery,
                        onValueChange = onTxSearchQueryChange,
                        dark = dark,
                        placeholder = "Search description, party..."
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (dark) Color(0xFF1E2D4A).copy(alpha = 0.6f) else Color(0xFFE2E8F4))
                        .border(1.dp, if (dark) Color(0xFF2D3F5E) else Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                        .clickable { onOpenDateFilter() }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date Filter",
                        tint = EmeraldGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (txDatePreset == "All") "All Dates" else txDatePreset,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) Color.White else Color(0xFF1E293B)
                    )
                }
            }

            if (projectTransactions.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    val emptyMessage = if (txSearchQuery.isNotEmpty() || txDatePreset != "All") {
                        "No matching transactions found"
                    } else {
                        "No transactions logged yet"
                    }
                    PremiumEmptyState(dark = dark, message = emptyMessage)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "All Receipts  ·  ${projectTransactions.size}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            letterSpacing = 0.3.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(projectTransactions, key = { it.id }) { tx ->
                        PremiumTransactionCard(tx = tx, dark = dark, onClick = { onSelectTx(tx) })
                    }
                }
            }
        }

        // Bottom Action Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            if (dark) PremiumNavy.copy(alpha = 0.95f) else Color(0xFAF0F4FF)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Payment In Button
                Button(
                    onClick = {
                        viewModel.transactionTypePreset = "Money In"
                        viewModel.showTransactionDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)), // Green/Teal
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Payment In",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Plus Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NeonPurple)
                        .clickable {
                            viewModel.transactionTypePreset = "Money Out"
                            viewModel.showTransactionDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Payment Out Button
                Button(
                    onClick = {
                        viewModel.transactionTypePreset = "Money Out"
                        viewModel.showTransactionDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)), // Red/Pink
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Payment Out",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// TAB: SITE INFO
// ─────────────────────────────────────────────
@Composable
private fun SiteInfoTab(dark: Boolean, currentProject: Project?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PremiumInfoCard(
                dark = dark,
                icon = Icons.Default.LocationOn,
                iconColor = AquaGlow,
                label = "WORKSPACE LOCATION",
                value = currentProject?.location ?: "Treasure Garden Road Site, India",
                gradient = Brush.linearGradient(listOf(AquaGlow.copy(alpha = 0.12f), Color.Transparent))
            )
        }
        item {
            PremiumInfoCard(
                dark = dark,
                icon = Icons.Default.AccountBalance,
                iconColor = EmeraldGlow,
                label = "ESTIMATED BUDGET",
                value = formatIndianRupees(currentProject?.budget ?: 1500000.0),
                valueColor = EmeraldGlow,
                gradient = Brush.linearGradient(listOf(EmeraldGlow.copy(alpha = 0.12f), Color.Transparent))
            )
        }
        item {
            PremiumInfoCard(
                dark = dark,
                icon = Icons.Default.Construction,
                iconColor = VioletGlow,
                label = "CONSTRUCTION PHASE",
                value = currentProject?.status ?: "Active Phase 1",
                valueColor = VioletGlow,
                gradient = Brush.linearGradient(listOf(VioletGlow.copy(alpha = 0.12f), Color.Transparent))
            )
        }
        item {
            PremiumInfoCard(
                dark = dark,
                icon = Icons.Default.CalendarToday,
                iconColor = AmberGlow,
                label = "PROJECT START DATE",
                value = "January 2026",
                gradient = Brush.linearGradient(listOf(AmberGlow.copy(alpha = 0.12f), Color.Transparent))
            )
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ─────────────────────────────────────────────
// TAB: TASKS
// ─────────────────────────────────────────────
@Composable
private fun TaskTab(
    dark: Boolean,
    allTasks: List<Task>,
    activeProjId: Int?,
    viewModel: MainViewModel
) {
    val projTasks = remember(allTasks, activeProjId) {
        if (activeProjId == null) emptyList()
        else allTasks.filter { it.projectId == activeProjId }
    }
    val doneCount    = projTasks.count { it.status == "Done" }
    val pendingCount = projTasks.count { it.status != "Done" }

    Column(modifier = Modifier.fillMaxSize()) {
        // Progress Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (dark)
                        Brush.horizontalGradient(listOf(Color(0xFF0D1B3E), Color(0xFF111827)))
                    else
                        Brush.horizontalGradient(listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFF)))
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Task Progress",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B)
                        )
                        Text(
                            "$doneCount completed · $pendingCount pending",
                            fontSize = 11.sp,
                            color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(AmberGlow.copy(alpha = 0.12f), CircleShape)
                            .border(1.dp, AmberGlow.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (projTasks.isEmpty()) "0%" else
                                "${((doneCount.toFloat() / projTasks.size) * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = AmberGlow
                        )
                    }
                }
                // Progress Bar
                if (projTasks.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (dark) Color(0xFF1E2D4A) else Color(0xFFDDE4F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(doneCount.toFloat() / projTasks.size)
                                .fillMaxHeight()
                                .background(GradientAmber)
                        )
                    }
                }
            }
        }

        if (projTasks.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                PremiumEmptyState(dark = dark, message = "No tasks registered yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(projTasks, key = { it.id }) { t ->
                    PremiumTaskCard(task = t, dark = dark, onCycle = { viewModel.cycleTaskStatus(t) })
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

// ─────────────────────────────────────────────
// TAB: ATTENDANCE
// ─────────────────────────────────────────────
@Composable
private fun AttendanceTab(
    dark: Boolean,
    allWorkers: List<Worker>,
    activeDateAttendance: List<Attendance>,
    presentCount: Int,
    absentCount: Int,
    totalOvertime: Double,
    dailyWages: Double,
    parsedDateString: String,
    activeDate: String,
    navigateDay: (Int) -> Unit,
    cFormatter: NumberFormat,
    onSelectWorker: (Worker) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Date Navigator
        item {
            PremiumDateNavigator(
                dark = dark,
                parsedDateString = parsedDateString,
                activeDate = activeDate,
                navigateDay = navigateDay
            )
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttendanceStatBadge(dark, "PRESENT", "$presentCount", EmeraldGlow, Modifier.weight(1f))
                AttendanceStatBadge(dark, "ABSENT", "$absentCount", RoseGlow, Modifier.weight(1f))
                AttendanceStatBadge(dark, "OVERTIME", "${totalOvertime}h", VioletGlow, Modifier.weight(1f))
            }
        }

        // Wages Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(EmeraldGlow.copy(alpha = 0.14f), EmeraldGlow.copy(alpha = 0.04f))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(EmeraldGlow.copy(alpha = 0.5f), EmeraldGlow.copy(alpha = 0.1f))),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "DAILY CREW PAYOUT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = EmeraldGlow.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cFormatter.format(dailyWages),
                            style = TextStyle(
                                brush = GradientEmerald,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(EmeraldGlow.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, EmeraldGlow.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = EmeraldGlow,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // Section Label
        item {
            Text(
                text = "CREW ROSTER",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (allWorkers.isEmpty()) {
            item { PremiumEmptyState(dark = dark, message = "No workers registered yet") }
        } else {
            items(allWorkers, key = { it.id }) { worker ->
                val record = activeDateAttendance.find { it.workerId == worker.id }
                PremiumAttendanceCard(
                    worker = worker,
                    record = record,
                    dark = dark,
                    cFormatter = cFormatter,
                    onClick = { onSelectWorker(worker) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ─────────────────────────────────────────────
// PAGE 2 — PARTY DETAIL
// ─────────────────────────────────────────────
@Composable
private fun PremiumPartyDetailPage(
    worker: Worker,
    dark: Boolean,
    currentProject: Project?,
    projectTransactions: List<Transaction>,
    context: Context,
    onBack: () -> Unit,
    onSelectTx: (Transaction) -> Unit,
    onIPaid: () -> Unit,
    onIReceived: () -> Unit,
    onAddTx: () -> Unit,
    onShowPdf: () -> Unit,
    onHistoryTabChange: (String) -> Unit,
    onTimeFilterChange: (String) -> Unit
) {
    val matchedTxs = remember(projectTransactions, worker) {
        projectTransactions.filter { it.partyId == worker.id || it.partyName == worker.name }
    }
    val totalReceived = matchedTxs.filter { it.type == "Money Out" }.sumOf { it.amount }
    val totalPaid     = matchedTxs.filter { it.type == "Money In" }.sumOf { it.amount }
    val diff          = totalReceived - totalPaid

    val receivedCount = remember(matchedTxs) { matchedTxs.count { it.type == "Money In" } }
    val paidCount     = remember(matchedTxs) { matchedTxs.count { it.type == "Money Out" } }

    var selectedHistoryTab by remember { mutableStateOf("All") }
    var selectedTimeFilter by remember { mutableStateOf("All Time") }
    var showTimeFilterMenu by remember { mutableStateOf(false) }

    LaunchedEffect(selectedHistoryTab, selectedTimeFilter) {
        onHistoryTabChange(selectedHistoryTab)
        onTimeFilterChange(selectedTimeFilter)
    }

    val historyTxs = remember(matchedTxs, selectedHistoryTab, selectedTimeFilter) {
        val typeFiltered = when (selectedHistoryTab) {
            "Received" -> matchedTxs.filter { it.type == "Money In" }
            "Paid"     -> matchedTxs.filter { it.type == "Money Out" }
            else       -> matchedTxs
        }
        if (selectedTimeFilter == "All Time") return@remember typeFiltered
        
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        val today = cal.time
        
        cal.time = today
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        val filterLimitTime = when (selectedTimeFilter) {
            "Today" -> startOfToday
            "Weekly" -> { cal.add(java.util.Calendar.DAY_OF_YEAR, -7); cal.timeInMillis }
            "2 Weeks" -> { cal.add(java.util.Calendar.DAY_OF_YEAR, -14); cal.timeInMillis }
            "3 Weeks" -> { cal.add(java.util.Calendar.DAY_OF_YEAR, -21); cal.timeInMillis }
            "Monthly" -> { cal.add(java.util.Calendar.MONTH, -1); cal.timeInMillis }
            else -> 0L
        }

        typeFiltered.filter { tx ->
            try {
                val d = format.parse(tx.date)
                if (d != null) {
                    if (selectedTimeFilter == "Today") {
                        d.time >= startOfToday
                    } else {
                        d.time >= filterLimitTime
                    }
                } else true
            } catch (e: Exception) { true }
        }
    }

    val bgBrush = if (dark)
        Brush.verticalGradient(listOf(PremiumNavy, Color(0xFF080C18)))
    else
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFFAFBFF)))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        // Left-aligned visual header matching the reference screenshot
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (dark) PremiumNavy else Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (dark) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Party Project Balance",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) Color.White else Color(0xFF131F3C)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderActionButton(
                    icon = Icons.Default.Description,
                    dark = dark,
                    onClick = onShowPdf
                )
                HeaderActionButton(
                    icon = Icons.Default.GetApp,
                    dark = dark,
                    onClick = {
                        Toast.makeText(context, "Exporting ledger...", Toast.LENGTH_SHORT).show()
                    }
                )
                HeaderActionButton(
                    icon = Icons.Default.MoreVert,
                    dark = dark,
                    onClick = {
                        Toast.makeText(context, "More Options", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Balance Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (dark) Color(0xFF111827) else Color.White)
                .border(
                    1.dp,
                    if (dark) Color(0xFF1E2D4A) else Color(0xFFE2E8F4),
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = worker.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) Color.White else Color(0xFF13203C)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentProject?.name ?: "Treasure garden",
                        fontSize = 14.sp,
                        color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    val isAdvance = diff >= 0
                    val statusColor = if (isAdvance) Color(0xFF0F766E) else Color(0xFFE11D48)
                    val statusText = if (isAdvance) "Paid" else "Received"
                    Text(
                        text = formatIndianRupees(diff.absoluteValue),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }

        // Static Balance Summary Card (Party Received & Party Paid)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (dark) Color(0xFF111827) else Color.White)
                .border(
                    1.dp,
                    if (dark) Color(0xFF1E2D4A) else Color(0xFFE2E8F4),
                    RoundedCornerShape(12.dp)
                )
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Party Received
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Party Received",
                        fontSize = 12.sp,
                        color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatIndianRupees(totalReceived),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) Color.White else Color(0xFF0F172A)
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(if (dark) Color(0xFF2D3F5E) else Color(0xFFE2E8F0))
                )

                // Right Column: Party Paid
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Party Paid",
                        fontSize = 12.sp,
                        color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatIndianRupees(totalPaid),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) Color.White else Color(0xFF0F172A)
                    )
                }
            }
        }

        // 3-way Horizontal Tab Bar Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                "All" to "All Transactions",
                "Received" to "Received ($receivedCount)",
                "Paid" to "Paid ($paidCount)"
            )

            tabs.forEach { (tabKey, tabLabel) ->
                val isActive = selectedHistoryTab == tabKey
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedHistoryTab = tabKey }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tabLabel,
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive) {
                            if (dark) Color.White else Color(0xFF5D53EA)
                        } else {
                            if (dark) Color(0xFF64748B) else Color(0xFF94A3B8)
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(
                                if (isActive) {
                                    if (dark) Color(0xFF818CF8) else Color(0xFF5D53EA)
                                } else {
                                    Color.Transparent
                                }
                            )
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(if (dark) Color(0xFF1E2D4A) else Color(0xFFE2E8F0))
        )

        // Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable { showTimeFilterMenu = true }.padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Filter: $selectedTimeFilter",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }
                DropdownMenu(
                    expanded = showTimeFilterMenu,
                    onDismissRequest = { showTimeFilterMenu = false },
                    modifier = Modifier.background(if (dark) Color(0xFF1E293B) else Color.White)
                ) {
                    val opts = listOf("All Time", "Today", "Weekly", "2 Weeks", "3 Weeks", "Monthly")
                    opts.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt, color = if (dark) Color.White else Color.Black) },
                            onClick = {
                                selectedTimeFilter = opt
                                showTimeFilterMenu = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "Amount",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (historyTxs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PremiumEmptyState(
                        dark = dark,
                        message = if (selectedHistoryTab == "Received") "No content received" else "No paid transactions"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(historyTxs, key = { it.id }) { tx ->
                        PartyTransactionCard(
                            tx = tx,
                            dark = dark,
                            workerName = worker.name,
                            onClick = { onSelectTx(tx) }
                        )
                    }
                }
            }

            // Bottom Action Bar overlaid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                if (dark) PremiumNavy.copy(alpha = 0.95f) else Color(0xFAF0F4FF)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFD11A5B))
                            .clickable(onClick = onIPaid),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "I Paid",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF5D53EA))
                            .clickable(onClick = onAddTx),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Transaction",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF00897B))
                            .clickable(onClick = onIReceived),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "I Received",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderActionButton(
    icon: ImageVector,
    dark: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (dark) Color(0xFF1E2D4A) else Color.White)
            .border(
                1.dp,
                if (dark) Color(0xFF2D3F5E) else Color(0xFFE2E8F0),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (dark) Color.White else Color(0xFF1E293B),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PartyTransactionCard(
    tx: Transaction,
    dark: Boolean,
    workerName: String,
    onClick: () -> Unit
) {
    TransactionCardLayout(tx = tx, dark = dark, workerName = workerName, onClick = onClick)
}

// ─────────────────────────────────────────────
// PAGE 3 — PAYMENT DETAIL
// ─────────────────────────────────────────────
@Composable
private fun PremiumPaymentDetailPage(
    tx: Transaction,
    dark: Boolean,
    currentProject: Project?,
    viewModel: MainViewModel,
    context: Context,
    onBack: () -> Unit,
    onShowPdf: () -> Unit
) {
    val amountStr  = formatIndianRupees(tx.amount)
    val isMoneyIn  = tx.type == "Money In"
    val accentColor = if (isMoneyIn) EmeraldGlow else RoseGlow
    val accentGrad  = if (isMoneyIn) GradientEmerald else GradientRose

    val bgBrush = if (dark)
        Brush.verticalGradient(listOf(PremiumNavy, Color(0xFF080C18)))
    else
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFFAFBFF)))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        PremiumPageHeader(
            dark = dark,
            title = "Payment Receipt",
            subtitle = "by ${viewModel.userSession.value?.displayName ?: "Tejas Harane"}",
            onBack = onBack,
            actions = {
                PremiumIconBtn(
                    icon = Icons.Default.PictureAsPdf, tint = accentColor, dark = dark,
                    onClick = onShowPdf
                )
                PremiumIconBtn(
                    icon = Icons.Default.Share,
                    tint = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B), dark = dark,
                    onClick = {
                        val amountStr = formatIndianRupees(tx.amount)
                        val pdfFile = PdfUtils.generateReceiptPdfFile(
                            context = context,
                            txId = tx.id,
                            name = tx.partyName ?: "Company",
                            amount = amountStr,
                            date = tx.date
                        )
                        PdfUtils.sharePdfFile(context, pdfFile, "Share Receipt")
                    }
                )
                PremiumIconBtn(
                    icon = Icons.Default.Edit,
                    tint = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B), dark = dark,
                    onClick = {
                        viewModel.transactionToEdit = tx
                        viewModel.transactionTypePreset = tx.type
                        viewModel.showTransactionDialog = true
                    }
                )
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Hero
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(accentColor.copy(alpha = 0.15f), accentColor.copy(alpha = 0.04f))
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(listOf(accentColor.copy(alpha = 0.7f), accentColor.copy(alpha = 0.1f))),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isMoneyIn) "MONEY RECEIVED" else "MONEY PAID OUT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = accentColor.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = amountStr,
                                style = TextStyle(
                                    brush = accentGrad,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(EmeraldGlow, CircleShape)
                                )
                                Text(
                                    "Authenticated & Sealed",
                                    fontSize = 10.sp,
                                    color = EmeraldGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(accentColor.copy(alpha = 0.15f), CircleShape)
                                .border(1.5.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isMoneyIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Detail Rows
            item {
                PremiumDetailCard(
                    dark = dark,
                    items = listOf(
                        Triple(Icons.Default.Person, "To",
                            if (!isMoneyIn) (tx.partyName ?: "Staff") else "Company"),
                        Triple(Icons.Default.PersonOutline, "From",
                            if (isMoneyIn) (tx.partyName ?: "Client") else "Company"),
                        Triple(Icons.Default.CalendarToday, "Date", tx.date),
                        Triple(Icons.Default.LocationOn, "Project",
                            currentProject?.name ?: "Treasure Garden"),
                        Triple(Icons.Default.AccountBalance, "Method", tx.paymentMethod),
                        Triple(Icons.Default.Notes, "Description", tx.description)
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }
        }

        // Bottom CTA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GradientAqua)
                    .clickable(onClick = onShowPdf)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Text("VIEW PDF", fontSize = 11.sp, fontWeight = FontWeight.Black,
                        color = Color.White, letterSpacing = 0.5.sp)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(VioletGlow, VioletGlow.copy(alpha = 0.3f))),
                        RoundedCornerShape(14.dp)
                    )
                    .background(VioletGlow.copy(alpha = 0.1f))
                    .clickable {
                        val pdfFile = PdfUtils.generateReceiptPdfFile(
                            context = context,
                            txId = tx.id,
                            name = tx.partyName ?: "Company",
                            amount = amountStr,
                            date = tx.date
                        )
                        PdfUtils.sharePdfFile(context, pdfFile, "Share Receipt PDF")
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Share, null, tint = VioletGlow, modifier = Modifier.size(15.dp))
                    Text("SHARE PDF", fontSize = 11.sp, fontWeight = FontWeight.Black,
                        color = VioletGlow, letterSpacing = 0.5.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// PREMIUM REUSABLE COMPONENTS
// ─────────────────────────────────────────────

@Composable
private fun PremiumPageHeader(
    dark: Boolean,
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val bg = if (dark)
        Brush.horizontalGradient(listOf(Color(0xFF0D1B3E), Color(0xFF111827)))
    else
        Brush.horizontalGradient(listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFF)))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(AquaGlow.copy(alpha = 0.12f), CircleShape)
                        .border(1.dp, AquaGlow.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ArrowBack, null,
                        tint = AquaGlow,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        title, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                        color = if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B)
                    )
                    Text(
                        subtitle, fontSize = 10.sp,
                        color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }

        // Bottom border glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.horizontalGradient(listOf(Color.Transparent, AquaGlow.copy(0.3f), Color.Transparent))
                )
        )
    }
}

@Composable
private fun PremiumIconBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    dark: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.1f))
            .border(1.dp, tint.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun PremiumStatCard(
    modifier: Modifier = Modifier,
    dark: Boolean,
    label: String,
    value: String,
    valueColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    borderColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    label, fontSize = 9.sp, fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8)
                )
                Icon(icon, null, tint = valueColor.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = valueColor,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PremiumPartyCard(
    worker: Worker,
    diff: Double,
    dark: Boolean,
    onClick: () -> Unit
) {
    val isPositive = diff >= 0
    val accentColor = if (isPositive) EmeraldGlow else RoseGlow

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (dark)
                    Brush.horizontalGradient(listOf(Color(0xFF111827), Color(0xFF0D1B3E)))
                else
                    Brush.horizontalGradient(listOf(Color.White, Color(0xFFF8FAFF)))
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(accentColor.copy(alpha = 0.2f), if (dark) Color(0xFF1E2D4A) else Color(0xFFDDE4F0))
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(worker.avatarColor), Color(worker.avatarColor).copy(alpha = 0.6f))
                            ), CircleShape
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        worker.name.take(2).uppercase(),
                        color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        worker.name, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B)
                    )
                    Text(
                        worker.role, fontSize = 11.sp,
                        color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatIndianRupees(diff.absoluteValue),
                    fontSize = 15.sp, fontWeight = FontWeight.Black, color = accentColor
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isPositive) "Advance" else "Pending",
                        fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumTransactionCard(
    tx: Transaction,
    dark: Boolean,
    workerName: String = "",
    onClick: () -> Unit
) {
    TransactionCardLayout(tx = tx, dark = dark, workerName = workerName, onClick = onClick)
}

@Composable
private fun TransactionCardLayout(
    tx: Transaction,
    dark: Boolean,
    workerName: String = "",
    onClick: () -> Unit
) {
    val isIn = tx.type == "Money In"
    val dateParts = tx.date.split("-")
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    val dayStr = dateParts.getOrNull(2) ?: "27"
    val monStr = months.getOrElse((dateParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1) { "May" }

    val cardBg = if (dark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (dark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (dark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val textSecondary = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val arrowBg = if (isIn) {
        if (dark) Color(0xFF143A25) else Color(0xFFE6F4EA)
    } else {
        if (dark) Color(0xFF4C1B1B) else Color(0xFFFCE8E6)
    }
    val arrowTint = if (isIn) {
        if (dark) Color(0xFF81C784) else Color(0xFF137333)
    } else {
        if (dark) Color(0xFFE57373) else Color(0xFFC5221F)
    }
    val amountColor = arrowTint

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Date Badge
            Box(
                modifier = Modifier
                    .size(width = 46.dp, height = 48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF5D53EA)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dayStr,
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = monStr,
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 2. Arrow Indicator
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(arrowBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = arrowTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            // 3. Name & Tag details Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                val partyText = tx.partyName ?: workerName.ifBlank { "Party" }
                val line1 = if (isIn) partyText else "Company"
                val line2 = if (isIn) "To: Company" else "To: $partyText"

                Text(
                    text = line1,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = line2,
                    fontSize = 12.sp,
                    color = textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Payment Method Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (dark) Color(0xFF334155) else Color(0xFFE8F0FE))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tx.paymentMethod,
                            fontSize = 10.sp,
                            color = if (dark) Color(0xFF90CDF4) else Color(0xFF1A73E8),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (tx.description.isNotBlank()) {
                        Text(
                            text = "·  ${tx.description}",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                }
            }

            // 4. Amount and Chevron
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = (if (isIn) "+" else "-") + formatIndianRupees(tx.amount),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PremiumTaskCard(task: Task, dark: Boolean, onCycle: () -> Unit) {
    val statusColor = when (task.status) {
        "Done"        -> EmeraldGlow
        "In Progress" -> AmberGlow
        else          -> if (dark) Color(0xFF475569) else Color(0xFF94A3B8)
    }
    val priorityColor = when (task.priority) {
        "High"   -> RoseGlow
        "Medium" -> AmberGlow
        else     -> AquaGlow
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (dark)
                    Brush.horizontalGradient(listOf(Color(0xFF111827), Color(0xFF0D1B3E)))
                else
                    Brush.horizontalGradient(listOf(Color.White, Color(0xFFF8FAFF)))
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(statusColor.copy(alpha = 0.3f), if (dark) Color(0xFF1E2D4A) else Color(0xFFDDE4F0))
                ),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onCycle)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Status indicator dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(statusColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        task.title, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 3.dp)
                    ) {
                        Text(
                            task.assignee, fontSize = 10.sp,
                            color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(priorityColor.copy(alpha = 0.12f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(task.priority, fontSize = 8.sp, color = priorityColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    task.status, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, color = statusColor
                )
            }
        }
    }
}

@Composable
private fun PremiumAttendanceCard(
    worker: Worker,
    record: Attendance?,
    dark: Boolean,
    cFormatter: NumberFormat,
    onClick: () -> Unit
) {
    val statusText = when (record?.status) {
        "Present"  -> "Present"
        "Absent"   -> "Absent"
        "Overtime" -> "OT ${record.overtimeHours}h"
        else       -> "Unmarked"
    }
    val statusColor = when (record?.status) {
        "Present"  -> EmeraldGlow
        "Absent"   -> RoseGlow
        "Overtime" -> VioletGlow
        else       -> if (dark) Color(0xFF2D3F5E) else Color(0xFFCBD5E1)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (dark)
                    Brush.horizontalGradient(listOf(Color(0xFF111827), Color(0xFF0D1B3E)))
                else
                    Brush.horizontalGradient(listOf(Color.White, Color(0xFFF8FAFF)))
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(statusColor.copy(alpha = 0.35f), if (dark) Color(0xFF1E2D4A) else Color(0xFFDDE4F0))
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(worker.avatarColor), Color(worker.avatarColor).copy(alpha = 0.5f))
                            ), CircleShape
                        )
                        .border(2.dp, statusColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        worker.name.take(2).uppercase(),
                        color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        worker.name, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B)
                    )
                    Text(
                        "${worker.role} · ${worker.shift} · ${cFormatter.format(worker.wageRate)}/day",
                        fontSize = 10.sp,
                        color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(statusText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
            }
        }
    }
}

@Composable
private fun PremiumDateNavigator(
    dark: Boolean,
    parsedDateString: String,
    activeDate: String,
    navigateDay: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (dark)
                    Brush.horizontalGradient(listOf(Color(0xFF111827), Color(0xFF0D1B3E)))
                else
                    Brush.horizontalGradient(listOf(Color.White, Color(0xFFF8FAFF)))
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(AquaGlow.copy(alpha = 0.4f), if (dark) Color(0xFF1E2D4A) else Color(0xFFDDE4F0))
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AquaGlow.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, AquaGlow.copy(alpha = 0.3f), CircleShape)
                    .clickable { navigateDay(-1) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = AquaGlow, modifier = Modifier.size(16.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    parsedDateString, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B),
                    textAlign = TextAlign.Center
                )
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                if (activeDate == today) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AquaGlow.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("TODAY", fontSize = 8.sp, fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp, color = AquaGlow)
                    }
                } else {
                    Text(
                        "TAP TO RETURN TODAY", fontSize = 8.sp, fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp, color = VioletGlow,
                        modifier = Modifier.clickable {
                            // reset to today
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AquaGlow.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, AquaGlow.copy(alpha = 0.3f), CircleShape)
                    .clickable { navigateDay(1) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowForward, null, tint = AquaGlow, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AttendanceStatBadge(
    dark: Boolean,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(color.copy(alpha = 0.14f), color.copy(alpha = 0.04f))))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black,
                letterSpacing = 1.sp, color = color.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
private fun MiniFinanceStat(
    dark: Boolean,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black,
                letterSpacing = 1.sp, color = color.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = color,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PremiumInfoCard(
    dark: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    valueColor: Color? = null,
    gradient: Brush
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(iconColor.copy(alpha = 0.3f), if (dark) Color(0xFF1E2D4A) else Color(0xFFDDE4F0))
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, iconColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    label, fontSize = 9.sp, fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    value, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = valueColor ?: (if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B))
                )
            }
        }
    }
}

@Composable
private fun PremiumDetailCard(
    dark: Boolean,
    items: List<Triple<androidx.compose.ui.graphics.vector.ImageVector, String, String>>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (dark)
                    Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF0D1B3E)))
                else
                    Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFF)))
            )
            .border(
                1.dp,
                if (dark) Color(0xFF1E2D4A) else Color(0xFFDDE4F0),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            items.forEachIndexed { idx, (icon, label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            icon, null,
                            tint = AquaGlow.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                            color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8)
                        )
                    }
                    Text(
                        value, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B),
                        textAlign = TextAlign.End,
                        modifier = Modifier.widthIn(max = 180.dp),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                if (idx < items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                if (dark) Color(0xFF1A2744) else Color(0xFFEEF2FF)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    dark: Boolean,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (dark) Color(0xFF111827) else Color.White
            )
            .border(
                1.dp,
                if (value.isNotEmpty()) AquaGlow.copy(alpha = 0.5f)
                else if (dark) Color(0xFF1E2D4A) else Color(0xFFDDE4F0),
                RoundedCornerShape(14.dp)
            )
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder, fontSize = 13.sp,
                    color = if (dark) Color(0xFF2D3F5E) else Color(0xFFCBD5E1)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search, null,
                    tint = if (value.isNotEmpty()) AquaGlow else
                        if (dark) Color(0xFF2D3F5E) else Color(0xFFCBD5E1),
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = if (value.isNotEmpty()) ({
                Icon(
                    Icons.Default.Clear, null,
                    tint = if (dark) Color(0xFF475569) else Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp).clickable { onValueChange("") }
                )
            }) else null,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B),
                unfocusedTextColor = if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B),
                cursorColor = AquaGlow
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PremiumEmptyState(dark: Boolean, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    Brush.radialGradient(listOf(AquaGlow.copy(0.1f), Color.Transparent)),
                    CircleShape
                )
                .border(1.dp, AquaGlow.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Inbox, null,
                tint = if (dark) Color(0xFF2D3F5E) else Color(0xFFCBD5E1),
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            message, fontSize = 13.sp, textAlign = TextAlign.Center,
            color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PulsatingDot(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "pulseScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "pulseAlpha"
    )
    Box(
        modifier = Modifier
            .size((8 * scale).dp)
            .background(color.copy(alpha = alpha), CircleShape)
    )
}

// ─────────────────────────────────────────────
// PREMIUM DIALOGS
// ─────────────────────────────────────────────

@Composable
private fun PremiumAttendanceDialog(
    worker: Worker,
    record: Attendance?,
    dark: Boolean,
    parsedDateString: String,
    activeProj: Project,
    activeDate: String,
    inputOvertimeHours: String,
    onOtChange: (String) -> Unit,
    onPresent: () -> Unit,
    onAbsent: () -> Unit,
    onSaveOt: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    GlassModalDialog(
        visible = true,
        onDismiss = onDismiss,
        title = "Mark Attendance",
        darkTheme = dark,
        glowColor = VioletGlow,
        scrollable = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Worker Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(worker.avatarColor), Color(worker.avatarColor).copy(alpha = 0.6f))),
                            CircleShape
                        )
                        .border(2.dp, VioletGlow.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(worker.name.take(2).uppercase(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                Column {
                    Text(worker.name, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                        color = if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B))
                    Text(parsedDateString, fontSize = 10.sp,
                        color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8))
                }
            }

            // Status Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (record?.status == "Present")
                                Brush.linearGradient(listOf(EmeraldGlow.copy(0.25f), EmeraldGlow.copy(0.1f)))
                            else Brush.linearGradient(listOf(Color(0x0A10B981), Color.Transparent))
                        )
                        .border(
                            1.5.dp,
                            if (record?.status == "Present") EmeraldGlow else EmeraldGlow.copy(0.25f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(onClick = onPresent)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Check, null, tint = EmeraldGlow, modifier = Modifier.size(16.dp))
                        Text("PRESENT", color = EmeraldGlow, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (record?.status == "Absent")
                                Brush.linearGradient(listOf(RoseGlow.copy(0.25f), RoseGlow.copy(0.1f)))
                            else Brush.linearGradient(listOf(Color(0x0AF43F5E), Color.Transparent))
                        )
                        .border(
                            1.5.dp,
                            if (record?.status == "Absent") RoseGlow else RoseGlow.copy(0.25f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(onClick = onAbsent)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Close, null, tint = RoseGlow, modifier = Modifier.size(16.dp))
                        Text("ABSENT", color = RoseGlow, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }

            // Divider
            Box(modifier = Modifier.fillMaxWidth().height(1.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, VioletGlow.copy(0.3f), Color.Transparent))))

            // Overtime
            Text(
                "Log Overtime Hours",
                fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GlassTextField(
                        value = inputOvertimeHours,
                        onValueChange = onOtChange,
                        label = "Hours",
                        isNumeric = true,
                        darkTheme = dark
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GradientViolet)
                        .clickable(onClick = onSaveOt)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SAVE OT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }

            if (record != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RoseGlow.copy(alpha = 0.08f))
                        .border(1.dp, RoseGlow.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onClear)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Delete, null, tint = RoseGlow, modifier = Modifier.size(14.dp))
                        Text("CLEAR RECORD", color = RoseGlow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumAddTransactionDialog(
    dark: Boolean,
    partyTxType: String,
    partyTxAmount: String,
    partyTxDesc: String,
    partyTxDate: String,
    partyTxCategory: String,
    partyTxMethod: String,
    onTypeChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onMethodChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    allWorkers: List<Worker>,
    selectedParty: Worker?,
    viewModel: MainViewModel
) {
    var reference by remember { mutableStateOf("") }
    GlassModalDialog(
        visible = true, onDismiss = onDismiss,
        title = "Record Payment",
        darkTheme = dark,
        glowColor = if (partyTxType == "Money Out") RoseGlow else EmeraldGlow,
        scrollable = true
    ) {
        UnifiedTransactionFormContent(
            dark = dark,
            type = partyTxType,
            onTypeChange = onTypeChange,
            allWorkers = allWorkers,
            selectedParty = selectedParty,
            onPartySelected = {},
            amountStr = partyTxAmount,
            onAmountChange = onAmountChange,
            category = partyTxCategory,
            onCategoryChange = onCategoryChange,
            description = partyTxDesc,
            onDescriptionChange = onDescChange,
            reference = reference,
            onReferenceChange = { reference = it },
            paymentMethod = partyTxMethod,
            onPaymentMethodChange = onMethodChange,
            date = partyTxDate,
            onDateChange = onDateChange,
            onSave = onSave,
            onCancel = onDismiss,
            isPartyLocked = true,
            viewModel = viewModel
        )
    }
}



// ─────────────────────────────────────────────
// DETAIL ROW (kept for backward compat)
// ─────────────────────────────────────────────
@Composable
fun DetailTextRow(label: String, value: String, darkTheme: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp,
            color = if (darkTheme) Color(0xFF64748B) else Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp,
            color = if (darkTheme) Color(0xFFE2E8F4) else Color(0xFF1E293B), fontWeight = FontWeight.Bold)
    }
}