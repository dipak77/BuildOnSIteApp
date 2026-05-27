package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ==========================================
// 1. DATABASE ENTITIES
// ==========================================

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val location: String,
    val budget: Double,
    val status: String, // "Active", "Completed", "On Hold"
    val customBackground: String? = null
)

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String,
    val shift: String, // "Day", "Night"
    val wageRate: Double,
    val avatarColor: Int, // Color packed Int
    val phone: String = "",
    val email: String = "",
    val partyType: String = "Worker", // "Client", "Staff", "Vendor", "Worker", "Investor", etc.
    val address: String = "",
    val partyId: String = "",
    val dateOfJoining: String = "",
    val aadhaar: String = "",
    val pan: String = "",
    val reference: String = "" // given reference field
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workerId: Int,
    val projectId: Int,
    val date: String, // YYYY-MM-DD
    val status: String, // "Present", "Absent", "Overtime"
    val overtimeHours: Double
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val title: String,
    val priority: String, // "High", "Medium", "Low"
    val status: String, // "To Do", "In Progress", "Done"
    val dueDate: String,
    val assignee: String
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val type: String, // "Money In", "Money Out"
    val amount: Double,
    val category: String, // "Material", "Labor", "Equipment", "Client Advance", "Other"
    val description: String,
    val date: String, // YYYY-MM-DD
    val partyId: Int? = null,
    val partyName: String? = null,
    val reference: String = "", // transaction reference
    val paymentMethod: String = "Cash" // "Cash", "Bank Transfer", "Cheque"
)

@Entity(tableName = "mom")
data class MOM(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val title: String,
    val content: String,
    val date: String // YYYY-MM-DD
)

@Entity(tableName = "payroll")
data class Payroll(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workerId: Int,
    val projectId: Int,
    val date: String, // YYYY-MM-DD
    val wagesPaid: Double,
    val status: String // "Paid", "Pending"
)

@Entity(tableName = "estimates")
data class Estimate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val itemName: String,
    val quantity: Double,
    val unit: String, // e.g. "Bags", "Tons", "SqFt"
    val rate: Double,
    val totalCost: Double
)

// ==========================================
// 2. DATA ACCESS OBJECT (DAO)
// ==========================================

@Dao
interface ConstructionDao {
    // Projects
    @Query("SELECT * FROM projects ORDER BY id DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: Int)

    // Workers
    @Query("SELECT * FROM workers ORDER BY name ASC")
    fun getAllWorkers(): Flow<List<Worker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker): Long

    @Update
    suspend fun updateWorker(worker: Worker)

    @Delete
    suspend fun deleteWorker(worker: Worker)

    // Attendance
    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Update
    suspend fun updateAttendance(attendance: Attendance)

    @Query("DELETE FROM attendance WHERE workerId = :workerId AND date = :date")
    suspend fun deleteAttendanceRecord(workerId: Int, date: String)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    // MOM
    @Query("SELECT * FROM mom ORDER BY date DESC")
    fun getAllMOMs(): Flow<List<MOM>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMOM(mom: MOM): Long

    @Delete
    suspend fun deleteMOM(mom: MOM)

    // Payroll
    @Query("SELECT * FROM payroll ORDER BY date DESC")
    fun getAllPayroll(): Flow<List<Payroll>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayroll(payroll: Payroll): Long

    @Update
    suspend fun updatePayroll(payroll: Payroll)

    @Delete
    suspend fun deletePayroll(payroll: Payroll)

    // Estimates
    @Query("SELECT * FROM estimates ORDER BY id DESC")
    fun getAllEstimates(): Flow<List<Estimate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstimate(estimate: Estimate): Long

    @Update
    suspend fun updateEstimate(estimate: Estimate)

    @Delete
    suspend fun deleteEstimate(estimate: Estimate)
}

// ==========================================
// 3. ROOM DATABASE CLASS
// ==========================================

