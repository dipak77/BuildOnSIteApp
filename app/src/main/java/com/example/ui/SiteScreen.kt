package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SiteScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val dark = viewModel.darkThemeEnabled
    val currentProject by viewModel.activeProject.collectAsState()
    val allWorkers by viewModel.workers.collectAsState()
    val allAttendance by viewModel.attendance.collectAsState()

    // Active navigational date YYYY-MM-DD
    val activeDate = viewModel.attendanceDate

    // Interactive dialog selection
    var selectedWorkerForAttendance by remember { mutableStateOf<Worker?>(null) }
    var inputOvertimeHours by remember { mutableStateOf("0.0") }

    val formatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val displayFormat = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US) }
    val cFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    // Navigation trigger functions
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

    // Filter attendance events for active date and active project
    val activeDateAttendance = remember(allAttendance, activeDate, currentProject) {
        val projId = currentProject?.id
        if (projId == null) emptyList()
        else allAttendance.filter { it.date == activeDate && it.projectId == projId }
    }

    // Compute stats
    val presentCount = activeDateAttendance.count { it.status == "Present" || it.status == "Overtime" }
    val absentCount = activeDateAttendance.count { it.status == "Absent" }
    val totalOvertime = activeDateAttendance.sumOf { it.overtimeHours }

    val dailyWages = remember(activeDateAttendance, allWorkers) {
        activeDateAttendance.sumOf { att ->
            val w = allWorkers.find { it.id == att.workerId } ?: return@sumOf 0.0
            val base = if (att.status == "Absent") 0.0 else w.wageRate
            val overtimeMultiplier = (w.wageRate / 8.0) * 1.5 // Time-and-a-half
            val otCost = att.overtimeHours * overtimeMultiplier
            base + otCost
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Page title
        item {
            Column {
                Text(
                    text = "Worker Attendance",
                    color = if (dark) NeonCyan else Color(0xFF0284C7),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Manage site crew logs",
                    color = if (dark) TextSecondary else TextSecondaryLight,
                    fontSize = 12.sp
                )
            }
        }

        // Calendar Date Navigator Row
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
                            viewModel.attendanceDate = "2026-05-26" // Reset back to default
                        }
                    ) {
                        Text(
                            text = parsedDateString,
                            color = if (dark) TextPrimary else TextPrimaryLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        // "Today" Returning pill badge
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

        // Live KPI Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Present Block
                GlassCard(
                    modifier = Modifier.weight(1f),
                    darkTheme = dark,
                    padding = 8.dp
                ) {
                    Text("PRESENT", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("$presentCount Workers", color = if (dark) TextPrimary else TextPrimaryLight, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }

                // Absent Block
                GlassCard(
                    modifier = Modifier.weight(1f),
                    darkTheme = dark,
                    padding = 8.dp
                ) {
                    Text("ABSENT", color = NeonPink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("$absentCount Workers", color = if (dark) TextPrimary else TextPrimaryLight, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }

                // OT Hours
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

        // Combined Financial Outgo Estimate Item
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

        // Worker list
        if (allWorkers.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), darkTheme = dark) {
                    Text(
                        text = "No workers registered. Click the action button below to expand your team crew.",
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

                // Standardized item clicks to register attendance logs!
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
                            // Colored Avatar Bubble
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

                        // Shift badge status
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

    // Interactive Attendance Overtime Logger Dialog Sheet
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
                    text = "Logging status for ${parsedDateString} in ${activeProj.name}",
                    fontSize = 12.sp,
                    color = if (dark) TextSecondary else TextSecondaryLight
                )

                // Selectable Quick Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Present
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(
                                if (record?.status == "Present") NeonGreen.copy(alpha = 0.25f)
                                else Color(0x1A9CA3AF)
                            )
                            .border(
                                BorderStroke(1.dp, if (record?.status == "Present") NeonGreen else Color.Transparent),
                                CircleShape
                            )
                            .clickable {
                                viewModel.recordAttendance(selectedWorker.id, activeProj.id, activeDate, "Present")
                                selectedWorkerForAttendance = null
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("PRESENT", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Absent
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(
                                if (record?.status == "Absent") NeonPink.copy(alpha = 0.25f)
                                else Color(0x1A9CA3AF)
                            )
                            .border(
                                BorderStroke(1.dp, if (record?.status == "Absent") NeonPink else Color.Transparent),
                                CircleShape
                            )
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

                // Overtime Block
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
                        GlassTextField(
                            value = inputOvertimeHours,
                            onValueChange = { inputOvertimeHours = it },
                            label = "Overtime Hours",
                            isNumeric = true,
                            darkTheme = dark,
                            modifier = Modifier.weight(1f)
                        )

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

                // Option to Clear Attendance Record
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
}
