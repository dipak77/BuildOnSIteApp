package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Filtered lists for active project
    val projectTransactions = remember(allTransactions, currentProject) {
        if (currentProject == null) emptyList()
        else allTransactions.filter { it.projectId == currentProject!!.id }
    }

    val projectTasks = remember(allTasks, currentProject) {
        if (currentProject == null) emptyList()
        else allTasks.filter { it.projectId == currentProject!!.id }
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

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Project Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ConstructPro",
                        color = if (dark) NeonCyan else Color(0xFF0284C7),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Unified Workspace",
                        color = if (dark) TextSecondary else TextSecondaryLight,
                        fontSize = 13.sp
                    )
                }

                // Smooth Horizontal project swapper
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch Project",
                        tint = if (dark) NeonPurple else Color(0xFF7C3AED),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentProject?.name ?: "No project selected",
                        color = if (dark) TextPrimary else TextPrimaryLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                // Rotate between projects easily
                                if (allProjects.isNotEmpty()) {
                                    val currentIndex = allProjects.indexOfFirst { it.id == currentProject?.id }
                                    val nextIndex = (currentIndex + 1) % allProjects.size
                                    viewModel.selectedProjectId = allProjects[nextIndex].id
                                }
                            }
                    )
                }
            }
        }

        // Project Hero Frosted Card
        item {
            if (currentProject != null) {
                val proj = currentProject!!
                val spendingProgress = if (proj.budget > 0) (moneyOut / proj.budget).toFloat() else 0f
                
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    darkTheme = dark,
                    borderColor = if (dark) GlassBorderNeonCyan else null,
                    glowColor = NeonCyan
                ) {
                    Text(
                        text = "ACTIVE PROJECT",
                        color = if (dark) NeonCyan else Color(0xFF0284C7),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = proj.name,
                        color = if (dark) TextPrimary else TextPrimaryLight,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = proj.location,
                        color = if (dark) TextSecondary else TextSecondaryLight,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Net Balance Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NET FLOW BALANCE",
                                color = if (dark) TextMuted else TextSecondaryLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currencyFormatter.format(netBalance),
                                color = if (netBalance >= 0) NeonGreen else NeonPink,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (netBalance >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (netBalance >= 0) NeonGreen else NeonPink,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Budget Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spend: ${currencyFormatter.format(moneyOut)} / ${currencyFormatter.format(proj.budget)}",
                            color = if (dark) TextSecondary else TextSecondaryLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(spendingProgress * 100).toInt()}%",
                            color = if (spendingProgress > 1.0f) NeonPink else NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    GlassProgressBar(
                        progress = spendingProgress,
                        darkTheme = dark,
                        glowColor = if (spendingProgress > 1.0f) NeonPink else NeonCyan
                    )
                }
            } else {
                GlassCard(modifier = Modifier.fillMaxWidth(), darkTheme = dark) {
                    Text(
                        text = "No active project found. Click 'More' to add new site operations.",
                        color = if (dark) TextSecondary else TextSecondaryLight
                    )
                }
            }
        }

        // Mini Progress & KPI Dual Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Budget Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    darkTheme = dark,
                    padding = 12.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Current Cash Out",
                        color = if (dark) TextSecondary else TextSecondaryLight,
                        fontSize = 11.sp
                    )
                    Text(
                        text = currencyFormatter.format(moneyOut),
                        color = if (dark) TextPrimary else TextPrimaryLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${projectTransactions.filter { it.type == "Money Out" }.size} outflows registered",
                        color = if (dark) TextMuted else TextSecondaryLight,
                        fontSize = 10.sp
                    )
                }

                // Task Ring Progression Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    darkTheme = dark,
                    padding = 12.dp,
                    borderColor = if (dark) GlassBorderNeonPurple else null
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Task Completion",
                        color = if (dark) TextSecondary else TextSecondaryLight,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${(taskPercentage * 100).toInt()}% Done",
                        color = if (dark) TextPrimary else TextPrimaryLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    GlassProgressBar(progress = taskPercentage, darkTheme = dark, glowColor = NeonPurple)
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
                    text = "Recent Cash Flow",
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
                    borderColor = if (tx.type == "Money In") GlassBorderNeonCyan else null,
                    padding = 12.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (tx.type == "Money In") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (tx.type == "Money In") NeonGreen else NeonPink,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tx.description,
                                    color = if (dark) TextPrimary else TextPrimaryLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${tx.category} • ${tx.date}",
                                    color = if (dark) TextSecondary else TextSecondaryLight,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "${if (tx.type == "Money In") "+" else "-"}${currencyFormatter.format(tx.amount)}",
                            color = if (tx.type == "Money In") NeonGreen else NeonPink,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
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
}

// Float helper function to add direct offsets
private fun Modifier.padding(top: Float) = this.padding(top = top.dp)
