package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    private val viewModel: MainViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ConstructionRepository(database.constructionDao())
        MainViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
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
    var txSelectedParty by remember { mutableStateOf<Worker?>(null) }
    var txReference by remember { mutableStateOf("") }
    var txPaymentMethod by remember { mutableStateOf("Cash") } // "Cash", "Bank Transfer", "Cheque"

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
    var workerPhone by remember { mutableStateOf("") }
    var workerEmail by remember { mutableStateOf("") }
    var workerPartyType by remember { mutableStateOf("Worker") } // Client, Staff, Vendor, Worker, Investor, etc.
    var workerAddress by remember { mutableStateOf("") }
    var workerPartyId by remember { mutableStateOf("") }
    var workerDateOfJoining by remember { mutableStateOf("27/05/2026") }
    var workerAadhaar by remember { mutableStateOf("") }
    var workerPan by remember { mutableStateOf("") }
    var workerReference by remember { mutableStateOf("") }

    // Synchronize UI modal sheets with global triggers
    LaunchedEffect(viewModel.showQuickDialog) {
        if (viewModel.showQuickDialog) {
            showQuickDialog = true
            viewModel.showQuickDialog = false
        }
    }
    LaunchedEffect(viewModel.showProjectDialog) {
        if (viewModel.showProjectDialog) {
            showProjectDialog = true
            viewModel.showProjectDialog = false
        }
    }
    LaunchedEffect(viewModel.showTransactionDialog) {
        if (viewModel.showTransactionDialog) {
            showTransactionDialog = true
            txType = viewModel.transactionTypePreset
            viewModel.showTransactionDialog = false
        }
    }
    LaunchedEffect(viewModel.showTaskDialog) {
        if (viewModel.showTaskDialog) {
            showTaskDialog = true
            viewModel.showTaskDialog = false
        }
    }
    LaunchedEffect(viewModel.showWorkerDialog) {
        if (viewModel.showWorkerDialog) {
            showWorkerDialog = true
            viewModel.showWorkerDialog = false
        }
    }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BuildOnSiteLogo(modifier = Modifier.size(36.dp), darkTheme = dark)
                        Text(
                            text = "ConstructPro",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (dark) NeonCyan else Color(0xFF0284C7)
                        )
                    }

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

                    // Elegant Floating Glassmorphism Bottom Navigation Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
                            .height(66.dp)
                            .background(
                                if (dark) Color(0x7D0B0F19) else Color(0xD2F8FAFC),
                                RoundedCornerShape(24.dp)
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (dark) GlassBorderDark else Color(0x1F6366F1)
                                ),
                                RoundedCornerShape(24.dp)
                            ),
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
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
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

            // Party Name Mapping Dropdown Lookup Look!
            Text("Party Name Mapping / Account", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            
            var partySearchQuery by remember { mutableStateOf("") }
            var isSearchingParty by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GlassTextField(
                        value = txSelectedParty?.name ?: partySearchQuery,
                        onValueChange = {
                            if (txSelectedParty != null) {
                                txSelectedParty = null
                            }
                            partySearchQuery = it
                            isSearchingParty = true
                        },
                        label = "Search or Select Party",
                        placeholder = "Type to search party...",
                        darkTheme = dark,
                        icon = Icons.Default.Search
                    )
                }
                
                if (txSelectedParty != null) {
                    IconButton(
                        onClick = {
                            txSelectedParty = null
                            partySearchQuery = ""
                        }
                    ) {
                        Icon(Icons.Default.Clear, "Clear mapping", tint = NeonPink)
                    }
                }
            }

            // Suggestions List
            if (isSearchingParty || (partySearchQuery.isNotEmpty() && txSelectedParty == null)) {
                val matched = allWorkers.filter {
                    it.name.contains(partySearchQuery, ignoreCase = true) ||
                    it.partyType.contains(partySearchQuery, ignoreCase = true)
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (dark) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        if (matched.isEmpty()) {
                            Text(
                                text = "No matching parties found.",
                                color = if (dark) TextSecondary else TextSecondaryLight,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            matched.take(5).forEach { party ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            txSelectedParty = party
                                            partySearchQuery = party.name
                                            isSearchingParty = false
                                        }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(party.name, color = if (dark) TextPrimary else TextPrimaryLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(party.partyType + (if (party.phone.isNotEmpty()) " • ${party.phone}" else ""), color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 11.sp)
                                    }
                                    Text("Select", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = if (dark) Color(0x33FFFFFF) else Color(0x33000000))
                        
                        // Create Party Link
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isSearchingParty = false
                                    showTransactionDialog = false
                                    showWorkerDialog = true
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AddCircleOutline, null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Create New Party", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (txSelectedParty != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (dark) Color(0x3310B981) else Color(0x2210B981))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mapped to: ${txSelectedParty?.name} [${txSelectedParty?.partyType}]",
                        color = if (dark) NeonGreen else Color(0xFF047857),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            GlassTextField(value = txAmount, onValueChange = { txAmount = it }, label = "Transaction Amount ($)", isNumeric = true, placeholder = "45000.0", darkTheme = dark)

            Text("Payment Method", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Cash", "Bank Transfer", "Cheque").forEach { method ->
                    val selected = txPaymentMethod == method
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) NeonGreen.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (selected) NeonGreen else if (dark) GlassBorderLight.copy(alpha = 0.2f) else GlassBorderLight, RoundedCornerShape(8.dp))
                            .clickable { txPaymentMethod = method }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(method, color = if (selected) NeonGreen else if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            GlassTextField(value = txReference, onValueChange = { txReference = it }, label = "Reference No. / Cheque / TxRef", placeholder = "REF-987293", darkTheme = dark)

            // Category Selection Row (Cost Code)
            Text("Add Cost Code / Segment", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

            GlassTextField(value = txDesc, onValueChange = { txDesc = it }, label = "Brief Expenditure Memo / More Details", placeholder = "Weekly worker payout session", darkTheme = dark)

            GlassButton(
                onClick = {
                    val amt = txAmount.toDoubleOrNull() ?: 0.0
                    val proj = currentProject
                    if (amt > 0 && txDesc.isNotBlank() && proj != null) {
                        viewModel.addTransaction(
                            projectId = proj.id,
                            type = txType,
                            amount = amt,
                            category = txCategory,
                            description = txDesc,
                            date = "2026-05-27",
                            partyId = txSelectedParty?.id,
                            partyName = txSelectedParty?.name,
                            reference = txReference,
                            paymentMethod = txPaymentMethod
                        )
                        txAmount = ""
                        txDesc = ""
                        txReference = ""
                        txSelectedParty = null
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
    LaunchedEffect(showWorkerDialog) {
        if (showWorkerDialog && workerPartyId.isBlank()) {
            workerPartyId = "PID-${allWorkers.size + 1}"
        }
    }

    GlassModalDialog(
        visible = showWorkerDialog,
        onDismiss = { showWorkerDialog = false },
        title = "Add New Party / Worker Profile",
        darkTheme = dark,
        glowColor = NeonGreen
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassTextField(value = workerPartyId, onValueChange = { workerPartyId = it }, label = "Party ID", placeholder = "PID-1", darkTheme = dark)
            
            GlassTextField(value = workerName, onValueChange = { workerName = it }, label = "Party / Worker Full Name", placeholder = "e.g. John Doe / Tejas Contractors", darkTheme = dark)
            
            GlassTextField(value = workerPhone, onValueChange = { workerPhone = it }, label = "Phone Number (+91)", placeholder = "e.g. 9876543210", darkTheme = dark)
            
            GlassTextField(value = workerEmail, onValueChange = { workerEmail = it }, label = "Email Address", placeholder = "e.g. client@example.com", darkTheme = dark)

            Text("Party Type Category", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Client", "Staff", "Vendor", "Worker").forEach { type ->
                    val selected = workerPartyType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) NeonGreen.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (selected) NeonGreen else if (dark) GlassBorderLight.copy(alpha = 0.2f) else GlassBorderLight, RoundedCornerShape(8.dp))
                            .clickable { workerPartyType = type }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(type, color = if (selected) NeonGreen else if (dark) TextSecondary else TextSecondaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (workerPartyType == "Investor") NeonGreen.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (workerPartyType == "Investor") NeonGreen else if (dark) GlassBorderLight.copy(alpha = 0.2f) else GlassBorderLight, RoundedCornerShape(8.dp))
                        .clickable { workerPartyType = "Investor" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Investor", color = if (workerPartyType == "Investor") NeonGreen else if (dark) TextSecondary else TextSecondaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            GlassTextField(value = workerAddress, onValueChange = { workerAddress = it }, label = "Your Address / Location", placeholder = "Enter home or office address...", darkTheme = dark)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GlassTextField(value = workerDateOfJoining, onValueChange = { workerDateOfJoining = it }, label = "Date of Joining", placeholder = "27/05/2026", darkTheme = dark)
                }
                Box(modifier = Modifier.weight(1f)) {
                    GlassTextField(value = workerRole, onValueChange = { workerRole = it }, label = "Designation/Role", placeholder = "e.g. Foreman", darkTheme = dark)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GlassTextField(value = workerAadhaar, onValueChange = { workerAadhaar = it }, label = "Aadhaar Card No.", placeholder = "12-digit number", darkTheme = dark)
                }
                Box(modifier = Modifier.weight(1f)) {
                    GlassTextField(value = workerPan, onValueChange = { workerPan = it }, label = "PAN Card No.", placeholder = "10-character code", darkTheme = dark)
                }
            }

            GlassTextField(value = workerReference, onValueChange = { workerReference = it }, label = "Referred By / Given Reference", placeholder = "Given reference, e.g. Partner X", darkTheme = dark)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1.2f)) {
                    GlassTextField(value = workerWage, onValueChange = { workerWage = it }, label = "Daily Wage / Rate Rate ($)", isNumeric = true, placeholder = "350.0", darkTheme = dark)
                }
                
                Column(modifier = Modifier.weight(0.8f)) {
                    Text("Standard Shift", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Day", "Night").forEach { sh ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (workerShift == sh) NeonGreen.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { workerShift = sh }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(sh, color = if (workerShift == sh) NeonGreen else if (dark) TextSecondary else TextSecondaryLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            GlassButton(
                onClick = {
                    val rate = workerWage.toDoubleOrNull() ?: 0.0
                    if (workerName.isNotBlank()) {
                        val colors = listOf(0xFF3B82F6.toInt(), 0xFFEC4899.toInt(), 0xFF10B981.toInt(), 0xFFF59E0B.toInt(), 0xFF8B5CF6.toInt())
                        val avatarC = colors.random()

                        viewModel.addWorker(
                            name = workerName,
                            role = if (workerRole.isNotBlank()) workerRole else workerPartyType,
                            shift = workerShift,
                            wageRate = rate,
                            color = avatarC,
                            phone = workerPhone,
                            email = workerEmail,
                            partyType = workerPartyType,
                            address = workerAddress,
                            partyId = workerPartyId,
                            dateOfJoining = workerDateOfJoining,
                            aadhaar = workerAadhaar,
                            pan = workerPan,
                            reference = workerReference
                        )
                        
                        // Clear states
                        workerName = ""
                        workerRole = "Mason Foreman"
                        workerWage = ""
                        workerPhone = ""
                        workerEmail = ""
                        workerPartyType = "Worker"
                        workerAddress = ""
                        workerPartyId = ""
                        workerAadhaar = ""
                        workerPan = ""
                        workerReference = ""
                        showWorkerDialog = false
                    }
                },
                enabled = workerName.isNotBlank(),
                glowColor = NeonGreen,
                darkTheme = dark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("PROCESS PARTY PROFILE", fontWeight = FontWeight.Bold)
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

// Bottom Bar Core Item views on mobile screens - dynamic luxury capsule styling
@Composable
fun BottomBarNavItem(
    icon: ImageVector,
    active: Boolean,
    darkTheme: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val tc = if (active) {
        if (darkTheme) NeonCyan else Color(0xFF0369A1) // Sky 700 for spectacular contrast in light theme
    } else {
        if (darkTheme) TextSecondary else TextSecondaryLight
    }

    val bubbleBg = if (active) {
        if (darkTheme) NeonCyan.copy(alpha = 0.12f) else Color(0x1F0284C7)
    } else {
        Color.Transparent
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bubbleBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tc,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = tc,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.Black else FontWeight.Bold
        )
    }
}