@Database(
    entities = [
        Project::class,
        Worker::class,
        Attendance::class,
        Task::class,
        Transaction::class,
        MOM::class,
        Payroll::class,
        Estimate::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun constructionDao(): ConstructionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "construction_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed database on creation
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database.constructionDao())
                    }
                }
            }
        }

        suspend fun seedDatabase(dao: ConstructionDao) {
            // Seed Projects
            val p1Id = dao.insertProject(Project(name = "Skyline Corporate Tower", location = "Sector 62, City Center", budget = 1250000.0, status = "Active", customBackground = "preset_cyber_blueprint")).toInt()
            val p2Id = dao.insertProject(Project(name = "Emerald Heights Villa", location = "Hilltop Greens", budget = 450000.0, status = "Active", customBackground = "preset_sunset_construct")).toInt()
            dao.insertProject(Project(name = "Metro Line Transit", location = "Subway Segment 4", budget = 3200000.0, status = "On Hold", customBackground = "preset_golden_truss"))

            // Seed Workers (Colors packed as ABGR Ints representing beautiful neon shades)
            val w1Id = dao.insertWorker(Worker(name = "John Carter", role = "Mason Foreman", shift = "Day", wageRate = 350.0, avatarColor = 0xFF3B82F6.toInt())).toInt()
            val w2Id = dao.insertWorker(Worker(name = "Alice Rivera", role = "Senior Electrician", shift = "Day", wageRate = 420.0, avatarColor = 0xFFEC4899.toInt())).toInt()
            val w3Id = dao.insertWorker(Worker(name = "Michael Tyson", role = "Site Supervisor", shift = "Day", wageRate = 500.0, avatarColor = 0xFF10B981.toInt())).toInt()
            val w4Id = dao.insertWorker(Worker(name = "David Kovacs", role = "Plumbing Expert", shift = "Night", wageRate = 380.0, avatarColor = 0xFFF59E0B.toInt())).toInt()
            val w5Id = dao.insertWorker(Worker(name = "Sarah Connor", role = "Safety Officer", shift = "Day", wageRate = 400.0, avatarColor = 0xFF8B5CF6.toInt())).toInt()

            // Seed Attendance for today YYYY-MM-DD
            val today = "2026-05-26"
            dao.insertAttendance(Attendance(workerId = w1Id, projectId = p1Id, date = today, status = "Present", overtimeHours = 0.0))
            dao.insertAttendance(Attendance(workerId = w2Id, projectId = p1Id, date = today, status = "Present", overtimeHours = 2.0))
            dao.insertAttendance(Attendance(workerId = w3Id, projectId = p1Id, date = today, status = "Present", overtimeHours = 0.0))
            dao.insertAttendance(Attendance(workerId = w4Id, projectId = p1Id, date = today, status = "Absent", overtimeHours = 0.0))
            dao.insertAttendance(Attendance(workerId = w5Id, projectId = p2Id, date = today, status = "Present", overtimeHours = 1.5))

            // Seed Tasks
            dao.insertTask(Task(projectId = p1Id, title = "Pour foundation concrete slab", priority = "High", status = "Done", dueDate = "2026-05-24", assignee = "John Carter"))
            dao.insertTask(Task(projectId = p1Id, title = "Conduct structural welding inspection", priority = "High", status = "In Progress", dueDate = "2026-05-28", assignee = "Michael Tyson"))
            dao.insertTask(Task(projectId = p1Id, title = "Finalize electrical conduit piping", priority = "Medium", status = "To Do", dueDate = "2026-05-30", assignee = "Alice Rivera"))
            dao.insertTask(Task(projectId = p2Id, title = "Install master bedroom plumbing lines", priority = "Medium", status = "In Progress", dueDate = "2026-05-27", assignee = "David Kovacs"))
            dao.insertTask(Task(projectId = p2Id, title = "Review exterior facade safety rigging", priority = "Low", status = "To Do", dueDate = "2026-06-02", assignee = "Sarah Connor"))

            // Seed Transactions
            dao.insertTransaction(Transaction(projectId = p1Id, type = "Money In", amount = 850000.0, category = "Client Advance", description = "Initial milestone payment received", date = "2026-05-15"))
            dao.insertTransaction(Transaction(projectId = p1Id, type = "Money Out", amount = 120000.0, category = "Material", description = "Super Grade Portland Cement (400 Bags)", date = "2026-05-18"))
            dao.insertTransaction(Transaction(projectId = p1Id, type = "Money Out", amount = 45000.0, category = "Labor", description = "Worker weekly salary payout", date = "2026-05-22"))
            dao.insertTransaction(Transaction(projectId = p2Id, type = "Money In", amount = 300000.0, category = "Client Advance", description = "Phase-1 booking advance received", date = "2026-05-10"))
            dao.insertTransaction(Transaction(projectId = p2Id, type = "Money Out", amount = 35000.0, category = "Equipment", description = "Excavator rental for foundation dig", date = "2026-05-12"))

            // Seed MOMs
            dao.insertMOM(MOM(projectId = p1Id, title = "Slab Casting Briefing", content = "Checked cement inventory. Approved slump test protocol. Discussed rain precautions and worker scheduling.", date = "2026-05-23"))
            dao.insertMOM(MOM(projectId = p1Id, title = "Weekly Architecture Alignment", content = "Aligned on layout modifications for the HVAC shaft on 3rd floor. Verified load bearing calculations.", date = "2026-05-20"))

            // Seed Payroll
            dao.insertPayroll(Payroll(workerId = w1Id, projectId = p1Id, date = "2026-05-25", wagesPaid = 2450.0, status = "Paid"))
            dao.insertPayroll(Payroll(workerId = w2Id, projectId = p1Id, date = "2026-05-25", wagesPaid = 2940.0, status = "Paid"))
            dao.insertPayroll(Payroll(workerId = w3Id, projectId = p1Id, date = "2026-05-25", wagesPaid = 3500.0, status = "Pending"))

            // Seed Estimates
            dao.insertEstimate(Estimate(projectId = p1Id, itemName = "Grade 500 TMT Steel Bars", quantity = 15.0, unit = "Tons", rate = 850.0, totalCost = 12750.0))
            dao.insertEstimate(Estimate(projectId = p1Id, itemName = "ReadyMix Concrete M25 Grade", quantity = 120.0, unit = "CuM", rate = 95.0, totalCost = 11400.0))
            dao.insertEstimate(Estimate(projectId = p2Id, itemName = "Bricks Red Fine Burned", quantity = 25000.0, unit = "Pcs", rate = 0.15, totalCost = 3750.0))
        }
    }
}

