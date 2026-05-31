package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

private const val DASHBOARD_TODAY_ISO = "2026-05-30"

// ─── Enhanced Color Palette ───────────────────────────────────────────────────
private val ElectricBlue   = Color(0xFF00D4FF)
private val DeepViolet     = Color(0xFF7C3AED)
private val CyberGreen     = Color(0xFF00FF87)
private val NeonOrange     = Color(0xFFFF6B35)
private val RoyalGold      = Color(0xFFFFD700)
private val PlatinumWhite  = Color(0xFFF8FAFC)
private val MidnightNavy   = Color(0xFF020817)
private val SlateCard      = Color(0xFF0F1729)
private val GlassWhite     = Color(0x1AFFFFFF)
private val GlassBorder    = Color(0x33FFFFFF)

// ─── Main Dashboard Screen ────────────────────────────────────────────────────
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = viewModel.darkThemeEnabled
    val currentProject    by viewModel.activeProject.collectAsState()
    val allTransactions   by viewModel.transactions.collectAsState()
    val allTasks          by viewModel.tasks.collectAsState()
    val allProjects       by viewModel.projects.collectAsState()
    val allWorkers        by viewModel.workers.collectAsState()
    val context           = LocalContext.current

    var showBackgroundPicker    by remember { mutableStateOf(false) }
    var customUrlInput          by remember { mutableStateOf("") }
    var selectedFilter          by remember { mutableStateOf("This Month") }
    var showFilterDropdown      by remember { mutableStateOf(false) }
    var showProjectSwitcher     by remember { mutableStateOf(false) }
    var showProfileDetailsDialog by remember { mutableStateOf(false) }

    // ── Computed metrics ──
    val projectTransactions = remember(allTransactions, currentProject) {
        allTransactions.filter { it.projectId == currentProject?.id }
    }
    val projectTasks = remember(allTasks, currentProject) {
        allTasks.filter { it.projectId == currentProject?.id }
    }
    val moneyIn       = projectTransactions.filter { it.type == "Money In"  }.sumOf { it.amount }
    val moneyOut      = projectTransactions.filter { it.type == "Money Out" }.sumOf { it.amount }
    val netBalance    = moneyIn - moneyOut
    val totalTasks    = projectTasks.size
    val doneTasks     = projectTasks.count { it.status == "Done" }
    val inProgressTasks = projectTasks.count { it.status == "In Progress" }
    val pendingTasks  = projectTasks.count { it.status == "To Do" }
    val overdueTasks  = projectTasks.count { it.status != "Done" && it.dueDate < DASHBOARD_TODAY_ISO }
    val taskPct       = if (totalTasks > 0) doneTasks.toFloat() / totalTasks else 0f
    val totalBudget   = currentProject?.budget ?: 1_250_000.0
    val totalSpent    = moneyOut
    val remaining     = (totalBudget - totalSpent).coerceAtLeast(0.0)
    val spendPct      = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    val budgetPct     = (spendPct * 100).toInt().coerceIn(0, 100)

    val infiniteTransition = rememberInfiniteTransition(label = "orbs_rotation")
    val rotationAnim by infiniteTransition.animateFloat(
        initialValue =  0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    // ── Background ──
    Box(
        modifier = modifier.fillMaxSize()
            .background(
                if (dark) Brush.radialGradient(
                    colors = listOf(Color(0xFF0D1B2A), Color(0xFF020817), Color(0xFF0D0D1A)),
                    center = Offset(0.3f, 0.1f), radius = 1200f
                ) else Brush.verticalGradient(
                    listOf(Color(0xFFF0F4FF), Color(0xFFE8EDF8), Color(0xFFF5F7FF))
                )
            )
            .then(
                if (dark) Modifier.drawBehind {
                    rotate(degrees = rotationAnim) {
                        val w = size.width; val h = size.height
                        // Large ambient orbs
                        drawCircle(
                            brush  = Brush.radialGradient(listOf(Color(0x1400D4FF), Color.Transparent)),
                            radius = w * 0.55f,
                            center = Offset(w * 0.1f, h * 0.15f)
                        )
                        drawCircle(
                            brush  = Brush.radialGradient(listOf(Color(0x107C3AED), Color.Transparent)),
                            radius = w * 0.65f,
                            center = Offset(w * 0.9f, h * 0.7f)
                        )
                        drawCircle(
                            brush  = Brush.radialGradient(listOf(Color(0x0D00FF87), Color.Transparent)),
                            radius = w * 0.4f,
                            center = Offset(w * 0.5f, h * 0.45f)
                        )
                    }
                } else Modifier
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Header ──
            item {
                EnhancedDashboardHeader(
                    dark        = dark,
                    onMenuClick = onMenuClick,
                    onThemeToggle = { viewModel.darkThemeEnabled = !viewModel.darkThemeEnabled },
                    onProfileClick = { showProfileDetailsDialog = true },
                    viewModel   = viewModel
                )
            }

            // ── Greeting Section ──
            item {
                val session by viewModel.userSession.collectAsState()
                GreetingSection(session = session, dark = dark)
            }

            // ── Main Content: Active Project and Site Overview ──
            item {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isWide = maxWidth >= 720.dp
                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                val proj = currentProject
                                if (proj != null) {
                                    EnhancedProjectHeroCard(
                                        proj = proj,
                                        dark = dark,
                                        netBalance = netBalance,
                                        allProjects = allProjects,
                                        allWorkersCount = allWorkers.size,
                                        showProjectSwitcher = showProjectSwitcher,
                                        onProjectSwitcherChange = { showProjectSwitcher = it },
                                        onProjectSelected = { p ->
                                            viewModel.selectedProjectId = p.id
                                            showProjectSwitcher = false
                                            Toast.makeText(context, "Switched to: ${p.name}", Toast.LENGTH_SHORT).show()
                                        },
                                        onCycleProject = {
                                            if (allProjects.isNotEmpty()) {
                                                val idx = allProjects.indexOfFirst { it.id == currentProject?.id }
                                                val next = ((idx.takeIf { it >= 0 } ?: 0) + 1) % allProjects.size
                                                viewModel.selectedProjectId = allProjects[next].id
                                            }
                                        },
                                        onCustomizeClick = { showBackgroundPicker = true }
                                    )
                                } else {
                                    EmptyProjectCard(dark = dark)
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SiteOverviewCard(
                                    dark = dark,
                                    workersPresent = if (allWorkers.isNotEmpty()) allWorkers.size else 45,
                                    equipmentActive = if (allWorkers.isNotEmpty()) allWorkers.size / 3 else 12,
                                    materialStockPct = 87,
                                    pendingBillsCount = 5,
                                    safetyScorePct = 98,
                                    projectProgressPct = 68
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            val proj = currentProject
                            if (proj != null) {
                                EnhancedProjectHeroCard(
                                    proj = proj,
                                    dark = dark,
                                    netBalance = netBalance,
                                    allProjects = allProjects,
                                    allWorkersCount = allWorkers.size,
                                    showProjectSwitcher = showProjectSwitcher,
                                    onProjectSwitcherChange = { showProjectSwitcher = it },
                                    onProjectSelected = { p ->
                                        viewModel.selectedProjectId = p.id
                                        showProjectSwitcher = false
                                        Toast.makeText(context, "Switched to: ${p.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    onCycleProject = {
                                        if (allProjects.isNotEmpty()) {
                                            val idx = allProjects.indexOfFirst { it.id == currentProject?.id }
                                            val next = ((idx.takeIf { it >= 0 } ?: 0) + 1) % allProjects.size
                                            viewModel.selectedProjectId = allProjects[next].id
                                        }
                                    },
                                    onCustomizeClick = { showBackgroundPicker = true }
                                )
                            } else {
                                EmptyProjectCard(dark = dark)
                            }
                            
                            SiteOverviewCard(
                                dark = dark,
                                workersPresent = if (allWorkers.isNotEmpty()) allWorkers.size else 45,
                                equipmentActive = if (allWorkers.isNotEmpty()) allWorkers.size / 3 else 12,
                                materialStockPct = 87,
                                pendingBillsCount = 5,
                                safetyScorePct = 98,
                                projectProgressPct = 68
                            )
                        }
                    }
                }
            }

            // ── Financial Snapshot ──
            item {
                FinancialSnapshotCard(
                    dark = dark,
                    revenue = if (moneyIn <= 0) 850_000.0 else moneyIn,
                    expenses = if (moneyOut <= 0) 465_000.0 else moneyOut,
                    netProfit = if (netBalance == 0.0) 385_000.0 else netBalance,
                    pendingPayments = 120_000.0,
                    selectedFilter = selectedFilter,
                    showFilterDropdown = showFilterDropdown,
                    onFilterExpand = { showFilterDropdown = !showFilterDropdown },
                    onFilterSelect = { selectedFilter = it; showFilterDropdown = false }
                )
            }

            // ── Recent Activity & Quick Actions ──
            item {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isWide = maxWidth >= 720.dp
                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                EnhancedSectionHeader(
                                    title = "Recent Activity",
                                    subtitle = "Recent events",
                                    dark = dark,
                                    action = {
                                        TextButton(onClick = { viewModel.currentScreen = AppScreen.Tasks }) {
                                            Text(
                                                "View All",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (dark) ElectricBlue else DeepViolet
                                            )
                                        }
                                    }
                                )
                                Spacer(Modifier.height(10.dp))
                                EnhancedActivityFeed(dark = dark, context = context)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                EnhancedQuickActions(dark = dark, viewModel = viewModel)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            EnhancedSectionHeader(
                                title = "Recent Activity",
                                subtitle = "Recent events",
                                dark = dark,
                                action = {
                                    TextButton(onClick = { viewModel.currentScreen = AppScreen.Tasks }) {
                                        Text(
                                            "View All",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (dark) ElectricBlue else DeepViolet
                                        )
                                    }
                                }
                            )
                            EnhancedActivityFeed(dark = dark, context = context)
                            
                            EnhancedQuickActions(dark = dark, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    // ── Dialogs ──
    val proj = currentProject
    if (showBackgroundPicker && proj != null) {
        BackgroundPickerDialog(
            dark           = dark,
            proj           = proj,
            customUrlInput = customUrlInput,
            onUrlChange    = { customUrlInput = it },
            onApply        = { key ->
                viewModel.updateProjectBackground(proj, key)
                showBackgroundPicker = false
            },
            onDismiss      = { showBackgroundPicker = false }
        )
    }
    if (showProfileDetailsDialog) {
        val session by viewModel.userSession.collectAsState()
        EnhancedProfileDialog(
            dark          = dark,
            session       = session,
            activeLocation = currentProject?.location ?: "Mumbai Sector 7, MH",
            onDismiss     = { showProfileDetailsDialog = false },
            onSettings    = { showProfileDetailsDialog = false; viewModel.currentScreen = AppScreen.More }
        )
    }
}

// ─── Enhanced Dashboard Header ────────────────────────────────────────────────
@Composable
private fun EnhancedDashboardHeader(
    dark: Boolean,
    onMenuClick: () -> Unit,
    onThemeToggle: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: MainViewModel
) {
    val session by viewModel.userSession.collectAsState()
    val displayName = session?.displayName ?: "Guest Builder"
    val initials = remember(displayName) {
        displayName.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .take(2)
            .ifEmpty { "GB" }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "header_shimmer")
    val shimmerOff by infiniteTransition.animateFloat(
        initialValue = -300f, targetValue = 700f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Menu Button with glow
        GlowIconButton(
            icon        = Icons.Default.Menu,
            description = "Menu",
            dark        = dark,
            glowColor   = ElectricBlue,
            onClick     = onMenuClick
        )

        // Brand Center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = "Construct",
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Black,
                    color      = if (dark) PlatinumWhite else Color(0xFF0F172A)
                )
                Spacer(Modifier.width(2.dp))
                // Animated shimmer "Pro" badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .drawWithContent {
                            val badgeBrush = Brush.linearGradient(
                                colors = listOf(ElectricBlue, DeepViolet, Color(0xFFEC4899), RoyalGold, ElectricBlue),
                                start  = Offset(shimmerOff, 0f),
                                end    = Offset(shimmerOff + 200f, 80f)
                            )
                            drawRect(brush = badgeBrush)
                            drawContent()
                        }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text       = "PRO",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Black,
                        color      = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
            Text(
                text          = "BUILD  •  MANAGE  •  GROW",
                fontSize      = 9.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
                letterSpacing = 2.sp
            )
        }

        // Right Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlowIconButton(
                icon        = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                description = "Theme",
                dark        = dark,
                glowColor   = if (dark) RoyalGold else DeepViolet,
                tint        = if (dark) RoyalGold else DeepViolet,
                onClick     = onThemeToggle
            )

            // User Profile Image / Initials Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (dark) Brush.radialGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                        else Brush.radialGradient(listOf(Color.White, Color(0xFFF1F5F9)))
                    )
                    .border(
                        1.dp,
                        Brush.sweepGradient(listOf(ElectricBlue, DeepViolet, Color(0xFFEC4899), ElectricBlue)),
                        CircleShape
                    )
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                if (session?.photoUrl != null) {
                    AsyncImage(
                        model = session?.photoUrl,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = initials,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (dark) ElectricBlue else DeepViolet
                    )
                }
            }
        }
    }
}// ─── Glow Icon Button ─────────────────────────────────────────────────────────
@Composable
private fun GlowIconButton(
    icon: ImageVector,
    description: String,
    dark: Boolean,
    glowColor: Color = ElectricBlue,
    tint: Color = if (dark) Color.White else Color(0xFF334155),
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .drawBehind {
                drawCircle(
                    color  = glowColor.copy(alpha = 0.25f),
                    radius = size.minDimension * 0.6f
                )
            }
            .clip(CircleShape)
            .background(
                if (dark) Brush.radialGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                else Brush.radialGradient(listOf(Color.White, Color(0xFFF1F5F9)))
            )
            .border(1.dp, glowColor.copy(alpha = 0.35f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = tint, modifier = Modifier.size(20.dp))
    }
}

