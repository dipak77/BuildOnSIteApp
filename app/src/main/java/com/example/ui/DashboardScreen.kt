package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val dark = viewModel.darkThemeEnabled
    val currentProject by viewModel.activeProject.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val allTasks by viewModel.tasks.collectAsState()
    val allProjects by viewModel.projects.collectAsState()
    val allWorkers by viewModel.workers.collectAsState()

    val context = LocalContext.current
    var showBackgroundPicker by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf("") }

    // Dynamic brand text gradient shifter animation
    val brandInfiniteTransition = rememberInfiniteTransition(label = "BrandShifterAnimation")
    val proGradientOffset by brandInfiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientOffset"
    )
    
    // Overview filter dropdown simulated state
    var selectedFilter by remember { mutableStateOf("This Month") }
    var showFilterDropdown by remember { mutableStateOf(false) }
    var showProjectSwitcher by remember { mutableStateOf(false) }
    var showProfileDetailsDialog by remember { mutableStateOf(false) }
    var showMenuDropdown by remember { mutableStateOf(false) }

    // Filtered lists for active project
    val projectTransactions = remember(allTransactions, currentProject) {
        val projId = currentProject?.id
        if (projId == null) emptyList()
        else allTransactions.filter { it.projectId == projId }
    }

    val projectTasks = remember(allTasks, currentProject) {
        val projId = currentProject?.id
        if (projId == null) emptyList()
        else allTasks.filter { it.projectId == projId }
    }

    // Dynamic Calculations
    val moneyIn = remember(projectTransactions) {
        projectTransactions.filter { it.type == "Money In" }.sumOf { it.amount }
    }
    val moneyOut = remember(projectTransactions) {
        projectTransactions.filter { it.type == "Money Out" }.sumOf { it.amount }
    }
    val netBalance = moneyIn - moneyOut

    val totalTasks = projectTasks.size
    val doneTasks = projectTasks.count { it.status == "Done" }
    val inProgressTasks = projectTasks.count { it.status == "In Progress" }
    val pendingTasks = projectTasks.count { it.status == "To Do" }
    // Overdue tasks are those that are of medium or high priority and not completed, or specifically marked "Overdue" (represented elegantly)
    val overdueTasks = projectTasks.count { it.status != "Done" && (it.priority == "High" || it.dueDate < "2026-05-27") }

    val taskPercentage = if (totalTasks > 0) doneTasks.toFloat() / totalTasks else 0.0f
    
    // Project budget stats (defaults to wireframe numbers if database seeds are unpushed)
    val totalBudget = currentProject?.budget ?: 1250000.0
    val totalSpent = moneyOut
    val remainingBudget = (totalBudget - totalSpent).coerceAtLeast(0.0)
    val spendingProgress = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    val budgetPercent = (spendingProgress * 100).toInt().coerceIn(0, 100)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. BRAND HEADER (MATCHES WIREFRAME PERFECTLY)
        // ==========================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Hamburguer Menu Trigger
                Box {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (dark) Color(0x1F111827) else Color(0x0F0F172A))
                            .clickable {
                                showMenuDropdown = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Main Menu",
                            tint = if (dark) Color.White else Color(0xFF0F172A)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenuDropdown,
                        onDismissRequest = { showMenuDropdown = false },
                        modifier = Modifier.background(if (dark) Color(0xFF1E293B) else Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Dashboard Screen", color = if (dark) Color.White else Color.Black) },
                            onClick = {
                                showMenuDropdown = false
                                viewModel.currentScreen = AppScreen.Dashboard
                            },
                            leadingIcon = { Icon(Icons.Default.Dashboard, contentDescription = null, tint = if (dark) NeonCyan else Color(0xFF4F46E5)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Money Tracker", color = if (dark) Color.White else Color.Black) },
                            onClick = {
                                showMenuDropdown = false
                                viewModel.currentScreen = AppScreen.Money
                            },
                            leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = if (dark) NeonGreen else Color(0xFF16A34A)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Site Workspace", color = if (dark) Color.White else Color.Black) },
                            onClick = {
                                showMenuDropdown = false
                                viewModel.currentScreen = AppScreen.Site
                            },
                            leadingIcon = { Icon(Icons.Default.EventAvailable, contentDescription = null, tint = if (dark) NeonPurple else Color(0xFF8B5CF6)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Tasks Board", color = if (dark) Color.White else Color.Black) },
                            onClick = {
                                showMenuDropdown = false
                                viewModel.currentScreen = AppScreen.Tasks
                            },
                            leadingIcon = { Icon(Icons.Default.Assignment, contentDescription = null, tint = if (dark) NeonPink else Color(0xFFEC4899)) }
                        )
                        DropdownMenuItem(
                            text = { Text("More Options", color = if (dark) Color.White else Color.Black) },
                            onClick = {
                                showMenuDropdown = false
                                viewModel.currentScreen = AppScreen.More
                            },
                            leadingIcon = { Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = if (dark) Color.LightGray else Color.Gray) }
                        )
                    }
                }

                // App Branding Title: ConstructPro (With premium animated gradient text)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Construct",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) Color.White else Color(0xFF0F172A)
                        )
                        val proGradient = Brush.linearGradient(
                            colors = listOf(NeonCyan, NeonPurple, NeonPink, NeonCyan),
                            start = Offset(proGradientOffset, 0f),
                            end = Offset(proGradientOffset + 180f, 180f)
                        )
                        Text(
                            text = "Pro",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            style = androidx.compose.ui.text.TextStyle(brush = proGradient)
                        )
                    }
                    Text(
                        text = "Build. Manage. Grow.",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) TextSecondary else TextSecondaryLight,
                        letterSpacing = 1.4.sp
                    )
                }

                // Bell Indicator + Avatar Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Theme Switcher Button replacing Notification Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (dark) Color(0x1F111827) else Color(0x0F0F172A))
                            .clickable {
                                viewModel.darkThemeEnabled = !viewModel.darkThemeEnabled
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Switch Theme",
                            tint = if (dark) NeonCyan else Color(0xFF4F46E5),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // User Profile image (With gorgeous animated gradient ring)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(listOf(NeonCyan, NeonPurple, NeonPink, NeonCyan)),
                                shape = CircleShape
                            )
                            .clickable {
                                showProfileDetailsDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val session by viewModel.userSession.collectAsState()
                        if (session?.photoUrl != null) {
                            AsyncImage(
                                model = session?.photoUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.matchParentSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // High-quality placeholder initials avatar
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "DH",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = if (dark) Color.White else Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. ACTIVE PROJECT HERO GRADIENT HEADER CARD
        // ==========================================
        item {
            val proj = currentProject
            if (proj != null) {
                // Linear Indigo/Blue Gradient background brushing with ultra premium metallic tones
                val gradientBg = Brush.linearGradient(
                    colors = if (dark) {
                        listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF311042))
                    } else {
                        listOf(Color(0xFFE0F2FE), Color(0xFFF1F5F9), Color(0xFFEDE9FE))
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(gradientBg)
                        .border(
                            BorderStroke(
                                1.5.dp,
                                Brush.linearGradient(listOf(GoldLight.copy(alpha = 0.5f), NeonCyan.copy(alpha = 0.3f), GoldDark.copy(alpha = 0.6f)))
                            ),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    // Embedded design background polygon patterns if preset is set
                    if (proj.customBackground != null && proj.customBackground.isNotBlank()) {
                        if (proj.customBackground != "preset_cyber_blueprint" && (proj.customBackground.startsWith("http://") || proj.customBackground.startsWith("https://"))) {
                            AsyncImage(
                                model = proj.customBackground,
                                contentDescription = "Custom Card Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize().opacityOverlay(0.25f)
                            )
                        } else {
                            CurvedPolygonBackdrop(style = proj.customBackground, darkTheme = dark)
                        }
                    }

                    // Front facing elements
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Top Row: Project Thumbnail + Text Info + Graph Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rounded Glass/Skyscraper thumbnail
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x33FFFFFF))
                            ) {
                                AsyncImage(
                                    // Professional modern glass architectural tower thumbnail
                                    model = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=300",
                                    contentDescription = "Project Cover Thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Text Project Metadata (Highly Interactive for Project Switching)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showProjectSwitcher = !showProjectSwitcher }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ACTIVE SITE DIRECTORY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White.copy(alpha = 0.82f),
                                        letterSpacing = 1.2.sp
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = "Switch",
                                        tint = Color.White.copy(alpha = 0.82f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = proj.name,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "Pin Icon",
                                        tint = Color.White.copy(alpha = 0.65f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = proj.location,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Frosted dropdown switcher overlay
                                DropdownMenu(
                                    expanded = showProjectSwitcher,
                                    onDismissRequest = { showProjectSwitcher = false },
                                    modifier = Modifier.background(Color(0xE60B0F19)) // Luxury frosted dark dropdown
                                ) {
                                    allProjects.forEach { p ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = p.name,
                                                    color = Color.White,
                                                    fontWeight = if (p.id == proj.id) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 13.sp
                                                )
                                            },
                                            onClick = {
                                                viewModel.selectedProjectId = p.id
                                                showProjectSwitcher = false
                                                scaffoldStateToast(context, "Loaded active project: ${p.name}")
                                            }
                                        )
                                    }
                                }
                            }

                            // Top Right Action Buttons (Theme Settings / Customizer)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Backdrop Theme/Background customizer button
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x28FFFFFF))
                                        .clickable { showBackgroundPicker = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Background Settings",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Middle: Available site balance & Switch Project (right aligned/center)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "AVAILABLE SITE BALANCE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.75f),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                // Format strictly according to Indian Rupee standard format (+₹6,85,000.00)
                                val balancePrefix = if (netBalance >= 0) "+" else ""
                                // Display the formatted rupees
                                val formattedRupee = formatIndianRupees(netBalance)
                                Text(
                                    text = "$balancePrefix$formattedRupee.00",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }                            // Sleek circular glass-styled shortcut button containing only the switch icon
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFFFFF))
                                    .border(1.dp, Color(0x4DFFFFFF), CircleShape)
                                    .clickable {
                                        if (allProjects.isNotEmpty()) {
                                            val currentIndex = allProjects.indexOfFirst { it.id == currentProject?.id }
                                            val nextIndex = (currentIndex + 1) % allProjects.size
                                            viewModel.selectedProjectId = allProjects[nextIndex].id
                                            scaffoldStateToast(context, "Switched project: ${allProjects[nextIndex].name}")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Switch Project",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Transparent boundary spacer line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.15f))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bottom Info: Date and Worker counter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Date section
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = "Date Frame",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "May 26, 2026",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            // Dynamic Workers section
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = "Staff Count",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Count of all workers, default dynamically seeded or fallback to 32
                                val workerNumDisplay = if (allWorkers.isNotEmpty()) allWorkers.size else 32
                                Text(
                                    text = "$workerNumDisplay Workers",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                // Empty Project State
                GlassCard(modifier = Modifier.fillMaxWidth(), darkTheme = dark) {
                    Text(
                        text = "No active project. Launch a new site operations project from the settings page.",
                        color = if (dark) TextSecondary else TextSecondaryLight
                    )
                }
            }
        }

        // ==========================================
        // 3. TITLE & DROPDOWN (OVERVIEW FILTER)
        // ==========================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overview",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) Color.White else Color(0xFF0F172A)
                )

                // Dropdown container
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (dark) Color(0x1F111827) else Color(0xFFFFFFFF))
                        .border(
                            1.dp,
                            if (dark) GlassBorderDark else Color(0x32000000),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { showFilterDropdown = !showFilterDropdown }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = selectedFilter,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) Color.White else Color(0xFF0F172A)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select segment",
                            tint = if (dark) NeonCyan else Color(0xFF4F46E5),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showFilterDropdown,
                        onDismissRequest = { showFilterDropdown = false },
                        modifier = Modifier.background(if (dark) Color(0xFF0F172A) else Color.White)
                    ) {
                        listOf("This Month", "Last Month", "All Time").forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, color = if (dark) Color.White else Color.Black) },
                                onClick = {
                                    selectedFilter = opt
                                    showFilterDropdown = false
                                    scaffoldStateToast(context, "Filtered by $opt")
                                }
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. OVERVIEW CARDS (2x2 GRID RESPONSIVE LAYOUT)
        // ==========================================
        item {
            val totalInVal = if (moneyIn <= 0) 850000.0 else moneyIn
            val totalOutVal = if (moneyOut <= 0) 465000.0 else moneyOut
            val netBalanceVal = totalInVal - totalOutVal
            val pendingTaskCount = if (totalTasks <= 0) 12 else totalTasks

            val totalInPoints = listOf(10f, 15f, 13f, 22f, 18f, 26f, 22f, 32f, 30f, 42f, 38f, 48f)
            val totalOutPoints = listOf(45f, 43f, 41f, 32f, 35f, 25f, 28f, 20f, 22f, 15f, 17f, 12f)
            val netBalancePoints = listOf(8f, 12f, 11f, 20f, 16f, 25f, 21f, 32f, 28f, 38f, 35f, 45f)
            val pendingTaskPoints = listOf(15f, 18f, 16f, 24f, 22f, 18f, 20f, 14f, 16f, 10f, 12f, 8f)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: Total In & Total Out
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: Total In
                    OverviewClassicStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total In",
                        value = formatIndianRupees(totalInVal),
                        badgeText = "↑ 18%",
                        badgeDesc = "vs last month",
                        badgePositive = true,
                        icon = Icons.Default.ArrowDownward,
                        iconColor = Color(0xFF10B981),
                        darkTheme = dark,
                        sparklinePoints = totalInPoints
                    )

                    // Card 2: Total Out
                    OverviewClassicStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Out",
                        value = formatIndianRupees(totalOutVal),
                        badgeText = "↑ 10%",
                        badgeDesc = "vs last month",
                        badgePositive = false, // Pink/Red styled negative growth
                        icon = Icons.Default.ArrowUpward,
                        iconColor = Color(0xFFF43F5E),
                        darkTheme = dark,
                        sparklinePoints = totalOutPoints
                    )
                }

                // Row 2: Net Balance & Pending Tasks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 3: Net Balance
                    OverviewClassicStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Net Balance",
                        value = formatIndianRupees(netBalanceVal),
                        badgeText = "↑ 22%",
                        badgeDesc = "vs last month",
                        badgePositive = true,
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = Color(0xFF0EA5E9),
                        darkTheme = dark,
                        sparklinePoints = netBalancePoints
                    )

                    // Card 4: Pending Tasks
                    OverviewClassicStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Pending Tasks",
                        value = pendingTaskCount.toString(),
                        badgeText = "${overdueTasks.coerceAtLeast(2)} overdue",
                        badgeDesc = "",
                        badgePositive = false, // Red subtext
                        icon = Icons.Default.TaskAlt,
                        iconColor = Color(0xFF8B5CF6),
                        darkTheme = dark,
                        sparklinePoints = pendingTaskPoints
                    )
                }
            }
        }

        // ==========================================
        // 5. BUDGET VS ACTUAL & TASK PROGRESS PANEL
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Card: Budget vs Actual
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.currentScreen = AppScreen.Money },
                    darkTheme = dark,
                    padding = 12.dp,
                    borderColor = if (dark) GlassBorderDark else null
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Budget vs Actual",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) Color.White else Color(0xFF0F172A),
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val resolvedSpendingPercent = if (budgetPercent <= 0) 68 else budgetPercent
                        ThreeQuarterArcGauge(
                            percentage = resolvedSpendingPercent.toFloat() / 100f,
                            color = if (dark) Color(0xFF3B82F6) else Color(0xFF1D4ED8),
                            darkTheme = dark,
                            modifier = Modifier.size(104.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$resolvedSpendingPercent%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (dark) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Spent",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (dark) TextSecondary else TextSecondaryLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Key detail rows under the donut chart
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (dark) Color(0xFF3B82F6) else Color(0xFF1D4ED8))
                                )
                                Text("Budget", fontSize = 10.sp, color = if (dark) TextSecondary else TextSecondaryLight, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("₹12,00,000", fontSize = 10.sp, color = if (dark) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (dark) Color(0xFF10B981) else Color(0xFF047857))
                                )
                                Text("Actual", fontSize = 10.sp, color = if (dark) TextSecondary else TextSecondaryLight, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("₹8,16,000", fontSize = 10.sp, color = if (dark) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bottom View Full Report
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.currentScreen = AppScreen.Money }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View Full Report",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dark) NeonCyan else Color(0xFF4F46E5)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Arrow link",
                                tint = if (dark) NeonCyan else Color(0xFF4F46E5),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Right Card: Task Progress
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.currentScreen = AppScreen.Tasks },
                    darkTheme = dark,
                    padding = 12.dp,
                    borderColor = if (dark) GlassBorderDark else null
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Task Progress",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) Color.White else Color(0xFF0F172A),
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val resolvedTaskPercent = if (taskPercentage <= 0.0) 75 else (taskPercentage * 100).toInt()
                        ThreeQuarterArcGauge(
                            percentage = resolvedTaskPercent.toFloat() / 100f,
                            color = if (dark) Color(0xFF8B5CF6) else Color(0xFF6D28D9),
                            darkTheme = dark,
                            modifier = Modifier.size(104.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$resolvedTaskPercent%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (dark) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Done",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (dark) TextSecondary else TextSecondaryLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Shows completed tasks count
                        val completedDisplay = if (doneTasks <= 0) 24 else doneTasks
                        val totalDisplay = if (totalTasks <= 0) 32 else totalTasks
                        Text(
                            text = "$completedDisplay / $totalDisplay Completed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) Color.White else Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // Bottom View All Tasks
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.currentScreen = AppScreen.Tasks }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View All Tasks",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dark) NeonCyan else Color(0xFF4F46E5)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Arrow link",
                                tint = if (dark) NeonCyan else Color(0xFF4F46E5),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // SITE STATUS ROW CARD
        // ==========================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Site Status",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = "Today",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) TextSecondary else TextSecondaryLight
                    )
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    darkTheme = dark,
                    padding = 16.dp,
                    borderColor = if (dark) GlassBorderDark else null
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SiteStatusPillar(
                            icon = Icons.Default.People,
                            tint = Color(0xFF10B981),
                            value = "48",
                            label = "Workers Present",
                            darkTheme = dark,
                            modifier = Modifier.weight(1f)
                        )
                        SiteStatusPillar(
                            icon = Icons.Default.PersonOutline,
                            tint = Color(0xFFF43F5E),
                            value = "4",
                            label = "Absent",
                            darkTheme = dark,
                            modifier = Modifier.weight(1f)
                        )
                        SiteStatusPillar(
                            icon = Icons.Default.Schedule,
                            tint = Color(0xFFF59E0B),
                            value = "2",
                            label = "Late",
                            darkTheme = dark,
                            modifier = Modifier.weight(1f)
                        )
                        SiteStatusPillar(
                            icon = Icons.Default.Shield,
                            tint = Color(0xFF0EA5E9),
                            value = "12",
                            label = "Equipment Active",
                            darkTheme = dark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ==========================================
        // RECENT ACTIVITY HEADER & TIMELINE
        // ==========================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) Color.White else Color(0xFF0F172A)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable {
                        viewModel.currentScreen = AppScreen.Tasks
                    }
                ) {
                    Text(
                        text = "View All Activity",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) NeonCyan else Color(0xFF4F46E5)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Arrow right icon",
                        tint = if (dark) NeonCyan else Color(0xFF4F46E5),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                darkTheme = dark,
                padding = 12.dp,
                borderColor = if (dark) GlassBorderDark else null
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RecentActivityRow(
                        icon = Icons.Default.CreditCard,
                        iconBgColor = if (dark) Color(0x2810B981) else Color(0xFFECFDF5),
                        iconTint = Color(0xFF10B981),
                        title = "Payment received from Metro Rail Corp",
                        timestamp = "Today, 10:30 AM",
                        rightInfo = "₹2,50,000",
                        rightColor = Color(0xFF10B981),
                        darkTheme = dark,
                        onClick = {
                            scaffoldStateToast(context, "Clicked Metro Rail Corp payment")
                        },
                        drawStartLine = false,
                        drawEndLine = true
                    )

                    RecentActivityRow(
                        icon = Icons.Default.Layers,
                        iconBgColor = if (dark) Color(0x280EA5E9) else Color(0xFFF0F9FF),
                        iconTint = Color(0xFF0EA5E9),
                        title = "Cement stock updated",
                        timestamp = "Today, 09:15 AM",
                        rightInfo = "120 Bags",
                        rightColor = Color(0xFF0EA5E9),
                        darkTheme = dark,
                        onClick = {
                            scaffoldStateToast(context, "Clicked Cement stock update")
                        },
                        drawStartLine = true,
                        drawEndLine = true
                    )

                    RecentActivityRow(
                        icon = Icons.Default.Build,
                        iconBgColor = if (dark) Color(0x288B5CF6) else Color(0xFFF5F3FF),
                        iconTint = Color(0xFF8B5CF6),
                        title = "Electrical work task completed",
                        timestamp = "Yesterday, 05:40 PM",
                        rightInfo = "Task #EL-245",
                        rightColor = Color(0xFF8B5CF6),
                        darkTheme = dark,
                        onClick = {
                            scaffoldStateToast(context, "Clicked Electrical work task")
                        },
                        drawStartLine = true,
                        drawEndLine = false
                    )
                }
            }
        }

        // ==========================================
        // 8. QUICK ACTIONS ROW (ICON BUTTONS + DIALOG SHORTCUTS)
        // ==========================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .background(
                        if (dark) Color(0x13111827) else Color(0x06000000),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Add Income Button
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Add Income",
                    tint = Color(0xFF10B981),
                    darkTheme = dark,
                    onClick = {
                        viewModel.transactionTypePreset = "Money In"
                        viewModel.showTransactionDialog = true
                    }
                )

                // Quick Add Expense Button
                QuickActionButton(
                    icon = Icons.Default.ArrowUpward,
                    label = "Add Expense",
                    tint = Color(0xFFF43F5E),
                    darkTheme = dark,
                    onClick = {
                        viewModel.transactionTypePreset = "Money Out"
                        viewModel.showTransactionDialog = true
                    }
                )

                // Quick Add Worker Button
                QuickActionButton(
                    icon = Icons.Default.Person,
                    label = "Add Worker",
                    tint = Color(0xFF3B82F6),
                    darkTheme = dark,
                    onClick = {
                        viewModel.showWorkerDialog = true
                    }
                )

                // Quick Add Task Button
                QuickActionButton(
                    icon = Icons.Default.Task,
                    label = "Add Task",
                    tint = Color(0xFF8B5CF6),
                    darkTheme = dark,
                    onClick = {
                        viewModel.showTaskDialog = true
                    }
                )
            }
        }
    }

    // ==========================================
    // BACKDROP CUSTOMIZER ALERT POPUP dialog
    // ==========================================
    val proj = currentProject
    if (showBackgroundPicker && proj != null) {
        AlertDialog(
            onDismissRequest = { showBackgroundPicker = false },
            title = {
                Text(
                    text = "Active Graphic Theme Picker",
                    color = if (dark) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Alter the active blueprint card visual mesh pattern style or enter customized project cover art background.",
                        fontSize = 12.sp,
                        color = if (dark) TextSecondary else TextSecondaryLight
                    )

                    val presets = listOf(
                        "preset_cyber_blueprint" to "Cyber Blueprint (Neon Grid)",
                        "preset_sunset_construct" to "Sunset Construct (Truss Polygon)",
                        "preset_golden_truss" to "Golden Truss (Premium Geo)",
                        "preset_forest_mason" to "Forest Mason (Sage Wave)",
                        "preset_friction_neon" to "Friction Neon (Amethyst Jib)"
                    )

                    presets.forEach { (styleKey, label) ->
                        Button(
                            onClick = {
                                viewModel.updateProjectBackground(proj, styleKey)
                                showBackgroundPicker = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dark) Color(0xFF1E293B) else Color(0x1F0F172A)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label, color = if (dark) Color.White else Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Cover Backdrop Photo URL Link", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (dark) NeonCyan else Color(0xFF4F46E5))
                    
                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://image_host.org/skyline.jpg", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (dark) NeonCyan else Color(0xFF4F46E5)
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customUrlInput.trim().isNotBlank()) {
                            viewModel.updateProjectBackground(proj, customUrlInput.trim())
                        }
                        showBackgroundPicker = false
                    }
                ) {
                    Text("Apply", color = if (dark) NeonCyan else Color(0xFF4F46E5), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.updateProjectBackground(proj, "preset_cyber_blueprint")
                        showBackgroundPicker = false
                    }) {
                        Text("Reset default", color = Color.Red)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(onClick = { showBackgroundPicker = false }) {
                        Text("Cancel", color = if (dark) Color.White else Color.Black)
                    }
                }
            }
        )
    }

    // ==========================================
    // USER PROFILE DETAILS MODAL DIALOG
    // ==========================================
    if (showProfileDetailsDialog) {
        val session by viewModel.userSession.collectAsState()
        val userName = session?.displayName ?: "Dipak Harane"
        val userEmail = session?.email ?: "haranedipak@gmail.com"
        val activeLocation = currentProject?.location ?: "Mumbai Sector 7, MH"

        GlassModalDialog(
            visible = showProfileDetailsDialog,
            onDismiss = { showProfileDetailsDialog = false },
            title = "Site Operations Profile",
            darkTheme = dark,
            glowColor = if (dark) NeonCyan else Color(0xFF4F46E5)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular Avatar
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            Brush.linearGradient(
                                listOf(
                                    if (dark) NeonCyan else Color(0xFF4F46E5),
                                    if (dark) NeonPurple else Color(0xFF8B5CF6)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (session?.photoUrl != null) {
                        AsyncImage(
                            model = session?.photoUrl,
                            contentDescription = "User Avatar",
                            modifier = Modifier.matchParentSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Initials placeholder with gradient background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                            if (dark) Color(0xFF0F172A) else Color(0xFFCBD5E1)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = if (dark) Color.White else Color(0xFF0F172A)
                            )
                        }
                    }
                }

                // Details Fields
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileDetailCard(
                        icon = Icons.Default.Person,
                        label = "User Name",
                        value = userName,
                        darkTheme = dark
                    )

                    ProfileDetailCard(
                        icon = Icons.Default.Email,
                        label = "Email Address",
                        value = userEmail,
                        darkTheme = dark
                    )

                    ProfileDetailCard(
                        icon = Icons.Default.Place,
                        label = "Operational Site / Location",
                        value = activeLocation,
                        darkTheme = dark
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation button to settings page
                Button(
                    onClick = {
                        showProfileDetailsDialog = false
                        viewModel.currentScreen = AppScreen.More
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (dark) NeonCyan else Color(0xFF4F46E5),
                        contentColor = if (dark) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Go to Workspace Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileDetailCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    darkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (darkTheme) Color(0x1F1E293B) else Color(0x33CBD5E1))
            .border(0.5.dp, if (darkTheme) Color(0x33FFFFFF) else Color(0x1F000000), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (darkTheme) Color(0x1A6366F1) else Color(0x1A4F46E5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (darkTheme) NeonCyan else Color(0xFF4F46E5),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (darkTheme) TextSecondary else TextSecondaryLight
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) Color.White else Color.Black
            )
        }
    }
}

// ==========================================
// SELECTION CHIP & GRID COMPONENTS HELPERS
// ==========================================

@Composable
fun SparklineCanvas(
    points: List<Float>,
    color: Color,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        
        val width = size.width
        val height = size.height
        val minX = 0f
        val maxX = (points.size - 1).toFloat()
        val minY = points.minOrNull() ?: 0f
        val maxY = points.maxOrNull() ?: 100f
        val rangeY = (maxY - minY).coerceAtLeast(1f)
        
        val path = Path()
        val fillPath = Path()
        
        val getX: (Int) -> Float = { index ->
            index * (width / maxX)
        }
        val getY: (Float) -> Float = { value ->
            height - ((value - minY) / rangeY) * height
        }
        
        path.moveTo(getX(0), getY(points[0]))
        fillPath.moveTo(getX(0), height)
        fillPath.lineTo(getX(0), getY(points[0]))
        
        for (i in 0 until points.size - 1) {
            val x1 = getX(i)
            val y1 = getY(points[i])
            val x2 = getX(i + 1)
            val y2 = getY(points[i + 1])
            
            // Cubic Bezier curve control points
            val cx1 = x1 + (x2 - x1) / 2f
            val cy1 = y1
            val cx2 = cx1
            val cy2 = y2
            
            path.cubicTo(cx1, cy1, cx2, cy2, x2, y2)
            fillPath.cubicTo(cx1, cy1, cx2, cy2, x2, y2)
        }
        fillPath.lineTo(width, height)
        fillPath.close()
        
        // Draw the beautiful soft gradient under spline
        val gradientBrush = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = if (darkTheme) 0.35f else 0.20f),
                Color.Transparent
            )
        )
        drawPath(
            path = fillPath,
            brush = gradientBrush
        )
        
        // Draw the main elegant line with rounded corners and a glow
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )

        // Draw dynamic glowing node marker on final trendline coordinate
        val lastX = getX(points.size - 1)
        val lastY = getY(points.last())
        drawCircle(
            color = color.copy(alpha = 0.5f),
            radius = 6.dp.toPx(),
            center = Offset(lastX, lastY)
        )
        drawCircle(
            color = Color.White,
            radius = 2.5.dp.toPx(),
            center = Offset(lastX, lastY)
        )
    }
}

