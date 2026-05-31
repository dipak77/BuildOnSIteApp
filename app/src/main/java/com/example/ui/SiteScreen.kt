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

    var showAddPartyTxDialog by remember { mutableStateOf(false) }
    var partyTxType          by remember { mutableStateOf("Money Out") }
    var partyTxAmount        by remember { mutableStateOf("") }
    var partyTxCategory      by remember { mutableStateOf("Labor") }
    var partyTxDesc          by remember { mutableStateOf("") }
    var partyTxMethod        by remember { mutableStateOf("Cash") }
    var partyTxDate          by remember { mutableStateOf("2026-05-27") }
    var showPdfPreviewDialog by remember { mutableStateOf(false) }

    val activeProjId = currentProject?.id
    val projectTransactions = remember(allTransactions, activeProjId) {
        if (activeProjId == null) emptyList()
        else allTransactions.filter { it.projectId == activeProjId }
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
                txDetail != null -> PremiumPaymentDetailPage(
                    tx = txDetail,
                    dark = dark,
                    currentProject = currentProject,
                    viewModel = viewModel,
                    context = context,
                    onBack = { selectedTxDetail = null },
                    onShowPdf = { showPdfPreviewDialog = true }
                )

                partyDetail != null -> PremiumPartyDetailPage(
                    worker = partyDetail,
                    dark = dark,
                    currentProject = currentProject,
                    projectTransactions = projectTransactions,
                    context = context,
                    onBack = { selectedPartyDetail = null },
                    onSelectTx = { selectedTxDetail = it },
                    onIPaid = {
                        partyTxType = "Money Out"; partyTxCategory = "Labor"
                        partyTxAmount = ""; partyTxDesc = "Crew payment"
                        showAddPartyTxDialog = true
                    },
                    onIReceived = {
                        partyTxType = "Money In"; partyTxCategory = "Client Advance"
                        partyTxAmount = ""; partyTxDesc = "Received funds"
                        showAddPartyTxDialog = true
                    },
                    onAddTx = { viewModel.showTransactionDialog = true }
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
                    onShowPdf = { showPdfPreviewDialog = true }
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
            partyName = activePartyForDialog.name,
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
            }
        )
    }

    if (showPdfPreviewDialog) {
        PremiumPdfDialog(
            dark = dark,
            selectedTxDetail = selectedTxDetail,
            selectedPartyDetail = selectedPartyDetail,
            context = context,
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
    onShowPdf: () -> Unit
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
                        projectTransactions = projectTransactions,
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
                    Text(
                        text = currentProject?.name ?: "Project",
                        style = TextStyle(
                            brush = GradientAqua,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.3).sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                    label = "ADVANCE PAID",
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
                    label = "PENDING PAY",
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

            if (projectTransactions.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    PremiumEmptyState(dark = dark, message = "No transactions logged yet")
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
    onAddTx: () -> Unit
) {
    val matchedTxs = remember(projectTransactions, worker) {
        projectTransactions.filter { it.partyId == worker.id || it.partyName == worker.name }
    }
    val totalReceived = matchedTxs.filter { it.type == "Money Out" }.sumOf { it.amount }
    val totalPaid     = matchedTxs.filter { it.type == "Money In" }.sumOf { it.amount }
    val diff          = totalReceived - totalPaid

    var selectedHistoryTab by remember { mutableStateOf("Received") }

    val historyTxs = remember(matchedTxs, selectedHistoryTab) {
        if (selectedHistoryTab == "Received") {
            matchedTxs.filter { it.type == "Money Out" }
        } else {
            matchedTxs.filter { it.type == "Money In" }
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
        // Center-aligned visual header mimicking the screenshot
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (dark) PremiumNavy else Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = if (dark) Color.White else Color(0xFF1E293B),
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Party Project Balance",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (dark) Color.White else Color(0xFF131F3C),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderActionButton(
                    icon = Icons.Default.ThumbUp,
                    dark = dark,
                    onClick = {
                        val amountStr = formatIndianRupees(diff.absoluteValue)
                        val statusText = if (diff >= 0) "Advance Paid" else "Pending to Pay"
                        val shareTxt = """
                            Party Project Balance:
                            Party: ${worker.name}
                            Project: ${currentProject?.name ?: "Treasure Garden"}
                            Balance: $amountStr ($statusText)
                            Received: ${formatIndianRupees(totalReceived)}
                            Paid: ${formatIndianRupees(totalPaid)}
                        """.trimIndent()
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareTxt)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Balance Review"))
                    }
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
                    val statusText = if (isAdvance) "Advance Paid" else "Pending to Pay"
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

        // Tab-wise received vs paid summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val leftActive = selectedHistoryTab == "Received"
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedHistoryTab = "Received" }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Party Received",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (dark) (if (leftActive) Color.White else Color(0xFF64748B)) else (if (leftActive) Color(0xFF1E293B) else Color(0xFF64748B))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatIndianRupees(totalReceived),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) (if (leftActive) Color.White else Color(0xFF94A3B8)) else (if (leftActive) Color(0xFF0F172A) else Color(0xFF475569))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(2.dp)
                        .background(if (leftActive) (if (dark) AquaGlow else Color(0xFF4F46E5)) else Color.Transparent)
                )
            }

            val rightActive = selectedHistoryTab == "Paid"
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedHistoryTab = "Paid" }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Party Paid",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (dark) (if (rightActive) Color.White else Color(0xFF64748B)) else (if (rightActive) Color(0xFF1E293B) else Color(0xFF64748B))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatIndianRupees(totalPaid),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) (if (rightActive) Color.White else Color(0xFF94A3B8)) else (if (rightActive) Color(0xFF0F172A) else Color(0xFF475569))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(2.dp)
                        .background(if (rightActive) (if (dark) AquaGlow else Color(0xFF4F46E5)) else Color.Transparent)
                )
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Filter",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
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
    val dateParts = tx.date.split("-")
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    val dayStr = dateParts.getOrNull(2) ?: "27"
    val monStr = months.getOrElse((dateParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1) { "May" }
    val yearStr = dateParts.getOrNull(0) ?: "2026"

    val topBg = if (dark) Color(0xFF7C3AED) else Color(0xFF4F46E5)
    val botBg = if (dark) Color(0xFF7C3AED).copy(alpha = 0.15f) else Color(0xFFEEF2FF)
    val botTextColor = if (dark) Color(0xFFC084FC) else Color(0xFF312E81)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (dark) Color(0xFF111827) else Color.White)
            .border(
                1.dp,
                if (dark) Color(0xFF1E2D4A) else Color(0xFFE2E8F0),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Split Date Badge
                Column(
                    modifier = Modifier
                        .width(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            1.dp,
                            if (dark) Color(0xFF7C3AED).copy(alpha = 0.4f) else Color(0xFFE0E7FF),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(topBg)
                            .padding(vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = yearStr,
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(botBg)
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$dayStr $monStr",
                            fontSize = 11.sp,
                            color = botTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                val directionText = if (tx.type == "Money Out") {
                    "Company  >  $workerName"
                } else {
                    "$workerName  >  Company"
                }

                Text(
                    text = directionText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B)
                )
            }

            Text(
                text = formatIndianRupees(tx.amount),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (dark) Color.White else Color(0xFF0F172A)
            )
        }
    }
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
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Payment Receipt")
                            putExtra(Intent.EXTRA_TEXT,
                                "Payment: $amountStr | To: ${tx.partyName ?: "Company"} | Date: ${tx.date} | Method: ${tx.paymentMethod}")
                        }
                        context.startActivity(Intent.createChooser(intent, "Share receipt"))
                    }
                )
                PremiumIconBtn(
                    icon = Icons.Default.Edit,
                    tint = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B), dark = dark,
                    onClick = { Toast.makeText(context, "Edit mode activated!", Toast.LENGTH_SHORT).show() }
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
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Payment: $amountStr | ID: ${tx.id}")
                        }
                        context.startActivity(Intent.createChooser(intent, "Share"))
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Share, null, tint = VioletGlow, modifier = Modifier.size(15.dp))
                    Text("SHARE LINK", fontSize = 11.sp, fontWeight = FontWeight.Black,
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
    val isIn = tx.type == "Money In"
    val accentColor = if (isIn) EmeraldGlow else RoseGlow
    val dateParts = tx.date.split("-")
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    val dayStr = dateParts.getOrNull(2) ?: "27"
    val monStr = months.getOrElse((dateParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1) { "May" }
    val yearStr = dateParts.getOrNull(0) ?: "2026"

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
                if (dark) Color(0xFF1E2D4A) else Color(0xFFDDE4F0),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Date Badge
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GradientViolet),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dayStr, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Black)
                        Text(monStr, fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                        Text(yearStr, fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    val dirText = if (tx.type == "Money Out")
                        "Company → ${tx.partyName ?: workerName.ifBlank { "Party" }}"
                    else
                        "${tx.partyName ?: workerName.ifBlank { "Party" }} → Company"
                    Text(
                        dirText, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 160.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (dark) Color(0xFF1E2D4A) else Color(0xFFEEF2FF))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                tx.paymentMethod, fontSize = 9.sp,
                                color = AquaGlow, fontWeight = FontWeight.Bold
                            )
                        }
                        if (tx.description.isNotBlank()) {
                            Text(
                                "· ${tx.description}", fontSize = 10.sp,
                                color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8),
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 100.dp)
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (isIn) "+" else "-") + formatIndianRupees(tx.amount),
                    fontSize = 14.sp, fontWeight = FontWeight.Black, color = accentColor
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight, null,
                    tint = if (dark) Color(0xFF2D3F5E) else Color(0xFFCBD5E1),
                    modifier = Modifier.size(14.dp)
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
    partyName: String,
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
    onSave: () -> Unit
) {
    GlassModalDialog(
        visible = true, onDismiss = onDismiss,
        title = "Record Payment",
        darkTheme = dark,
        glowColor = if (partyTxType == "Money Out") RoseGlow else EmeraldGlow,
        scrollable = true
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Party label
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(VioletGlow.copy(alpha = 0.08f))
                    .border(1.dp, VioletGlow.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Person, null, tint = VioletGlow, modifier = Modifier.size(14.dp))
                    Text(partyName, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = if (dark) Color(0xFFE2E8F4) else Color(0xFF1E293B))
                }
            }

            // Type selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Money Out" to RoseGlow, "Money In" to EmeraldGlow).forEach { (type, color) ->
                    val sel = partyTxType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (sel) Brush.linearGradient(listOf(color.copy(0.2f), color.copy(0.08f)))
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                            .border(1.5.dp, if (sel) color else color.copy(0.25f), RoundedCornerShape(12.dp))
                            .clickable { onTypeChange(type) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (type == "Money Out") "I PAID" else "I RECEIVED",
                            color = if (sel) color else color.copy(0.5f),
                            fontWeight = FontWeight.Black, fontSize = 12.sp
                        )
                    }
                }
            }

            GlassTextField(value = partyTxAmount, onValueChange = onAmountChange,
                label = "Amount (₹)", isNumeric = true, placeholder = "e.g. 5000", darkTheme = dark)
            GlassTextField(value = partyTxDesc, onValueChange = onDescChange,
                label = "Description", placeholder = "e.g. Weekly advance", darkTheme = dark)
            GlassDatePickerField(
                value = partyTxDate,
                onValueChange = onDateChange,
                label = "Date (YYYY-MM-DD)",
                darkTheme = dark,
                focusedStroke = if (partyTxType == "Money Out") RoseGlow else EmeraldGlow
            )

            // Category
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CATEGORY", fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp,
                    color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Labor", "Material", "Equipment", "Client Advance", "Other").forEach { cat ->
                        val sel = partyTxCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (sel) VioletGlow.copy(alpha = 0.18f) else Color.Transparent)
                                .border(1.dp, if (sel) VioletGlow else VioletGlow.copy(0.2f), RoundedCornerShape(10.dp))
                                .clickable { onCategoryChange(cat) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(cat, color = if (sel) VioletGlow else if (dark) Color(0xFF475569) else Color(0xFF94A3B8),
                                fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Method
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PAYMENT METHOD", fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp,
                    color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Cash", "Bank Transfer", "Cheque").forEach { method ->
                        val sel = partyTxMethod == method
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (sel) AquaGlow.copy(alpha = 0.15f) else Color.Transparent)
                                .border(1.dp, if (sel) AquaGlow else AquaGlow.copy(0.2f), RoundedCornerShape(10.dp))
                                .clickable { onMethodChange(method) }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(method, color = if (sel) AquaGlow else if (dark) Color(0xFF475569) else Color(0xFF94A3B8),
                                fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, RoseGlow.copy(0.5f), RoundedCornerShape(12.dp))
                        .background(RoseGlow.copy(alpha = 0.08f))
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CANCEL", color = RoseGlow, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GradientEmerald)
                        .clickable(onClick = onSave)
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SAVE RECORD", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun PremiumPdfDialog(
    dark: Boolean,
    selectedTxDetail: Transaction?,
    selectedPartyDetail: Worker?,
    context: Context,
    onDismiss: () -> Unit
) {
    val amount   = selectedTxDetail?.amount ?: 1000.0
    val name     = selectedTxDetail?.partyName ?: selectedPartyDetail?.name ?: "Tejas Harane"
    val date     = selectedTxDetail?.date ?: "2026-05-27"
    val txId     = selectedTxDetail?.id ?: 1024

    GlassModalDialog(
        visible = true, onDismiss = onDismiss,
        title = "PDF Receipt Preview",
        darkTheme = dark, glowColor = AquaGlow,
        scrollable = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Simulated PDF Sheet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF0A0E1A), Color(0xFF0D1B3E))),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CONSTRUCT PRO INC.", fontSize = 12.sp,
                                color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Text("OFFICIAL PAYMENT RECEIPT", fontSize = 9.sp,
                                color = Color(0xFF00D4FF), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Receipt #TX-$txId", fontSize = 9.sp, color = Color(0xFF64748B))
                        Text("Date: $date", fontSize = 9.sp, color = Color(0xFF64748B))
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEF2FF)))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PAID TO / FROM", fontSize = 8.sp, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                        Text(name, fontSize = 18.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Black)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PRINCIPAL SUM", fontSize = 8.sp, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                        Text(
                            formatIndianRupees(amount),
                            style = TextStyle(
                                brush = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                                fontSize = 28.sp, fontWeight = FontWeight.Black
                            )
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEF2FF)))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                        Text("Digitally Verified & Authenticated", fontSize = 9.sp,
                            color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            // Build receipt content
                            val receiptText = buildString {
                                appendLine("========================================")
                                appendLine("        CONSTRUCTPRO - SITE RECEIPT     ")
                                appendLine("========================================")
                                appendLine()
                                appendLine("Party   : $name")
                                appendLine("Amount  : \u20B9 $amount")
                                appendLine("Date    : $date")
                                appendLine("Ref ID  : #$txId")
                                appendLine()
                                appendLine("----------------------------------------")
                                appendLine("   Digitally Verified & Authenticated   ")
                                appendLine("========================================")
                            }
                            val fileName = "Receipt_${txId}_${System.currentTimeMillis()}.txt"
                            try {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                    val contentValues = android.content.ContentValues().apply {
                                        put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                                        put(android.provider.MediaStore.Downloads.MIME_TYPE, "text/plain")
                                        put(android.provider.MediaStore.Downloads.RELATIVE_PATH,
                                            android.os.Environment.DIRECTORY_DOWNLOADS + "/ConstructPro")
                                    }
                                    val uri = context.contentResolver.insert(
                                        android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                        contentValues
                                    )
                                    if (uri != null) {
                                        context.contentResolver.openOutputStream(uri)?.use { out ->
                                            out.write(receiptText.toByteArray())
                                        }
                                        Toast.makeText(context,
                                            "Saved to Downloads/ConstructPro/$fileName",
                                            Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context,
                                            "Download failed: could not create file",
                                            Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    // Pre-Q fallback: save directly to Downloads
                                    val dir = android.os.Environment.getExternalStoragePublicDirectory(
                                        android.os.Environment.DIRECTORY_DOWNLOADS
                                    )
                                    val folder = java.io.File(dir, "ConstructPro").also { it.mkdirs() }
                                    val file = java.io.File(folder, fileName)
                                    file.writeText(receiptText)
                                    Toast.makeText(context,
                                        "Saved to Downloads/ConstructPro/$fileName",
                                        Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context,
                                    "Download failed: ${e.localizedMessage}",
                                    Toast.LENGTH_LONG).show()
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