// ─── Greeting Section ────────────────────────────────────────────────────────
@Composable
private fun GreetingSection(
    session: GoogleUser?,
    dark: Boolean
) {
    val userName = session?.displayName ?: "Guest Builder"
    val parsedDate = remember {
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(DASHBOARD_TODAY_ISO)
        } catch (e: Exception) {
            Date()
        }
    }
    val dayOfWeek = remember(parsedDate) { SimpleDateFormat("EEEE", Locale.US).format(parsedDate) }
    val formattedDate = remember(parsedDate) { SimpleDateFormat("d MMMM yyyy", Locale.US).format(parsedDate) }

    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Good Morning 👋",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = if (dark) Color.White else Color(0xFF0F172A)
            )
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (dark) Color(0x1F00FF87) else Color(0xFFECFDF5))
                        .border(1.dp, CyberGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .drawBehind {
                                drawCircle(CyberGreen.copy(alpha = pulseAlpha * 0.5f), size.minDimension * 1.5f)
                            }
                            .clip(CircleShape)
                            .background(CyberGreen)
                    )
                    Text(
                        text = "LIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (dark) CyberGreen else Color(0xFF065F46)
                    )
                }
                Text(
                    text = "Site operations running smoothly",
                    fontSize = 11.sp,
                    color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = dayOfWeek,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formattedDate,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = if (dark) ElectricBlue else DeepViolet
            )
        }
    }
}