@Composable
fun OverviewClassicStatCard(
    title: String,
    value: String,
    badgeText: String,
    badgeDesc: String,
    badgePositive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    darkTheme: Boolean,
    sparklinePoints: List<Float>,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        darkTheme = darkTheme,
        padding = 0.dp,
        borderColor = if (darkTheme) GlassBorderDark else null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (darkTheme) TextSecondary else TextSecondaryLight
                    )
                    
                    // Small Circle Containing icon
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Stat Logo",
                            tint = iconColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Value text
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (darkTheme) Color.White else Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Growth/status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // badge box container
                    val badgeBg = if (badgePositive) Color(0x2810B981) else Color(0x24F43F5E)
                    val badgeColor = if (badgePositive) Color(0xFF10B981) else Color(0xFFF43F5E)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            badgeText,
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    if (badgeDesc.isNotBlank()) {
                        Text(
                            badgeDesc,
                            fontSize = 10.sp,
                            color = if (darkTheme) TextMuted else TextSecondaryLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Beautiful Sparkline matching wireframe/mockup perfectly
            SparklineCanvas(
                points = sparklinePoints,
                color = iconColor,
                darkTheme = darkTheme,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}

@Composable
fun ThreeQuarterArcGauge(
    percentage: Float, // 0f to 1f
    color: Color,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    innerContent: @Composable () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sw = 10.dp.toPx()
            val diam = (size.minDimension - sw).coerceAtLeast(1f)
            val arcSize = Size(diam, diam)
            val offset = Offset((size.width - diam) / 2f, (size.height - diam) / 2f)

            // Background arc: 3/4 circle = 270 degrees. Center of gap is at the bottom (90 degrees).
            // So start from 135 degrees and sweep 270 degrees.
            drawArc(
                color = if (darkTheme) Color(0x1F94A3B8) else Color(0x140F172A),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                size = arcSize,
                topLeft = offset,
                style = Stroke(width = sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Foreground progress arc with custom glow and smooth rounded cap drawing
            drawArc(
                color = color,
                startAngle = 135f,
                sweepAngle = 270f * percentage.coerceIn(0f, 1f),
                useCenter = false,
                size = arcSize,
                topLeft = offset,
                style = Stroke(width = sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Draw radial tick dash markers along the gauge boundary representing fine dials
            val cx = size.width / 2f
            val cy = size.height / 2f
            for (angleDegrees in 135..405 step 15) {
                val angleRad = Math.toRadians(angleDegrees.toDouble())
                val tickLength = 5.dp.toPx()
                val outerR = diam / 2f + sw / 2f + 4.dp.toPx()
                val innerR = outerR - tickLength
                
                val startX = cx + cos(angleRad).toFloat() * innerR
                val startY = cy + sin(angleRad).toFloat() * innerR
                val endX = cx + cos(angleRad).toFloat() * outerR
                val endY = cy + sin(angleRad).toFloat() * outerR
                
                drawLine(
                    color = if (darkTheme) Color(0x3BFFFFFF) else Color(0x28000000),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        innerContent()
    }
}

@Composable
fun RecentActivityRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    timestamp: String,
    rightInfo: String,
    rightColor: Color,
    darkTheme: Boolean,
    onClick: () -> Unit = {},
    drawStartLine: Boolean = false,
    drawEndLine: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // High-contrast vector badge with timeline vertical connector drawing
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val h = size.height
                val lineColor = if (darkTheme) Color(0x2494A3B8) else Color(0x1F0F172A)
                
                if (drawStartLine) {
                    drawLine(color = lineColor, start = Offset(cx, 0f), end = Offset(cx, h * 0.2f), strokeWidth = 1.5.dp.toPx())
                }
                if (drawEndLine) {
                    drawLine(color = lineColor, start = Offset(cx, h * 0.8f), end = Offset(cx, h), strokeWidth = 1.5.dp.toPx())
                }
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (darkTheme) Color.White else Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = timestamp,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = if (darkTheme) TextSecondary else TextSecondaryLight
            )
        }
        
        Text(
            text = rightInfo,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = rightColor
        )
    }
}

@Composable
fun SiteStatusPillar(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    value: String,
    label: String,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Determine dynamic filled capacity ratio representing mini-statistics
    val capacityFraction = when (label) {
        "Workers Present" -> 0.92f
        "Absent" -> 0.08f
        "Late" -> 0.04f
        "Equipment Active" -> 1.0f
        else -> 0.60f
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = if (darkTheme) Color.White else Color(0xFF0F172A)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = if (darkTheme) TextSecondary else TextSecondaryLight,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            lineHeight = 11.sp
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        // Mini progress indicators representing current state capacity
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(if (darkTheme) Color(0x1F94A3B8) else Color(0x0F000000))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(capacityFraction)
                    .background(tint)
            )
        }
    }
}

@Composable
fun DonutDetailsRow(
    color: Color,
    label: String,
    amount: String,
    darkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (darkTheme) TextSecondary else TextSecondaryLight
            )
        }
        Text(
            text = amount,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (darkTheme) Color.White else Color(0xFF0F172A)
        )
    }
}