// ==========================================
// 4. REPOSITORY WRAPPER
// ==========================================

class ConstructionRepository(private val dao: ConstructionDao) {
    val allProjects: Flow<List<Project>> = dao.getAllProjects()
    val allWorkers: Flow<List<Worker>> = dao.getAllWorkers()
    val allAttendance: Flow<List<Attendance>> = dao.getAllAttendance()
    val allTasks: Flow<List<Task>> = dao.getAllTasks()
    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()
    val allMOMs: Flow<List<MOM>> = dao.getAllMOMs()
    val allPayroll: Flow<List<Payroll>> = dao.getAllPayroll()
    val allEstimates: Flow<List<Estimate>> = dao.getAllEstimates()

    suspend fun insertProject(project: Project) = dao.insertProject(project)
    suspend fun deleteProject(project: Project) = dao.deleteProject(project)
    suspend fun deleteProjectById(projectId: Int) = dao.deleteProjectById(projectId)
    suspend fun updateProject(project: Project) = dao.updateProject(project)

    suspend fun insertWorker(worker: Worker) = dao.insertWorker(worker)
    suspend fun deleteWorker(worker: Worker) = dao.deleteWorker(worker)
    suspend fun updateWorker(worker: Worker) = dao.updateWorker(worker)

    suspend fun insertAttendance(attendance: Attendance) = dao.insertAttendance(attendance)
    suspend fun deleteAttendanceRecord(workerId: Int, date: String) = dao.deleteAttendanceRecord(workerId, date)

    suspend fun insertTask(task: Task) = dao.insertTask(task)
    suspend fun updateTask(task: Task) = dao.updateTask(task)
    suspend fun deleteTask(task: Task) = dao.deleteTask(task)
    suspend fun deleteTaskById(taskId: Int) = dao.deleteTaskById(taskId)

    suspend fun insertTransaction(transaction: Transaction) = dao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: Transaction) = dao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = dao.deleteTransaction(transaction)
    suspend fun deleteTransactionById(id: Int) = dao.deleteTransactionById(id)

    suspend fun insertMOM(mom: MOM) = dao.insertMOM(mom)
    suspend fun deleteMOM(mom: MOM) = dao.deleteMOM(mom)

    suspend fun insertPayroll(payroll: Payroll) = dao.insertPayroll(payroll)
    suspend fun updatePayroll(payroll: Payroll) = dao.updatePayroll(payroll)
    suspend fun deletePayroll(payroll: Payroll) = dao.deletePayroll(payroll)

    suspend fun insertEstimate(estimate: Estimate) = dao.insertEstimate(estimate)
    suspend fun updateEstimate(estimate: Estimate) = dao.updateEstimate(estimate)
    suspend fun deleteEstimate(estimate: Estimate) = dao.deleteEstimate(estimate)
}
