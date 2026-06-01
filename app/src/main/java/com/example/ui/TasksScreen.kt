package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun TasksScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val dark = viewModel.darkThemeEnabled
    val currentProject by viewModel.activeProject.collectAsState()
    val allTasks by viewModel.tasks.collectAsState()

    // Status Filter Chip States
    val statusFilter = viewModel.taskStatusFilter
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteConfirmForTask by remember { mutableStateOf<Task?>(null) }

    // Filtered lists for active project
    val projectTasks = remember(allTasks, currentProject) {
        val projId = currentProject?.id
        if (projId == null) emptyList()
        else allTasks.filter { it.projectId == projId }
    }

    // Calculations
    val totalCount = projectTasks.size
    val pendingCount = projectTasks.count { it.status == "To Do" }
    val progressCount = projectTasks.count { it.status == "In Progress" }
    val doneCount = projectTasks.count { it.status == "Done" }

    // Dynamic filtering!
    val filteredTasks = remember(projectTasks, statusFilter, searchQuery) {
        projectTasks.filter { task ->
            val matchesStatus = (statusFilter == "All") || (task.status == statusFilter)
            val matchesSearch = (searchQuery.isBlank()) ||
                    task.title.contains(searchQuery, ignoreCase = true) ||
                    task.assignee.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Page Header
        item {
            Column {
                Text(
                    text = "Project Tasks",
                    color = if (dark) NeonCyan else Color(0xFF0284C7),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Schedule tracking for ${currentProject?.name ?: "None"}",
                    color = if (dark) TextSecondary else TextSecondaryLight,
                    fontSize = 12.sp
                )
            }
        }

        // Stats summary row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total
                GlassCard(
                    modifier = Modifier.weight(1f),
                    darkTheme = dark,
                    padding = 8.dp
                ) {
                    Text("TOTALS", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("$totalCount Tasks", color = if (dark) TextPrimary else TextPrimaryLight, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }

                // In Progress
                GlassCard(
                    modifier = Modifier.weight(1f),
                    darkTheme = dark,
                    padding = 8.dp
                ) {
                    Text("PENDING", color = NeonAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("${pendingCount + progressCount}", color = if (dark) TextPrimary else TextPrimaryLight, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }

                // Completed
                GlassCard(
                    modifier = Modifier.weight(1f),
                    darkTheme = dark,
                    padding = 8.dp,
                    borderColor = if (dark) GlassBorderNeonPurple else null
                ) {
                    Text("DONE", color = NeonPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("$doneCount", color = if (dark) TextPrimary else TextPrimaryLight, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Search Bar Block
        item {
            GlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search tasks...",
                darkTheme = dark,
                placeholder = "Type task title or assignee...",
                icon = Icons.Default.Search,
                focusedStroke = NeonCyan
            )
        }

        // Filters Column
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "To Do", "In Progress", "Done").forEach { tab ->
                    GlassChip(
                        text = tab,
                        selected = statusFilter == tab,
                        onClick = { viewModel.taskStatusFilter = tab },
                        darkTheme = dark,
                        activeColor = when (tab) {
                            "To Do" -> NeonCyan
                            "In Progress" -> NeonAmber
                            "Done" -> NeonGreen
                            else -> NeonPurple
                        }
                    )
                }
            }
        }

        // Tasks Stack
        if (filteredTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Task,
                            contentDescription = "Empty",
                            tint = if (dark) TextMuted else TextSecondaryLight,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No tasks matching filter in this project",
                            color = if (dark) TextSecondary else TextSecondaryLight,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                val priorityColor = when (task.priority) {
                    "High" -> NeonPink
                    "Medium" -> NeonAmber
                    else -> NeonCyan
                }

                val statusIcon = when (task.status) {
                    "Done" -> Icons.Default.CheckCircle
                    "In Progress" -> Icons.Default.PlayArrow
                    else -> Icons.Default.Circle
                }

                val statusColor = when (task.status) {
                    "Done" -> NeonGreen
                    "In Progress" -> NeonAmber
                    else -> if (dark) TextMuted else TextSecondaryLight
                }

                // Interactive click-to-cycle status
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    darkTheme = dark,
                    borderColor = priorityColor.copy(alpha = 0.35f), // Border color matches priority!
                    padding = 12.dp,
                    onClick = { viewModel.cycleTaskStatus(task) }
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
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = task.title,
                                    color = if (dark) TextPrimary else TextPrimaryLight,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (task.status == "Done") TextDecoration.LineThrough else TextDecoration.None
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Small bubble for Priority
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(priorityColor, shape = CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${task.priority} Priority • Assignee: ${task.assignee}",
                                        color = if (dark) TextSecondary else TextSecondaryLight,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Due Date Tag
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Due Date",
                                color = if (dark) TextMuted else TextSecondaryLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = task.dueDate,
                                color = if (dark) TextPrimary else TextPrimaryLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Option to quick delete task
                            IconButton(
                                onClick = { showDeleteConfirmForTask = task },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = if (dark) TextMuted else TextSecondaryLight,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmForTask != null) {
        val task = showDeleteConfirmForTask!!
        GlassModalDialog(
            visible = true,
            onDismiss = { showDeleteConfirmForTask = null },
            title = "⚠ Confirm Deletion",
            darkTheme = dark,
            glowColor = NeonPink
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Are you sure you want to delete this task assignment? This action cannot be undone.",
                    color = if (dark) TextSecondary else TextSecondaryLight,
                    fontSize = 13.sp
                )
                Text(
                    "Task: ${task.title}\nAssignee: ${task.assignee}",
                    color = if (dark) TextPrimary else TextPrimaryLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassButton(
                        onClick = { showDeleteConfirmForTask = null },
                        darkTheme = dark,
                        outlineMode = true,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    GlassButton(
                        onClick = {
                            viewModel.deleteTask(task)
                            showDeleteConfirmForTask = null
                        },
                        darkTheme = dark,
                        glowColor = NeonPink,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