@Composable
fun TaskProgressStatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    label: String,
    count: Int,
    darkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (darkTheme) TextSecondary else TextSecondaryLight
            )
        }
        Text(
            text = count.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (darkTheme) Color.White else Color(0xFF0F172A)
        )
    }
}

@Composable
fun WireframeStaticTransactionRow(
    title: String,
    category: String,
    date: String,
    party: String,
    amount: String,
    isCredit: Boolean,
    darkTheme: Boolean,
    onClick: (() -> Unit)? = null
) {
    val barColor = if (isCredit) Color(0xFF10B981) else Color(0xFFF43F5E)
    
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        darkTheme = darkTheme,
        padding = 12.dp,
        borderColor = if (darkTheme) GlassBorderDark else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Symmetrical colorful vertical border accent strip matching wireframe mockup perfectly
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(barColor)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (darkTheme) Color.White else Color(0xFF0F172A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$category • $date • $party",
                        fontSize = 11.sp,
                        color = if (darkTheme) TextSecondary else TextSecondaryLight,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = "${if (isCredit) "+" else "-"}$amount",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = barColor
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    darkTheme: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        // High-end container for the icon (with beautiful glow outline)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (darkTheme) Color(0x3B1E293B) else Color(0x0F000000))
                .border(
                    BorderStroke(1.dp, tint.copy(alpha = 0.45f)),
                    RoundedCornerShape(14.dp)
                )
                .drawBehind {
                    drawCircle(
                        color = tint.copy(alpha = 0.08f),
                        radius = this.size.width * 0.48f,
                        center = this.center
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (darkTheme) TextPrimary else TextPrimaryLight,
            letterSpacing = 0.1.sp
        )
    }
}

// Utility Toast popup helper
fun scaffoldStateToast(context: android.content.Context, message: String) {
    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
}

// Extends modifiers for custom transparency opacity inside Coil AsyncImage drawings
fun Modifier.opacityOverlay(alpha: Float): Modifier = this.then(Modifier.background(Color.Black.copy(alpha = alpha)))

@Composable
fun CurvedPolygonBackdrop(
    style: String,
    darkTheme: Boolean
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
    ) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f) return@Canvas

        when (style) {
            "preset_cyber_blueprint" -> {
                if (darkTheme) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                        )
                    )
                    val step = 40f
                    for (x in 0..width.toInt() step step.toInt()) {
                        drawLine(
                            color = NeonCyan.copy(alpha = 0.08f),
                            start = Offset(x.toFloat(), 0f),
                            end = Offset(x.toFloat(), height),
                            strokeWidth = 1f
                        )
                    }
                    for (y in 0..height.toInt() step step.toInt()) {
                        drawLine(
                            color = NeonCyan.copy(alpha = 0.08f),
                            start = Offset(0f, y.toFloat()),
                            end = Offset(width, y.toFloat()),
                            strokeWidth = 1f
                        )
                    }
                    val path = Path().apply {
                        moveTo(width * 0.5f, 0f)
                        lineTo(width, 0f)
                        lineTo(width, height * 0.6f)
                        cubicTo(width * 0.85f, height * 0.4f, width * 0.7f, height * 0.2f, width * 0.5f, 0f)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.radialGradient(
                            colors = listOf(NeonCyan.copy(alpha = 0.15f), Color.Transparent),
                            center = Offset(width, 0f),
                            radius = width * 0.4f
                        )
                    )
                } else {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFE0F2FE), Color(0xFFF1F5F9))
                        )
                    )
                    val step = 40f
                    for (x in 0..width.toInt() step step.toInt()) {
                        drawLine(
                            color = Color(0xFF0284C7).copy(alpha = 0.05f),
                            start = Offset(x.toFloat(), 0f),
                            end = Offset(x.toFloat(), height),
                            strokeWidth = 1f
                        )
                    }
                    for (y in 0..height.toInt() step step.toInt()) {
                        drawLine(
                            color = Color(0xFF0284C7).copy(alpha = 0.05f),
                            start = Offset(0f, y.toFloat()),
                            end = Offset(width, y.toFloat()),
                            strokeWidth = 1f
                        )
                    }
                }
            }
            "preset_sunset_construct" -> {
                if (darkTheme) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF451A03), Color(0xFF781A44))
                        )
                    )
                } else {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFFF1F2), Color(0xFFFFE4E6))
                        )
                    )
                }
            }
            "preset_golden_truss" -> {
                if (darkTheme) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1C1917), Color(0xFF44403C))
                        )
                    )
                } else {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))
                        )
                    )
                }
            }
            "preset_forest_mason" -> {
                if (darkTheme) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF064E3B), Color(0xFF022C22))
                        )
                    )
                } else {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))
                        )
                    )
                }
            }
            else -> {
                if (darkTheme) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E1B4B), Color(0xFF311042))
                        )
                    )
                } else {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE))
                        )
                    )
                }
            }
        }
    }
}
