package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import com.example.data.*
import com.example.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.text.NumberFormat
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM DESIGN TOKENS
// ─────────────────────────────────────────────────────────────────────────────

private val PremiumDark        = DarkBg0
private val PremiumCard        = DarkBg2
private val PremiumCardAlt     = DarkBg3
private val PremiumBorder      = GlassBorderDark
private val PremiumBorderLight = GlassBorderLight

private val AccentCyan         = NeonCyan
private val AccentPurple       = NeonPurple
private val AccentGreen        = NeonGreen
private val AccentAmber        = NeonAmber
private val AccentPink         = NeonPink
private val AccentBlue         = NeonBlue
private val AccentOrange       = NeonOrange

private val GradientCyan = Brush.linearGradient(
    listOf(NeonCyan, Color(0xFF0077FF))
)
private val GradientPurple = Brush.linearGradient(
    listOf(NeonPurple, Color(0xFFDB2777))
)
private val GradientGreen = Brush.linearGradient(
    listOf(NeonGreen, Color(0xFF00C4FF))
)
private val GradientAmber = Brush.linearGradient(
    listOf(NeonAmber, NeonOrange)
)
private val GradientPink = Brush.linearGradient(
    listOf(NeonPink, NeonPurple)
)

// ─────────────────────────────────────────────────────────────────────────────
// PRO-LEVEL COLOR MANAGEMENT HELPERS
// ─────────────────────────────────────────────────────────────────────────────

private fun getPremiumAccent(color: Color, dark: Boolean): Color {
    if (dark) return color
    return when (color) {
        AccentCyan -> LightCyan
        AccentGreen -> LightGreen
        AccentPurple -> LightPurple
        AccentAmber -> LightAmber
        AccentPink -> LightPink
        AccentBlue -> LightBlue
        AccentOrange -> LightOrange
        else -> color
    }
}

