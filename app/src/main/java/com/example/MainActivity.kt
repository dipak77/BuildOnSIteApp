package com.example

import android.os.Bundle
import android.content.Intent
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
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
import com.example.ui.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ConstructionRepository(database.constructionDao())
        MainViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("constructpro_prefs", MODE_PRIVATE)
        val enabled = prefs.getBoolean("drive_auto_backup_enabled", false)
        if (enabled) {
            AutoBackupWorker.schedule(applicationContext)
        }
        enableEdgeToEdge()
        setContent {
            val dark = viewModel.darkThemeEnabled
            LaunchedEffect(Unit) {
                viewModel.loadUserSessionFromPrefs(applicationContext)
            }
            LaunchedEffect(key1 = true) {
                viewModel.uiEvents.collectLatest { event ->
                    when (event) {
                        is UiEvent.ShowToast -> {
                            android.widget.Toast.makeText(applicationContext, event.message, if (event.long) android.widget.Toast.LENGTH_LONG else android.widget.Toast.LENGTH_SHORT).show()
                        }
                        is UiEvent.ShowError -> {
                            android.widget.Toast.makeText(applicationContext, "Error: ${event.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                        is UiEvent.ShareFile -> {
                            try {
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    applicationContext,
                                    "${packageName}.fileprovider",
                                    event.file
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = event.mimeType
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_TITLE, event.title)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                startActivity(Intent.createChooser(intent, event.title))
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(applicationContext, "Failed to share file: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                        UiEvent.ImportSuccess -> {
                            android.widget.Toast.makeText(applicationContext, "Import completed successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        UiEvent.ExportSuccess -> {
                            android.widget.Toast.makeText(applicationContext, "Export completed successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            MyApplicationTheme(darkTheme = dark) {
                GlassAtmosphereBox(darkTheme = dark) {
                    val userSession by viewModel.userSession.collectAsState()

                    // PIN verification state — resets on every cold app open (not persisted)
                    var pinVerified by remember { mutableStateOf(false) }

                    when {
                        userSession == null -> {
                            // Step 1: not signed in → Google login
                            GoogleLoginScreen(viewModel = viewModel)
                        }
                        !pinVerified -> {
                            // Step 2: signed in but PIN not yet verified this session
                            PinScreen(
                                dark = dark,
                                userName = userSession?.displayName ?: "User",
                                onPinVerified = { pinVerified = true }
                            )
                        }
                        else -> {
                            // Step 3: fully authenticated → app
                            ScaffoldFrame(viewModel = viewModel)
                        }
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
    var txType by remember { mutableStateOf("Money Out") }

    val currentProject by viewModel.activeProject.collectAsState()
    val allWorkers by viewModel.workers.collectAsState()

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

                    if (currentTab != AppScreen.Site) {
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
        }

        if (currentTab != AppScreen.Site) {
            FloatingActionButton(
                onClick = {
                    when (currentTab) {
                        AppScreen.Dashboard -> showQuickDialog = true
                        AppScreen.Money -> showTransactionDialog = true
                        AppScreen.Tasks -> showTaskDialog = true
                        AppScreen.Site -> {
                            when (viewModel.activeSiteTab) {
                                "Party" -> showWorkerDialog = true
                                "Transaction" -> showTransactionDialog = true
                                "Task" -> showTaskDialog = true
                                "Attendance" -> showWorkerDialog = true
                                "Site" -> showProjectDialog = true
                                else -> showWorkerDialog = true
                            }
                        }
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

    }

    QuickAddDialog(
        visible = showQuickDialog,
        onDismiss = { showQuickDialog = false },
        darkTheme = dark,
        onAddTransaction = { showTransactionDialog = true },
        onAddTask = { showTaskDialog = true },
        onAddWorker = { showWorkerDialog = true }
    )

    ProjectFormDialog(
        visible = showProjectDialog,
        onDismiss = { showProjectDialog = false },
        darkTheme = dark,
        onSave = { name, location, budget ->
            viewModel.addProject(name, location, budget)
        }
    )

    TransactionFormDialog(
        visible = showTransactionDialog,
        onDismiss = {
            showTransactionDialog = false
            viewModel.transactionToEdit = null
        },
        darkTheme = dark,
        presetType = txType,
        allWorkers = allWorkers,
        viewModel = viewModel,
        transactionToEdit = viewModel.transactionToEdit,
        onSave = { type, amount, category, description, party, reference, paymentMethod, date ->
            val proj = currentProject
            if (proj != null) {
                val toEdit = viewModel.transactionToEdit
                if (toEdit != null) {
                    viewModel.updateTransaction(
                        toEdit.copy(
                            type = type,
                            amount = amount,
                            category = category,
                            description = description,
                            date = date,
                            partyId = party?.id,
                            partyName = party?.name,
                            reference = reference,
                            paymentMethod = paymentMethod
                        )
                    )
                } else {
                    viewModel.addTransaction(
                        projectId = proj.id,
                        type = type,
                        amount = amount,
                        category = category,
                        description = description,
                        date = date,
                        partyId = party?.id,
                        partyName = party?.name,
                        reference = reference,
                        paymentMethod = paymentMethod
                    )
                }
            }
            viewModel.transactionToEdit = null
        }
    )

    TaskFormDialog(
        visible = showTaskDialog,
        onDismiss = { showTaskDialog = false },
        darkTheme = dark,
        onSave = { title, priority, assignee, dueDate ->
            val proj = currentProject
            if (proj != null) {
                viewModel.addTask(proj.id, title, priority, assignee, dueDate)
            }
        }
    )

    WorkerFormDialog(
        visible = showWorkerDialog,
        onDismiss = { showWorkerDialog = false },
        darkTheme = dark,
        workerCount = allWorkers.size,
        onSave = { name, role, shift, wageRate, phone, email, partyType, address, partyId, dateOfJoining, aadhaar, pan, reference ->
            viewModel.addWorker(
                name = name,
                role = role,
                shift = shift,
                wageRate = wageRate,
                color = listOf(0xFF3B82F6.toInt(), 0xFFEC4899.toInt(), 0xFF10B981.toInt(), 0xFFF59E0B.toInt(), 0xFF8B5CF6.toInt()).random(),
                phone = phone,
                email = email,
                partyType = partyType,
                address = address,
                partyId = partyId,
                dateOfJoining = dateOfJoining,
                aadhaar = aadhaar,
                pan = pan,
                reference = reference
            )
        }
    )
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
    val resolvedColor = if (!darkTheme) {
        when (selectedColor) {
            NeonCyan -> Color(0xFF0284C7)
            NeonPurple -> Color(0xFF6D28D9)
            NeonGreen -> Color(0xFF047857)
            NeonPink -> Color(0xFFBE123C)
            else -> selectedColor
        }
    } else selectedColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) resolvedColor.copy(alpha = 0.20f) else Color.Transparent)
            .border(1.dp, if (selected) resolvedColor else if (darkTheme) GlassBorderLight.copy(alpha = 0.20f) else GlassBorderLight, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) resolvedColor else if (darkTheme) TextSecondary else TextSecondaryLight,
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

    val projectName = currentProject?.name ?: ""
    val userEmail = userSession?.email ?: ""
    val userName = userSession?.displayName ?: "Guest"

    // Compute initials dynamically from user name as fallback for profile picture placeholder
    val userInitials = userName.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .take(2)
        .ifEmpty { "U" }

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
                // Circular Profile Photo
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
                    val photo = userSession?.photoUrl
                    if (!photo.isNullOrEmpty()) {
                        AsyncImage(
                            model = photo,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = userInitials,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                // Workspace & Email Names
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = userName,
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
                    text = "ConstructPro Secure Ledger Core v3.0",
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