// ─── Enhanced Project Hero Card ───────────────────────────────────────────────
@Composable
private fun EnhancedProjectHeroCard(
    proj: Project,
    dark: Boolean,
    netBalance: Double,
    allProjects: List<Project>,
    allWorkersCount: Int,
    showProjectSwitcher: Boolean,
    onProjectSwitcherChange: (Boolean) -> Unit,
    onProjectSelected: (Project) -> Unit,
    onCycleProject: () -> Unit,
    onCustomizeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (dark) Brush.linearGradient(listOf(Color(0xFF0C1322), Color(0xFF060A13)))
                else Brush.linearGradient(listOf(Color.White, Color(0xFFF1F5F9)))
            )
            .border(
                1.dp,
                if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Main Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Thumbnail with status dot
                Box(
                    modifier = Modifier
                        .size(height = 90.dp, width = 76.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=300",
                        contentDescription = "Project Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Live Dot Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(CyberGreen)
                            .border(1.5.dp, if (dark) Color(0xFF0F172A) else Color.White, CircleShape)
                    )
                }

                Spacer(Modifier.width(14.dp))

                // Right: Brand labels & title
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onProjectSwitcherChange(!showProjectSwitcher) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ElectricBlue.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE PROJECT",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = ElectricBlue,
                                letterSpacing = 1.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Go",
                            tint = if (dark) Color.White.copy(0.6f) else Color.Black.copy(0.6f),
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = proj.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (dark) Color.White else Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Location",
                            tint = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = proj.location,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    DropdownMenu(
                        expanded = showProjectSwitcher,
                        onDismissRequest = { onProjectSwitcherChange(false) },
                        modifier = Modifier.background(if (dark) Color(0xFF0F172A) else Color.White)
                    ) {
                        allProjects.forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            Modifier.size(8.dp).clip(CircleShape)
                                                .background(if (p.id == proj.id) CyberGreen else Color(0xFF475569))
                                        )
                                        Text(
                                            text = p.name,
                                            color = if (dark) Color.White else Color.Black,
                                            fontWeight = if (p.id == proj.id) FontWeight.Black else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                    }
                                },
                                onClick = { onProjectSelected(p) }
                            )
                        }
                    }
                }
            }

            // Progress Bar Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8)
                    )
                    Text(
                        text = "68%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (dark) Color.White else Color(0xFF0F172A)
                    )
                }
                Spacer(Modifier.height(6.dp))
                // Progress Bar Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.68f)
                            .clip(CircleShape)
                            .background(ElectricBlue)
                    )
                }
            }

            // Grid of 3 info chips: Deadline, Team, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val deadlineDate = "30 May 2026"
                val teamCount = "${if (allWorkersCount > 0) allWorkersCount else 5} Workers"
                val statusText = "On Track"

                EnhancedHeroChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Event,
                    label = "Deadline",
                    value = deadlineDate,
                    dark = dark
                )
                EnhancedHeroChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.People,
                    label = "Team",
                    value = teamCount,
                    dark = dark
                )
                EnhancedHeroChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Shield,
                    label = "Status",
                    value = statusText,
                    valueColor = CyberGreen,
                    dark = dark
                )
            }

            // Bottom Financial numbers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FinancialCol(
                    label = "Budget",
                    value = formatRupees(proj.budget),
                    valueColor = CyberGreen,
                    dark = dark
                )
                FinancialCol(
                    label = "Spent",
                    value = formatRupees(if (netBalance > 0) proj.budget * 0.55 else 465_000.0),
                    valueColor = Color(0xFFFF6B6B),
                    dark = dark
                )
                FinancialCol(
                    label = "Balance",
                    value = formatRupees(if (netBalance > 0) netBalance else 385_000.0),
                    valueColor = ElectricBlue,
                    dark = dark
                )
            }
        }
    }
}

@Composable
private fun FinancialCol(
    label: String,
    value: String,
    valueColor: Color,
    dark: Boolean
) {
    Column {
        Text(
            text = label.uppercase(Locale.US),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = valueColor
        )
    }
}

@Composable
private fun EnhancedHeroChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified,
    dark: Boolean
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (dark) Color(0xFF161E2E) else Color(0xFFF8FAFC))
            .border(1.dp, if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = Color.White.copy(0.5f), modifier = Modifier.size(11.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.5f), letterSpacing = 0.5.sp)
        }
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White, maxLines = 1)
    }
}

// ─── Enhanced Section Header ──────────────────────────────────────────────────
@Composable
private fun EnhancedSectionHeader(
    title: String,
    subtitle: String? = null,
    dark: Boolean,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(listOf(ElectricBlue, DeepViolet))
                    )
            )
            Column {
                Text(
                    text       = title,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Black,
                    color      = if (dark) PlatinumWhite else Color(0xFF0F172A)
                )
                if (subtitle != null) {
                    Text(
                        text       = subtitle,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8)
                    )
                }
            }
        }
        action?.invoke()
    }
}

