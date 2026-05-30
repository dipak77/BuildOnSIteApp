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

            // ── Live Status Banner ──
            item {
                LiveStatusBanner(dark = dark)
            }

            // ── Project Hero Card ──
            item {
                val proj = currentProject
                if (proj != null) {
                    EnhancedProjectHeroCard(
                        proj               = proj,
                        dark               = dark,
                        netBalance         = netBalance,
                        allProjects        = allProjects,
                        allWorkersCount    = allWorkers.size,
                        showProjectSwitcher = showProjectSwitcher,
                        onProjectSwitcherChange = { showProjectSwitcher = it },
                        onProjectSelected  = { p ->
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

            // ── Overview Header with Filter ──
            item {
                EnhancedSectionHeader(
                    title    = "Financial Overview",
                    subtitle = "Real-time metrics",
                    dark     = dark,
                    action   = {
                        PremiumFilterChip(
                            selected = selectedFilter,
                            options  = listOf("Today", "This Week", "This Month", "All Time"),
                            expanded = showFilterDropdown,
                            onExpand = { showFilterDropdown = !showFilterDropdown },
                            onSelect = { selectedFilter = it; showFilterDropdown = false },
                            dark     = dark
                        )
                    }
                )
            }

            // ── KPI Cards Row ──
            item {
                EnhancedKpiGrid(
                    dark         = dark,
                    moneyIn      = if (moneyIn <= 0) 850_000.0 else moneyIn,
                    moneyOut     = if (moneyOut <= 0) 465_000.0 else moneyOut,
                    netBalance   = if (netBalance == 0.0) 385_000.0 else netBalance,
                    pendingTasks = if (totalTasks <= 0) 12 else pendingTasks,
                    overdueTasks = overdueTasks.coerceAtLeast(2)
                )
            }

            // ── Gauge Panels ──
            item {
                EnhancedSectionHeader(title = "Progress Analytics", subtitle = "Live tracking", dark = dark)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    EnhancedGaugeCard(
                        modifier     = Modifier.weight(1f).clickable { viewModel.currentScreen = AppScreen.Money },
                        title        = "Budget Health",
                        percentage   = if (budgetPct <= 0) 68 else budgetPct,
                        centerLabel  = "Utilized",
                        accentColor  = ElectricBlue,
                        secondColor  = DeepViolet,
                        dark         = dark,
                        rows         = listOf(
                            Triple(ElectricBlue, "Budget",  formatRupees(totalBudget)),
                            Triple(CyberGreen,   "Spent",   formatRupees(if (totalSpent <= 0) 816_000.0 else totalSpent)),
                            Triple(NeonOrange,   "Left",    formatRupees(if (remaining <= 0) 434_000.0 else remaining))
                        ),
                        ctaText      = "Full Report →",
                        onCtaClick   = { viewModel.currentScreen = AppScreen.Money }
                    )
                    EnhancedGaugeCard(
                        modifier     = Modifier.weight(1f).clickable { viewModel.currentScreen = AppScreen.Tasks },
                        title        = "Task Velocity",
                        percentage   = if (taskPct <= 0f) 75 else (taskPct * 100).toInt(),
                        centerLabel  = "Complete",
                        accentColor  = DeepViolet,
                        secondColor  = Color(0xFFEC4899),
                        dark         = dark,
                        rows         = listOf(
                            Triple(CyberGreen,        "Done",     if (doneTasks <= 0) "24" else doneTasks.toString()),
                            Triple(RoyalGold,          "Active",   if (inProgressTasks <= 0) "6" else inProgressTasks.toString()),
                            Triple(Color(0xFFEF4444),  "Pending",  if (pendingTasks <= 0) "2" else pendingTasks.toString())
                        ),
                        ctaText      = "Manage Tasks →",
                        onCtaClick   = { viewModel.currentScreen = AppScreen.Tasks }
                    )
                }
            }

            // ── Site Status ──
            item {
                EnhancedSectionHeader(title = "Site Operations", subtitle = "Today — Live", dark = dark)
            }
            item {
                EnhancedSiteStatusCard(
                    dark           = dark,
                    workersPresent = if (allWorkers.isNotEmpty()) allWorkers.size else 48
                )
            }

            // ── Activity Feed ──
            item {
                EnhancedSectionHeader(
                    title    = "Activity Feed",
                    subtitle = "Recent events",
                    dark     = dark,
                    action   = {
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
            }
            item {
                EnhancedActivityFeed(dark = dark, context = context)
            }

            // ── Quick Actions ──
            item {
                EnhancedQuickActions(dark = dark, viewModel = viewModel)
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
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ElectricBlue, DeepViolet, Color(0xFFEC4899), RoyalGold, ElectricBlue),
                                start  = Offset(shimmerOff, 0f),
                                end    = Offset(shimmerOff + 200f, 80f)
                            )
                        )
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
            // Avatar with animated ring
            AnimatedAvatarButton(
                session     = session,
                dark        = dark,
                pulseAlpha  = pulseAlpha,
                onClick     = onProfileClick
            )
        }
    }
}

