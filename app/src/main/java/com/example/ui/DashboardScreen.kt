package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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

    val context = LocalContext.current
    var showBackgroundPicker by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf("") }

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

    // Calculations
    val moneyIn = remember(projectTransactions) {
        projectTransactions.filter { it.type == "Money In" }.sumOf { it.amount }
    }
    val moneyOut = remember(projectTransactions) {
        projectTransactions.filter { it.type == "Money Out" }.sumOf { it.amount }
    }
    val netBalance = moneyIn - moneyOut

    val totalTasks = projectTasks.size
    val doneTasks = projectTasks.count { it.status == "Done" }
    val taskPercentage = if (totalTasks > 0) doneTasks.toFloat() / totalTasks else 0.0f

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Project Hero Custom Reference Card
        item {
            val proj = currentProject
            if (proj != null) {
                val bgStyle = proj.customBackground
                
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    darkTheme = dark,
                    borderColor = if (dark) GlassBorderNeonCyan else null,
                    glowColor = NeonCyan,
                    padding = 0.dp
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        // Render Backdrop
                        if (bgStyle != null && bgStyle.isNotBlank()) {
                            if (bgStyle.startsWith("http://") || bgStyle.startsWith("https://")) {
                                AsyncImage(
                                    model = bgStyle,
                                    contentDescription = "Project Cover Art",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize().clip(RoundedCornerShape(20.dp))
                                )
                                // Dark tint overlay
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color.Black.copy(alpha = 0.45f))
                                )
                            } else {
                                CurvedPolygonBackdrop(style = bgStyle, darkTheme = dark)
                            }
                        }

                        // Reference-styled Main Card Content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Top Row: Active Project Icon, Title & Customizer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (allProjects.isNotEmpty()) {
                                                val currentIndex = allProjects.indexOfFirst { it.id == currentProject?.id }
                                                val nextIndex = (currentIndex + 1) % allProjects.size
                                                viewModel.selectedProjectId = allProjects[nextIndex].id
                                            }
                                        }
                                        .padding(4.dp)
                                ) {
                                    // Custom Rounded container for Project Icon
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (dark) Color(0x3306B6D4) else Color(0x1F0284C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = null,
                                            tint = if (dark) NeonCyan else Color(0xFF0284C7),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Active Project",
                                                color = if (dark) TextSecondary else TextSecondaryLight,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.SwapHoriz,
                                                contentDescription = "Switch",
                                                tint = if (dark) NeonPurple else Color(0xFF7C3AED),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                        Text(
                                            text = proj.name,
                                            color = if (dark) Color.White else Color.Black,
                                            fontSize = 19.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 1.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { showBackgroundPicker = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "Customize Art",
                                        tint = if (dark) NeonCyan else Color(0xFF0284C7),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Middle: Net Balance Label + Glowing Balance Display
                            Column {
                                Text(
                                    text = "Net Balance",
                                    color = if (dark) TextSecondary else TextSecondaryLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))

                                val balanceSign = if (netBalance >= 0) "+" else ""
                                val balanceText = balanceSign + currencyFormatter.format(netBalance)

                                Text(
                                    text = balanceText,
                                    color = if (netBalance >= 0) NeonGreen else NeonPink,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    style = LocalTextStyle.current.copy(
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = (if (netBalance >= 0) NeonGreen else NeonPink).copy(alpha = 0.5f),
                                            offset = Offset(0f, 0f),
                                            blurRadius = 14f
                                        )
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Bottom Meta Row: Date & Location icons side-by-side
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = "Date",
                                        tint = if (dark) TextSecondary else TextSecondaryLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "2026-05-26",
                                        color = if (dark) TextSecondary else TextSecondaryLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.width(20.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "Location",
                                        tint = if (dark) TextSecondary else TextSecondaryLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = proj.location,
                                        color = if (dark) TextSecondary else TextSecondaryLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                GlassCard(modifier = Modifier.fillMaxWidth(), darkTheme = dark) {
                    Text(
                        text = "No active project found. Swipe or go to 'More' to configure architectural sites.",
                        color = if (dark) TextSecondary else TextSecondaryLight
                    )
                }
            }
        }

        // New Reference-Inspired Budget and Tasks side-by-side cards
        item {
            val proj = currentProject
            if (proj != null) {
                val spendingProgress = if (proj.budget > 0) (moneyOut / proj.budget).toFloat() else 0f
                val budgetPercent = (spendingProgress * 100).toInt()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Budget Card (Left)
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.currentScreen = AppScreen.Money },
                        darkTheme = dark,
                        padding = 12.dp,
                        borderColor = if (dark) GlassBorderNeonCyan.copy(alpha = 0.3f) else null
                    ) {
                        Column {
                            // Top Row: Icon Circle + Title + Arrow
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (dark) Color(0x2206B6D4) else Color(0x110284C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = if (dark) NeonCyan else Color(0xFF0284C7),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Budget",
                                        color = if (dark) TextPrimary else TextPrimaryLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Expand Budget",
                                    tint = if (dark) TextMuted else TextSecondaryLight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            // Middle percentage Text
                            Text(
                                text = "$budgetPercent%",
                                color = if (dark) Color.White else Color.Black,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Custom Minimal Progress Bar
                            val progressValue = spendingProgress.coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (dark) Color(0x33FFFFFF) else Color(0x1E000000))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progressValue)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    NeonCyan,
                                                    NeonCyan.copy(alpha = 0.8f)
                                                )
                                            )
                                        )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Bottom Label
                            Text(
                                text = "${currencyFormatter.format(moneyOut)} / ${currencyFormatter.format(proj.budget)}",
                                color = if (dark) TextSecondary else TextSecondaryLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Tasks Card (Right)
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.currentScreen = AppScreen.Tasks },
                        darkTheme = dark,
                        padding = 12.dp,
                        borderColor = if (dark) GlassBorderNeonPurple.copy(alpha = 0.3f) else null
                    ) {
                        Column {
                            // Top Row: Icon Circle + Title + Arrow
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (dark) Color(0x228B5CF6) else Color(0x117C3AED)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (dark) NeonPurple else Color(0xFF7C3AED),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Tasks",
                                        color = if (dark) TextPrimary else TextPrimaryLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Expand Tasks",
                                    tint = if (dark) TextMuted else TextSecondaryLight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            // Middle percentage Text
                            val taskPercent = (taskPercentage * 100).toInt()
                            Text(
                                text = "$taskPercent%",
                                color = if (dark) Color.White else Color.Black,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Custom Minimal Progress Bar
                            val taskValue = taskPercentage.coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (dark) Color(0x33FFFFFF) else Color(0x1E000000))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(taskValue)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    NeonPurple,
                                                    NeonPurple.copy(alpha = 0.8f)
                                                )
                                            )
                                        )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Bottom Label
                            Text(
                                text = "$doneTasks / $totalTasks completed",
                                color = if (dark) TextSecondary else TextSecondaryLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    color = if (dark) TextPrimary else TextPrimaryLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    color = if (dark) NeonCyan else Color(0xFF0284C7),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.currentScreen = AppScreen.Money }
                )
            }
        }

        // Recent Transactions Stack (Top 3)
        val recentTransactions = projectTransactions.take(3)
        if (recentTransactions.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), darkTheme = dark) {
                    Text(
                        text = "No recorded cash transactions for this project. Use the flow button below to register a sale/purchase.",
                        color = if (dark) TextMuted else TextSecondaryLight,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(recentTransactions) { tx ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    darkTheme = dark,
                    borderColor = if (dark) GlassBorderDark else null,
                    padding = 12.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Thick Rounded Neon Left Bar indicator matching reference perfectly
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (tx.type == "Money In") NeonGreen else NeonPink)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tx.description,
                                    color = if (dark) TextPrimary else TextPrimaryLight,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${tx.category} • ${tx.date}",
                                    color = if (dark) TextSecondary else TextSecondaryLight,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        val amtSign = if (tx.type == "Money In") "+" else "-"
                        Text(
                            text = "${amtSign}${currencyFormatter.format(tx.amount)}",
                            color = if (tx.type == "Money In") NeonGreen else NeonPink,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Pending Tasks Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pending Site Tasks",
                    color = if (dark) TextPrimary else TextPrimaryLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    color = if (dark) NeonCyan else Color(0xFF0284C7),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.currentScreen = AppScreen.Tasks }
                )
            }
        }

        // Active Tasks Stack (Top 3 non-Done)
        val pendingTasks = projectTasks.filter { it.status != "Done" }.take(3)
        if (pendingTasks.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), darkTheme = dark) {
                    Text(
                        text = "Nice work! All project schedules are on track and completed.",
                        color = if (dark) TextMuted else TextSecondaryLight,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(pendingTasks) { task ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    darkTheme = dark,
                    padding = 12.dp,
                    onClick = { viewModel.cycleTaskStatus(task) } // Cycle direct on tap!
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                color = if (dark) TextPrimary else TextPrimaryLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${task.assignee} • Due: ${task.dueDate}",
                                    color = if (dark) TextSecondary else TextSecondaryLight,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Priority Badge
                        val priorityColor = when (task.priority) {
                            "High" -> NeonPink
                            "Medium" -> NeonAmber
                            else -> NeonCyan
                        }
                        
                        GlassChip(
                            text = task.status,
                            selected = true,
                            onClick = { viewModel.cycleTaskStatus(task) },
                            activeColor = priorityColor
                        )
                    }
                }
            }
        }
    }

    val proj = currentProject
    if (showBackgroundPicker && proj != null) {
        AlertDialog(
            onDismissRequest = { showBackgroundPicker = false },
            title = { Text("Customize Card Background", color = if (dark) Color.White else Color.Black, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Select a Premium Polygon / Grid Vector theme, or enter a custom construction background image URL.", fontSize = 12.sp, color = if (dark) TextSecondary else TextSecondaryLight)
                    
                    // Presets
                    val presets = listOf(
                        "preset_cyber_blueprint" to "Cyber Blueprint (Neon Grid)",
                        "preset_sunset_construct" to "Sunset Construct (Truss Triangle)",
                        "preset_golden_truss" to "Golden Truss (Premium Geo)",
                        "preset_forest_mason" to "Forest Mason (Paving Wave)",
                        "preset_friction_neon" to "Friction Neon (Peaks & Force)"
                    )
                    
                    presets.forEach { (styleKey, styleLabel) ->
                        Button(
                            onClick = {
                                viewModel.updateProjectBackground(proj, styleKey)
                                showBackgroundPicker = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (styleKey) {
                                    "preset_cyber_blueprint" -> Color(0xFF0F172A)
                                    "preset_sunset_construct" -> Color(0xFF451A03)
                                    "preset_golden_truss" -> Color(0xFF1C1917)
                                    "preset_forest_mason" -> Color(0xFF064E3B)
                                    else -> Color(0xFF1E1B4B)
                                }
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(styleLabel, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Custom Background Image URL", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (dark) NeonCyan else Color(0xFF0284C7))
                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        placeholder = { Text("https://example.com/civil.jpg", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customUrlInput.isNotBlank()) {
                            viewModel.updateProjectBackground(proj, customUrlInput.trim())
                        }
                        showBackgroundPicker = false
                    }
                ) {
                    Text("Apply Link", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.updateProjectBackground(proj, "")
                            showBackgroundPicker = false
                        }
                    ) {
                        Text("Reset Default", color = NeonPink)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showBackgroundPicker = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun CurvedPolygonBackdrop(
    style: String,
    darkTheme: Boolean
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
    ) {
        val width = size.width
        val height = size.height

        when (style) {
            "preset_cyber_blueprint" -> {
                // Background gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                    )
                )
                // Technical Grid
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
                // Curved polygon slashes
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
                drawPath(
                    path = path,
                    color = NeonCyan.copy(alpha = 0.4f),
                    style = Stroke(width = 2f)
                )
            }
            "preset_sunset_construct" -> {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF451A03), Color(0xFF781A44))
                    )
                )
                // Structural trusses (triangles)
                val path = Path().apply {
                    moveTo(0f, height)
                    lineTo(width * 0.25f, height * 0.2f)
                    lineTo(width * 0.5f, height)
                    lineTo(width * 0.75f, height * 0.2f)
                    lineTo(width, height)
                    close()
                }
                drawPath(
                    path = path,
                    color = NeonAmber.copy(alpha = 0.15f),
                    style = Stroke(width = 3f)
                )
                // Curve slice
                val curvePath = Path().apply {
                    moveTo(0f, height * 0.7f)
                    quadraticTo(width * 0.5f, height * 0.4f, width, height * 0.8f)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = curvePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFEC4899).copy(alpha = 0.12f), Color.Transparent)
                    )
                )
            }
            "preset_golden_truss" -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1C1917), Color(0xFF44403C))
                    )
                )
                // Geodesic / Polygon structure radial rings
                val centerX = width * 0.85f
                val centerY = height * 0.3f
                drawCircle(
                    color = Color(0xFFEAB308).copy(alpha = 0.12f),
                    radius = width * 0.2f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.5f)
                )
                drawCircle(
                    color = Color(0xFFEAB308).copy(alpha = 0.08f),
                    radius = width * 0.35f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1f)
                )
                
                // Polygon architectural outline
                val path = Path().apply {
                    moveTo(0f, height * 0.3f)
                    lineTo(width * 0.3f, height * 0.15f)
                    lineTo(width * 0.6f, height * 0.45f)
                    lineTo(width * 0.8f, height * 0.2f)
                    lineTo(width, height * 0.5f)
                }
                drawPath(
                    path = path,
                    color = Color(0xFFFACC15).copy(alpha = 0.3f),
                    style = Stroke(width = 2.5f)
                )
            }
            "preset_forest_mason" -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF064E3B), Color(0xFF022C22))
                    )
                )
                // Curved paving polygon masonry design
                val step = 60f
                for (i in 0..8) {
                    val path = Path().apply {
                        moveTo(0f, height - (i * step))
                        cubicTo(width * 0.3f, height - (i * step) - 50f, width * 0.7f, height - (i * step) + 50f, width, height - (i * step))
                    }
                    drawPath(
                        path = path,
                        color = NeonGreen.copy(alpha = 0.12f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }
            "preset_friction_neon" -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1B4B), Color(0xFF311042))
                    )
                )
                // Symmetrical polygon mountain peaks representing vector forces
                val path = Path().apply {
                    moveTo(0f, height)
                    lineTo(width * 0.2f, height * 0.4f)
                    lineTo(width * 0.4f, height * 0.7f)
                    lineTo(width * 0.65f, height * 0.3f)
                    lineTo(width * 0.8f, height * 0.6f)
                    lineTo(width, height * 0.25f)
                    lineTo(width, height)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(NeonPink.copy(alpha = 0.18f), Color.Transparent)
                    )
                )
                drawPath(
                    path = path,
                    color = NeonPurple.copy(alpha = 0.4f),
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

// Float helper function to add direct offsets
private fun Modifier.padding(top: Float) = this.padding(top = top.dp)