private fun getPremiumGradient(gradient: Brush, dark: Boolean): Brush {
    if (dark) return gradient
    return when (gradient) {
        GradientCyan -> Brush.linearGradient(listOf(Color(0xFF0EA5E9), LightCyan))
        GradientPurple -> Brush.linearGradient(listOf(LightPurple, Color(0xFF6D28D9)))
        GradientGreen -> Brush.linearGradient(listOf(LightGreen, Color(0xFF047857)))
        GradientAmber -> Brush.linearGradient(listOf(LightAmber, LightOrange))
        GradientPink -> Brush.linearGradient(listOf(LightPink, LightPurple))
        else -> gradient
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MAIN SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MoreScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val dark = viewModel.darkThemeEnabled
    val allProjects by viewModel.projects.collectAsState()
    val allWorkers by viewModel.workers.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val allMOMs by viewModel.moms.collectAsState()
    val allPayroll by viewModel.payroll.collectAsState()
    val allEstimates by viewModel.estimates.collectAsState()
    val currentProject by viewModel.activeProject.collectAsState()
    val userSession by viewModel.userSession.collectAsState()
    val context = LocalContext.current
    val cFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    var activeSubModal by remember { mutableStateOf<String?>(null) }

    // Input States
    var inputEstName by remember { mutableStateOf("") }
    var inputEstQty by remember { mutableStateOf("") }
    var inputEstRate by remember { mutableStateOf("") }
    var inputMOMTitle by remember { mutableStateOf("") }
    var inputMOMContent by remember { mutableStateOf("") }
    var selectedWorkerForPayroll by remember { mutableStateOf<Worker?>(null) }
    var inputPayrollAmount by remember { mutableStateOf("") }
    var googleDriveSyncing by remember { mutableStateOf(false) }
    var driveSyncSuccess by remember { mutableStateOf(false) }
    var showProjectModal by remember { mutableStateOf(false) }
    var editingProject by remember { mutableStateOf<Project?>(null) }
    var projName by remember { mutableStateOf("") }
    var projLocation by remember { mutableStateOf("") }
    var projBudget by remember { mutableStateOf("") }
    var projStatus by remember { mutableStateOf("Active") }
    var projBg by remember { mutableStateOf("") }
    var showDeleteProjectConfirmForObj by remember { mutableStateOf<Project?>(null) }

    val projectImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val jsonString = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() }
                if (jsonString != null) {
                    viewModel.importProjectBackup(context, jsonString)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read backup file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val projectImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val dir = java.io.File(context.filesDir, "project_images")
                if (!dir.exists()) dir.mkdirs()
                val file = java.io.File(dir, "proj_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
                projBg = file.absolutePath
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to copy image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val systemRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val jsonString = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() }
                if (jsonString != null) {
                    viewModel.importFullBackup(context, jsonString)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read backup file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var showingPartyForm by remember { mutableStateOf(false) }
    var editingWorker by remember { mutableStateOf<Worker?>(null) }
    var pName by remember { mutableStateOf("") }
    var pRole by remember { mutableStateOf("") }
    var pShift by remember { mutableStateOf("Day") }
    var pWage by remember { mutableStateOf("") }
    var pPhone by remember { mutableStateOf("") }
    var pEmail by remember { mutableStateOf("") }
    var pPartyType by remember { mutableStateOf("Worker") }
    var pAddress by remember { mutableStateOf("") }
    var pPartyId by remember { mutableStateOf("") }
    var pDateOfJoining by remember { mutableStateOf("27/05/2026") }
    var pAadhaar by remember { mutableStateOf("") }
    var pPan by remember { mutableStateOf("") }
    var pReference by remember { mutableStateOf("") }

    val bgColor = if (dark) PremiumDark else Color(0xFFF5F8FF)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Subtle radial background glow
        if (dark) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0D2137).copy(alpha = 0.8f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.1f),
                        radius = size.width * 0.7f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A0A2E).copy(alpha = 0.6f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.1f, size.height * 0.3f),
                        radius = size.width * 0.5f
                    )
                )
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Soft gradient pastel orbs for a premium glassmorphic atmosphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE0F2FE).copy(alpha = 0.85f), // Sky blue flare
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.95f, size.height * 0.05f),
                        radius = size.width * 0.75f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF5F3FF).copy(alpha = 0.9f), // Lavender flare
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.05f, size.height * 0.25f),
                        radius = size.width * 0.6f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFCE7F3).copy(alpha = 0.72f), // Warm rose pink flare
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.65f),
                        radius = size.width * 0.7f
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── HEADER HERO SECTION ──────────────────────────────────────────
            item {
                PremiumHeaderHero(dark = dark)
            }

            // ── STATS ROW ────────────────────────────────────────────────────
            item {
                PremiumStatsRow(
                    projects = allProjects.size,
                    workers = allWorkers.size,
                    transactions = allTransactions.size,
                    dark = dark,
                    cFormatter = cFormatter,
                    totalBudget = allProjects.sumOf { it.budget }
                )
            }

            // ── SECTION LABEL: MODULES ────────────────────────────────────────
            item {
                PremiumSectionHeader(
                    label = "WORKSPACE MODULES",
                    subtitle = "Tap any module to manage your construction operations",
                    dark = dark
                )
            }

            // ── MODULE GRID (3 rows, premium cards) ──────────────────────────
            item {
                PremiumModuleGrid(
                    dark = dark,
                    onModuleClick = { activeSubModal = it }
                )
            }

            // ── SECTION LABEL: PREFERENCES ────────────────────────────────────
            item {
                PremiumSectionHeader(
                    label = "ACCOUNT & PREFERENCES",
                    subtitle = "Personalize your workspace experience",
                    dark = dark
                )
            }

            // ── GOOGLE ACCOUNT CARD ───────────────────────────────────────────
            item {
                val user = userSession
                if (user != null) {
                    PremiumAccountCard(
                        user = user,
                        dark = dark,
                        onSignOut = {
                            try {
                                val gso = GoogleSignInOptions
                                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(com.example.BuildConfig.GOOGLE_OAUTH_CLIENT_ID)
                                    .requestEmail().build()
                                GoogleSignIn.getClient(context, gso).signOut()
                            } catch (t: Throwable) { t.printStackTrace() }
                            viewModel.handleGoogleSignOut(context)
                            Toast.makeText(context, "Signed out of Workspace", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // ── THEME & BACKUP CARD ───────────────────────────────────────────
            item {
                PremiumThemeBackupCard(
                    dark = dark,
                    googleDriveSyncing = googleDriveSyncing,
                    driveSyncSuccess = driveSyncSuccess,
                    currentProject = currentProject,
                    onThemeToggle = { viewModel.darkThemeEnabled = it },
                    onSync = {
                        googleDriveSyncing = true
                        driveSyncSuccess = false
                        val proj = currentProject
                        if (proj != null) {
                            viewModel.exportTransactionsCSV(context)
                            viewModel.exportProjectBackup(context, proj)
                        } else viewModel.exportFullBackup(context)
                    },
                    onSyncComplete = {
                        googleDriveSyncing = false
                        driveSyncSuccess = true
                    },
                    onExportProject = { viewModel.exportProjectBackup(context, currentProject!!) },
                    onImportProject = { projectImportLauncher.launch("*/*") },
                    onBackupSystem = { viewModel.exportFullBackup(context) },
                    onRestoreSystem = { systemRestoreLauncher.launch("*/*") }
                )
            }

            // ── SECTION LABEL: PROJECTS ───────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumSectionHeader(
                        label = "CONSTRUCTION PROJECTS",
                        subtitle = "${allProjects.size} project${if (allProjects.size != 1) "s" else ""} registered",
                        dark = dark
                    )
                    PremiumActionButton(
                        label = "NEW",
                        icon = Icons.Default.Add,
                        gradient = GradientPurple,
                        onClick = {
                            editingProject = null
                            projName = ""; projLocation = ""; projBudget = ""; projStatus = "Active"
                            projBg = ""
                            showProjectModal = true
                        }
                    )
                }
            }

            // ── PROJECT CARDS ─────────────────────────────────────────────────
            items(allProjects) { proj ->
                PremiumProjectCard(
                    project = proj,
                    isActive = currentProject?.id == proj.id,
                    dark = dark,
                    onSelect = { viewModel.selectedProjectId = proj.id },
                    onEdit = {
                        editingProject = proj
                        projName = proj.name; projLocation = proj.location
                        projBudget = proj.budget.toString(); projStatus = proj.status
                        projBg = proj.customBackground ?: ""
                        showProjectModal = true
                    },
                    onDelete = { showDeleteProjectConfirmForObj = proj }
                )
            }

            // ── DEVELOPER SECTION ─────────────────────────────────────────────
            item {
                PremiumSectionHeader(
                    label = "DEVELOPER & SUPPORT",
                    subtitle = "DipTech Pune — Crafted with precision",
                    dark = dark
                )
            }

            item {
                PremiumDeveloperCard(
                    dark = dark,
                    onClick = { activeSubModal = "Developer" }
                )
            }
        }
    }

    // ── MODALS ─────────────────────────────────────────────────────────────────

    // Parties Modal
    var expandedWorkerId by remember { mutableStateOf<Int?>(null) }
    GlassModalDialog(
        visible = activeSubModal == "Parties",
        onDismiss = { activeSubModal = null; showingPartyForm = false; editingWorker = null },
        title = "Parties & Workers",
        darkTheme = dark,
        glowColor = AccentCyan
    ) {
        PremiumPartiesContent(
            dark = dark,
            showingPartyForm = showingPartyForm,
            editingWorker = editingWorker,
            allWorkers = allWorkers,
            expandedWorkerId = expandedWorkerId,
            pName = pName, pRole = pRole, pShift = pShift, pWage = pWage,
            pPhone = pPhone, pEmail = pEmail, pPartyType = pPartyType,
            pAddress = pAddress, pPartyId = pPartyId, pDateOfJoining = pDateOfJoining,
            pAadhaar = pAadhaar, pPan = pPan, pReference = pReference,
            onExpandedWorkerChange = { expandedWorkerId = it },
            onFormOpen = {
                editingWorker = null
                pName = ""; pRole = ""; pShift = "Day"; pWage = ""
                pPhone = ""; pEmail = ""; pPartyType = "Worker"
                pAddress = ""; pPartyId = "PID-${allWorkers.size + 1}"
                pDateOfJoining = "27/05/2026"; pAadhaar = ""; pPan = ""; pReference = ""
                showingPartyForm = true
            },
            onEditWorker = { w ->
                editingWorker = w; pName = w.name; pRole = w.role; pShift = w.shift
                pWage = w.wageRate.toString(); pPhone = w.phone; pEmail = w.email
                pPartyType = w.partyType; pAddress = w.address; pPartyId = w.partyId
                pDateOfJoining = w.dateOfJoining; pAadhaar = w.aadhaar
                pPan = w.pan; pReference = w.reference; showingPartyForm = true
            },
            onDeleteWorker = { viewModel.deleteWorker(it, context) },
            onNameChange = { pName = it }, onRoleChange = { pRole = it },
            onShiftChange = { pShift = it }, onWageChange = { pWage = it },
            onPhoneChange = { pPhone = it }, onEmailChange = { pEmail = it },
            onPartyTypeChange = { pPartyType = it }, onAddressChange = { pAddress = it },
            onPartyIdChange = { pPartyId = it }, onDateChange = { pDateOfJoining = it },
            onAadhaarChange = { pAadhaar = it }, onPanChange = { pPan = it },
            onReferenceChange = { pReference = it },
            onCancel = { showingPartyForm = false; editingWorker = null },
            onSave = {
                val rate = pWage.toDoubleOrNull() ?: 0.0
                if (pName.isNotBlank()) {
                    if (editingWorker == null) {
                        val colors = listOf(0xFF3B82F6.toInt(), 0xFFEC4899.toInt(),
                            0xFF10B981.toInt(), 0xFFF59E0B.toInt(), 0xFF8B5CF6.toInt())
                        viewModel.addWorker(pName, if (pRole.isNotBlank()) pRole else pPartyType,
                            pShift, rate, colors.random(), pPhone, pEmail, pPartyType,
                            pAddress, pPartyId, pDateOfJoining, pAadhaar, pPan, pReference)
                    } else {
                        viewModel.updateWorker(editingWorker!!.copy(
                            name = pName, role = if (pRole.isNotBlank()) pRole else pPartyType,
                            shift = pShift, wageRate = rate, phone = pPhone, email = pEmail,
                            partyType = pPartyType, address = pAddress, partyId = pPartyId,
                            dateOfJoining = pDateOfJoining, aadhaar = pAadhaar,
                            pan = pPan, reference = pReference))
                    }
                    showingPartyForm = false; editingWorker = null
                }
            }
        )
    }

    // Estimates Modal
    GlassModalDialog(
        visible = activeSubModal == "Estimates",
        onDismiss = { activeSubModal = null },
        title = "Estimates & Materials",
        darkTheme = dark,
        glowColor = AccentPurple
    ) {
        PremiumEstimatesContent(
            dark = dark,
            allEstimates = allEstimates,
            currentProject = currentProject,
            inputEstName = inputEstName,
            inputEstQty = inputEstQty,
            inputEstRate = inputEstRate,
            cFormatter = cFormatter,
            onNameChange = { inputEstName = it },
            onQtyChange = { inputEstQty = it },
            onRateChange = { inputEstRate = it },
            onAdd = {
                val qty = inputEstQty.toDoubleOrNull() ?: 1.0
                val rate = inputEstRate.toDoubleOrNull() ?: 1.0
                if (inputEstName.isNotBlank() && currentProject != null) {
                    viewModel.addEstimate(currentProject!!.id, inputEstName, qty, "Bag", rate)
                    inputEstName = ""; inputEstQty = ""; inputEstRate = ""
                }
            }
        )
    }

    // Minutes Modal
    GlassModalDialog(
        visible = activeSubModal == "Minutes",
        onDismiss = { activeSubModal = null },
        title = "Meeting Minutes (MOM)",
        darkTheme = dark,
        glowColor = AccentPink
    ) {
        PremiumMOMContent(
            dark = dark,
            allMOMs = allMOMs,
            currentProject = currentProject,
            inputTitle = inputMOMTitle,
            inputContent = inputMOMContent,
            onTitleChange = { inputMOMTitle = it },
            onContentChange = { inputMOMContent = it },
            onAdd = {
                if (inputMOMTitle.isNotBlank() && currentProject != null) {
                    viewModel.addMOM(currentProject!!.id, inputMOMTitle, inputMOMContent, "2026-05-26")
                    inputMOMTitle = ""; inputMOMContent = ""
                }
            }
        )
    }

    // Payroll Modal
    GlassModalDialog(
        visible = activeSubModal == "Payroll",
        onDismiss = { activeSubModal = null },
        title = "Wages Disbursements Ledger",
        darkTheme = dark,
        glowColor = AccentGreen
    ) {
        PremiumPayrollContent(
            dark = dark,
            allPayroll = allPayroll,
            allWorkers = allWorkers,
            currentProject = currentProject,
            selectedWorker = selectedWorkerForPayroll,
            inputAmount = inputPayrollAmount,
            cFormatter = cFormatter,
            onWorkerSelect = { selectedWorkerForPayroll = if (allWorkers.isNotEmpty()) allWorkers.random() else null },
            onAmountChange = { inputPayrollAmount = it },
            onProcess = {
                val amt = inputPayrollAmount.toDoubleOrNull() ?: 100.0
                if (selectedWorkerForPayroll != null && currentProject != null) {
                    viewModel.addPayroll(selectedWorkerForPayroll!!.id, currentProject!!.id, "2026-05-26", amt, "Paid")
                    inputPayrollAmount = ""; selectedWorkerForPayroll = null
                }
            }
        )
    }

    // Reports Modal
    GlassModalDialog(
        visible = activeSubModal == "Reports",
        onDismiss = { activeSubModal = null },
        title = "Financial Audit Reports",
        darkTheme = dark,
        glowColor = AccentAmber
    ) {
        PremiumReportsContent(
            dark = dark,
            allTransactions = allTransactions,
            currentProject = currentProject,
            cFormatter = cFormatter
        )
    }

    // Developer Modal
    var userMessageText by remember { mutableStateOf("") }
    GlassModalDialog(
        visible = activeSubModal == "Developer",
        onDismiss = { activeSubModal = null },
        title = "Developer & Support",
        darkTheme = dark,
        glowColor = AccentPurple
    ) {
        PremiumDeveloperContent(
            dark = dark,
            userMessageText = userMessageText,
            onMessageChange = { userMessageText = it },
            onSendFeedback = {
                if (userMessageText.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("haranedipak@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Build On Site App - User Feedback")
                        putExtra(Intent.EXTRA_TEXT, "Hello Dipak,\n\nFeedback:\n\n$userMessageText\n\nSent from Build On Site App")
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, "Send Feedback"))
                        userMessageText = ""
                    } catch (ex: Exception) {
                        Toast.makeText(context, "No email client found.", Toast.LENGTH_SHORT).show()
                    }
                } else Toast.makeText(context, "Please write feedback first.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Project Modal
    if (showProjectModal) {
        GlassModalDialog(
            visible = true,
            onDismiss = { showProjectModal = false; editingProject = null },
            title = if (editingProject == null) "Create New Project" else "Edit Project",
            darkTheme = dark,
            glowColor = AccentPurple
        ) {
            PremiumProjectFormContent(
                dark = dark,
                projName = projName, projLocation = projLocation,
                projBudget = projBudget, projStatus = projStatus,
                projBg = projBg,
                editingProject = editingProject,
                onNameChange = { projName = it },
                onLocationChange = { projLocation = it },
                onBudgetChange = { projBudget = it },
                onStatusChange = { projStatus = it },
                onBgChange = { projBg = it },
                onUploadImageClick = { projectImagePickerLauncher.launch("image/*") },
                onCancel = { showProjectModal = false; editingProject = null },
                onSave = {
                    val bud = projBudget.toDoubleOrNull() ?: 0.0
                    if (projName.isNotBlank() && projLocation.isNotBlank()) {
                        if (editingProject == null) viewModel.addProject(projName, projLocation, bud, projBg)
                        else viewModel.updateProject(editingProject!!.copy(
                            name = projName, location = projLocation,
                            budget = bud, status = projStatus, customBackground = projBg))
                        showProjectModal = false; editingProject = null
                    }
                }
            )
        }
    }

    // Delete Confirm Modal
    if (showDeleteProjectConfirmForObj != null) {
        val projToDelete = showDeleteProjectConfirmForObj!!
        GlassModalDialog(
            visible = true,
            onDismiss = { showDeleteProjectConfirmForObj = null },
            title = "⚠ Confirm Deletion",
            darkTheme = dark,
            glowColor = AccentPink
        ) {
            PremiumDeleteConfirmContent(
                dark = dark,
                projectName = projToDelete.name,
                onCancel = { showDeleteProjectConfirmForObj = null },
                onConfirm = {
                    viewModel.deleteProject(projToDelete, context)
                    showDeleteProjectConfirmForObj = null
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM HEADER HERO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PremiumHeaderHero(dark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "heroGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    val cyanRes = getPremiumAccent(AccentCyan, dark)
    val purpleRes = getPremiumAccent(AccentPurple, dark)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (dark) 0.dp else 4.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (dark)
                    Brush.linearGradient(
                        listOf(Color(0xFF0D1F3C), Color(0xFF0A1628), Color(0xFF0F1E38))
                    )
                else
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color(0xFFEFF6FF).copy(alpha = 0.95f),
                            Color(0xFFF5F3FF).copy(alpha = 0.85f)
                        )
                    )
            )
            .drawWithContent {
                drawContent()
                val borderBrush = Brush.linearGradient(
                    listOf(
                        cyanRes.copy(alpha = glowAlpha),
                        purpleRes.copy(alpha = glowAlpha * 0.7f),
                        cyanRes.copy(alpha = glowAlpha * 0.4f)
                     )
                )
                drawRoundRect(
                    brush = borderBrush,
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(cyanRes.copy(alpha = 0.12f))
                        .border(1.dp, cyanRes.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = cyanRes,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "CONTROL CENTER",
                        color = cyanRes,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
                Column {
                    Text(
                        "Admin",
                        color = if (dark) Color.White else Color(0xFF0F172A),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 32.sp,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        "Workspace",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 34.sp,
                        letterSpacing = (-0.5).sp,
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                            )
                        )
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Unified construction management platform",
                    color = if (dark) Color(0xFF94A3B8) else Color(0xFF475569),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
            
            // Icon cluster with Glassmorphic orb and Apps 2x2 grid icon
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(
                        if (dark) Color(0xFF0F1E38).copy(alpha = 0.7f)
                        else Color.White.copy(alpha = 0.85f)
                    )
                    .shadow(
                        elevation = if (dark) 0.dp else 4.dp,
                        shape = CircleShape
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(cyanRes.copy(alpha = 0.8f), purpleRes.copy(alpha = 0.4f))
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = cyanRes,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM STATS ROW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PremiumStatsRow(
    projects: Int, workers: Int, transactions: Int,
    dark: Boolean, cFormatter: NumberFormat, totalBudget: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PremiumStatChip(
            value = "$projects",
            label = "Projects",
            gradient = GradientCyan,
            icon = Icons.Default.Business,
            accentColor = AccentCyan,
            dark = dark,
            modifier = Modifier.weight(1f)
        )
        PremiumStatChip(
            value = "$workers",
            label = "Parties",
            gradient = GradientPurple,
            icon = Icons.Default.Groups,
            accentColor = AccentPurple,
            dark = dark,
            modifier = Modifier.weight(1f)
        )
        PremiumStatChip(
            value = formatIndianRupeesShort(totalBudget),
            label = "Budget",
            gradient = GradientGreen,
            icon = Icons.Default.Payments,
            accentColor = AccentGreen,
            dark = dark,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
private fun PremiumStatChip(
    value: String,
    label: String,
    gradient: Brush,
    icon: ImageVector,
    accentColor: Color,
    dark: Boolean,
    modifier: Modifier = Modifier
) {
    val resolvedAccent = getPremiumAccent(accentColor, dark)

    Box(
        modifier = modifier
            .shadow(
                elevation = if (dark) 0.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (dark) Color(0xFF0D1526)
                else Color.White
            )
            .border(
                1.3.dp,
                if (dark) Brush.linearGradient(listOf(PremiumBorder.copy(alpha = 0.5f), PremiumBorder.copy(alpha = 0.2f)))
                else Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color(0xFFCBD5E1).copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.9f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Circular icon indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(resolvedAccent.copy(alpha = 0.08f))
                        .border(1.dp, resolvedAccent.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = resolvedAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = value,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.ui.text.TextStyle(brush = getPremiumGradient(gradient, dark)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = if (dark) Color(0xFF94A3B8) else Color(0xFF475569),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Flowing Sparkline Graph
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                // Pre-defined relative coords for variations in wave shapes
                val points = when (label) {
                    "Projects" -> listOf(0.0f to 0.7f, 0.2f to 0.5f, 0.4f to 0.65f, 0.6f to 0.4f, 0.8f to 0.6f, 1.0f to 0.2f)
                    "Parties" -> listOf(0.0f to 0.65f, 0.2f to 0.62f, 0.4f to 0.45f, 0.6f to 0.55f, 0.8f to 0.35f, 1.0f to 0.48f)
                    else -> listOf(0.0f to 0.62f, 0.2f to 0.55f, 0.4f to 0.72f, 0.6f to 0.38f, 0.8f to 0.48f, 1.0f to 0.35f)
                }

                val path = Path()
                if (points.isNotEmpty()) {
                    val p0 = points[0]
                    path.moveTo(p0.first * size.width, p0.second * size.height)

                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val cx = (prev.first + curr.first) / 2f
                        path.quadraticTo(
                            prev.first * size.width, prev.second * size.height,
                            cx * size.width, ((prev.second + curr.second) / 2f) * size.height
                        )
                    }
                    val last = points.last()
                    path.lineTo(last.first * size.width, last.second * size.height)
                }

                // 1. Draw smooth gradient area under curve
                val filledPath = Path().apply {
                    addPath(path)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    path = filledPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            resolvedAccent.copy(alpha = if (dark) 0.18f else 0.12f),
                            Color.Transparent
                        )
                    )
                )

                // 2. Draw outline path with anti-aliasing
                drawPath(
                    path = path,
                    color = resolvedAccent,
                    style = Stroke(
                        width = 1.8.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM SECTION HEADER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PremiumSectionHeader(label: String, subtitle: String, dark: Boolean) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(getPremiumGradient(GradientCyan, dark))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                color = if (dark) AccentCyan else Color(0xFF0369A1),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
        }
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                color = if (dark) Color(0xFF94A3B8) else Color(0xFF475569),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 11.dp, top = 2.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM MODULE GRID
// ─────────────────────────────────────────────────────────────────────────────

private data class ModuleItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradient: Brush,
    val accentColor: Color,
    val badge: String? = null
)

@Composable
private fun PremiumModuleGrid(dark: Boolean, onModuleClick: (String) -> Unit) {
    val modules = listOf(
        ModuleItem("Parties", "Parties", "Workers & Vendors", Icons.Default.Groups, GradientCyan, AccentCyan, "TEAM"),
        ModuleItem("Estimates", "Estimates", "Bills & Materials", Icons.Default.Construction, GradientPurple, AccentPurple, "BOQ"),
        ModuleItem("Payroll", "Payroll", "Wage Disbursements", Icons.Default.Receipt, GradientGreen, AccentGreen, "PAY"),
        ModuleItem("Reports", "Reports", "Cost Audit & Charts", Icons.Default.Analytics, GradientAmber, AccentAmber, "AUDIT"),
        ModuleItem("Minutes", "Minutes", "Site Meeting Logs", Icons.Default.FilePresent, GradientPink, AccentPink, "MOM"),
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // First row: 2 cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            modules.take(2).forEach { module ->
                PremiumModuleCard(
                    module = module,
                    dark = dark,
                    modifier = Modifier.weight(1f),
                    onClick = { onModuleClick(module.id) }
                )
            }
        }
        // Second row: 2 cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            modules.drop(2).take(2).forEach { module ->
                PremiumModuleCard(
                    module = module,
                    dark = dark,
                    modifier = Modifier.weight(1f),
                    onClick = { onModuleClick(module.id) }
                )
            }
        }
        // Third row: 1 wide card
        modules.drop(4).forEach { module ->
            PremiumModuleCard(
                module = module,
                dark = dark,
                modifier = Modifier.fillMaxWidth(),
                fullWidth = true,
                onClick = { onModuleClick(module.id) }
            )
        }
    }
}

@Composable
private fun PremiumModuleCard(
    module: ModuleItem,
    dark: Boolean,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = false,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "border"
    )

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )

    val resolvedAccent = getPremiumAccent(module.accentColor, dark)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (dark) 0.dp else 4.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (dark)
                    Brush.linearGradient(listOf(Color(0xFF0D1526), Color(0xFF111D35)))
                else
                    Brush.linearGradient(
                        listOf(
                            Color.White,
                            Color(0xFFFBFDFF)
                        )
                    )
            )
            .border(
                1.3.dp,
                Brush.linearGradient(
                    colors = listOf(
                        resolvedAccent.copy(alpha = borderAlpha),
                        resolvedAccent.copy(alpha = borderAlpha * 0.2f),
                        if (dark) Color.White.copy(alpha = 0.05f) else Color(0xFFCBD5E1).copy(alpha = 0.35f),
                        resolvedAccent.copy(alpha = borderAlpha * 0.5f)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .clickable {
                pressed = true
                onClick()
            }
    ) {
        // Soft background glow radiating from top-left
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (fullWidth) 90.dp else 145.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            resolvedAccent.copy(alpha = 0.09f),
                            Color.Transparent
                        ),
                        radius = 280f
                    )
                )
        )

        if (fullWidth) {
            // Wide layout (Minutes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumModuleIcon(module = module, dark = dark)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = module.title,
                            color = if (dark) Color.White else Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = module.subtitle,
                            color = if (dark) Color(0xFF94A3B8) else Color(0xFF475569),
                            fontSize = 11.sp
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    module.badge?.let {
                        PremiumBadge(text = it, color = resolvedAccent)
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(resolvedAccent.copy(alpha = 0.08f))
                            .border(1.dp, resolvedAccent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = resolvedAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        } else {
            // Square layout matching reference exactly
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(115.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    PremiumModuleIcon(module = module, dark = dark)
                    module.badge?.let { PremiumBadge(text = it, color = resolvedAccent) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = module.title,
                            color = if (dark) Color.White else Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = module.subtitle,
                            color = if (dark) Color(0xFF94A3B8) else Color(0xFF475569),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(resolvedAccent.copy(alpha = 0.08f))
                            .border(1.dp, resolvedAccent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = resolvedAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(150)
            pressed = false
        }
    }
}

@Composable
private fun PremiumModuleIcon(module: ModuleItem, dark: Boolean) {
    val resolvedAccent = getPremiumAccent(module.accentColor, dark)
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(resolvedAccent.copy(alpha = 0.12f))
            .border(1.dp, resolvedAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = module.icon,
            contentDescription = null,
            tint = resolvedAccent,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun PremiumBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, color = color, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM ACCOUNT CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PremiumAccountCard(
    user: GoogleUser, dark: Boolean, onSignOut: () -> Unit
) {
    val purpleRes = getPremiumAccent(AccentPurple, dark)
    val cyanRes = getPremiumAccent(AccentCyan, dark)
    val greenRes = getPremiumAccent(AccentGreen, dark)
    val pinkRes = getPremiumAccent(AccentPink, dark)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (dark) 0.dp else 4.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (dark)
                    Brush.linearGradient(listOf(Color(0xFF0D1A2E), Color(0xFF1A0A2E)))
                else
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color(0xFFF5F3FF).copy(alpha = 0.95f),
                            Color(0xFFEDE9FE).copy(alpha = 0.85f)
                        )
                    )
            )
            .border(
                1.2.dp,
                Brush.linearGradient(
                    listOf(
                        purpleRes.copy(alpha = if (dark) 0.5f else 0.4f),
                        cyanRes.copy(alpha = if (dark) 0.3f else 0.25f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(purpleRes, cyanRes))
                        )
                        .border(2.dp, cyanRes.copy(0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.displayName.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.displayName,
                            color = if (dark) Color.White else Color(0xFF1E1B4B),
                            fontWeight = FontWeight.Bold, fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        // Online dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(greenRes, CircleShape)
                        )
                    }
                    Text(
                        text = user.email,
                        color = if (dark) Color(0xFF64748B) else Color(0xFF475569),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(greenRes.copy(0.12f))
                            .border(1.dp, greenRes.copy(0.35f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("WORKSPACE ACTIVE", color = greenRes,
                            fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
            }
            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Change PIN button
                val context = LocalContext.current
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF7C3AED).copy(0.12f))
                        .border(1.dp, Color(0xFF7C3AED).copy(0.35f), RoundedCornerShape(10.dp))
                        .clickable {
                            // Clear stored PIN so setup runs again on next app open
                            context.getSharedPreferences("constructpro_prefs", android.content.Context.MODE_PRIVATE)
                                .edit().remove("app_security_pin").apply()
                            android.widget.Toast.makeText(context,
                                "PIN cleared. You will set a new PIN next time you open the app.",
                                android.widget.Toast.LENGTH_LONG).show()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Lock, null, tint = Color(0xFF7C3AED), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("CHANGE PIN", color = Color(0xFF7C3AED), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                    }
                }
                // Sign out button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(pinkRes.copy(0.12f))
                        .border(1.dp, pinkRes.copy(0.35f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onSignOut)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Logout, null, tint = pinkRes, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("SIGN OUT", color = pinkRes, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM THEME & BACKUP CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PremiumThemeBackupCard(
    dark: Boolean,
    googleDriveSyncing: Boolean,
    driveSyncSuccess: Boolean,
    currentProject: Project?,
    onThemeToggle: (Boolean) -> Unit,
    onSync: () -> Unit,
    onSyncComplete: () -> Unit,
    onExportProject: () -> Unit,
    onImportProject: () -> Unit,
    onBackupSystem: () -> Unit,
    onRestoreSystem: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val cloudCyan = getPremiumAccent(AccentCyan, dark)
    val cloudGreen = getPremiumAccent(AccentGreen, dark)
    val cloudPurple = getPremiumAccent(AccentPurple, dark)
    val cloudAmber = getPremiumAccent(AccentAmber, dark)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (dark) 0.dp else 4.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .then(
                if (dark) Modifier.background(Color(0xFF0D1526))
                else Modifier.background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color(0xFFF8FAFF).copy(alpha = 0.95f)
                        )
                    )
                )
            )
            .then(
                if (dark) Modifier.border(1.2.dp, PremiumBorder, RoundedCornerShape(24.dp))
                else Modifier.border(
                    1.2.dp,
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color(0xFFCBD5E1).copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.9f)
                        )
                    ),
                    RoundedCornerShape(24.dp)
                )
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Theme Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (dark) AccentCyan.copy(0.12f)
                            else Color(0xFF0EA5E9).copy(0.1f)
                        )
                        .border(1.dp,
                            if (dark) AccentCyan.copy(0.3f) else Color(0xFF0EA5E9).copy(0.3f),
                            RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (dark) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = if (dark) AccentCyan else Color(0xFF0284C7),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        if (dark) "Dark Glass Neon" else "Light Professional",
                        color = if (dark) Color.White else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold, fontSize = 14.sp
                    )
                    Text(
                        if (dark) "Frosted glass with neon accents" else "Clean minimal light interface",
                        color = if (dark) Color(0xFF64748B) else Color(0xFF475569),
                        fontSize = 11.sp
                    )
                }
            }
            Switch(
                checked = dark,
                onCheckedChange = onThemeToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentCyan,
                    checkedTrackColor = AccentCyan.copy(0.25f),
                    uncheckedThumbColor = Color(0xFF94A3B8),
                    uncheckedTrackColor = Color(0xFFE2E8F0)
                )
            )
        }

        PremiumDivider(dark)

        // Cloud Sync Section
        PremiumSubSectionHeader("Cloud Sync & Backup", Icons.Default.CloudSync, cloudCyan, dark)

        // Drive info card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(cloudCyan.copy(0.05f))
                .border(1.dp, cloudCyan.copy(0.2f), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudQueue, null, tint = cloudCyan, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Google Drive Integration", color = if (dark) Color.White else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Text("/My Drive/ConstructPro_Backups/",
                    color = if (dark) Color(0xFF94A3B8) else Color(0xFF475569), fontSize = 10.sp)
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(cloudGreen.copy(0.08f))
                        .border(1.dp, cloudGreen.copy(0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("💡 ", fontSize = 11.sp)
                    Text(
                        "Tap SYNC → select \"Drive\" from the share sheet to securely export CSV + JSON to your Google Account.",
                        color = if (dark) cloudGreen.copy(0.9f) else Color(0xFF047857),
                        fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Auto Backup Switch Row
        var autoBackupEnabled by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            val prefs = context.getSharedPreferences("constructpro_prefs", android.content.Context.MODE_PRIVATE)
            autoBackupEnabled = prefs.getBoolean("drive_auto_backup_enabled", false)
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (dark) cloudCyan.copy(0.12f)
                            else Color(0xFF0EA5E9).copy(0.1f)
                        )
                        .border(1.dp,
                            if (dark) cloudCyan.copy(0.3f) else Color(0xFF0EA5E9).copy(0.3f),
                            RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = if (dark) cloudCyan else Color(0xFF0284C7),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "Midnight Auto Backup",
                        color = if (dark) Color.White else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold, fontSize = 14.sp
                    )
                    Text(
                        "Backup to Google Drive daily at 12:00 AM",
                        color = if (dark) Color(0xFF64748B) else Color(0xFF475569),
                        fontSize = 11.sp
                    )
                }
            }
            Switch(
                checked = autoBackupEnabled,
                onCheckedChange = { enabled ->
                    autoBackupEnabled = enabled
                    val prefs = context.getSharedPreferences("constructpro_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("drive_auto_backup_enabled", enabled).apply()
                    if (enabled) {
                        com.example.AutoBackupWorker.schedule(context)
                        Toast.makeText(context, "Midnight auto backup enabled!", Toast.LENGTH_SHORT).show()
                    } else {
                        androidx.work.WorkManager.getInstance(context).cancelUniqueWork("GoogleDriveAutoBackup")
                        Toast.makeText(context, "Auto backup disabled", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = cloudCyan,
                    checkedTrackColor = cloudCyan.copy(0.25f),
                    uncheckedThumbColor = Color(0xFF94A3B8),
                    uncheckedTrackColor = Color(0xFFE2E8F0)
                )
            )
        }

        // Sync Button
        if (googleDriveSyncing) {
            PremiumSyncingIndicator(dark = dark, onComplete = onSyncComplete)
        } else {
            PremiumGradientButton(
                label = "SYNC TO GOOGLE DRIVE (CSV + JSON)",
                icon = Icons.Default.Sync,
                gradient = getPremiumGradient(GradientCyan, dark),
                onClick = onSync,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (driveSyncSuccess) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(cloudGreen.copy(0.12f))
                    .border(1.dp, cloudGreen.copy(0.35f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = cloudGreen, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Sync complete — CSV & JSON written to /My Drive/ConstructPro_Backups/",
                    color = cloudGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold
                )
            }
        }

        PremiumDivider(dark)

        // Project-wise operations
        PremiumSubSectionHeader("Project Database", Icons.Default.FolderOpen, cloudPurple, dark)

        if (currentProject != null) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PremiumGradientButton(
                    label = "EXPORT",
                    icon = Icons.Default.Upload,
                    gradient = getPremiumGradient(GradientPurple, dark),
                    onClick = onExportProject,
                    modifier = Modifier.weight(1f)
                )
                PremiumOutlineButton(
                    label = "IMPORT",
                    icon = Icons.Default.Download,
                    color = cloudCyan,
                    onClick = onImportProject,
                    dark = dark,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(cloudAmber.copy(0.07f))
                    .border(1.dp, cloudAmber.copy(0.25f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = cloudAmber, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Select an active project below to enable project-level exports.",
                        color = if (dark) Color(0xFF94A3B8) else Color(0xFF475569),
                        fontSize = 11.sp)
                }
            }
        }

        PremiumDivider(dark)

        // Full system backup
        PremiumSubSectionHeader("Full System Backup", Icons.Default.Storage, cloudGreen, dark)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PremiumGradientButton(
                label = "BACKUP ALL",
                icon = Icons.Default.CloudUpload,
                gradient = getPremiumGradient(GradientGreen, dark),
                onClick = onBackupSystem,
                modifier = Modifier.weight(1f)
            )
            PremiumOutlineButton(
                label = "RESTORE",
                icon = Icons.Default.Restore,
                color = cloudAmber,
                onClick = onRestoreSystem,
                dark = dark,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM PROJECT CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PremiumProjectCard(
    project: Project,
    isActive: Boolean,
    dark: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val rawStatusColor = when (project.status) {
        "Active" -> AccentGreen
        "On Hold" -> AccentAmber
        else -> AccentCyan
    }
    val statusColor = getPremiumAccent(rawStatusColor, dark)

    val borderBrush = if (isActive)
        Brush.linearGradient(listOf(getPremiumAccent(AccentCyan, dark), getPremiumAccent(AccentPurple, dark)))
    else
        Brush.linearGradient(listOf(
            if (dark) PremiumBorder else Color.White.copy(alpha = 0.95f),
            if (dark) PremiumBorder else Color(0xFFCBD5E1).copy(alpha = 0.35f)
        ))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (dark) 0.dp else if (isActive) 5.dp else 2.dp,
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isActive)
                    Brush.linearGradient(
                        listOf(
                            if (dark) Color(0xFF0D1F3C) else Color(0xFFEFF6FF).copy(alpha = 0.95f),
                            if (dark) Color(0xFF111D35) else Color(0xFFF5F3FF).copy(alpha = 0.95f)
                        )
                    )
                else
                    Brush.linearGradient(
                        listOf(
                            if (dark) Color(0xFF0D1526) else Color.White.copy(alpha = 0.85f),
                            if (dark) Color(0xFF0D1526) else Color(0xFFF8FAFF).copy(alpha = 0.85f)
                        )
                    )
            )
            .border(
                if (isActive) 1.5.dp else 1.dp,
                borderBrush,
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onSelect)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                // Status indicator
                Column(
                    modifier = Modifier.padding(end = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    if (isActive) {
                        Spacer(Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(16.dp)
                                .background(statusColor.copy(0.3f))
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.name,
                            color = if (dark) Color.White else Color(0xFF0F172A),
                            fontSize = 15.sp, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isActive) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(getPremiumAccent(AccentCyan, dark).copy(0.12f))
                                    .border(1.dp, getPremiumAccent(AccentCyan, dark).copy(0.35f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ACTIVE", color = getPremiumAccent(AccentCyan, dark),
                                    fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null,
                            tint = if (dark) Color(0xFF475569) else Color(0xFF64748B),
                            modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(
                            project.location,
                            color = if (dark) Color(0xFF475569) else Color(0xFF64748B),
                            fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Budget: ${formatIndianRupeesWithLakhCr(project.budget)}",
                        color = statusColor,
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                    )
                }
            }
            // Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                PremiumIconAction(icon = Icons.Default.Edit, color = getPremiumAccent(AccentCyan, dark), onClick = onEdit)
                Spacer(Modifier.width(4.dp))
                PremiumIconAction(icon = Icons.Default.DeleteOutline, color = getPremiumAccent(AccentPink, dark), onClick = onDelete)
            }
        }
    }
}

@Composable
private fun PremiumIconAction(icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(0.1f))
            .border(1.dp, color.copy(0.25f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM DEVELOPER CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PremiumDeveloperCard(dark: Boolean, onClick: () -> Unit) {
    val purpleRes = getPremiumAccent(AccentPurple, dark)
    val pinkRes = getPremiumAccent(AccentPink, dark)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (dark)
                    Brush.linearGradient(listOf(Color(0xFF1A0A2E), Color(0xFF0D1526)))
                else
                    Brush.linearGradient(listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE)))
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(purpleRes.copy(0.6f), pinkRes.copy(0.4f))),
                RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(purpleRes, pinkRes))),
                    contentAlignment = Alignment.Center
                ) {
                    BuildOnSiteLogo(modifier = Modifier.size(40.dp), darkTheme = dark)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "DipTech Pune",
                        color = if (dark) Color.White else Color(0xFF1E1B4B),
                        fontWeight = FontWeight.Black, fontSize = 18.sp
                    )
                    Text(
                        "Lead Engineer: Dipak Harane",
                        color = purpleRes,
                        fontSize = 12.sp, fontWeight = FontWeight.Medium
                    )
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        PremiumBadge("v2.0 PRO", purpleRes)
                        Spacer(Modifier.width(6.dp))
                        PremiumBadge("PUNE", pinkRes)
                    }
                }
            }
            Text(
                "Meticulously crafted to empower site engineers, project managers & contractors with real-time financial audits, digital wage registers, and secure cloud backups.",
                color = if (dark) Color(0xFF64748B) else Color(0xFF475569),
                fontSize = 12.sp, lineHeight = 18.sp
            )
            PremiumGradientButton(
                label = "VIEW DETAILS & SEND FEEDBACK",
                icon = Icons.Default.Email,
                gradient = getPremiumGradient(GradientPurple, dark),
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM MODAL CONTENT SECTIONS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PremiumPartiesContent(
    dark: Boolean,
    showingPartyForm: Boolean,
    editingWorker: Worker?,
    allWorkers: List<Worker>,
    expandedWorkerId: Int?,
    pName: String, pRole: String, pShift: String, pWage: String,
    pPhone: String, pEmail: String, pPartyType: String, pAddress: String,
    pPartyId: String, pDateOfJoining: String, pAadhaar: String,
    pPan: String, pReference: String,
    onExpandedWorkerChange: (Int?) -> Unit,
    onFormOpen: () -> Unit,
    onEditWorker: (Worker) -> Unit,
    onDeleteWorker: (Worker) -> Unit,
    onNameChange: (String) -> Unit, onRoleChange: (String) -> Unit,
    onShiftChange: (String) -> Unit, onWageChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit, onEmailChange: (String) -> Unit,
    onPartyTypeChange: (String) -> Unit, onAddressChange: (String) -> Unit,
    onPartyIdChange: (String) -> Unit, onDateChange: (String) -> Unit,
    onAadhaarChange: (String) -> Unit, onPanChange: (String) -> Unit,
    onReferenceChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    if (showingPartyForm) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (editingWorker == null) "Register New Party" else "Edit Party Profile",
                color = if (dark) Color.White else Color(0xFF0F172A),
                fontWeight = FontWeight.Black, fontSize = 18.sp
            )
            PremiumDivider(dark)

            PremiumFormSection("IDENTITY", dark) {
                GlassTextField(value = pPartyId, onValueChange = onPartyIdChange,
                    label = "Party ID", placeholder = "PID-001", darkTheme = dark)
                GlassTextField(value = pName, onValueChange = onNameChange,
                    label = "Full Name", placeholder = "John Doe / Tejas Contractors", darkTheme = dark)
            }

            PremiumFormSection("CONTACT", dark) {
                GlassTextField(value = pPhone, onValueChange = onPhoneChange,
                    label = "Phone (+91)", placeholder = "9876543210", darkTheme = dark)
                GlassTextField(value = pEmail, onValueChange = onEmailChange,
                    label = "Email Address", placeholder = "client@example.com", darkTheme = dark)
                GlassTextField(value = pAddress, onValueChange = onAddressChange,
                    label = "Address", placeholder = "Home or office address", darkTheme = dark)
            }

            PremiumFormSection("CATEGORY", dark) {
                PremiumChipSelector(
                    options = listOf("Client", "Staff", "Vendor", "Worker", "Investor"),
                    selected = pPartyType,
                    accentColor = AccentCyan,
                    dark = dark,
                    onSelect = onPartyTypeChange
                )
            }

            PremiumFormSection("EMPLOYMENT", dark) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        GlassTextField(value = pDateOfJoining, onValueChange = onDateChange,
                            label = "Joining Date", placeholder = "DD/MM/YYYY", darkTheme = dark)
                    }
                    Box(Modifier.weight(1f)) {
                        GlassTextField(value = pRole, onValueChange = onRoleChange,
                            label = "Designation", placeholder = "Mason Foreman", darkTheme = dark)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1.2f)) {
                        GlassTextField(value = pWage, onValueChange = onWageChange,
                            label = "Daily Wage (₹)", isNumeric = true,
                            placeholder = "e.g. 500", darkTheme = dark)
                    }
                    Column(Modifier.weight(0.8f)) {
                        Text("Shift", color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        PremiumChipSelector(
                            options = listOf("Day", "Night"), selected = pShift,
                            accentColor = AccentCyan, dark = dark, onSelect = onShiftChange
                        )
                    }
                }
                GlassTextField(value = pReference, onValueChange = onReferenceChange,
                    label = "Referred By", placeholder = "Partner X", darkTheme = dark)
            }

            PremiumFormSection("KYC DOCUMENTS", dark) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        GlassTextField(value = pAadhaar, onValueChange = onAadhaarChange,
                            label = "Aadhaar No.", placeholder = "12-digit", darkTheme = dark)
                    }
                    Box(Modifier.weight(1f)) {
                        GlassTextField(value = pPan, onValueChange = onPanChange,
                            label = "PAN No.", placeholder = "10-char", darkTheme = dark)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PremiumOutlineButton("CANCEL", Icons.Default.Close, AccentPink, onCancel, dark, Modifier.weight(1f))
                PremiumGradientButton(
                    if (editingWorker == null) "SAVE PROFILE" else "APPLY CHANGES",
                    Icons.Default.Save, GradientCyan, onSave,
                    enabled = pName.isNotBlank(), modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Registered Parties", color = if (dark) Color.White else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${allWorkers.size} parties found",
                        color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8), fontSize = 11.sp)
                }
                PremiumActionButton("ADD PARTY", Icons.Default.Add, GradientCyan, onFormOpen)
            }
            PremiumDivider(dark)
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allWorkers) { worker ->
                    PremiumWorkerCard(
                        worker = worker,
                        expanded = expandedWorkerId == worker.id,
                        dark = dark,
                        onToggle = { onExpandedWorkerChange(if (expandedWorkerId == worker.id) null else worker.id) },
                        onEdit = { onEditWorker(worker) },
                        onDelete = { onDeleteWorker(worker) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumWorkerCard(
    worker: Worker, expanded: Boolean, dark: Boolean,
    onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit
) {
    val partyAccent = when (worker.partyType) {
        "Client" -> AccentCyan
        "Investor" -> AccentPurple
        "Vendor" -> AccentAmber
        "Staff" -> AccentGreen
        else -> AccentPink
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (dark) Color(0xFF0D1526) else Color.White)
            .border(1.dp,
                Brush.linearGradient(listOf(partyAccent.copy(0.4f), partyAccent.copy(0.1f))),
                RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(worker.avatarColor).copy(0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        worker.name.take(2).uppercase(),
                        color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(worker.name, color = if (dark) Color.White else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(partyAccent.copy(0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(worker.partyType, color = partyAccent,
                                fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("ID: ${worker.partyId.ifBlank { "N/A" }}",
                            color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PremiumIconAction(Icons.Default.Edit, AccentCyan, onEdit)
                PremiumIconAction(Icons.Default.DeleteOutline, AccentPink, onDelete)
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = if (dark) Color(0xFF475569) else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(partyAccent.copy(0.04f))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PremiumDivider(dark)
                Spacer(Modifier.height(4.dp))
                val details = listOf(
                    "Phone" to worker.phone.ifBlank { "Not provided" },
                    "Email" to worker.email.ifBlank { "Not provided" },
                    "Address" to worker.address.ifBlank { "Not provided" },
                    "Joining Date" to worker.dateOfJoining.ifBlank { "Not provided" },
                    "Referred By" to worker.reference.ifBlank { "Not provided" },
                    "Aadhaar" to worker.aadhaar.ifBlank { "Not provided" },
                    "PAN" to worker.pan.ifBlank { "Not provided" },
                    "Role" to worker.role,
                    "Shift" to "${worker.shift} Shift",
                    "Daily Wage" to formatIndianRupees(worker.wageRate)
                )
                details.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, color = if (dark) Color(0xFF475569) else Color(0xFF94A3B8),
                            fontSize = 11.sp)
                        Text(value, color = if (dark) Color.White else Color(0xFF0F172A),
                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumEstimatesContent(
    dark: Boolean, allEstimates: List<Estimate>,
    currentProject: Project?, inputEstName: String,
    inputEstQty: String, inputEstRate: String,
    cFormatter: NumberFormat,
    onNameChange: (String) -> Unit, onQtyChange: (String) -> Unit,
    onRateChange: (String) -> Unit, onAdd: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PremiumFormSection("ADD MATERIAL ESTIMATE", dark) {
            GlassTextField(value = inputEstName, onValueChange = onNameChange,
                label = "Material Name (e.g. Cement)", darkTheme = dark)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) {
                    GlassTextField(value = inputEstQty, onValueChange = onQtyChange,
                        label = "Qty", isNumeric = true, darkTheme = dark)
                }
                Box(Modifier.weight(1f)) {
                    GlassTextField(value = inputEstRate, onValueChange = onRateChange,
                        label = "Rate (₹)", isNumeric = true, darkTheme = dark)
                }
            }
            PremiumGradientButton("ADD ESTIMATE", Icons.Default.Add, GradientPurple, onAdd,
                enabled = inputEstName.isNotBlank() && currentProject != null,
                modifier = Modifier.fillMaxWidth())
        }
        PremiumDivider(dark)
        val filtered = allEstimates.filter { it.projectId == currentProject?.id }
        val total = filtered.sumOf { it.totalCost }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Materials List (${filtered.size})",
                color = if (dark) Color.White else Color(0xFF0F172A),
                fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(cFormatter.format(total), color = AccentGreen,
                fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered) { est ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (dark) Color(0xFF0D1526) else Color(0xFFF8FAFF))
                        .border(1.dp, if (dark) PremiumBorder else PremiumBorderLight, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(est.itemName, color = if (dark) Color.White else Color(0xFF0F172A),
                            fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("${est.quantity} ${est.unit} @ ${formatIndianRupees(est.rate)}/${est.unit}",
                            color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Text(cFormatter.format(est.totalCost), color = AccentGreen,
                        fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun PremiumMOMContent(
    dark: Boolean, allMOMs: List<MOM>, currentProject: Project?,
    inputTitle: String, inputContent: String,
    onTitleChange: (String) -> Unit, onContentChange: (String) -> Unit, onAdd: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PremiumFormSection("LOG MEETING MINUTES", dark) {
            GlassTextField(value = inputTitle, onValueChange = onTitleChange,
                label = "Meeting Title / Subject", darkTheme = dark)
            GlassTextField(value = inputContent, onValueChange = onContentChange,
                label = "Key discussion points & decisions...", darkTheme = dark)
            PremiumGradientButton("ADD RECORD", Icons.Default.Add, GradientPink, onAdd,
                enabled = inputTitle.isNotBlank() && currentProject != null,
                modifier = Modifier.fillMaxWidth())
        }
        PremiumDivider(dark)
        val projMOMs = allMOMs.filter { it.projectId == currentProject?.id }
        Text("Meeting Records (${projMOMs.size})",
            color = if (dark) Color.White else Color(0xFF0F172A),
            fontWeight = FontWeight.Bold, fontSize = 14.sp)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(projMOMs) { mom ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (dark) Color(0xFF0D1526) else Color.White)
                        .border(1.dp,
                            Brush.linearGradient(listOf(AccentPink.copy(0.4f), AccentPink.copy(0.1f))),
                            RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(3.dp).height(20.dp).background(AccentPink, RoundedCornerShape(2.dp)))
                        Spacer(Modifier.width(10.dp))
                        Text(mom.title, color = if (dark) Color.White else Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(mom.content, color = if (dark) Color(0xFF64748B) else Color(0xFF475569), fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null,
                            tint = if (dark) Color(0xFF334155) else Color(0xFFCBD5E1),
                            modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(mom.date, color = if (dark) Color(0xFF334155) else Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumPayrollContent(
    dark: Boolean, allPayroll: List<Payroll>, allWorkers: List<Worker>,
    currentProject: Project?, selectedWorker: Worker?, inputAmount: String,
    cFormatter: NumberFormat, onWorkerSelect: () -> Unit,
    onAmountChange: (String) -> Unit, onProcess: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PremiumFormSection("ISSUE WAGE PAYMENT", dark) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (dark) Color(0xFF0A1628) else Color(0xFFF0F9FF))
                    .border(1.dp, AccentGreen.copy(0.3f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onWorkerSelect)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        selectedWorker?.name ?: "Tap to select worker →",
                        color = if (selectedWorker != null) AccentGreen
                        else if (dark) Color(0xFF475569) else Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = if (selectedWorker != null) FontWeight.Bold else FontWeight.Normal
                    )
                    Icon(Icons.Default.PersonSearch, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                }
            }
            GlassTextField(value = inputAmount, onValueChange = onAmountChange,
                label = "Payment Amount (₹)", isNumeric = true, darkTheme = dark)
            PremiumGradientButton("PROCESS PAYROLL", Icons.Default.Send, GradientGreen, onProcess,
                enabled = selectedWorker != null && currentProject != null && inputAmount.isNotBlank(),
                modifier = Modifier.fillMaxWidth())
        }
        PremiumDivider(dark)
        val projPayroll = allPayroll.filter { it.projectId == currentProject?.id }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Disbursements (${projPayroll.size})",
                color = if (dark) Color.White else Color(0xFF0F172A),
                fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(cFormatter.format(projPayroll.sumOf { it.wagesPaid }),
                color = AccentGreen, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(projPayroll) { py ->
                val worker = allWorkers.find { it.id == py.workerId }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (dark) Color(0xFF0D1526) else Color.White)
                        .border(1.dp,
                            Brush.linearGradient(listOf(AccentGreen.copy(0.35f), AccentGreen.copy(0.1f))),
                            RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AccentGreen.copy(0.15f))
                                .border(1.dp, AccentGreen.copy(0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(worker?.name ?: "Unknown", color = if (dark) Color.White else Color(0xFF0F172A),
                                fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(py.date, color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                    Text(cFormatter.format(py.wagesPaid), color = AccentGreen,
                        fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun PremiumReportsContent(
    dark: Boolean, allTransactions: List<Transaction>,
    currentProject: Project?, cFormatter: NumberFormat
) {
    val projTx = allTransactions.filter { it.projectId == currentProject?.id }
    val categoryTotals = projTx.groupBy { it.category }
        .mapValues { it.value.sumOf { tx -> tx.amount } }
    val totalSum = projTx.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF0D1F3C), Color(0xFF1A0A2E))))
                .border(1.dp,
                    Brush.linearGradient(listOf(AccentAmber.copy(0.5f), AccentOrange.copy(0.3f))),
                    RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Column {
                Text("Total Expenditure", color = AccentAmber,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(4.dp))
                Text(cFormatter.format(totalSum),
                    fontSize = 28.sp, fontWeight = FontWeight.Black,
                    style = androidx.compose.ui.text.TextStyle(brush = GradientAmber))
                Text("${projTx.size} transactions • ${categoryTotals.size} categories",
                    color = Color(0xFF64748B), fontSize = 11.sp)
            }
        }

        Text("Expenditure by Category", color = if (dark) Color.White else Color(0xFF0F172A),
            fontWeight = FontWeight.Bold, fontSize = 15.sp)

        categoryTotals.forEach { (cat, tot) ->
            val ratio = if (totalSum > 0.0) (tot / totalSum).toFloat() else 0f
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (dark) Color(0xFF0D1526) else Color.White)
                    .border(1.dp, if (dark) PremiumBorder else PremiumBorderLight, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(cat, color = if (dark) Color(0xFF94A3B8) else Color(0xFF475569), fontSize = 13.sp)
                    Text(cFormatter.format(tot),
                        color = if (dark) Color.White else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (dark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(ratio).fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(GradientAmber)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text("${(ratio * 100).toInt()}% of total",
                    color = AccentAmber, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PremiumDeveloperContent(
    dark: Boolean, userMessageText: String,
    onMessageChange: (String) -> Unit, onSendFeedback: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF1A0A2E), Color(0xFF0D1526))))
                    .border(1.dp,
                        Brush.linearGradient(listOf(AccentPurple.copy(0.6f), AccentPink.copy(0.3f))),
                        RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BuildOnSiteLogo(darkTheme = dark)
                    Spacer(Modifier.height(12.dp))
                    Text("DipTech Pune", color = Color.White,
                        fontWeight = FontWeight.Black, fontSize = 24.sp,
                        textAlign = TextAlign.Center)
                    Text("Building the digital backbone of construction",
                        color = AccentPurple.copy(0.9f), fontSize = 12.sp,
                        textAlign = TextAlign.Center)
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (dark) Color(0xFF0D1526) else Color.White)
                    .border(1.dp, if (dark) PremiumBorder else PremiumBorderLight, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("LEAD SOFTWARE ENGINEER", color = AccentCyan,
                    fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                listOf(
                    Triple(Icons.Default.Person, "Dipak Harane", true),
                    Triple(Icons.Default.Home, "New Sangvi, Pune", false),
                    Triple(Icons.Default.Phone, "7709320496", false),
                    Triple(Icons.Default.Email, "haranedipak@gmail.com", false)
                ).forEach { (icon, value, isName) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentPurple.copy(0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = AccentPurple, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(value,
                            color = if (isName) {
                                if (dark) Color.White else Color(0xFF0F172A)
                            } else {
                                if (dark) Color(0xFF64748B) else Color(0xFF475569)
                            },
                            fontWeight = if (isName) FontWeight.Bold else FontWeight.Normal,
                            fontSize = if (isName) 15.sp else 13.sp)
                    }
                }
            }
        }

        item {
            Text(
                "Welcome to the Unified ConstructPro Workspace! Meticulously crafted to empower site engineers, project managers & contractors with real-time financial audits, digital wage registers, seamless estimations, and secure cloud backups. Thank you for choosing DipTech Pune.",
                color = if (dark) Color(0xFF64748B) else Color(0xFF475569),
                fontSize = 12.sp, lineHeight = 18.sp
            )
        }

        item {
            PremiumFormSection("SEND FEEDBACK", dark) {
                GlassTextField(
                    value = userMessageText,
                    onValueChange = onMessageChange,
                    label = "Share your feedback, review or feature request...",
                    focusedStroke = AccentPurple,
                    darkTheme = dark
                )
                Spacer(Modifier.height(4.dp))
                PremiumGradientButton(
                    "SUBMIT FEEDBACK VIA EMAIL",
                    Icons.Default.Send, GradientPurple, onSendFeedback,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PremiumProjectFormContent(
    dark: Boolean, projName: String, projLocation: String,
    projBudget: String, projStatus: String, projBg: String, editingProject: Project?,
    onNameChange: (String) -> Unit, onLocationChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit, onStatusChange: (String) -> Unit,
    onBgChange: (String) -> Unit,
    onUploadImageClick: () -> Unit,
    onCancel: () -> Unit, onSave: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassTextField(value = projName, onValueChange = onNameChange,
            label = "Project Site Name", placeholder = "Emerald Plaza Block C", darkTheme = dark)
        GlassTextField(value = projLocation, onValueChange = onLocationChange,
            label = "Site Location / Address", placeholder = "Metro Sector 15, Pune", darkTheme = dark)
        GlassTextField(value = projBudget, onValueChange = onBudgetChange,
            label = "Base Budget (₹)", isNumeric = true,
            placeholder = "e.g. 15000000 (1.5 Cr)", darkTheme = dark)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                GlassTextField(value = projBg, onValueChange = onBgChange,
                    label = "Project Image URL / Path", placeholder = "https://images.unsplash.com/... or path", darkTheme = dark)
            }
            IconButton(
                onClick = onUploadImageClick,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (dark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                    .border(1.dp, AccentPurple.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.Image, contentDescription = "Upload Image", tint = AccentPurple)
            }
        }

        val parsedBudget = projBudget.toDoubleOrNull() ?: 0.0
        if (parsedBudget > 0.0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentGreen.copy(0.08f))
                    .border(1.dp, AccentGreen.copy(0.3f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CurrencyRupee, null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(formatIndianRupeesWithLakhCr(parsedBudget),
                    color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text("Project Status", color = if (dark) Color(0xFF64748B) else Color(0xFF94A3B8),
            fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Active" to AccentGreen, "On Hold" to AccentAmber, "Completed" to AccentCyan)
                .forEach { (status, col) ->
                    val selected = projStatus == status
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) col.copy(0.15f) else Color.Transparent)
                            .border(
                                if (selected) 1.5.dp else 1.dp,
                                if (selected) col else if (dark) PremiumBorder else PremiumBorderLight,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onStatusChange(status) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(8.dp).background(col, CircleShape))
                            Spacer(Modifier.height(4.dp))
                            Text(status, color = if (selected) col
                            else if (dark) Color(0xFF475569) else Color(0xFF94A3B8),
                                fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
        }

        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PremiumOutlineButton("CANCEL", Icons.Default.Close, AccentPink, onCancel, dark, Modifier.weight(1f))
            PremiumGradientButton(
                if (editingProject == null) "CREATE PROJECT" else "APPLY CHANGES",
                Icons.Default.Save, GradientPurple, onSave,
                enabled = projName.isNotBlank() && projLocation.isNotBlank(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PremiumDeleteConfirmContent(
    dark: Boolean, projectName: String, onCancel: () -> Unit, onConfirm: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Warning icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(AccentPink.copy(0.12f))
                .border(2.dp, AccentPink.copy(0.4f), CircleShape)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Warning, null, tint = AccentPink, modifier = Modifier.size(32.dp))
        }
        Text(
            "Delete \"$projectName\"?",
            color = if (dark) Color.White else Color(0xFF0F172A),
            fontWeight = FontWeight.Black, fontSize = 18.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AccentPink.copy(0.07f))
                .border(1.dp, AccentPink.copy(0.25f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(
                "⚠ This is irreversible. All tasks, transactions, estimates, meeting records, and timesheets for this project will be permanently deleted.",
                color = if (dark) Color(0xFF94A3B8) else Color(0xFF475569),
                fontSize = 12.sp, lineHeight = 18.sp
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PremiumOutlineButton("CANCEL", Icons.Default.Close, AccentCyan, onCancel, dark, Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(AccentPink, Color(0xFF7C3AED))))
                    .clickable(onClick = onConfirm)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("DELETE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM UTILITY COMPOSABLES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PremiumGradientButton(
    label: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) gradient else Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF1E293B))))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 13.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null,
                tint = if (enabled) Color.White else Color(0xFF475569),
                modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(label,
                color = if (enabled) Color.White else Color(0xFF475569),
                fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
private fun PremiumOutlineButton(
    label: String, icon: ImageVector, color: Color,
    onClick: () -> Unit, dark: Boolean, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(0.08f))
            .border(1.dp, color.copy(0.4f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, color = color, fontWeight = FontWeight.Black,
                fontSize = 12.sp, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
private fun PremiumActionButton(
    label: String, icon: ImageVector, gradient: Brush, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(gradient)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.Black,
                fontSize = 11.sp, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
private fun PremiumChipSelector(
    options: List<String>, selected: String, accentColor: Color,
    dark: Boolean, onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) accentColor.copy(0.15f) else Color.Transparent)
                    .border(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) accentColor else if (dark) PremiumBorder else PremiumBorderLight,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(option) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(option,
                    color = if (isSelected) accentColor
                    else if (dark) Color(0xFF475569) else Color(0xFF94A3B8),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PremiumFormSection(title: String, dark: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (dark) Color(0xFF0A1628) else Color(0xFFF8FAFF))
            .border(1.dp, if (dark) PremiumBorder else PremiumBorderLight, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(title, color = AccentCyan, fontSize = 9.sp,
            fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        content()
    }
}

@Composable
private fun PremiumSubSectionHeader(title: String, icon: ImageVector, color: Color, dark: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PremiumDivider(dark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.linearGradient(listOf(
                    Color.Transparent,
                    if (dark) PremiumBorder else PremiumBorderLight,
                    Color.Transparent
                ))
            )
    )
}

@Composable
private fun PremiumSyncingIndicator(dark: Boolean, onComplete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AccentCyan.copy(0.08f))
            .border(1.dp, AccentCyan.copy(0.25f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            CircularProgressIndicator(
                color = AccentCyan, modifier = Modifier.size(20.dp), strokeWidth = 2.dp
            )
            Spacer(Modifier.width(12.dp))
            Text("Uploading CSV & JSON to Drive...",
                color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1400)
        onComplete()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPER / CONSTANTS
// ─────────────────────────────────────────────────────────────────────────────

private fun formatIndianRupeesShort(amount: Double): String {
    return when {
        amount >= 10_000_000 -> "₹${String.format("%.1f", amount / 10_000_000)}Cr"
        amount >= 100_000 -> "₹${String.format("%.1f", amount / 100_000)}L"
        amount >= 1_000 -> "₹${String.format("%.0f", amount / 1_000)}K"
        else -> "₹${amount.toInt()}"
    }
}

private const val SEED_PROJECT_JSON = """
{
  "project": {
    "id": 88, "name": "Pune Highway Segment B",
    "location": "Pune Ring Road Bypass", "budget": 2500000.0, "status": "Active"
  },
  "tasks": [{"id":1,"title":"Piling Foundation","priority":"High","status":"In Progress","dueDate":"2026-06-15","assignee":"Suresh Kumar"}],
  "transactions": [{"id":1,"type":"Money In","amount":500000.0,"category":"Client Advance","description":"Initial release","date":"2026-05-26"}],
  "attendance": [], "moms": [], "payroll": [], "estimates": []
}
"""

private const val SEED_FULL_JSON = """
{
  "projects": [{"id":1,"name":"Emerald Plaza Restore","location":"High Street Segment 2","budget":800000.0,"status":"Active"}],
  "workers": [{"id":1,"name":"Jason Becker","role":"Architect Designer","shift":"Day","wageRate":750.0,"avatarColor":-16724321}],
  "tasks": [], "transactions": [], "attendance": [], "moms": [], "payroll": [], "estimates": []
}
"""