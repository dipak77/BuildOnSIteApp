package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.ConstructionRepository
import com.example.data.Worker
import com.example.ui.MainViewModel
import com.example.ui.AppScreen
import com.example.ui.BuildOnSiteLogo
import com.example.ui.DashboardScreen
import com.example.ui.GlassAtmosphereBox
import com.example.ui.GlassButton
import com.example.ui.GlassModalDialog
import com.example.ui.GlassTextField
import com.example.ui.GoogleLoginScreen
import com.example.ui.MoneyScreen
import com.example.ui.MoreScreen
import com.example.ui.SiteScreen
import com.example.ui.TasksScreen
import com.example.ui.theme.GlassBorderDark
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextSecondaryLight

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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScaffoldFrame(viewModel: MainViewModel) {
    val dark = viewModel.darkThemeEnabled
    val currentTab = viewModel.currentScreen

    var showQuickDialog by remember { mutableStateOf(false) }
    var showProjectDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var showWorkerDialog by remember { mutableStateOf(false) }

    val currentProject by viewModel.activeProject.collectAsState()
    val allWorkers by viewModel.workers.collectAsState()

    var newProjName by remember { mutableStateOf("") }
    var newProjLoc by remember { mutableStateOf("") }
    var newProjBudget by remember { mutableStateOf("") }

    var txType by remember { mutableStateOf("Money Out") }
    var txAmount by remember { mutableStateOf("") }
    var txCategory by remember { mutableStateOf("Material") }
    var txDesc by remember { mutableStateOf("") }
    var txSelectedParty by remember { mutableStateOf<Worker?>(null) }
    var txReference by remember { mutableStateOf("") }
    var txPaymentMethod by remember { mutableStateOf("Cash") }
    var partySearchQuery by remember { mutableStateOf("") }
    var isSearchingParty by remember { mutableStateOf(false) }

    var taskTitle by remember { mutableStateOf("") }
    var taskPriority by remember { mutableStateOf("Medium") }
    var taskAssigneeName by remember { mutableStateOf("") }
    var taskDueDate by remember { mutableStateOf("2026-05-30") }

    var workerName by remember { mutableStateOf("") }
    var workerRole by remember { mutableStateOf("Mason Foreman") }
    var workerShift by remember { mutableStateOf("Day") }
    var workerWage by remember { mutableStateOf("") }
    var workerPhone by remember { mutableStateOf("") }
    var workerEmail by remember { mutableStateOf("") }
    var workerPartyType by remember { mutableStateOf("Worker") }
    var workerAddress by remember { mutableStateOf("") }
    var workerPartyId by remember { mutableStateOf("") }
    var workerDateOfJoining by remember { mutableStateOf("30/05/2026") }
    var workerAadhaar by remember { mutableStateOf("") }
    var workerPan by remember { mutableStateOf("") }
    var workerReference by remember { mutableStateOf("") }

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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(viewModel = viewModel) {
                coroutineScope.launch { drawerState.close() }
            }
        },
        gesturesEnabled = true
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
        val isDesktopWidth = maxWidth >= 1024.dp

        if (isDesktopWidth) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                        .padding(16.dp)
                        .background(if (dark) Color(0x35111827) else Color(0xDFFFFFFF), RoundedCornerShape(24.dp))
                        .border(BorderStroke(1.dp, if (dark) GlassBorderDark else GlassBorderLight), RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BuildOnSiteLogo(modifier = Modifier.size(36.dp), darkTheme = dark)
                        Text(
                            text = "ConstructPro",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (dark) NeonCyan else Color(0xFF0284C7)
                        )
                    }
                    HorizontalDivider(color = if (dark) GlassBorderDark else GlassBorderLight)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SidebarNavRow("Dashboard", Icons.Default.Dashboard, currentTab == AppScreen.Dashboard, dark) { viewModel.currentScreen = AppScreen.Dashboard }
                        SidebarNavRow("Cash Ledger", Icons.Default.MonetizationOn, currentTab == AppScreen.Money, dark) { viewModel.currentScreen = AppScreen.Money }
                        SidebarNavRow("Site Tasks", Icons.Default.TaskAlt, currentTab == AppScreen.Tasks, dark) { viewModel.currentScreen = AppScreen.Tasks }
                        SidebarNavRow("Attendance Logs", Icons.Default.Face, currentTab == AppScreen.Site, dark) { viewModel.currentScreen = AppScreen.Site }
                        SidebarNavRow("More Options", Icons.Default.MoreHoriz, currentTab == AppScreen.More, dark) { viewModel.currentScreen = AppScreen.More }
                    }
                    GlassButton(onClick = { showProjectDialog = true }, glowColor = NeonPurple, darkTheme = dark, modifier = Modifier.fillMaxWidth()) {
                        Text("+ NEW SITE PROJECT", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() + slideInVertically { it / 6 } togetherWith fadeOut() + slideOutVertically { it / 6 } },
                        label = "desktopTabsTransition"
                    ) { tab ->
                        when (tab) {
                            AppScreen.Dashboard -> DashboardScreen(
                                viewModel = viewModel,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } }
                            )
                            AppScreen.Money -> MoneyScreen(viewModel = viewModel)
                            AppScreen.Tasks -> TasksScreen(viewModel = viewModel)
                            AppScreen.Site -> SiteScreen(viewModel = viewModel)
                            AppScreen.More -> MoreScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = { fadeIn() + slideInVertically { it / 8 } togetherWith fadeOut() + slideOutVertically { it / 8 } },
                            label = "mobileTabsTransition"
                        ) { tab ->
                            when (tab) {
                                AppScreen.Dashboard -> DashboardScreen(
                                    viewModel = viewModel,
                                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                                )
                                AppScreen.Money -> MoneyScreen(viewModel = viewModel)
                                AppScreen.Tasks -> TasksScreen(viewModel = viewModel)
                                AppScreen.Site -> SiteScreen(viewModel = viewModel)
                                AppScreen.More -> MoreScreen(viewModel = viewModel)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
                            .height(66.dp)
                            .background(if (dark) Color(0x7D0B0F19) else Color(0xDDF8FAFC), RoundedCornerShape(24.dp))
                            .border(BorderStroke(1.dp, if (dark) GlassBorderDark else Color(0x1F6366F1)), RoundedCornerShape(24.dp)),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomBarNavItem(Icons.Default.Dashboard, currentTab == AppScreen.Dashboard, dark, "Dashboard") { viewModel.currentScreen = AppScreen.Dashboard }
                        BottomBarNavItem(Icons.Default.AccountBalanceWallet, currentTab == AppScreen.Money, dark, "Money") { viewModel.currentScreen = AppScreen.Money }
                        BottomBarNavItem(Icons.Default.TaskAlt, currentTab == AppScreen.Tasks, dark, "Tasks") { viewModel.currentScreen = AppScreen.Tasks }
                        BottomBarNavItem(Icons.Default.EventAvailable, currentTab == AppScreen.Site, dark, "Site") { viewModel.currentScreen = AppScreen.Site }
                        BottomBarNavItem(Icons.Default.Menu, currentTab == AppScreen.More, dark, "More") { viewModel.currentScreen = AppScreen.More }
                    }
                }
            }
        }

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
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = if (isDesktopWidth) 24.dp else 94.dp, end = 24.dp)
                .size(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Record action", modifier = Modifier.size(28.dp))
        }
    }

    }

    GlassModalDialog(
        visible = showQuickDialog,
        onDismiss = { showQuickDialog = false },
        title = "Operations Quick Actions",
        darkTheme = dark,
        glowColor = NeonCyan
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Register a quick site ledger transaction or assignment slot.", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp)
            GlassButton(onClick = { showQuickDialog = false; showTransactionDialog = true }, darkTheme = dark, glowColor = NeonCyan, modifier = Modifier.fillMaxWidth()) {
                Text("Register Cash Transaction", fontWeight = FontWeight.Bold)
            }
            GlassButton(onClick = { showQuickDialog = false; showTaskDialog = true }, darkTheme = dark, glowColor = NeonPurple, modifier = Modifier.fillMaxWidth()) {
                Text("Assign Crew Task", fontWeight = FontWeight.Bold)
            }
            GlassButton(onClick = { showQuickDialog = false; showWorkerDialog = true }, darkTheme = dark, glowColor = NeonGreen, modifier = Modifier.fillMaxWidth()) {
                Text("Create Worker Profile", fontWeight = FontWeight.Bold)
            }
        }
    }

    GlassModalDialog(
        visible = showProjectDialog,
        onDismiss = { showProjectDialog = false },
        title = "Initialize Construction Site Project",
        darkTheme = dark,
        glowColor = NeonPurple
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassTextField(newProjName, { newProjName = it }, "Site / Project Class Name", placeholder = "Skyline Corporate Tower", darkTheme = dark)
            GlassTextField(newProjLoc, { newProjLoc = it }, "Geological Location Block", placeholder = "Sector 62, City Center", darkTheme = dark)
            GlassTextField(newProjBudget, { newProjBudget = it }, "Fiscal Budget (₹)", isNumeric = true, placeholder = "1250000.0", darkTheme = dark)
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
            ) { Text("LAUNCH SITE OPERATIONS", fontWeight = FontWeight.Bold) }
        }
    }

    GlassModalDialog(
        visible = showTransactionDialog,
        onDismiss = { showTransactionDialog = false },
        title = "Register Cash Ledgers",
        darkTheme = dark,
        glowColor = if (txType == "Money In") NeonGreen else NeonPink
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TogglePill(
                    text = "Cash In",
                    selected = txType == "Money In",
                    selectedColor = NeonGreen,
                    darkTheme = dark,
                    modifier = Modifier.weight(1f)
                ) { txType = "Money In" }
                TogglePill(
                    text = "Cash Out",
                    selected = txType == "Money Out",
                    selectedColor = NeonPink,
                    darkTheme = dark,
                    modifier = Modifier.weight(1f)
                ) { txType = "Money Out" }
            }

            Text("Party Name Mapping / Account", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    GlassTextField(
                        value = txSelectedParty?.name ?: partySearchQuery,
                        onValueChange = {
                            if (txSelectedParty != null) txSelectedParty = null
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
                    androidx.compose.material3.IconButton(onClick = { txSelectedParty = null; partySearchQuery = "" }) {
                        Icon(Icons.Default.Clear, "Clear mapping", tint = NeonPink)
                    }
                }
            }

            if (isSearchingParty || (partySearchQuery.isNotEmpty() && txSelectedParty == null)) {
                val matched = allWorkers.filter {
                    it.name.contains(partySearchQuery, ignoreCase = true) || it.partyType.contains(partySearchQuery, ignoreCase = true)
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (dark) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        if (matched.isEmpty()) {
                            Text("No matching parties found.", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                        } else {
                            matched.take(5).forEach { party ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        txSelectedParty = party
                                        partySearchQuery = party.name
                                        isSearchingParty = false
                                    }.padding(8.dp),
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
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                isSearchingParty = false
                                showTransactionDialog = false
                                showWorkerDialog = true
                            }.padding(8.dp),
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
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (dark) Color(0x3310B981) else Color(0x2210B981)).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mapped to: ${txSelectedParty?.name} [${txSelectedParty?.partyType}]", color = if (dark) NeonGreen else Color(0xFF047857), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            GlassTextField(txAmount, { txAmount = it }, "Transaction Amount (₹)", isNumeric = true, placeholder = "45000.0", darkTheme = dark)
            Text("Payment Method", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Cash", "Bank Transfer", "Cheque").forEach { method ->
                    TogglePill(method, txPaymentMethod == method, NeonGreen, dark, Modifier.weight(1f)) { txPaymentMethod = method }
                }
            }
            GlassTextField(txReference, { txReference = it }, "Reference No. / Cheque / TxRef", placeholder = "REF-987293", darkTheme = dark)
            Text("Add Cost Code / Segment", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Material", "Labor", "Equipment", "Other").forEach { cat ->
                    TogglePill(cat, txCategory == cat, NeonPurple, dark, Modifier.weight(1f)) { txCategory = cat }
                }
            }
            GlassTextField(txDesc, { txDesc = it }, "Brief Expenditure Memo / More Details", placeholder = "Weekly worker payout session", darkTheme = dark)
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
                            date = "2026-05-30",
                            partyId = txSelectedParty?.id,
                            partyName = txSelectedParty?.name,
                            reference = txReference,
                            paymentMethod = txPaymentMethod
                        )
                        txAmount = ""
                        txDesc = ""
                        txReference = ""
                        txSelectedParty = null
                        partySearchQuery = ""
                        showTransactionDialog = false
                    }
                },
                enabled = txAmount.isNotBlank() && txDesc.isNotBlank(),
                glowColor = if (txType == "Money In") NeonGreen else NeonPink,
                darkTheme = dark,
                modifier = Modifier.fillMaxWidth()
            ) { Text("PROCESS TRANSACTION RECORD", fontWeight = FontWeight.Bold) }
        }
    }

    GlassModalDialog(
        visible = showTaskDialog,
        onDismiss = { showTaskDialog = false },
        title = "Deploy Site Task Assignment",
        darkTheme = dark,
        glowColor = NeonCyan
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassTextField(taskTitle, { taskTitle = it }, "Task Description Title", placeholder = "Conduct structural welding integration", darkTheme = dark)
            GlassTextField(taskAssigneeName, { taskAssigneeName = it }, "Select/Type Assignee Name", placeholder = "Supervisor / Crew", darkTheme = dark)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("High", "Medium", "Low").forEach { p ->
                    TogglePill(p, taskPriority == p, NeonCyan, dark, Modifier.weight(1f)) { taskPriority = p }
                }
            }
            GlassTextField(taskDueDate, { taskDueDate = it }, "Task Due Date Deadline", placeholder = "2026-05-30", darkTheme = dark)
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
            ) { Text("DELEGATE SITE TASK", fontWeight = FontWeight.Bold) }
        }
    }

    LaunchedEffect(showWorkerDialog) {
        if (showWorkerDialog && workerPartyId.isBlank()) workerPartyId = "PID-${allWorkers.size + 1}"
    }
    GlassModalDialog(
        visible = showWorkerDialog,
        onDismiss = { showWorkerDialog = false },
        title = "Add New Party / Worker Profile",
        darkTheme = dark,
        glowColor = NeonGreen
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassTextField(workerPartyId, { workerPartyId = it }, "Party ID", placeholder = "PID-1", darkTheme = dark)
            GlassTextField(workerName, { workerName = it }, "Party / Worker Full Name", placeholder = "e.g. John Doe / Tejas Contractors", darkTheme = dark)
            GlassTextField(workerPhone, { workerPhone = it }, "Phone Number (+91)", placeholder = "9876543210", darkTheme = dark)
            GlassTextField(workerEmail, { workerEmail = it }, "Email Address", placeholder = "client@example.com", darkTheme = dark)
            Text("Party Type Category", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Client", "Staff", "Vendor", "Worker", "Investor").forEach { type ->
                    TogglePill(type, workerPartyType == type, NeonGreen, dark, Modifier.weight(1f), fontSize = 10.sp) { workerPartyType = type }
                }
            }
            GlassTextField(workerAddress, { workerAddress = it }, "Address / Location", placeholder = "Enter home or office address", darkTheme = dark)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) { GlassTextField(workerDateOfJoining, { workerDateOfJoining = it }, "Date of Joining", placeholder = "30/05/2026", darkTheme = dark) }
                Box(modifier = Modifier.weight(1f)) { GlassTextField(workerRole, { workerRole = it }, "Designation / Role", placeholder = "Foreman", darkTheme = dark) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) { GlassTextField(workerAadhaar, { workerAadhaar = it }, "Aadhaar Card No.", placeholder = "12-digit", darkTheme = dark) }
                Box(modifier = Modifier.weight(1f)) { GlassTextField(workerPan, { workerPan = it }, "PAN Card No.", placeholder = "10-character", darkTheme = dark) }
            }
            GlassTextField(workerReference, { workerReference = it }, "Referred By / Given Reference", placeholder = "Partner X", darkTheme = dark)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1.2f)) {
                    GlassTextField(workerWage, { workerWage = it }, "Daily Wage / Rate (₹)", isNumeric = true, placeholder = "350.0", darkTheme = dark)
                }
                Column(modifier = Modifier.weight(0.8f)) {
                    Text("Standard Shift", color = if (dark) TextSecondary else TextSecondaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Day", "Night").forEach { sh ->
                            TogglePill(sh, workerShift == sh, NeonGreen, dark, Modifier.weight(1f), fontSize = 11.sp) { workerShift = sh }
                        }
                    }
                }
            }
            GlassButton(
                onClick = {
                    val rate = workerWage.toDoubleOrNull() ?: 0.0
                    if (workerName.isNotBlank()) {
                        val colors = listOf(0xFF3B82F6.toInt(), 0xFFEC4899.toInt(), 0xFF10B981.toInt(), 0xFFF59E0B.toInt(), 0xFF8B5CF6.toInt())
                        viewModel.addWorker(
                            name = workerName,
                            role = if (workerRole.isNotBlank()) workerRole else workerPartyType,
                            shift = workerShift,
                            wageRate = rate,
                            color = colors.random(),
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
            ) { Text("PROCESS PARTY PROFILE", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun TogglePill(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) selectedColor.copy(alpha = 0.20f) else Color.Transparent)
            .border(1.dp, if (selected) selectedColor else if (darkTheme) GlassBorderLight.copy(alpha = 0.20f) else GlassBorderLight, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) selectedColor else if (darkTheme) TextSecondary else TextSecondaryLight,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SidebarNavRow(label: String, icon: ImageVector, active: Boolean, darkTheme: Boolean, onClick: () -> Unit) {
    val bg = if (active) {
        if (darkTheme) NeonCyan.copy(alpha = 0.15f) else Color(0xFF0284C7).copy(alpha = 0.10f)
    } else Color.Transparent
    val tc = if (active) {
        if (darkTheme) NeonCyan else Color(0xFF0284C7)
    } else if (darkTheme) TextSecondary else TextSecondaryLight

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(bg).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tc, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = tc, fontSize = 14.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
fun BottomBarNavItem(icon: ImageVector, active: Boolean, darkTheme: Boolean, label: String, onClick: () -> Unit) {
    val tc = if (active) {
        if (darkTheme) NeonCyan else Color(0xFF0369A1)
    } else if (darkTheme) TextSecondary else TextSecondaryLight
    val bubbleBg = if (active) {
        if (darkTheme) NeonCyan.copy(alpha = 0.12f) else Color(0x1F0284C7)
    } else Color.Transparent

    Column(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(bubbleBg).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = tc, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, color = tc, fontSize = 11.sp, fontWeight = if (active) FontWeight.Black else FontWeight.Bold)
    }
}

@Composable
fun DrawerContent(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val dark = viewModel.darkThemeEnabled
    val currentProject by viewModel.activeProject.collectAsState()
    val userSession by viewModel.userSession.collectAsState()
    val currentTab = viewModel.currentScreen
    val activeSiteTab = viewModel.activeSiteTab

    // Compute initials dynamically (e.g. "Treasure Garden" -> "TG")
    val projectName = currentProject?.name ?: "Treasure Garden"
    val initials = projectName.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .take(2)
        .ifEmpty { "TG" }

    val userEmail = userSession?.email ?: "haranedipak@gmail.com"

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(310.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        if (dark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        if (dark) Color(0xFF1E1B4B) else Color(0xFFEEF2F6),
                        if (dark) Color(0xFF090D1A) else Color(0xFFE2E8F0)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (dark) GlassBorderDark else Color(0x330284C7)
                ),
                RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            // Profile & Project Badge Block (arranged vertically exactly as in the image)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Circular Initials Logo (TG)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFF4F46E5),
                                    Color(0xFF312E81)
                                )
                            )
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                // Workspace & Email Names
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = projectName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dark) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = userEmail,
                        fontSize = 12.sp,
                        color = if (dark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Channels Header
            Text(
                text = "WORKSPACE CHANNELS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = if (dark) Color.White.copy(alpha = 0.4f) else Color(0xFF64748B),
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Channels Navigation List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // 1. Site Dashboard
                DrawerNavItem(
                    label = "Site Dashboard",
                    icon = Icons.Default.Dashboard,
                    active = currentTab == AppScreen.Dashboard,
                    darkTheme = dark
                ) {
                    viewModel.currentScreen = AppScreen.Dashboard
                    onClose()
                }

                // 2. Affiliates & Crew
                DrawerNavItem(
                    label = "Affiliates & Crew",
                    icon = Icons.Default.People,
                    active = currentTab == AppScreen.Site && activeSiteTab == "Party",
                    darkTheme = dark
                ) {
                    viewModel.currentScreen = AppScreen.Site
                    viewModel.activeSiteTab = "Party"
                    onClose()
                }

                // 3. Chronicle Ledger
                DrawerNavItem(
                    label = "Chronicle Ledger",
                    icon = Icons.Default.AccountBalanceWallet,
                    active = currentTab == AppScreen.Money,
                    darkTheme = dark
                ) {
                    viewModel.currentScreen = AppScreen.Money
                    onClose()
                }

                // 4. Velocity Tasks
                DrawerNavItem(
                    label = "Velocity Tasks",
                    icon = Icons.Default.TaskAlt,
                    active = currentTab == AppScreen.Tasks,
                    darkTheme = dark
                ) {
                    viewModel.currentScreen = AppScreen.Tasks
                    onClose()
                }

                // 5. Staff Attendance
                DrawerNavItem(
                    label = "Staff Attendance",
                    icon = Icons.Default.CheckCircle,
                    active = currentTab == AppScreen.Site && activeSiteTab == "Attendance",
                    darkTheme = dark
                ) {
                    viewModel.currentScreen = AppScreen.Site
                    viewModel.activeSiteTab = "Attendance"
                    onClose()
                }

                // 6. Site Control Hub
                DrawerNavItem(
                    label = "Site Control Hub",
                    icon = Icons.Default.Settings,
                    active = currentTab == AppScreen.More,
                    darkTheme = dark
                ) {
                    viewModel.currentScreen = AppScreen.More
                    onClose()
                }
            }
        }

        // Bottom Controls Section
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            HorizontalDivider(color = if (dark) GlassBorderDark else Color(0x1F0284C7))

            // Dark Mode Switch
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (dark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                        contentDescription = "Theme status",
                        tint = if (dark) NeonCyan else Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Dark Mode",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (dark) Color.White else Color(0xFF0F172A)
                    )
                }
                Switch(
                    checked = dark,
                    onCheckedChange = { viewModel.darkThemeEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
            }

            // Online Cloud Link Switch
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Cloud,
                        contentDescription = "Cloud alignment",
                        tint = if (dark) NeonCyan else Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Online Cloud Link",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (dark) Color.White else Color(0xFF0F172A)
                    )
                }
                Switch(
                    checked = viewModel.onlineCloudLinkEnabled,
                    onCheckedChange = { viewModel.onlineCloudLinkEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Text
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "ConstructoPro • Secure Ledger Core v3.0",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (dark) Color.White.copy(alpha = 0.4f) else Color(0xFF64748B)
                )
                Text(
                    text = "Authenticated - Affiliate Position Active",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = if (dark) Color(0xFF2DD4BF) else Color(0xFF0F766E),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DrawerNavItem(
    label: String,
    icon: ImageVector,
    active: Boolean,
    darkTheme: Boolean,
    onClick: () -> Unit
) {
    val bg = if (active) {
        if (darkTheme) Color(0x332DD4BF) else Color(0x1A0284C7)
    } else Color.Transparent

    val tc = if (active) {
        if (darkTheme) Color(0xFF2DD4BF) else Color(0xFF0284C7)
    } else {
        if (darkTheme) Color.White.copy(alpha = 0.8f) else Color(0xFF334155)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(
                1.dp,
                if (active) (if (darkTheme) Color(0x4D2DD4BF) else Color(0x4D0284C7)) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tc,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            color = tc,
            fontSize = 15.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}