// ─── Premium Filter Chip ──────────────────────────────────────────────────────
@Composable
private fun PremiumFilterChip(
    selected: String,
    options: List<String>,
    expanded: Boolean,
    onExpand: () -> Unit,
    onSelect: (String) -> Unit,
    dark: Boolean
) {
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (dark) Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                    else Brush.linearGradient(listOf(Color.White, Color(0xFFF8FAFC)))
                )
                .border(1.dp, if (dark) ElectricBlue.copy(0.3f) else DeepViolet.copy(0.2f), RoundedCornerShape(12.dp))
                .clickable(onClick = onExpand)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                selected,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                color      = if (dark) ElectricBlue else DeepViolet
            )
            Icon(
                Icons.Default.ArrowDropDown,
                null,
                tint     = if (dark) ElectricBlue else DeepViolet,
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(
            expanded         = expanded,
            onDismissRequest = { onExpand() },
            modifier         = Modifier
                .background(if (dark) Color(0xFF0F172A) else Color.White)
                .border(1.dp, if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text    = {
                        Text(
                            opt,
                            color      = if (opt == selected) {
                                if (dark) ElectricBlue else DeepViolet
                            } else {
                                if (dark) Color.White else Color.Black
                            },
                            fontWeight = if (opt == selected) FontWeight.Black else FontWeight.Normal
                        )
                    },
                    onClick = { onSelect(opt) }
                )
            }
        }
    }
}

// ─── Kpi Horizontal Row ───────────────────────────────────────────────────────
@Composable
private fun KpiHorizontalRow(
    dark: Boolean,
    activeProjectsCount: Int,
    availableBalance: Double,
    pendingTasksCount: Int,
    workersOnSiteCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KpiRowItem(
            title = "Active Projects",
            value = activeProjectsCount.toString(),
            icon = Icons.Default.Work,
            iconColor = ElectricBlue,
            iconBg = if (dark) Color(0x1F00D4FF) else Color(0xFFE0F2FE),
            dark = dark
        )
        KpiRowItem(
            title = "Available Balance",
            value = formatRupees(availableBalance),
            icon = Icons.Default.AccountBalanceWallet,
            iconColor = CyberGreen,
            iconBg = if (dark) Color(0x1F00FF87) else Color(0xFFDCFCE7),
            dark = dark
        )
        KpiRowItem(
            title = "Pending Tasks",
            value = pendingTasksCount.toString(),
            icon = Icons.Default.CheckCircle,
            iconColor = DeepViolet,
            iconBg = if (dark) Color(0x1F7C3AED) else Color(0xFFF3E8FF),
            dark = dark
        )
        KpiRowItem(
            title = "Workers On Site",
            value = workersOnSiteCount.toString(),
            icon = Icons.Default.People,
            iconColor = NeonOrange,
            iconBg = if (dark) Color(0x1FFF6B35) else Color(0xFFFFE5D9),
            dark = dark
        )
    }
}

@Composable
private fun KpiRowItem(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    dark: Boolean
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (dark) Color(0xFF0B1222) else Color(0xFFF8FAFC)
            )
            .border(1.dp, if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (dark) Color.White else Color(0xFF0F172A)
            )
        }
    }
}

