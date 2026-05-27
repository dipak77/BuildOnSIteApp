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

    // Google User Session
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
            // Auto sign in with a default profile for friction-free developer/demo experience in the streaming emulator!
            _userSession.value = GoogleUser(
                displayName = "Dipak Harane",
                email = "haranedipak@gmail.com",
                photoUrl = null,
                isGuest = true
            )
        }
    }

    // UI States
    var currentScreen by mutableStateOf(AppScreen.Dashboard)
    var selectedProjectId by mutableStateOf<Int?>(null) // Dynamic first project selector
    var attendanceDate by mutableStateOf("2026-05-26") // Date navigator
    var darkThemeEnabled by mutableStateOf(true) // Premium dark glassmorphism mode toggle

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

    // Base Database Flows
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
            }

            projects.collectLatest { projectList ->
                if (selectedProjectId == null && projectList.isNotEmpty()) {
                    selectedProjectId = projectList.first().id
                }
            }
        }
    }

    // ==========================================
    // CRUD DATA MUTATORS
    // ==========================================

    // Projects
    fun addProject(name: String, location: String, budget: Double) {
        viewModelScope.launch {
            val id = repository.insertProject(Project(name = name, location = location, budget = budget, status = "Active"))
            selectedProjectId = id.toInt()
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            repository.updateProject(project)
        }
    }

    fun deleteProject(project: Project, context: Context) {
        viewModelScope.launch {
            repository.deleteProject(project)
            Toast.makeText(context, "Project deleted successfully", Toast.LENGTH_SHORT).show()
            // Reset project focus
            selectedProjectId = null
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
        viewModelScope.launch {
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
        }
    }

    fun updateWorker(worker: Worker) {
        viewModelScope.launch {
            repository.updateWorker(worker)
        }
    }

    fun deleteWorker(worker: Worker, context: Context) {
        viewModelScope.launch {
            repository.deleteWorker(worker)
            Toast.makeText(context, "Worker profile removed", Toast.LENGTH_SHORT).show()
        }
    }

    // Attendance (Toggle/Mark)
    fun recordAttendance(workerId: Int, projectId: Int, date: String, status: String, overtimeHours: Double = 0.0) {
        viewModelScope.launch {
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
        }
    }

    // Tasks
    fun addTask(projectId: Int, title: String, priority: String, assignee: String, dueDate: String) {
        viewModelScope.launch {
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
        }
    }

    fun cycleTaskStatus(task: Task) {
        viewModelScope.launch {
            val nextStatus = when (task.status) {
                "To Do" -> "In Progress"
                "In Progress" -> "Done"
                else -> "To Do"
            }
            repository.updateTask(task.copy(status = nextStatus))
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
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
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // MOM
    fun addMOM(projectId: Int, title: String, content: String, date: String) {
        viewModelScope.launch {
            repository.insertMOM(MOM(projectId = projectId, title = title, content = content, date = date))
        }
    }

    fun deleteMOM(mom: MOM) {
        viewModelScope.launch {
            repository.deleteMOM(mom)
        }
    }

    // Payroll
    fun addPayroll(workerId: Int, projectId: Int, date: String, wagesPaid: Double, status: String) {
        viewModelScope.launch {
            repository.insertPayroll(Payroll(workerId = workerId, projectId = projectId, date = date, wagesPaid = wagesPaid, status = status))
        }
    }

    fun updatePayroll(payroll: Payroll) {
        viewModelScope.launch {
            repository.updatePayroll(payroll)
        }
    }

    fun deletePayroll(payroll: Payroll) {
        viewModelScope.launch {
            repository.deletePayroll(payroll)
        }
    }

    // Estimates
    fun addEstimate(projectId: Int, itemName: String, quantity: Double, unit: String, rate: Double) {
        viewModelScope.launch {
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
        }
    }

    fun deleteEstimate(estimate: Estimate) {
        viewModelScope.launch {
            repository.deleteEstimate(estimate)
        }
    }

    // ==========================================
    // DATA EXPORT/IMPORT RIGS
    // ==========================================

    fun exportTransactionsCSV(context: Context) {
        viewModelScope.launch {
            val activeProj = activeProject.value ?: return@launch
            val list = transactions.value.filter { it.projectId == activeProj.id }
            DataIO.exportTransactionsCSV(context, list, activeProj.name)
        }
    }

    fun exportFullBackup(context: Context) {
        viewModelScope.launch {
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
        }
    }

    fun importFullBackup(context: Context, jsonString: String) {
        viewModelScope.launch {
            val success = DataIO.importBackupJSON(jsonString, AppDatabase.getDatabase(context).constructionDao())
            if (success) {
                Toast.makeText(context, "Database backup restored successfully!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to restore backup. Please verify file integrity.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Project-wise backup & background mutators
    fun exportProjectBackup(context: Context, project: Project) {
        viewModelScope.launch {
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
        }
    }

    fun importProjectBackup(context: Context, jsonString: String) {
        viewModelScope.launch {
            val success = DataIO.importProjectBackupJSON(jsonString, AppDatabase.getDatabase(context).constructionDao())
            if (success) {
                Toast.makeText(context, "Project restored successfully!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to restore project backup. Check file integrity.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun updateProjectBackground(project: Project, backgroundStyle: String) {
        viewModelScope.launch {
            val updated = project.copy(customBackground = backgroundStyle)
            repository.updateProject(updated)
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
