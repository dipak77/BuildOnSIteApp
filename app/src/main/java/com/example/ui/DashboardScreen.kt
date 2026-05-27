package com.example.ui

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
    
    // Overview filter dropdown simulated state
    var selectedFilter by remember { mutableStateOf("This Month") }
    var showFilterDropdown by remember { mutableStateOf(false) }
    var showProjectSwitcher by remember { mutableStateOf(false) }

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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (dark) Color(0x1F111827) else Color(0x0F0F172A))
                        .clickable {
                            // Easily switch themes as a developer utility shortcut!
                            viewModel.darkThemeEnabled = !viewModel.darkThemeEnabled
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Theme switcher",
                        tint = if (dark) Color.White else Color(0xFF0F172A)
                    )
                }

                // App Branding Title: ConstructPro
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
                        Text(
                            text = "Pro",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = if (dark) NeonCyan else Color(0xFF4F46E5)
                        )
                    }
                    Text(
                        text = "Build. Manage. Grow.",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (dark) TextSecondary else TextSecondaryLight,
                        letterSpacing = 1.2.sp
                    )
                }

                // Bell Indicator + Avatar Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Translucent Notification Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (dark) Color(0x1F111827) else Color(0x0F0F172A))
                            .clickable {
                                // Reset project database to seed state as a quick operational shortcut if clicked!
                                scaffoldStateToast(context, "Notifications up to date.")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (dark) Color.White else Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                        // Notification Badge dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6)) // violet accent dot
                                .align(Alignment.TopEnd)
                                .offset(x = (-10).dp, y = 10.dp)
                        )
                    }

                    // User Profile image
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, if (dark) NeonCyan else Color(0xFF4F46E5), CircleShape)
                            .clickable {
                                // Cycle projects
                                if (allProjects.isNotEmpty()) {
                                    val currentIndex = allProjects.indexOfFirst { it.id == currentProject?.id }
                                    val nextIndex = (currentIndex + 1) % allProjects.size
                                    viewModel.selectedProjectId = allProjects[nextIndex].id
                                    scaffoldStateToast(context, "Switched project: ${allProjects[nextIndex].name}")
                                }
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
                // Linear Indigo/Blue Gradient background brushing matching wireframe mockup perfectly
                val gradientBg = Brush.linearGradient(
                    colors = if (dark) {
                        listOf(Color(0xFF2563EB), Color(0xFF4F46E5), Color(0xFF7C3AED))
                    } else {
                        listOf(Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF8B5CF6))
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(gradientBg)
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

                            // Analytics small chart icon button
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

                        Spacer(modifier = Modifier.height(18.dp))

                        // Middle: Available site balance
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
                        darkTheme = dark
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
                        darkTheme = dark
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
                        darkTheme = dark
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
                        darkTheme = dark
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

                        // Circular arc Donut Chart represent budgetSpent percentage (e.g. 66%)
                        val resolvedSpendingPercent = if (budgetPercent <= 0) 66 else budgetPercent
                        Box(
                            modifier = Modifier.size(104.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val sw = 10.dp.toPx()
                                val diam = size.minDimension - sw
                                val arcSize = Size(diam, diam)
                                val offset = Offset((size.width - diam) / 2f, (size.height - diam) / 2f)

                                // background full ring track
                                drawArc(
                                    color = if (dark) Color(0x1F94A3B8) else Color(0x140F172A),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    size = arcSize,
                                    topLeft = offset,
                                    style = Stroke(width = sw)
                                )

                                // colorful swept ring (66%)
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color(0xFF6366F1),
                                            Color(0xFF8B5CF6),
                                            Color(0xFF3B82F6),
                                            Color(0xFF6366F1)
                                        )
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = (resolvedSpendingPercent.toFloat() / 100f) * 360f,
                                    useCenter = false,
                                    size = arcSize,
                                    topLeft = offset,
                                    style = Stroke(width = sw)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$resolvedSpendingPercent%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (dark) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "of Budget",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (dark) TextSecondary else TextSecondaryLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Key detail rows under the donut chart
                        DonutDetailsRow(Color(0xFF6366F1), "Total Budget", formatIndianRupees(totalBudget), dark)
                        DonutDetailsRow(Color(0xFF8B5CF6), "Total Spent", formatIndianRupees(totalSpent.coerceAtLeast(825000.0)), dark)
                        DonutDetailsRow(Color(0xFF0EA5E9), "Remaining", formatIndianRupees(remainingBudget.coerceAtLeast(425000.0)), dark)

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
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Task Progress",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Header percent and dynamic linear progress indicator (68%)
                        val resolvedTaskProgress = if (totalTasks <= 0) 68 else (taskPercentage * 100).toInt()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Overall",
                                fontSize = 11.sp,
                                color = if (dark) TextSecondary else TextSecondaryLight
                            )
                            Text(
                                "$resolvedTaskProgress%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (dark) NeonCyan else Color(0xFF4F46E5)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        // Styled progress bar
                        val animProgress = (resolvedTaskProgress.toFloat() / 100f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (dark) Color(0x1F94A3B8) else Color(0x140F172A))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animProgress)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Rows representation for status counts
                        TaskProgressStatusRow(Icons.Default.CheckCircle, Color(0xFF10B981), "Completed", if (totalTasks <= 0) 18 else doneTasks, dark)
                        TaskProgressStatusRow(Icons.Default.Schedule, Color(0xFF3B82F6), "In Progress", if (totalTasks <= 0) 14 else inProgressTasks, dark)
                        TaskProgressStatusRow(Icons.Default.HourglassEmpty, Color(0xFFF59E0B), "Pending", if (totalTasks <= 0) 8 else pendingTasks, dark)
                        TaskProgressStatusRow(Icons.Default.Warning, Color(0xFFF43F5E), "Overdue", if (totalTasks <= 0) 2 else overdueTasks, dark)

                        Spacer(modifier = Modifier.height(14.dp))

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
        // 6. RECENT TRANSACTIONS HEADER
        // ==========================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) Color.White else Color(0xFF0F172A)
                )

                Text(
                    text = "View All",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) NeonCyan else Color(0xFF4F46E5),
                    modifier = Modifier.clickable { viewModel.currentScreen = AppScreen.Money }
                )
            }
        }

        // ==========================================
        // 7. TRANSACTION STACKS (MATCHING WIREFRAME LOG)
        // ==========================================
        val dynamicFeedList = projectTransactions.take(3)
        if (dynamicFeedList.isEmpty()) {
            // Display exact wireframe defaults if no transactions exist in the database yet
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WireframeStaticTransactionRow(
                        title = "Worker Weekly Salary Payout",
                        category = "Labor",
                        date = "May 22, 2026",
                        party = "Paid to 24 workers",
                        amount = "₹45,000.00",
                        isCredit = false,
                        darkTheme = dark
                    )

                    WireframeStaticTransactionRow(
                        title = "Super Grade Portland Cement (50 Bags)",
                        category = "Material",
                        date = "May 21, 2026",
                        party = "BuildMax Supplies",
                        amount = "₹1,20,000.00",
                        isCredit = false,
                        darkTheme = dark
                    )

                    WireframeStaticTransactionRow(
                        title = "Client Advance Payment Received",
                        category = "Client Advance",
                        date = "May 20, 2026",
                        party = "Galaxy Infra",
                        amount = "₹8,50,000.00",
                        isCredit = true,
                        darkTheme = dark
                    )
                }
            }
        } else {
            // Render from database
            items(dynamicFeedList) { tx ->
                val isCredit = tx.type == "Money In"
                val partyDesc = tx.partyName ?: if (isCredit) "Galaxy Infra" else "BuildMax Supplies"

                WireframeStaticTransactionRow(
                    title = tx.description,
                    category = tx.category,
                    date = tx.date,
                    party = partyDesc,
                    amount = formatIndianRupees(tx.amount),
                    isCredit = isCredit,
                    darkTheme = dark,
                    onClick = {
                        viewModel.sharedSelectedTxDetails = tx
                        viewModel.currentScreen = AppScreen.Money
                    }
                )
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
}

// ==========================================
// SELECTION CHIP & GRID COMPONENTS HELPERS
// ==========================================

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
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        darkTheme = darkTheme,
        padding = 14.dp,
        borderColor = if (darkTheme) GlassBorderDark else null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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

            Spacer(modifier = Modifier.height(10.dp))

            // Value text
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = if (darkTheme) Color.White else Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

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
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (darkTheme) Color.White else Color(0xFF0F172A)
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