// ─── Premium Sparkline ────────────────────────────────────────────────────────
@Composable
fun PremiumSparkline(
    points: List<Float>,
    color: Color,
    dark: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))) {
        if (points.size < 2) return@Canvas
        val w     = size.width
        val h     = size.height
        val maxX  = (points.size - 1).toFloat().coerceAtLeast(1f)
        val minY  = points.min()
        val maxY  = points.max()
        val range = (maxY - minY).coerceAtLeast(1f)

        fun getX(i: Int) = i * (w / maxX)
        fun getY(v: Float) = h - ((v - minY) / range) * h * 0.85f - h * 0.05f

        val linePath = Path()
        val fillPath = Path()

        linePath.moveTo(getX(0), getY(points[0]))
        fillPath.moveTo(getX(0), h)
        fillPath.lineTo(getX(0), getY(points[0]))

        for (i in 0 until points.size - 1) {
            val x1 = getX(i);   val y1 = getY(points[i])
            val x2 = getX(i+1); val y2 = getY(points[i+1])
            val cx = x1 + (x2 - x1) / 2f
            linePath.cubicTo(cx, y1, cx, y2, x2, y2)
            fillPath.cubicTo(cx, y1, cx, y2, x2, y2)
        }
        fillPath.lineTo(w, h); fillPath.close()

        drawPath(fillPath, Brush.verticalGradient(
            listOf(color.copy(if (dark) 0.25f else 0.15f), Color.Transparent)
        ))
        // Glow stroke
        drawPath(linePath, color.copy(0.15f), style = Stroke(8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        // Main stroke
        drawPath(linePath, color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // End dot
        val lx = getX(points.size - 1)
        val ly = getY(points.last())
        drawCircle(color.copy(0.35f), 7.dp.toPx(), Offset(lx, ly))
        drawCircle(color, 3.dp.toPx(), Offset(lx, ly))
        drawCircle(Color.White, 1.5.dp.toPx(), Offset(lx, ly))
    }
}

// ─── Enhanced Gauge Card ──────────────────────────────────────────────────────
@Composable
private fun EnhancedGaugeCard(
    modifier: Modifier,
    title: String,
    percentage: Int,
    centerLabel: String,
    accentColor: Color,
    secondColor: Color,
    dark: Boolean,
    rows: List<Triple<Color, String, String>>,
    ctaText: String,
    onCtaClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (dark) Brush.linearGradient(listOf(Color(0xFF111827), Color(0xFF0F172A)))
                else Brush.linearGradient(listOf(Color.White, Color(0xFFF8FAFC)))
            )
            .border(1.dp, accentColor.copy(0.22f), RoundedCornerShape(28.dp))
            .drawBehind {
                drawCircle(
                    accentColor.copy(0.06f),
                    size.width * 0.6f,
                    Offset(size.width * 0.5f, 0f)
                )
            }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = title,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Black,
                color      = if (dark) PlatinumWhite else Color(0xFF0F172A),
                modifier   = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(16.dp))

            // Gauge
            PremiumArcGauge(
                percentage  = percentage.toFloat() / 100f,
                accentColor = accentColor,
                secondColor = secondColor,
                dark        = dark,
                modifier    = Modifier.size(116.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$percentage%",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Black,
                        color      = if (dark) PlatinumWhite else Color(0xFF0F172A)
                    )
                    Text(
                        centerLabel,
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color      = accentColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Divider
            Box(
                Modifier.fillMaxWidth().height(1.dp)
                    .background(if (dark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
            )
            Spacer(Modifier.height(12.dp))

            // Detail rows
            rows.forEach { (color, label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                                .drawBehind {
                                    drawCircle(color.copy(0.4f), size.minDimension * 0.9f)
                                }
                        )
                        Text(
                            label,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                    Text(
                        value,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Black,
                        color      = if (dark) PlatinumWhite else Color(0xFF0F172A)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(0.12f))
                    .border(1.dp, accentColor.copy(0.25f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onCtaClick)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    ctaText,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Black,
                    color      = accentColor
                )
            }
        }
    }
}

// ─── Premium Arc Gauge ────────────────────────────────────────────────────────
@Composable
private fun PremiumArcGauge(
    percentage: Float,
    accentColor: Color,
    secondColor: Color,
    dark: Boolean,
    modifier: Modifier = Modifier,
    innerContent: @Composable () -> Unit
) {
    val animated by animateFloatAsState(
        targetValue  = percentage.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label        = "gauge"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke   = 10.dp.toPx()
            val diameter = size.minDimension - stroke - 10.dp.toPx()
            val arcSize  = Size(diameter, diameter)
            val topLeft  = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

            // Track
            drawArc(
                color      = if (dark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                startAngle = 135f, sweepAngle = 270f,
                useCenter  = false, size = arcSize, topLeft = topLeft,
                style      = Stroke(stroke, cap = StrokeCap.Round)
            )

            // Glow layer
            drawArc(
                brush      = Brush.sweepGradient(listOf(accentColor.copy(0f), accentColor.copy(0.3f), secondColor.copy(0.3f))),
                startAngle = 135f, sweepAngle = 270f * animated,
                useCenter  = false, size = arcSize, topLeft = topLeft,
                style      = Stroke(stroke + 8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Main arc
            drawArc(
                brush      = Brush.sweepGradient(listOf(accentColor, secondColor, accentColor)),
                startAngle = 135f, sweepAngle = 270f * animated,
                useCenter  = false, size = arcSize, topLeft = topLeft,
                style      = Stroke(stroke, cap = StrokeCap.Round)
            )

            // Tick marks
            val cx = size.width / 2f; val cy = size.height / 2f
            val outerR = diameter / 2f + stroke / 2f + 5.dp.toPx()
            val innerR = outerR - 4.dp.toPx()
            for (deg in 135..405 step 18) {
                val rad  = Math.toRadians(deg.toDouble())
                drawLine(
                    color       = if (dark) Color(0x22FFFFFF) else Color(0x22000000),
                    start       = Offset(cx + cos(rad).toFloat() * innerR, cy + sin(rad).toFloat() * innerR),
                    end         = Offset(cx + cos(rad).toFloat() * outerR, cy + sin(rad).toFloat() * outerR),
                    strokeWidth = 1.2.dp.toPx()
                )
            }
        }
        innerContent()
    }
}

// ─── Site Overview Card ───────────────────────────────────────────────────────
@Composable
private fun SiteOverviewCard(
    dark: Boolean,
    workersPresent: Int,
    equipmentActive: Int,
    materialStockPct: Int,
    pendingBillsCount: Int,
    safetyScorePct: Int,
    projectProgressPct: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (dark) Brush.linearGradient(listOf(Color(0xFF0C1322), Color(0xFF060A13)))
                else Brush.linearGradient(listOf(Color.White, Color(0xFFF1F5F9)))
            )
            .border(
                1.dp,
                if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "SITE OVERVIEW",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = if (dark) Color.White else Color(0xFF0F172A),
                letterSpacing = 1.sp
            )

            // 2x3 Grid
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    SiteOverviewItem(
                        modifier = Modifier.weight(1f),
                        title = "Workers Present",
                        value = workersPresent.toString(),
                        icon = Icons.Default.People,
                        iconColor = ElectricBlue,
                        iconBg = if (dark) Color(0x1F00D4FF) else Color(0xFFE0F2FE),
                        dark = dark
                    )
                    Spacer(Modifier.width(14.dp))
                    SiteOverviewItem(
                        modifier = Modifier.weight(1f),
                        title = "Equipment Active",
                        value = equipmentActive.toString(),
                        icon = Icons.Default.Build,
                        iconColor = CyberGreen,
                        iconBg = if (dark) Color(0x1F00FF87) else Color(0xFFDCFCE7),
                        dark = dark
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    SiteOverviewItem(
                        modifier = Modifier.weight(1f),
                        title = "Material Stock",
                        value = "$materialStockPct%",
                        icon = Icons.Default.Layers,
                        iconColor = DeepViolet,
                        iconBg = if (dark) Color(0x1F7C3AED) else Color(0xFFF3E8FF),
                        dark = dark
                    )
                    Spacer(Modifier.width(14.dp))
                    SiteOverviewItem(
                        modifier = Modifier.weight(1f),
                        title = "Pending Bills",
                        value = pendingBillsCount.toString(),
                        icon = Icons.Default.Description,
                        iconColor = RoyalGold,
                        iconBg = if (dark) Color(0x1FFFD700) else Color(0xFFFEF3C7),
                        dark = dark
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    SiteOverviewItem(
                        modifier = Modifier.weight(1f),
                        title = "Safety Score",
                        value = "$safetyScorePct%",
                        icon = Icons.Default.Shield,
                        iconColor = CyberGreen,
                        iconBg = if (dark) Color(0x1F00FF87) else Color(0xFFDCFCE7),
                        dark = dark
                    )
                    Spacer(Modifier.width(14.dp))
                    SiteOverviewItem(
                        modifier = Modifier.weight(1f),
                        title = "Project Progress",
                        value = "$projectProgressPct%",
                        icon = Icons.Default.TrendingUp,
                        iconColor = ElectricBlue,
                        iconBg = if (dark) Color(0x1F00D4FF) else Color(0xFFE0F2FE),
                        dark = dark
                    )
                }
            }
        }
    }
}

@Composable
private fun SiteOverviewItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    dark: Boolean
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (dark) Color(0xFF111827) else Color(0xFFF8FAFC))
            .border(1.dp, if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Column {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = if (dark) Color.White else Color(0xFF0F172A)
            )
        }
    }
}