// ─── Glow Icon Button ─────────────────────────────────────────────────────────
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

// ─── Animated Avatar Button ───────────────────────────────────────────────────
@Composable
private fun AnimatedAvatarButton(
    session: Any?,
    dark: Boolean,
    pulseAlpha: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .drawBehind {
                drawCircle(
                    color  = ElectricBlue.copy(alpha = pulseAlpha * 0.3f),
                    radius = size.minDimension * 0.62f
                )
            }
            .clip(CircleShape)
            .border(
                2.dp,
                Brush.sweepGradient(listOf(ElectricBlue, DeepViolet, Color(0xFFEC4899), RoyalGold, ElectricBlue)),
                CircleShape
            )
            .padding(2.5.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "DH",
                fontWeight = FontWeight.Black,
                fontSize   = 15.sp,
                color      = if (dark) ElectricBlue else DeepViolet
            )
        }
    }
}

// ─── Live Status Banner ───────────────────────────────────────────────────────
@Composable
private fun LiveStatusBanner(dark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (dark) Brush.linearGradient(listOf(Color(0xFF0A2218), Color(0xFF052E16)))
                else Brush.linearGradient(listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5)))
            )
            .border(1.dp, CyberGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pulsing live dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .drawBehind {
                        drawCircle(CyberGreen.copy(alpha = pulseAlpha * 0.5f), radius = size.minDimension)
                    }
                    .clip(CircleShape)
                    .background(CyberGreen)
            )
            Text(
                text       = "LIVE • Site operations running smoothly",
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                color      = if (dark) CyberGreen else Color(0xFF065F46)
            )
        }
        Text(
            text       = "May 30, 2026",
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color      = if (dark) Color(0xFF34D399) else Color(0xFF059669)
        )
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
    val infiniteTransition = rememberInfiniteTransition(label = "hero_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f, targetValue = 700f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )

    val enterScale by animateFloatAsState(
        targetValue  = 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 100f),
        label        = "card_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = enterScale; scaleY = enterScale }
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    colors = if (dark)
                        listOf(Color(0xFF0B1628), Color(0xFF1A1040), Color(0xFF2D1B4E), Color(0xFF0B1628))
                    else
                        listOf(Color(0xFF1E3A8A), Color(0xFF4338CA), Color(0xFF6D28D9))
                )
            )
            .border(
                BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            ElectricBlue.copy(alpha = 0.7f),
                            DeepViolet.copy(alpha = 0.5f),
                            Color(0xFFEC4899).copy(alpha = 0.4f),
                            ElectricBlue.copy(alpha = 0.3f)
                        ),
                        start = Offset(shimmerOffset, 0f),
                        end   = Offset(shimmerOffset + 300f, 200f)
                    )
                ),
                RoundedCornerShape(32.dp)
            )
            .drawBehind {
                // Ambient glow circles
                drawCircle(
                    ElectricBlue.copy(alpha = 0.12f),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.05f, size.height * 0.05f)
                )
                drawCircle(
                    DeepViolet.copy(alpha = 0.15f),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.95f, size.height * 0.95f)
                )
                drawCircle(
                    Color(0xFFEC4899).copy(alpha = 0.08f),
                    radius = size.width * 0.35f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                )
            }
    ) {
        // Background pattern
        CurvedPolygonBackdrop(style = proj.customBackground ?: "preset_cyber_blueprint", darkTheme = true)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // ── Top Row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Project thumbnail
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x33FFFFFF))
                        .border(1.5.dp, Color(0x55FFFFFF), RoundedCornerShape(20.dp))
                ) {
                    AsyncImage(
                        model          = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=300",
                        contentDescription = "Project",
                        contentScale   = ContentScale.Crop,
                        modifier       = Modifier.fillMaxSize()
                    )
                    // Status badge overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(CyberGreen)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }

                Spacer(Modifier.width(16.dp))

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
                                .background(ElectricBlue.copy(alpha = 0.2f))
                                .border(0.5.dp, ElectricBlue.copy(0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "ACTIVE PROJECT",
                                fontSize   = 9.sp,
                                fontWeight = FontWeight.Black,
                                color      = ElectricBlue,
                                letterSpacing = 1.2.sp
                            )
                        }
                        Icon(
                            Icons.Default.SwapHoriz,
                            "Switch",
                            tint     = Color.White.copy(0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text       = proj.name,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Black,
                        color      = Color.White,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Place,
                            null,
                            tint     = Color.White.copy(0.6f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text       = proj.location,
                            fontSize   = 12.sp,
                            color      = Color.White.copy(0.7f),
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                    }

                    DropdownMenu(
                        expanded        = showProjectSwitcher,
                        onDismissRequest = { onProjectSwitcherChange(false) },
                        modifier        = Modifier.background(Color(0xF2050E1F))
                    ) {
                        allProjects.forEach { p ->
                            DropdownMenuItem(
                                text  = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            Modifier.size(8.dp).clip(CircleShape)
                                                .background(if (p.id == proj.id) CyberGreen else Color(0xFF475569))
                                        )
                                        Text(
                                            p.name,
                                            color      = Color.White,
                                            fontWeight = if (p.id == proj.id) FontWeight.Black else FontWeight.Medium,
                                            fontSize   = 13.sp
                                        )
                                    }
                                },
                                onClick = { onProjectSelected(p) }
                            )
                        }
                    }
                }

                // Customize button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(14.dp))
                        .clickable(onClick = onCustomizeClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.TrendingUp, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Divider ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color.White.copy(0.2f), Color.Transparent)
                        )
                    )
            )

            Spacer(Modifier.height(20.dp))

            // ── Balance Display ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "AVAILABLE BALANCE",
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.ExtraBold,
                        color         = Color.White.copy(0.55f),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    val prefix = if (netBalance >= 0) "+" else ""
                    Text(
                        text       = "$prefix${formatRupees(netBalance)}",
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.Black,
                        color      = Color.White,
                        style      = TextStyle(
                            shadow = Shadow(
                                color  = ElectricBlue.copy(0.5f),
                                offset = Offset(0f, 4f),
                                blurRadius = 12f
                            )
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MiniStatChip("↑ IN", formatRupees(850_000.0), CyberGreen)
                        MiniStatChip("↓ OUT", formatRupees(465_000.0), Color(0xFFFF6B6B))
                    }
                }

                // Cycle project button
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .drawBehind {
                            drawCircle(ElectricBlue.copy(0.25f), radius = size.minDimension * 0.65f)
                        }
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .border(1.5.dp, Color(0x66FFFFFF), CircleShape)
                        .clickable(onClick = onCycleProject),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SwapHoriz, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Info Chips Row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EnhancedHeroChip(
                    modifier = Modifier.weight(1f),
                    icon     = Icons.Default.Event,
                    label    = "Deadline",
                    value    = "May 30, 2026"
                )
                EnhancedHeroChip(
                    modifier = Modifier.weight(1f),
                    icon     = Icons.Default.People,
                    label    = "Team",
                    value    = "${if (allWorkersCount > 0) allWorkersCount else 32} Workers"
                )
                EnhancedHeroChip(
                    modifier = Modifier.weight(1f),
                    icon     = Icons.Default.Shield,
                    label    = "Status",
                    value    = "On Track"
                )
            }
        }
    }
}

