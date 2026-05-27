package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Room database repositories init
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ConstructionRepository(database.constructionDao())

        setContent {
            val viewModel: MainViewModel by viewModels { MainViewModel.Factory(repository) }
            val dark = viewModel.darkThemeEnabled

            // Load user session from shared preferences on launch
            LaunchedEffect(Unit) {
                viewModel.loadUserSessionFromPrefs(applicationContext)
            }

            MyApplicationTheme(darkTheme = dark) {
                GlassAtmosphereBox(darkTheme = dark) {
                    val userSession by viewModel.userSession.collectAsState()
                    if (userSession == null) {
                        GoogleLoginScreen(viewModel = viewModel)
                    } else {
                        ScaffoldFrame(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ScaffoldFrame(viewModel: MainViewModel) {
    val dark = viewModel.darkThemeEnabled
    val currentTab = viewModel.currentScreen

    // Creation Modal Dialogue triggers
    var showQuickDialog by remember { mutableStateOf(false) }
    var showProjectDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var showWorkerDialog by remember { mutableStateOf(false) }

    // Input States
    val currentProject by viewModel.activeProject.collectAsState()
    val allWorkers by viewModel.workers.collectAsState()

    // 1. Project Input States
    var newProjName by remember { mutableStateOf("") }
    var newProjLoc by remember { mutableStateOf("") }
    var newProjBudget by remember { mutableStateOf("") }

    // 2. Transaction Input States
    var txType by remember { mutableStateOf("Money Out") } // "Money In" or "Money Out"
    var txAmount by remember { mutableStateOf("") }
    var txCategory by remember { mutableStateOf("Material") }
    var txDesc by remember { mutableStateOf("") }

    // 3. Task Input States
    var taskTitle by remember { mutableStateOf("") }
    var taskPriority by remember { mutableStateOf("Medium") }
    var taskAssigneeName by remember { mutableStateOf("") }
    var taskDueDate by remember { mutableStateOf("2026-05-29") }

    // 4. Worker Input States
    var workerName by remember { mutableStateOf("") }
    var workerRole by remember { mutableStateOf("Mason Foreman") }
    var workerShift by remember { mutableStateOf("Day") }
    var workerWage by remember { mutableStateOf("") }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val isDesktopWidth = maxWidth >= 1024.dp

        if (isDesktopWidth) {
            // ==========================================
            // DESKTOP RESPONSIVE FRAME (SIDEBAR + CONTENT)
            // ==========================================
            Row(modifier = Modifier.fillMaxSize()) {
                // Frosted Sticky Sidebar
                Column(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                        .padding(16.dp)
                        .background(if (dark) Color(0x35111827) else Color(0xDFFFFFFF), RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Corporate Branding / Logo
                    Text(
                        text = "ConstructPro",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (dark) NeonCyan else Color(0xFF0284C7)
                    )

                    Divider(color = if (dark) GlassBorderDark else GlassBorderLight)

                    // Sticky Nav Bars
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SidebarNavRow(
                            label = "Dashboard",
                            icon = Icons.Default.Dashboard,
                            active = currentTab == AppScreen.Dashboard,
                            darkTheme = dark,
                            onClick = { viewModel.currentScreen = AppScreen.Dashboard }
                        )
                        SidebarNavRow(
                            label = "Cash Ledger",
                            icon = Icons.Default.MonetizationOn,
                            active = currentTab == AppScreen.Money,
                            darkTheme = dark,
                            onClick = { viewModel.currentScreen = AppScreen.Money }
                        )
                        SidebarNavRow(
                            label = "Site Tasks",
                            icon = Icons.Default.TaskAlt,
                            active = currentTab == AppScreen.Tasks,
                            darkTheme = dark,
                            onClick = { viewModel.currentScreen = AppScreen.Tasks }
                        )
                        SidebarNavRow(
                            label = "Attendance Logs",
                            icon = Icons.Default.Face,
                            active = currentTab == AppScreen.Site,
                            darkTheme = dark,
                            onClick = { viewModel.currentScreen = AppScreen.Site }
                        )
                        SidebarNavRow(
                            label = "More Options",
                            icon = Icons.Default.MoreHoriz,
                            active = currentTab == AppScreen.More,
                            darkTheme = dark,
                            onClick = { viewModel.currentScreen = AppScreen.More }
                        )
                    }

                    // Bottom Project Creation shortcuts
                    GlassButton(
                        onClick = { showProjectDialog = true },
                        glowColor = NeonPurple,
                        darkTheme = dark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ NEW SITE PROJECT", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Main Content View
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            fadeIn() + slideInVertically { it / 6 } togetherWith fadeOut() + slideOutVertically { it / 6 }
                        },
                        label = "desktopTabsTransition"
                    ) { tab ->
                        when (tab) {
                            AppScreen.Dashboard -> DashboardScreen(viewModel = viewModel)
                            AppScreen.Money -> MoneyScreen(viewModel = viewModel)
                            AppScreen.Tasks -> TasksScreen(viewModel = viewModel)
                            AppScreen.Site -> SiteScreen(viewModel = viewModel)
                            AppScreen.More -> MoreScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // MOBILE RESPONSIVE FRAME (TAB NAVIGATION + FAB)
            // ==========================================
            Box(modifier = Modifier.fillMaxSize()) {
                // Interactive Tab switching pane with margin bottoms
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                fadeIn() + slideInVertically { it / 8 } togetherWith fadeOut() + slideOutVertically { it / 8 }
                            },
                            label = "mobileTabsTransition"
                        ) { tab ->
                            when (tab) {
                                AppScreen.Dashboard -> DashboardScreen(viewModel = viewModel)
                                AppScreen.Money -> MoneyScreen(viewModel = viewModel)
                                AppScreen.Tasks -> TasksScreen(viewModel = viewModel)
                                AppScreen.Site -> SiteScreen(viewModel = viewModel)
                                AppScreen.More -> MoreScreen(viewModel = viewModel)
                            }
                        }
                    }

                    // Glassmorphism Bottom Navigation Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .background(if (dark) Color(0x35111827) else Color(0xDFFFFFFF))
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomBarNavItem(
                            icon = Icons.Default.Dashboard,
                            active = currentTab == AppScreen.Dashboard,
                            darkTheme = dark,
                            label = "Dashboard",
                            onClick = { viewModel.currentScreen = AppScreen.Dashboard }
                        )

                        BottomBarNavItem(
                            icon = Icons.Default.AccountBalanceWallet,
                            active = currentTab == AppScreen.Money,
                            darkTheme = dark,
                            label = "Money",
                            onClick = { viewModel.currentScreen = AppScreen.Money }
                        )

                        BottomBarNavItem(
                            icon = Icons.Default.TaskAlt,
                            active = currentTab == AppScreen.Tasks,
                            darkTheme = dark,
                            label = "Tasks",
                            onClick = { viewModel.currentScreen = AppScreen.Tasks }
                        )

                        BottomBarNavItem(
                            icon = Icons.Default.EventAvailable,
                            active = currentTab == AppScreen.Site,
                            darkTheme = dark,
                            label = "Site",
                            onClick = { viewModel.currentScreen = AppScreen.Site }
                        )

                        BottomBarNavItem(
                            icon = Icons.Default.Menu,
                            active = currentTab == AppScreen.More,
                            darkTheme = dark,
                            label = "More",
                            onClick = { viewModel.currentScreen = AppScreen.More }
                        )
                    }
                }
            }
        }

        // ==========================================
        // DYNAMIC FAB TRIGGER (WORKS ON ALL RESOLUTIONS)
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isDesktopWidth) 24.dp else 84.dp, end = 24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = {
                    when (currentTab) {
                        AppScreen.Dashboard -> showQuickDialog = true
                        AppScreen.Money -> showTransactionDialog = true
                        AppScreen.Tasks -> showTaskDialog = true
                        AppScreen.Site -> showWorkerDialog = true
                        AppScreen.More -> showProjectDialog = true
                    }
                },
                containerColor = NeonPurple,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Record action",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    // ==========================================
    // SYSTEM INPUT MODE POPUP SHEETS
    // ==========================================

    // 0. QUICK CHOICE SHEET (Dashboard FAB)
    GlassModalDialog(
        visible = showQuickDialog,
        onDismiss = { showQuickDialog = false },
        title = "Operations Quick Actions",
        darkTheme = dark,
        glowColor = NeonCyan
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Register a quick site ledger transaction or assignment slot.", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp)

            GlassButton(
                onClick = { showQuickDialog = false; showTransactionDialog = true },
                darkTheme = dark,
                glowColor = NeonCyan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register Cash Transaction", fontWeight = FontWeight.Bold)
            }

            GlassButton(
                onClick = { showQuickDialog = false; showTaskDialog = true },
                darkTheme = dark,
                glowColor = NeonPurple,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Assign Crew Task", fontWeight = FontWeight.Bold)
            }

            GlassButton(
                onClick = { showQuickDialog = false; showWorkerDialog = true },
                darkTheme = dark,
                glowColor = NeonGreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Worker Profile", fontWeight = FontWeight.Bold)
            }
        }
    }

    // 1. ADD PROJECT DIALOG
    GlassModalDialog(
        visible = showProjectDialog,
        onDismiss = { showProjectDialog = false },
        title = "Initialize Construction Site Project",
        darkTheme = dark,
        glowColor = NeonPurple
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassTextField(value = newProjName, onValueChange = { newProjName = it }, label = "Site/Project Class Name", placeholder = "Skyline Corporate Tower", darkTheme = dark)
            GlassTextField(value = newProjLoc, onValueChange = { newProjLoc = it }, label = "Geological Location Block", placeholder = "Sector 62, City Center", darkTheme = dark)
            GlassTextField(value = newProjBudget, onValueChange = { newProjBudget = it }, label = "Fiscal Budget ($)", isNumeric = true, placeholder = "1250000.0", darkTheme = dark)

            GlassButton(
                onClick = {
                    val budget = newProjBudget.toDoubleOrNull() ?: 100000.0
                    if (newProjName.isNotBlank() && newProjLoc.isNotBlank()) {
                        viewModel.addProject(newProjName, newProjLoc, budget)
                        newProjName = ""
                        newProjLoc = ""
                        newProjBudget = ""
                        showProjectDialog = false
                    }
                },
                enabled = newProjName.isNotBlank(),
                glowColor = NeonPurple,
                darkTheme = dark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("LAUNCH SITE OPERATIONS", fontWeight = FontWeight.Bold)
            }
        }
    }

    // 2. ADD TRANSACTION DIALOG
    GlassModalDialog(
        visible = showTransactionDialog,
        onDismiss = { showTransactionDialog = false },
        title = "Register Cash Ledgers",
        darkTheme = dark,
        glowColor = if (txType == "Money In") NeonGreen else NeonPink
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Money In Toggle
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (txType == "Money In") NeonGreen.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { txType = "Money In" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cash In (Milestone Payment)", color = if (txType == "Money In") NeonGreen else if (dark) TextSecondary else TextSecondaryLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Money Out Toggle
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (txType == "Money Out") NeonPink.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { txType = "Money Out" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cash Out (Expenses/Wages)", color = if (txType == "Money Out") NeonPink else if (dark) TextSecondary else TextSecondaryLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            GlassTextField(value = txAmount, onValueChange = { txAmount = it }, label = "Transaction Amount ($)", isNumeric = true, placeholder = "45000.0", darkTheme = dark)
            GlassTextField(value = txDesc, onValueChange = { txDesc = it }, label = "Brief Expenditure Memo", placeholder = "Weekly worker payout session", darkTheme = dark)

            // Category Selection Row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Material", "Labor", "Equipment", "Other").forEach { cat ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (txCategory == cat) NeonPurple.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { txCategory = cat }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(cat, color = if (txCategory == cat) NeonPurple else if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp)
                    }
                }
            }

            GlassButton(
                onClick = {
                    val amt = txAmount.toDoubleOrNull() ?: 0.0
                    val proj = currentProject
                    if (amt > 0 && txDesc.isNotBlank() && proj != null) {
                        viewModel.addTransaction(proj.id, txType, amt, txCategory, txDesc, "2026-05-26")
                        txAmount = ""
                        txDesc = ""
                        showTransactionDialog = false
                    }
                },
                enabled = txAmount.isNotBlank() && txDesc.isNotBlank(),
                glowColor = if (txType == "Money In") NeonGreen else NeonPink,
                darkTheme = dark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("PROCESS TRANSACTION RECORD", fontWeight = FontWeight.Bold)
            }
        }
    }

    // 3. ADD TASK DIALOG
    GlassModalDialog(
        visible = showTaskDialog,
        onDismiss = { showTaskDialog = false },
        title = "Deploy Site Task Assignment",
        darkTheme = dark,
        glowColor = NeonCyan
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassTextField(value = taskTitle, onValueChange = { taskTitle = it }, label = "Task Description Title", placeholder = "Conduct structural welding integration", darkTheme = dark)
            GlassTextField(value = taskAssigneeName, onValueChange = { taskAssigneeName = it }, label = "Select/Type Assignee Name", placeholder = "Michael Tyson (Supervisor)", darkTheme = dark)

            // Priority Selection Row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("High", "Medium", "Low").forEach { p ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (taskPriority == p) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { taskPriority = p }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(p, color = if (taskPriority == p) NeonCyan else if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp)
                    }
                }
            }

            GlassTextField(value = taskDueDate, onValueChange = { taskDueDate = it }, label = "Task Due Date deadline", placeholder = "2026-05-29", darkTheme = dark)

            GlassButton(
                onClick = {
                    val proj = currentProject
                    if (taskTitle.isNotBlank() && proj != null) {
                        viewModel.addTask(proj.id, taskTitle, taskPriority, if (taskAssigneeName.isBlank()) "Crew" else taskAssigneeName, taskDueDate)
                        taskTitle = ""
                        taskAssigneeName = ""
                        showTaskDialog = false
                    }
                },
                enabled = taskTitle.isNotBlank(),
                glowColor = NeonCyan,
                darkTheme = dark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("DELEGATE SITE TASK", fontWeight = FontWeight.Bold)
            }
        }
    }

    // 4. ADD WORKER DIALOG
    GlassModalDialog(
        visible = showWorkerDialog,
        onDismiss = { showWorkerDialog = false },
        title = "Add Worker Crew Profile",
        darkTheme = dark,
        glowColor = NeonGreen
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassTextField(value = workerName, onValueChange = { workerName = it }, label = "Worker Full Name", placeholder = "John Doe", darkTheme = dark)
            GlassTextField(value = workerRole, onValueChange = { workerRole = it }, label = "Crew Role / Class Designation", placeholder = "Brick Mason Foreman", darkTheme = dark)
            GlassTextField(value = workerWage, onValueChange = { workerWage = it }, label = "Daily Standard Wage Rate ($)", isNumeric = true, placeholder = "350.0", darkTheme = dark)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Day", "Night").forEach { sh ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (workerShift == sh) NeonGreen.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { workerShift = sh }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$sh Shift", color = if (workerShift == sh) NeonGreen else if (dark) TextSecondary else TextSecondaryLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            GlassButton(
                onClick = {
                    val rate = workerWage.toDoubleOrNull() ?: 250.0
                    if (workerName.isNotBlank() && workerRole.isNotBlank()) {
                        // Dynamically choose random vivid neon color for avatar bubble
                        val colors = listOf(0xFF3B82F6.toInt(), 0xFFEC4899.toInt(), 0xFF10B981.toInt(), 0xFFF59E0B.toInt(), 0xFF8B5CF6.toInt())
                        val avatarC = colors.random()

                        viewModel.addWorker(workerName, workerRole, workerShift, rate, avatarC)
                        workerName = ""
                        workerWage = ""
                        showWorkerDialog = false
                    }
                },
                enabled = workerName.isNotBlank() && workerRole.isNotBlank(),
                glowColor = NeonGreen,
                darkTheme = dark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("APPREND CROW PROFILE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Side navigations rows on desktop screens
@Composable
fun SidebarNavRow(
    label: String,
    icon: ImageVector,
    active: Boolean,
    darkTheme: Boolean,
    onClick: () -> Unit
) {
    val bg = if (active) {
        if (darkTheme) NeonCyan.copy(alpha = 0.15f) else Color(0xFF0284C7).copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }

    val tc = if (active) {
        if (darkTheme) NeonCyan else Color(0xFF0284C7)
    } else {
        if (darkTheme) TextSecondary else TextSecondaryLight
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tc,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = tc,
            fontSize = 14.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// Bottom Bar Core Item views on mobile screens
@Composable
fun BottomBarNavItem(
    icon: ImageVector,
    active: Boolean,
    darkTheme: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val tc = if (active) {
        if (darkTheme) NeonCyan else Color(0xFF0284C7)
    } else {
        if (darkTheme) TextSecondary else TextSecondaryLight
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tc,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = tc,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
        )
    }
}