// ─── Enhanced Activity Feed ───────────────────────────────────────────────────
@Composable
private fun EnhancedActivityFeed(dark: Boolean, context: Context) {
    val activities = listOf(
        ActivityItem(Icons.Default.CreditCard, CyberGreen,         "Payment Received",       "Metro Rail Corp",        "Today, 10:30 AM", "+₹2,50,000",  true),
        ActivityItem(Icons.Default.Layers,     ElectricBlue,        "Inventory Updated",      "Cement stock — 120 bags","Today, 09:15 AM", "120 Bags",    null),
        ActivityItem(Icons.Default.Build,      DeepViolet,          "Task Completed",         "Electrical work #EL-245","Yesterday",       "Task Done",   true),
        ActivityItem(Icons.Default.Person,     RoyalGold,           "Worker Check-in",        "Rajesh Kumar joined",    "Yesterday",       "48 Active",   null)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (dark) Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF111827)))
                else Brush.linearGradient(listOf(Color.White, Color(0xFFF8FAFC)))
            )
            .border(1.dp, if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
            .padding(6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            activities.forEachIndexed { index, item ->
                EnhancedActivityRow(
                    item    = item,
                    dark    = dark,
                    isLast  = index == activities.lastIndex,
                    onClick = { Toast.makeText(context, "Viewing: ${item.title}", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}

private data class ActivityItem(
    val icon: ImageVector, val color: Color,
    val title: String, val subtitle: String,
    val time: String, val rightText: String,
    val positive: Boolean?
)

@Composable
private fun EnhancedActivityRow(
    item: ActivityItem,
    dark: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icon with timeline line
        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(1.dp)
                        .height(28.dp)
                        .offset(y = 28.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(item.color.copy(0.3f), Color.Transparent)
                            )
                        )
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .drawBehind {
                        drawCircle(item.color.copy(0.1f), size.minDimension * 0.65f)
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .background(item.color.copy(0.12f))
                    .border(1.dp, item.color.copy(0.25f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null, tint = item.color, modifier = Modifier.size(20.dp))
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = if (dark) PlatinumWhite else Color(0xFF0F172A)
            )
            Text(
                item.subtitle,
                fontSize   = 11.sp,
                color      = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                item.time,
                fontSize   = 10.sp,
                color      = if (dark) Color(0xFF475569) else Color(0xFFB0BAC9)
            )
        }

        // Right info
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                    when (item.positive) {
                        true  -> CyberGreen.copy(0.1f)
                        false -> Color(0xFFFF6B6B).copy(0.1f)
                        null  -> item.color.copy(0.1f)
                    }
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                item.rightText,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Black,
                color      = when (item.positive) {
                    true  -> CyberGreen
                    false -> Color(0xFFFF6B6B)
                    null  -> item.color
                }
            )
        }
    }
}

// ─── Enhanced Quick Actions ───────────────────────────────────────────────────
@Composable
private fun EnhancedQuickActions(dark: Boolean, viewModel: MainViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Quick Actions",
            fontSize   = 14.sp,
            fontWeight = FontWeight.Black,
            color      = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
            letterSpacing = 0.5.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    if (dark) Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF111827)))
                    else Brush.linearGradient(listOf(Color.White, Color(0xFFF8FAFC)))
                )
                .border(1.dp, if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            EnhancedQuickActionBtn(
                icon    = Icons.Default.Add,
                label   = "Income",
                color   = CyberGreen,
                dark    = dark,
                onClick = { viewModel.transactionTypePreset = "Money In"; viewModel.showTransactionDialog = true }
            )
            EnhancedQuickActionBtn(
                icon    = Icons.Default.ArrowUpward,
                label   = "Expense",
                color   = Color(0xFFFF6B6B),
                dark    = dark,
                onClick = { viewModel.transactionTypePreset = "Money Out"; viewModel.showTransactionDialog = true }
            )
            EnhancedQuickActionBtn(
                icon    = Icons.Default.Person,
                label   = "Worker",
                color   = ElectricBlue,
                dark    = dark,
                onClick = { viewModel.showWorkerDialog = true }
            )
            EnhancedQuickActionBtn(
                icon    = Icons.Default.Task,
                label   = "Task",
                color   = DeepViolet,
                dark    = dark,
                onClick = { viewModel.showTaskDialog = true }
            )
        }
    }
}

@Composable
private fun EnhancedQuickActionBtn(
    icon: ImageVector,
    label: String,
    color: Color,
    dark: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .drawBehind {
                    drawCircle(color.copy(0.2f), size.minDimension * 0.65f)
                }
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(listOf(color.copy(0.18f), color.copy(0.08f)))
                )
                .border(1.5.dp, color.copy(0.4f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = color, modifier = Modifier.size(26.dp))
        }
        Text(
            label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold,
            color      = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
            textAlign  = TextAlign.Center
        )
    }
}

// ─── Enhanced Profile Dialog ──────────────────────────────────────────────────
@Composable
private fun EnhancedProfileDialog(
    dark: Boolean,
    session: GoogleUser?,
    activeLocation: String,
    onDismiss: () -> Unit,
    onSettings: () -> Unit
) {
    val displayName = session?.displayName ?: "Guest Builder"
    val email = session?.email ?: "guest@example.com"
    val initials = remember(displayName) {
        displayName.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .take(2)
            .ifEmpty { "GB" }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = if (dark) Color(0xFF0F172A) else Color.White,
        shape            = RoundedCornerShape(32.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .drawBehind {
                            drawCircle(ElectricBlue.copy(0.2f), size.minDimension * 0.65f)
                        }
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            Brush.sweepGradient(listOf(ElectricBlue, DeepViolet, Color(0xFFEC4899), ElectricBlue)),
                            CircleShape
                        )
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (session?.photoUrl != null) {
                        AsyncImage(
                            model = session.photoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            initials,
                            fontWeight = FontWeight.Black,
                            fontSize   = 30.sp,
                            color      = ElectricBlue
                        )
                    }
                }

                Text(
                    displayName,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Black,
                    color      = if (dark) PlatinumWhite else Color(0xFF0F172A)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileDetailRow(Icons.Default.Email,   "Email",    email, dark)
                    ProfileDetailRow(Icons.Default.Place,   "Location", activeLocation,          dark)
                    ProfileDetailRow(Icons.Default.Shield,  "Role",     if (session?.isGuest == true) "Guest — VIEW" else "Site Manager — PRO",    dark)
                }

                // Settings Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(listOf(ElectricBlue, DeepViolet))
                        )
                        .clickable(onClick = onSettings)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("Workspace Settings", fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
private fun ProfileDetailRow(icon: ImageVector, label: String, value: String, dark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (dark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ElectricBlue.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = ElectricBlue, modifier = Modifier.size(17.dp))
        }
        Column {
            Text(label, fontSize = 10.sp, color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (dark) PlatinumWhite else Color(0xFF0F172A))
        }
    }
}

// ─── Empty Project Card ───────────────────────────────────────────────────────
@Composable
private fun EmptyProjectCard(dark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (dark) Brush.linearGradient(listOf(Color(0xFF111827), Color(0xFF0F172A)))
                else Brush.linearGradient(listOf(Color.White, Color(0xFFF8FAFC)))
            )
            .border(1.dp, if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Dashboard,
                null,
                tint     = if (dark) Color(0xFF334155) else Color(0xFFCBD5E1),
                modifier = Modifier.size(48.dp)
            )
            Text(
                "No Active Project",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Black,
                color      = if (dark) PlatinumWhite else Color(0xFF0F172A)
            )
            Text(
                "Create or select a project to begin tracking your site operations.",
                fontSize   = 13.sp,
                color      = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
                textAlign  = TextAlign.Center
            )
        }
    }
}