@Composable
private fun MiniStatChip(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(0.15f))
            .border(0.5.dp, color.copy(0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black, color = color, letterSpacing = 0.5.sp)
        Text(value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.85f))
    }
}

@Composable
private fun EnhancedHeroChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x1AFFFFFF))
            .border(1.dp, Color(0x2AFFFFFF), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
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

// ─── Enhanced KPI Grid ────────────────────────────────────────────────────────
@Composable
private fun EnhancedKpiGrid(
    dark: Boolean,
    moneyIn: Double,
    moneyOut: Double,
    netBalance: Double,
    pendingTasks: Int,
    overdueTasks: Int
) {
    val cards = listOf(
        KpiData(
            title      = "Total Income",
            value      = formatRupees(moneyIn),
            badge      = "↑ 18.4%",
            positive   = true,
            icon       = Icons.Default.ArrowDownward,
            color      = CyberGreen,
            sparkline  = listOf(10f, 15f, 13f, 22f, 18f, 26f, 22f, 32f, 30f, 42f, 38f, 48f)
        ),
        KpiData(
            title      = "Total Expenses",
            value      = formatRupees(moneyOut),
            badge      = "↑ 10.2%",
            positive   = false,
            icon       = Icons.Default.ArrowUpward,
            color      = Color(0xFFFF6B6B),
            sparkline  = listOf(45f, 43f, 41f, 32f, 35f, 25f, 28f, 20f, 22f, 15f, 17f, 12f)
        ),
        KpiData(
            title      = "Net Balance",
            value      = formatRupees(netBalance),
            badge      = "↑ 22.1%",
            positive   = true,
            icon       = Icons.Default.AccountBalanceWallet,
            color      = ElectricBlue,
            sparkline  = listOf(8f, 12f, 11f, 20f, 16f, 25f, 21f, 32f, 28f, 38f, 35f, 45f)
        ),
        KpiData(
            title      = "Pending Tasks",
            value      = pendingTasks.toString(),
            badge      = "$overdueTasks overdue",
            positive   = false,
            icon       = Icons.Default.TaskAlt,
            color      = DeepViolet,
            sparkline  = listOf(15f, 18f, 16f, 24f, 22f, 18f, 20f, 14f, 16f, 10f, 12f, 8f)
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EnhancedKpiCard(data = cards[0], dark = dark, modifier = Modifier.weight(1f))
            EnhancedKpiCard(data = cards[1], dark = dark, modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EnhancedKpiCard(data = cards[2], dark = dark, modifier = Modifier.weight(1f))
            EnhancedKpiCard(data = cards[3], dark = dark, modifier = Modifier.weight(1f))
        }
    }
}

private data class KpiData(
    val title: String, val value: String, val badge: String,
    val positive: Boolean, val icon: ImageVector,
    val color: Color, val sparkline: List<Float>
)

@Composable
private fun EnhancedKpiCard(data: KpiData, dark: Boolean, modifier: Modifier = Modifier) {
    val cardBg = if (dark)
        Brush.linearGradient(listOf(Color(0xFF111827), Color(0xFF0F172A)))
    else
        Brush.linearGradient(listOf(Color.White, Color(0xFFF8FAFC)))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(1.dp, data.color.copy(0.2f), RoundedCornerShape(24.dp))
            .drawBehind {
                drawCircle(
                    color  = data.color.copy(0.08f),
                    radius = size.width * 0.45f,
                    center = Offset(size.width, 0f)
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        data.title,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        maxLines   = 1
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(data.color.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(data.icon, null, tint = data.color, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text       = data.value,
                    fontSize   = 19.sp,
                    fontWeight = FontWeight.Black,
                    color      = if (dark) PlatinumWhite else Color(0xFF0F172A),
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            (if (data.positive) CyberGreen else Color(0xFFFF6B6B)).copy(0.12f)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text       = data.badge,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Black,
                        color      = if (data.positive) CyberGreen else Color(0xFFFF6B6B)
                    )
                }

                Spacer(Modifier.height(10.dp))
            }

            // Sparkline at bottom
            PremiumSparkline(
                points    = data.sparkline,
                color     = data.color,
                dark      = dark,
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
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

// ─── Enhanced Site Status Card ────────────────────────────────────────────────
@Composable
private fun EnhancedSiteStatusCard(
    dark: Boolean,
    workersPresent: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "site_card_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue =  0.4f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (dark) Brush.linearGradient(listOf(Color(0xFF0A1628), Color(0xFF0F172A)))
                else Brush.linearGradient(listOf(Color.White, Color(0xFFF8FAFC)))
            )
            .border(1.dp, CyberGreen.copy(0.2f), RoundedCornerShape(28.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .drawBehind {
                                drawCircle(CyberGreen.copy(pulseAlpha * 0.5f), size.minDimension)
                            }
                            .clip(CircleShape)
                            .background(CyberGreen)
                    )
                    Text(
                        "Live Workforce",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = CyberGreen
                    )
                }
                Text(
                    "Today, 10:45 AM",
                    fontSize   = 11.sp,
                    color      = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8)
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                EnhancedSitePillar(Icons.Default.People,        CyberGreen,         workersPresent.toString(), "On Site",   dark)
                EnhancedSitePillar(Icons.Default.PersonOutline, Color(0xFFFF6B6B),  "4",                       "Absent",    dark)
                EnhancedSitePillar(Icons.Default.Schedule,      RoyalGold,          "2",                       "Late",      dark)
                EnhancedSitePillar(Icons.Default.Shield,        ElectricBlue,       "12",                      "Equipment", dark)
            }
        }
    }
}

@Composable
private fun EnhancedSitePillar(
    icon: ImageVector,
    color: Color,
    value: String,
    label: String,
    dark: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .drawBehind {
                    drawCircle(color.copy(0.15f), size.minDimension * 0.7f)
                }
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(0.12f))
                .border(1.dp, color.copy(0.3f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        }

        Text(
            value,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Black,
            color      = if (dark) PlatinumWhite else Color(0xFF0F172A)
        )

        Text(
            label,
            fontSize   = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color      = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
            textAlign  = TextAlign.Center
        )

        // Mini progress bar
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
        ) {
            val fill = when (label) {
                "On Site"   -> 0.92f
                "Absent"    -> 0.08f
                "Late"      -> 0.04f
                "Equipment" -> 1f
                else        -> 0.5f
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fill)
                    .clip(CircleShape)
                    .background(color)
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
    session: Any?,
    activeLocation: String,
    onDismiss: () -> Unit,
    onSettings: () -> Unit
) {
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
                    Text(
                        "DH",
                        fontWeight = FontWeight.Black,
                        fontSize   = 30.sp,
                        color      = ElectricBlue
                    )
                }

                Text(
                    "Dipak Harane",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Black,
                    color      = if (dark) PlatinumWhite else Color(0xFF0F172A)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileDetailRow(Icons.Default.Email,   "Email",    "haranedipak@gmail.com", dark)
                    ProfileDetailRow(Icons.Default.Place,   "Location", activeLocation,          dark)
                    ProfileDetailRow(Icons.Default.Shield,  "Role",     "Site Manager — PRO",    dark)
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