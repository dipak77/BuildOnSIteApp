package com.example.ui

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen {
    Dashboard, Money, Tasks, Site, More
}

data class GoogleUser(
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val idToken: String? = null,
    val isGuest: Boolean = false
)

class MainViewModel(private val repository: ConstructionRepository) : ViewModel() {

    // ==========================================
    // UI EVENT CHANNEL (replaces Context-based Toasts)
    // ==========================================
    private val _uiEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 16)
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    private fun emitEvent(event: UiEvent) {
        viewModelScope.launch { _uiEvents.emit(event) }
    }

    // ==========================================
    // OPERATION PROGRESS TRACKING
    // ==========================================
    var operationProgress by mutableStateOf(OperationProgress())
        private set

    // ==========================================
    // GOOGLE USER SESSION
    // ==========================================
    private val _userSession = MutableStateFlow<GoogleUser?>(null)
    val userSession: StateFlow<GoogleUser?> = _userSession.asStateFlow()

    fun handleGoogleSignIn(user: GoogleUser, context: Context) {
        _userSession.value = user
        // Persist session
        val prefs = context.getSharedPreferences("constructpro_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("auth_name", user.displayName)
            putString("auth_email", user.email)
            putString("auth_photo", user.photoUrl ?: "")
            putString("auth_token", user.idToken ?: "")
            putBoolean("auth_guest", user.isGuest)
            apply()
        }
    }

    fun handleGoogleSignOut(context: Context) {
        _userSession.value = null
        val prefs = context.getSharedPreferences("constructpro_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun loadUserSessionFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences("constructpro_prefs", Context.MODE_PRIVATE)
        val name = prefs.getString("auth_name", null)
        val email = prefs.getString("auth_email", null)
        if (name != null && email != null) {
            val photo = prefs.getString("auth_photo", "") ?: ""
            val token = prefs.getString("auth_token", "") ?: ""
            val isGuest = prefs.getBoolean("auth_guest", false)
            _userSession.value = GoogleUser(
                displayName = name,
                email = email,
                photoUrl = if (photo.isEmpty()) null else photo,
                idToken = if (token.isEmpty()) null else token,
                isGuest = isGuest
            )
        } else {
            _userSession.value = null
        }
    }

    // ==========================================
    // DATE UTILITIES
    // ==========================================
    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /** Returns today's date as YYYY-MM-DD string */
    fun todayIso(): String = isoFormatter.format(Date())

    // ==========================================
    // UI STATES
    // ==========================================
    var currentScreen by mutableStateOf(AppScreen.Dashboard)
    var selectedProjectId by mutableStateOf<Int?>(null) // Dynamic first project selector
    var attendanceDate by mutableStateOf(isoFormatter.format(Date())) // Date navigator — uses live date
    var darkThemeEnabled by mutableStateOf(false) // Premium dark glassmorphism mode toggle
    var transactionToEdit by mutableStateOf<Transaction?>(null)
    var activeSiteTab by mutableStateOf("Party")
    var onlineCloudLinkEnabled by mutableStateOf(true)

    // Dialogue triggers accessible globally across composing widgets
    var showQuickDialog by mutableStateOf(false)
    var showProjectDialog by mutableStateOf(false)
    var showTransactionDialog by mutableStateOf(false)
    var showTaskDialog by mutableStateOf(false)
    var showWorkerDialog by mutableStateOf(false)
    var transactionTypePreset by mutableStateOf("Money Out") // "Money In" or "Money Out"

    // Filtering/Search States
    var transactionSearchQuery by mutableStateOf("")
    var transactionTypeFilter by mutableStateOf("All") // "All", "Money In", "Money Out"
    var transactionCategoryFilter by mutableStateOf("All") // "All", "Material", "Labor", "Equipment", etc.

    var taskStatusFilter by mutableStateOf("All") // "All", "To Do", "In Progress", "Done"

    // Selected transaction state globally shared for beautiful click details navigation
    var sharedSelectedTxDetails by mutableStateOf<Transaction?>(null)

    // ==========================================
    // BASE DATABASE FLOWS
    // ==========================================
    val projects = repository.allProjects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val workers = repository.allWorkers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val attendance = repository.allAttendance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transactions = repository.allTransactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val moms = repository.allMOMs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val payroll = repository.allPayroll.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val estimates = repository.allEstimates.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Project
    val activeProject: StateFlow<Project?> = combine(projects, snapshotFlow { selectedProjectId }) { projectList: List<Project>, selectedId: Int? ->
        if (selectedId == null) projectList.firstOrNull()
        else projectList.find { it.id == selectedId } ?: projectList.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Set selected project automatically if first project loads and selected is null
    init {
        viewModelScope.launch {
            try {
                val initialDbProjects = repository.allProjects.first()
                if (initialDbProjects.isEmpty()) {
                    repository.seedDatabase()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emitEvent(UiEvent.ShowError("Failed to initialize database", e))
            }

            projects.collectLatest { projectList ->
                if (selectedProjectId == null && projectList.isNotEmpty()) {
                    selectedProjectId = projectList.first().id
                }
            }
        }
    }

    // ==========================================
    // CRUD DATA MUTATORS (with error handling)
    // ==========================================

    // Projects
    fun addProject(name: String, location: String, budget: Double, customBackground: String? = null) {
        val nameResult = FormValidator.validateProjectName(name)
        val locResult = FormValidator.validateLocation(location)
        if (!nameResult.isValid) { emitEvent(UiEvent.ShowToast(nameResult.errorMessage ?: "Invalid name")); return }
        if (!locResult.isValid) { emitEvent(UiEvent.ShowToast(locResult.errorMessage ?: "Invalid location")); return }

        viewModelScope.launch {
            try {
                val id = repository.insertProject(Project(name = name, location = location, budget = budget, status = "Active", customBackground = customBackground))
                selectedProjectId = id.toInt()
                emitEvent(UiEvent.ShowToast("Project \"$name\" created!"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to create project", e))
            }
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            try {
                repository.updateProject(project)
                emitEvent(UiEvent.ShowToast("Project updated successfully"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to update project", e))
            }
        }
    }

    fun deleteProject(project: Project, context: Context) {
        viewModelScope.launch {
            try {
                repository.deleteProject(project)
                emitEvent(UiEvent.ShowToast("Project deleted successfully"))
                // Reset project focus
                selectedProjectId = null
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to delete project", e))
            }
        }
    }

    // Workers
    fun addWorker(
        name: String,
        role: String,
        shift: String,
        wageRate: Double,
        color: Int,
        phone: String = "",
        email: String = "",
        partyType: String = "Worker",
        address: String = "",
        partyId: String = "",
        dateOfJoining: String = "",
        aadhaar: String = "",
        pan: String = "",
        reference: String = ""
    ) {
        val nameResult = FormValidator.validateWorkerName(name)
        if (!nameResult.isValid) { emitEvent(UiEvent.ShowToast(nameResult.errorMessage ?: "Invalid name")); return }

        viewModelScope.launch {
            try {
                repository.insertWorker(
                    Worker(
                        name = name,
                        role = role,
                        shift = shift,
                        wageRate = wageRate,
                        avatarColor = color,
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
                )
                emitEvent(UiEvent.ShowToast("Party \"$name\" added!"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to add worker", e))
            }
        }
    }

    fun addWorkerInline(
        name: String,
        role: String,
        shift: String,
        wageRate: Double,
        color: Int,
        phone: String = "",
        email: String = "",
        partyType: String = "Worker",
        address: String = "",
        partyId: String = "",
        dateOfJoining: String = "",
        aadhaar: String = "",
        pan: String = "",
        reference: String = "",
        onSuccess: (Worker) -> Unit
    ) {
        val nameResult = FormValidator.validateWorkerName(name)
        if (!nameResult.isValid) { emitEvent(UiEvent.ShowToast(nameResult.errorMessage ?: "Invalid name")); return }

        viewModelScope.launch {
            try {
                val newWorker = Worker(
                    name = name,
                    role = role,
                    shift = shift,
                    wageRate = wageRate,
                    avatarColor = color,
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
                val insertedId = repository.insertWorker(newWorker)
                val finalWorker = newWorker.copy(id = insertedId.toInt())
                emitEvent(UiEvent.ShowToast("Party \"$name\" created and selected!"))
                onSuccess(finalWorker)
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to add party inline", e))
            }
        }
    }

    fun updateWorker(worker: Worker) {
        viewModelScope.launch {
            try {
                repository.updateWorker(worker)
                emitEvent(UiEvent.ShowToast("Worker profile updated"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to update worker", e))
            }
        }
    }

    fun deleteWorker(worker: Worker, context: Context) {
        viewModelScope.launch {
            try {
                repository.deleteWorker(worker)
                emitEvent(UiEvent.ShowToast("Worker profile removed"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to delete worker", e))
            }
        }
    }

    // Attendance (Toggle/Mark)
    fun recordAttendance(workerId: Int, projectId: Int, date: String, status: String, overtimeHours: Double = 0.0) {
        viewModelScope.launch {
            try {
                if (status == "Clear") {
                    repository.deleteAttendanceRecord(workerId, date)
                } else {
                    repository.insertAttendance(
                        Attendance(
                            workerId = workerId,
                            projectId = projectId,
                            date = date,
                            status = status,
                            overtimeHours = overtimeHours
                        )
                    )
                }
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to record attendance", e))
            }
        }
    }

    // Tasks
    fun addTask(projectId: Int, title: String, priority: String, assignee: String, dueDate: String) {
        val titleResult = FormValidator.validateTaskTitle(title)
        if (!titleResult.isValid) { emitEvent(UiEvent.ShowToast(titleResult.errorMessage ?: "Invalid title")); return }

        viewModelScope.launch {
            try {
                repository.insertTask(
                    Task(
                        projectId = projectId,
                        title = title,
                        priority = priority,
                        status = "To Do",
                        dueDate = dueDate,
                        assignee = assignee
                    )
                )
                emitEvent(UiEvent.ShowToast("Task assigned!"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to create task", e))
            }
        }
    }

    fun cycleTaskStatus(task: Task) {
        viewModelScope.launch {
            try {
                val nextStatus = when (task.status) {
                    "To Do" -> "In Progress"
                    "In Progress" -> "Done"
                    else -> "To Do"
                }
                repository.updateTask(task.copy(status = nextStatus))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to update task", e))
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.updateTask(task)
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to update task", e))
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.deleteTask(task)
                emitEvent(UiEvent.ShowToast("Task deleted"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to delete task", e))
            }
        }
    }

    // Transactions
    fun addTransaction(
        projectId: Int,
        type: String,
        amount: Double,
        category: String,
        description: String,
        date: String,
        partyId: Int? = null,
        partyName: String? = null,
        reference: String = "",
        paymentMethod: String = "Cash"
    ) {
        viewModelScope.launch {
            try {
                repository.insertTransaction(
                    Transaction(
                        projectId = projectId,
                        type = type,
                        amount = amount,
                        category = category,
                        description = description,
                        date = date,
                        partyId = partyId,
                        partyName = partyName,
                        reference = reference,
                        paymentMethod = paymentMethod
                    )
                )
                emitEvent(UiEvent.ShowToast("Transaction recorded!"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to record transaction", e))
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
                emitEvent(UiEvent.ShowToast("Transaction deleted"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to delete transaction", e))
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.updateTransaction(transaction)
                emitEvent(UiEvent.ShowToast("Transaction updated!"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to update transaction", e))
            }
        }
    }

    // MOM
    fun addMOM(projectId: Int, title: String, content: String, date: String) {
        viewModelScope.launch {
            try {
                repository.insertMOM(MOM(projectId = projectId, title = title, content = content, date = date))
                emitEvent(UiEvent.ShowToast("Meeting minutes saved"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to save MOM", e))
            }
        }
    }

    fun deleteMOM(mom: MOM) {
        viewModelScope.launch {
            try {
                repository.deleteMOM(mom)
                emitEvent(UiEvent.ShowToast("Meeting minutes deleted"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to delete MOM", e))
            }
        }
    }

    // Payroll
    fun addPayroll(workerId: Int, projectId: Int, date: String, wagesPaid: Double, status: String) {
        viewModelScope.launch {
            try {
                repository.insertPayroll(Payroll(workerId = workerId, projectId = projectId, date = date, wagesPaid = wagesPaid, status = status))
                emitEvent(UiEvent.ShowToast("Payroll entry recorded"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to process payroll", e))
            }
        }
    }

    fun updatePayroll(payroll: Payroll) {
        viewModelScope.launch {
            try {
                repository.updatePayroll(payroll)
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to update payroll", e))
            }
        }
    }

    fun deletePayroll(payroll: Payroll) {
        viewModelScope.launch {
            try {
                repository.deletePayroll(payroll)
                emitEvent(UiEvent.ShowToast("Payroll entry removed"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to delete payroll", e))
            }
        }
    }

    // Estimates
    fun addEstimate(projectId: Int, itemName: String, quantity: Double, unit: String, rate: Double) {
        viewModelScope.launch {
            try {
                repository.insertEstimate(
                    Estimate(
                        projectId = projectId,
                        itemName = itemName,
                        quantity = quantity,
                        unit = unit,
                        rate = rate,
                        totalCost = quantity * rate
                    )
                )
                emitEvent(UiEvent.ShowToast("Estimate added"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to add estimate", e))
            }
        }
    }

    fun deleteEstimate(estimate: Estimate) {
        viewModelScope.launch {
            try {
                repository.deleteEstimate(estimate)
                emitEvent(UiEvent.ShowToast("Estimate removed"))
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to delete estimate", e))
            }
        }
    }

    // ==========================================
    // DATA EXPORT/IMPORT RIGS (with progress tracking)
    // ==========================================

    fun exportTransactionsCSV(context: Context) {
        viewModelScope.launch {
            try {
                operationProgress = OperationProgress(isActive = true, progress = 0.3f, message = "Preparing CSV...")
                val activeProj = activeProject.value ?: run {
                    emitEvent(UiEvent.ShowToast("No active project selected")); return@launch
                }
                val list = transactions.value.filter { it.projectId == activeProj.id }
                operationProgress = operationProgress.copy(progress = 0.7f, message = "Writing file...")
                DataIO.exportTransactionsCSV(context, list, activeProj.name)
                operationProgress = OperationProgress()
                emitEvent(UiEvent.ExportSuccess)
            } catch (e: Exception) {
                operationProgress = OperationProgress()
                emitEvent(UiEvent.ShowError("CSV export failed", e))
            }
        }
    }

    fun exportFullBackup(context: Context) {
        viewModelScope.launch {
            try {
                operationProgress = OperationProgress(isActive = true, progress = 0.2f, message = "Collecting data...")
                DataIO.exportBackupJSON(
                    context,
                    projects.value,
                    workers.value,
                    tasks.value,
                    transactions.value,
                    attendance.value,
                    moms.value,
                    payroll.value,
                    estimates.value
                )
                operationProgress = OperationProgress()
                emitEvent(UiEvent.ExportSuccess)
                emitEvent(UiEvent.ShowToast("Full backup exported!"))
            } catch (e: Exception) {
                operationProgress = OperationProgress()
                emitEvent(UiEvent.ShowError("Backup export failed", e))
            }
        }
    }

    fun importFullBackup(context: Context, jsonString: String) {
        viewModelScope.launch {
            try {
                operationProgress = OperationProgress(isActive = true, progress = 0.1f, message = "Parsing backup...")
                val success = DataIO.importBackupJSON(jsonString, AppDatabase.getDatabase(context).constructionDao())
                operationProgress = OperationProgress()
                if (success) {
                    emitEvent(UiEvent.ImportSuccess)
                    emitEvent(UiEvent.ShowToast("Database backup restored successfully!", long = true))
                } else {
                    emitEvent(UiEvent.ShowError("Failed to restore backup. Please verify file integrity."))
                }
            } catch (e: Exception) {
                operationProgress = OperationProgress()
                emitEvent(UiEvent.ShowError("Import failed: ${e.localizedMessage}", e))
            }
        }
    }

    // Project-wise backup & background mutators
    fun exportProjectBackup(context: Context, project: Project) {
        viewModelScope.launch {
            try {
                operationProgress = OperationProgress(isActive = true, progress = 0.3f, message = "Exporting project...")
                DataIO.exportProjectBackupJSON(
                    context,
                    project,
                    tasks.value,
                    transactions.value,
                    attendance.value,
                    moms.value,
                    payroll.value,
                    estimates.value
                )
                operationProgress = OperationProgress()
                emitEvent(UiEvent.ExportSuccess)
            } catch (e: Exception) {
                operationProgress = OperationProgress()
                emitEvent(UiEvent.ShowError("Project export failed", e))
            }
        }
    }

    fun importProjectBackup(context: Context, jsonString: String) {
        viewModelScope.launch {
            try {
                operationProgress = OperationProgress(isActive = true, progress = 0.1f, message = "Importing project...")
                val success = DataIO.importProjectBackupJSON(jsonString, AppDatabase.getDatabase(context).constructionDao())
                operationProgress = OperationProgress()
                if (success) {
                    emitEvent(UiEvent.ImportSuccess)
                    emitEvent(UiEvent.ShowToast("Project restored successfully!", long = true))
                } else {
                    emitEvent(UiEvent.ShowError("Failed to restore project backup. Check file integrity."))
                }
            } catch (e: Exception) {
                operationProgress = OperationProgress()
                emitEvent(UiEvent.ShowError("Project import failed", e))
            }
        }
    }

    fun updateProjectBackground(project: Project, backgroundStyle: String) {
        viewModelScope.launch {
            try {
                val updated = project.copy(customBackground = backgroundStyle)
                repository.updateProject(updated)
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowError("Failed to update background", e))
            }
        }
    }

    // ==========================================
    // VIEW MODEL FACTORY DEFINITION
    // ==========================================
    class Factory(private val repository: ConstructionRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