// ─── Background Picker Dialog ─────────────────────────────────────────────────
@Composable
private fun BackgroundPickerDialog(
    dark: Boolean,
    proj: Project,
    customUrlInput: String,
    onUrlChange: (String) -> Unit,
    onApply: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor  = if (dark) Color(0xFF0F172A) else Color.White,
        shape           = RoundedCornerShape(28.dp),
        title = {
            Text(
                "Card Visual Theme",
                fontWeight = FontWeight.Black,
                color      = if (dark) PlatinumWhite else Color(0xFF0F172A)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val presets = listOf(
                    "preset_cyber_blueprint" to "⚡ Cyber Blueprint",
                    "preset_sunset_construct" to "🌅 Sunset Construct",
                    "preset_golden_truss"    to "✨ Golden Truss",
                    "preset_forest_mason"    to "🌿 Forest Mason",
                    "preset_friction_neon"   to "💜 Amethyst Neon"
                )
                presets.forEach { (key, label) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (dark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                            .border(1.dp, ElectricBlue.copy(0.2f), RoundedCornerShape(14.dp))
                            .clickable { onApply(key) }
                            .padding(14.dp)
                    ) {
                        Text(label, fontWeight = FontWeight.Bold, color = if (dark) PlatinumWhite else Color(0xFF0F172A))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Custom Image URL",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color      = ElectricBlue
                )
                OutlinedTextField(
                    value         = customUrlInput,
                    onValueChange = onUrlChange,
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text("https://...", fontSize = 12.sp) },
                    shape         = RoundedCornerShape(14.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue
                    )
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(ElectricBlue, DeepViolet)))
                    .clickable { if (customUrlInput.isNotBlank()) onApply(customUrlInput.trim()) else onDismiss() }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Apply", fontWeight = FontWeight.Black, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8))
            }
        }
    )
}

// ─── Curved Polygon Backdrop (unchanged, kept for compatibility) ───────────────
@Composable
fun CurvedPolygonBackdrop(style: String, darkTheme: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp))) {
        val w = size.width; val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas
        when (style) {
            "preset_cyber_blueprint" -> {
                drawRect(Brush.verticalGradient(
                    if (darkTheme) listOf(Color(0xFF020817), Color(0xFF0D1B2A)) else listOf(Color(0xFFE0F2FE), Color(0xFFF1F5F9))
                ))
                val gridColor = if (darkTheme) ElectricBlue.copy(0.06f) else Color(0xFF0284C7).copy(0.05f)
                val step = 36f
                for (x in 0..w.toInt() step step.toInt()) drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), h), 0.8f)
                for (y in 0..h.toInt() step step.toInt()) drawLine(gridColor, Offset(0f, y.toFloat()), Offset(w, y.toFloat()), 0.8f)
                val path = Path().apply {
                    moveTo(w * 0.5f, 0f); lineTo(w, 0f); lineTo(w, h * 0.6f)
                    cubicTo(w * 0.85f, h * 0.4f, w * 0.7f, h * 0.2f, w * 0.5f, 0f); close()
                }
                drawPath(path, Brush.radialGradient(listOf(ElectricBlue.copy(0.12f), Color.Transparent), Offset(w, 0f), w * 0.45f))
            }
            "preset_sunset_construct" -> drawRect(Brush.linearGradient(
                if (darkTheme) listOf(Color(0xFF3B0764), Color(0xFF7C2D12), Color(0xFF0F172A))
                else listOf(Color(0xFFFFF1F2), Color(0xFFFFEDD5))
            ))
            "preset_golden_truss"    -> drawRect(Brush.verticalGradient(
                if (darkTheme) listOf(Color(0xFF1C1917), Color(0xFF78350F)) else listOf(Color(0xFFFFFBEB), Color(0xFFFDE68A))
            ))
            "preset_forest_mason"    -> drawRect(Brush.verticalGradient(
                if (darkTheme) listOf(Color(0xFF022C22), Color(0xFF064E3B)) else listOf(Color(0xFFECFDF5), Color(0xFFBBF7D0))
            ))
            else                     -> drawRect(Brush.verticalGradient(
                if (darkTheme) listOf(Color(0xFF1E1B4B), Color(0xFF0F172A)) else listOf(Color(0xFFF5F3FF), Color(0xFFE0E7FF))
            ))
        }
        // Blueprint linework
        val accent = when (style) {
            "preset_golden_truss"    -> RoyalGold
            "preset_forest_mason"   -> CyberGreen
            "preset_sunset_construct" -> Color(0xFFEC4899)
            else                    -> ElectricBlue
        }.copy(if (darkTheme) 0.18f else 0.14f)

        for (i in 0..5) {
            val y = h * (0.12f + i * 0.14f)
            drawLine(accent.copy(0.10f), Offset(w * 0.06f, y), Offset(w * 0.94f, y + (i % 2) * 16f), 1f)
        }
        val truss = Path().apply {
            moveTo(w * 0.06f, h * 0.80f)
            lineTo(w * 0.28f, h * 0.50f)
            lineTo(w * 0.50f, h * 0.80f)
            lineTo(w * 0.72f, h * 0.50f)
            lineTo(w * 0.94f, h * 0.70f)
        }
        drawPath(truss, accent, style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

// ─── Financial Snapshot Card ──────────────────────────────────────────────────
@Composable
private fun FinancialSnapshotCard(
    dark: Boolean,
    revenue: Double,
    expenses: Double,
    netProfit: Double,
    pendingPayments: Double,
    selectedFilter: String,
    showFilterDropdown: Boolean,
    onFilterExpand: () -> Unit,
    onFilterSelect: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (dark) Brush.linearGradient(listOf(Color(0xFF0C1322), Color(0xFF060A13)))
                else Brush.linearGradient(listOf(Color.White, Color(0xFFF1F5F9)))
            )
            .border(
                1.dp,
                if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FINANCIAL SNAPSHOT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (dark) Color.White else Color(0xFF0F172A),
                    letterSpacing = 1.sp
                )

                // Filter Dropdown
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (dark) Color(0xFF111827) else Color(0xFFF1F5F9))
                            .border(1.dp, if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable(onClick = onFilterExpand)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = selectedFilter,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) ElectricBlue else DeepViolet
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (dark) ElectricBlue else DeepViolet,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showFilterDropdown,
                        onDismissRequest = onFilterExpand,
                        modifier = Modifier.background(if (dark) Color(0xFF0F172A) else Color.White)
                    ) {
                        listOf("This Month", "Last Month", "This Quarter", "All Time").forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, color = if (dark) Color.White else Color.Black) },
                                onClick = { onFilterSelect(opt) }
                            )
                        }
                    }
                }
            }

            // Content
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 500.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Column: Metrics
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricSnapshotRow("Total Revenue", formatRupees(revenue), CyberGreen, dark)
                            MetricSnapshotRow("Total Expenses", formatRupees(expenses), Color(0xFFFF6B6B), dark)
                            MetricSnapshotRow("Net Profit", formatRupees(netProfit), ElectricBlue, dark)
                            MetricSnapshotRow("Pending Payments", formatRupees(pendingPayments), NeonOrange, dark)
                        }

                        // Right Column: Chart
                        Column(
                            modifier = Modifier.weight(1.2f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Cash Flow ($selectedFilter)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8)
                            )
                            Spacer(Modifier.height(8.dp))
                            CashFlowLineChart(
                                dark = dark,
                                netProfitText = formatRupees(netProfit),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricSnapshotRow("Total Revenue", formatRupees(revenue), CyberGreen, dark)
                            MetricSnapshotRow("Total Expenses", formatRupees(expenses), Color(0xFFFF6B6B), dark)
                            MetricSnapshotRow("Net Profit", formatRupees(netProfit), ElectricBlue, dark)
                            MetricSnapshotRow("Pending Payments", formatRupees(pendingPayments), NeonOrange, dark)
                        }
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Cash Flow ($selectedFilter)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8)
                            )
                            Spacer(Modifier.height(8.dp))
                            CashFlowLineChart(
                                dark = dark,
                                netProfitText = formatRupees(netProfit),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricSnapshotRow(
    label: String,
    value: String,
    valueColor: Color,
    dark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (dark) Color(0xFF111827) else Color(0xFFF8FAFC))
            .border(1.dp, if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = valueColor
        )
    }
}

@Composable
private fun CashFlowLineChart(
    dark: Boolean,
    netProfitText: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val leftPadding = 36.dp.toPx()
        val rightPadding = 44.dp.toPx()
        val topPadding = 10.dp.toPx()
        val bottomPadding = 20.dp.toPx()

        val chartWidth = w - leftPadding - rightPadding
        val chartHeight = h - topPadding - bottomPadding

        val yLabels = listOf("10L", "5L", "0", "-5L")
        val gridLinesCount = yLabels.size

        val textPaint = android.graphics.Paint().apply {
            color = if (dark) 0xFF64748B.toInt() else 0xFF94A3B8.toInt()
            textSize = 8.sp.toPx()
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }

        for (i in 0 until gridLinesCount) {
            val y = topPadding + (i.toFloat() / (gridLinesCount - 1)) * chartHeight
            drawLine(
                color = if (dark) Color(0x1F64748B) else Color(0x1F94A3B8),
                start = Offset(leftPadding, y),
                end = Offset(leftPadding + chartWidth, y),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText(
                yLabels[i],
                leftPadding - 6.dp.toPx(),
                y + 3.dp.toPx(),
                textPaint
            )
        }

        val xLabels = listOf("1 May", "10 May", "20 May", "30 May")
        val xPositionsCount = xLabels.size
        val xLabelPaint = android.graphics.Paint().apply {
            color = if (dark) 0xFF64748B.toInt() else 0xFF94A3B8.toInt()
            textSize = 8.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        for (i in 0 until xPositionsCount) {
            val x = leftPadding + (i.toFloat() / (xPositionsCount - 1)) * chartWidth
            drawContext.canvas.nativeCanvas.drawText(
                xLabels[i],
                x,
                h - 4.dp.toPx(),
                xLabelPaint
            )
        }

        val incomePoints = listOf(0.4f, 0.55f, 0.48f, 0.65f, 0.6f, 0.8f, 0.85f)
        val expensePoints = listOf(0.18f, 0.22f, 0.2f, 0.32f, 0.28f, 0.42f, 0.45f)

        fun drawLineGraph(points: List<Float>, lineColor: Color, fillColor: Color) {
            val path = Path()
            val fillPath = Path()

            val getPtX = { idx: Int -> leftPadding + (idx.toFloat() / (points.size - 1)) * chartWidth }
            val getPtY = { idx: Int -> topPadding + chartHeight - (points[idx] * chartHeight) }

            path.moveTo(getPtX(0), getPtY(0))
            fillPath.moveTo(getPtX(0), topPadding + chartHeight)
            fillPath.lineTo(getPtX(0), getPtY(0))

            for (i in 0 until points.size - 1) {
                val x1 = getPtX(i); val y1 = getPtY(i)
                val x2 = getPtX(i + 1); val y2 = getPtY(i + 1)
                val cx = x1 + (x2 - x1) / 2f
                path.cubicTo(cx, y1, cx, y2, x2, y2)
                fillPath.cubicTo(cx, y1, cx, y2, x2, y2)
            }

            fillPath.lineTo(getPtX(points.size - 1), topPadding + chartHeight)
            fillPath.close()

            drawPath(fillPath, Brush.verticalGradient(listOf(fillColor.copy(alpha = 0.15f), Color.Transparent)))
            drawPath(path, lineColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

            for (i in points.indices) {
                drawCircle(lineColor, 3.dp.toPx(), Offset(getPtX(i), getPtY(i)))
                drawCircle(Color.White, 1.dp.toPx(), Offset(getPtX(i), getPtY(i)))
            }
        }

        drawLineGraph(expensePoints, Color(0xFFFF6B6B), Color(0xFFFF6B6B))
        drawLineGraph(incomePoints, CyberGreen, CyberGreen)

        val finalX = leftPadding + chartWidth
        val finalY = topPadding + chartHeight - (incomePoints.last() * chartHeight)

        val tooltipPaint = android.graphics.Paint().apply {
            color = if (dark) 0xFF0F1729.toInt() else 0xFFFFFFFF.toInt()
            style = android.graphics.Paint.Style.FILL
            isAntiAlias = true
            setShadowLayer(8f, 0f, 4f, 0x40000000)
        }
        val tooltipBorderPaint = android.graphics.Paint().apply {
            color = if (dark) 0x33FFFFFF.toInt() else 0x1F6366F1.toInt()
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 1.dp.toPx()
            isAntiAlias = true
        }
        val tooltipTextPaint = android.graphics.Paint().apply {
            color = if (dark) 0xFF00FF87.toInt() else 0xFF065F46.toInt()
            textSize = 8.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val boxWidth = 56.dp.toPx()
        val boxHeight = 18.dp.toPx()
        val boxX = finalX - boxWidth / 2f - 4.dp.toPx()
        val boxY = finalY - boxHeight - 8.dp.toPx()

        drawContext.canvas.nativeCanvas.drawRoundRect(
            boxX, boxY, boxX + boxWidth, boxY + boxHeight,
            6.dp.toPx(), 6.dp.toPx(),
            tooltipPaint
        )
        drawContext.canvas.nativeCanvas.drawRoundRect(
            boxX, boxY, boxX + boxWidth, boxY + boxHeight,
            6.dp.toPx(), 6.dp.toPx(),
            tooltipBorderPaint
        )
        drawContext.canvas.nativeCanvas.drawText(
            netProfitText,
            boxX + boxWidth / 2f,
            boxY + boxHeight / 2f + 3.dp.toPx(),
            tooltipTextPaint
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = CyberGreen, label = "Income")
        LegendItem(color = Color(0xFFFF6B6B), label = "Expenses")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
fun formatRupees(value: Double): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    fmt.maximumFractionDigits = 0
    fmt.minimumFractionDigits = 0
    return fmt.format(value)
}

fun scaffoldStateToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}