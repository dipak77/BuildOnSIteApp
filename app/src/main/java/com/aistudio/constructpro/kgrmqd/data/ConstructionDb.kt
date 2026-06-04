package com.aistudio.constructpro.kgrmqd.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import kotlinx.coroutines.flow.Flow
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ==========================================
// 1. DATABASE ENTITIES
// ==========================================

@Entity(
    tableName = "projects",
    indices = [
        Index(value = ["status"]),
        Index(value = ["name"])
    ]
)
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

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workerId", "date"], unique = true),
        Index(value = ["projectId"]),
        Index(value = ["date"]),
        Index(value = ["status"])
    ]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workerId: Int,
    val projectId: Int,
    val date: String, // YYYY-MM-DD
    val status: String, // "Present", "Absent", "Overtime"
    val overtimeHours: Double
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["status"]),
        Index(value = ["priority"])
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val title: String,
    val priority: String, // "High", "Medium", "Low"
    val status: String, // "To Do", "In Progress", "Done"
    val dueDate: String,
    val assignee: String
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["partyId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["type"]),
        Index(value = ["date"]),
        Index(value = ["partyId"]),
        Index(value = ["category"])
    ]
)
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

@Entity(
    tableName = "mom",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class MOM(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val title: String,
    val content: String,
    val date: String // YYYY-MM-DD
)

@Entity(
    tableName = "payroll",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workerId"]),
        Index(value = ["projectId"]),
        Index(value = ["date"])
    ]
)
data class Payroll(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workerId: Int,
    val projectId: Int,
    val date: String, // YYYY-MM-DD
    val wagesPaid: Double,
    val status: String // "Paid", "Pending"
)

@Entity(
    tableName = "estimates",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
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

    // ── Bulk Insert Methods (for 10K-record JSON imports) ──
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProjects(projects: List<Project>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWorkers(workers: List<Worker>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendance(attendance: List<Attendance>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<Task>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransactions(transactions: List<Transaction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMOMs(moms: List<MOM>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPayroll(payroll: List<Payroll>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEstimates(estimates: List<Estimate>)

    // ── Count queries for UI stats ──
    @Query("SELECT COUNT(*) FROM transactions WHERE projectId = :projectId")
    suspend fun getTransactionCount(projectId: Int): Int

    @Query("SELECT COUNT(*) FROM workers")
    suspend fun getWorkerCount(): Int

    // ── Clear all tables (for full restore) ──
    @Query("DELETE FROM projects")
    suspend fun clearProjects()

    @Query("DELETE FROM workers")
    suspend fun clearWorkers()

    @Query("DELETE FROM attendance")
    suspend fun clearAttendance()

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM mom")
    suspend fun clearMOMs()

    @Query("DELETE FROM payroll")
    suspend fun clearPayroll()

    @Query("DELETE FROM estimates")
    suspend fun clearEstimates()

    @Query("UPDATE transactions SET partyId = NULL WHERE partyId = :workerId")
    suspend fun clearTransactionPartyLinks(workerId: Int)

    @Query("DELETE FROM payroll WHERE workerId = :workerId")
    suspend fun deletePayrollForWorker(workerId: Int)

    @Query("DELETE FROM attendance WHERE workerId = :workerId")
    suspend fun deleteAttendanceForWorker(workerId: Int)

    @Query("DELETE FROM tasks WHERE projectId = :projectId")
    suspend fun deleteTasksForProject(projectId: Int)

    @Query("DELETE FROM transactions WHERE projectId = :projectId")
    suspend fun deleteTransactionsForProject(projectId: Int)

    @Query("DELETE FROM attendance WHERE projectId = :projectId")
    suspend fun deleteAttendanceForProject(projectId: Int)

    @Query("DELETE FROM mom WHERE projectId = :projectId")
    suspend fun deleteMOMsForProject(projectId: Int)

    @Query("DELETE FROM payroll WHERE projectId = :projectId")
    suspend fun deletePayrollForProject(projectId: Int)

    @Query("DELETE FROM estimates WHERE projectId = :projectId")
    suspend fun deleteEstimatesForProject(projectId: Int)

    @androidx.room.Transaction
    suspend fun replaceAllData(backupData: BackupData) {
        clearEstimates()
        clearPayroll()
        clearMOMs()
        clearTransactions()
        clearTasks()
        clearAttendance()
        clearWorkers()
        clearProjects()

        insertAllProjects(backupData.projects)
        insertAllWorkers(backupData.workers)
        insertAllTasks(backupData.tasks)
        insertAllTransactions(backupData.transactions)
        insertAllAttendance(backupData.attendance)
        insertAllMOMs(backupData.moms)
        insertAllPayroll(backupData.payroll)
        insertAllEstimates(backupData.estimates)
    }

    @androidx.room.Transaction
    suspend fun deleteProjectWithChildren(project: Project) {
        deleteEstimatesForProject(project.id)
        deletePayrollForProject(project.id)
        deleteMOMsForProject(project.id)
        deleteTransactionsForProject(project.id)
        deleteTasksForProject(project.id)
        deleteAttendanceForProject(project.id)
        deleteProject(project)
    }

    @androidx.room.Transaction
    suspend fun deleteWorkerWithChildren(worker: Worker) {
        clearTransactionPartyLinks(worker.id)
        deletePayrollForWorker(worker.id)
        deleteAttendanceForWorker(worker.id)
        deleteWorker(worker)
    }
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
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun constructionDao(): ConstructionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `attendance_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `workerId` INTEGER NOT NULL, `projectId` INTEGER NOT NULL, `date` TEXT NOT NULL, `status` TEXT NOT NULL, `overtimeHours` REAL NOT NULL, FOREIGN KEY(`workerId`) REFERENCES `workers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("INSERT INTO `attendance_new` (`id`, `workerId`, `projectId`, `date`, `status`, `overtimeHours`) SELECT a.`id`, a.`workerId`, a.`projectId`, a.`date`, a.`status`, a.`overtimeHours` FROM `attendance` a WHERE EXISTS (SELECT 1 FROM `workers` w WHERE w.`id` = a.`workerId`) AND EXISTS (SELECT 1 FROM `projects` p WHERE p.`id` = a.`projectId`)")
                db.execSQL("DROP TABLE `attendance`")
                db.execSQL("ALTER TABLE `attendance_new` RENAME TO `attendance`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_attendance_workerId_date` ON `attendance` (`workerId`, `date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_projectId` ON `attendance` (`projectId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_date` ON `attendance` (`date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_status` ON `attendance` (`status`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `tasks_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `projectId` INTEGER NOT NULL, `title` TEXT NOT NULL, `priority` TEXT NOT NULL, `status` TEXT NOT NULL, `dueDate` TEXT NOT NULL, `assignee` TEXT NOT NULL, FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("INSERT INTO `tasks_new` (`id`, `projectId`, `title`, `priority`, `status`, `dueDate`, `assignee`) SELECT t.`id`, t.`projectId`, t.`title`, t.`priority`, t.`status`, t.`dueDate`, t.`assignee` FROM `tasks` t WHERE EXISTS (SELECT 1 FROM `projects` p WHERE p.`id` = t.`projectId`)")
                db.execSQL("DROP TABLE `tasks`")
                db.execSQL("ALTER TABLE `tasks_new` RENAME TO `tasks`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_projectId` ON `tasks` (`projectId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_status` ON `tasks` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_priority` ON `tasks` (`priority`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `transactions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `projectId` INTEGER NOT NULL, `type` TEXT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `description` TEXT NOT NULL, `date` TEXT NOT NULL, `partyId` INTEGER, `partyName` TEXT, `reference` TEXT NOT NULL, `paymentMethod` TEXT NOT NULL, FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`partyId`) REFERENCES `workers`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL)")
                db.execSQL("INSERT INTO `transactions_new` (`id`, `projectId`, `type`, `amount`, `category`, `description`, `date`, `partyId`, `partyName`, `reference`, `paymentMethod`) SELECT t.`id`, t.`projectId`, t.`type`, t.`amount`, t.`category`, t.`description`, t.`date`, CASE WHEN t.`partyId` IS NULL OR EXISTS (SELECT 1 FROM `workers` w WHERE w.`id` = t.`partyId`) THEN t.`partyId` ELSE NULL END, t.`partyName`, t.`reference`, t.`paymentMethod` FROM `transactions` t WHERE EXISTS (SELECT 1 FROM `projects` p WHERE p.`id` = t.`projectId`)")
                db.execSQL("DROP TABLE `transactions`")
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_projectId` ON `transactions` (`projectId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_type` ON `transactions` (`type`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_date` ON `transactions` (`date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_partyId` ON `transactions` (`partyId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_category` ON `transactions` (`category`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `mom_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `projectId` INTEGER NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `date` TEXT NOT NULL, FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("INSERT INTO `mom_new` (`id`, `projectId`, `title`, `content`, `date`) SELECT m.`id`, m.`projectId`, m.`title`, m.`content`, m.`date` FROM `mom` m WHERE EXISTS (SELECT 1 FROM `projects` p WHERE p.`id` = m.`projectId`)")
                db.execSQL("DROP TABLE `mom`")
                db.execSQL("ALTER TABLE `mom_new` RENAME TO `mom`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_mom_projectId` ON `mom` (`projectId`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `payroll_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `workerId` INTEGER NOT NULL, `projectId` INTEGER NOT NULL, `date` TEXT NOT NULL, `wagesPaid` REAL NOT NULL, `status` TEXT NOT NULL, FOREIGN KEY(`workerId`) REFERENCES `workers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("INSERT INTO `payroll_new` (`id`, `workerId`, `projectId`, `date`, `wagesPaid`, `status`) SELECT pr.`id`, pr.`workerId`, pr.`projectId`, pr.`date`, pr.`wagesPaid`, pr.`status` FROM `payroll` pr WHERE EXISTS (SELECT 1 FROM `workers` w WHERE w.`id` = pr.`workerId`) AND EXISTS (SELECT 1 FROM `projects` p WHERE p.`id` = pr.`projectId`)")
                db.execSQL("DROP TABLE `payroll`")
                db.execSQL("ALTER TABLE `payroll_new` RENAME TO `payroll`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payroll_workerId` ON `payroll` (`workerId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payroll_projectId` ON `payroll` (`projectId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payroll_date` ON `payroll` (`date`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `estimates_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `projectId` INTEGER NOT NULL, `itemName` TEXT NOT NULL, `quantity` REAL NOT NULL, `unit` TEXT NOT NULL, `rate` REAL NOT NULL, `totalCost` REAL NOT NULL, FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("INSERT INTO `estimates_new` (`id`, `projectId`, `itemName`, `quantity`, `unit`, `rate`, `totalCost`) SELECT e.`id`, e.`projectId`, e.`itemName`, e.`quantity`, e.`unit`, e.`rate`, e.`totalCost` FROM `estimates` e WHERE EXISTS (SELECT 1 FROM `projects` p WHERE p.`id` = e.`projectId`)")
                db.execSQL("DROP TABLE `estimates`")
                db.execSQL("ALTER TABLE `estimates_new` RENAME TO `estimates`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_estimates_projectId` ON `estimates` (`projectId`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "construction_database"
                )
                .addMigrations(MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedDatabase(dao: ConstructionDao) {
            // Seeding disabled for production ready clean database
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

    suspend fun seedDatabase() {
        AppDatabase.seedDatabase(dao)
    }

    suspend fun insertProject(project: Project) = dao.insertProject(project)
    suspend fun deleteProject(project: Project) = dao.deleteProjectWithChildren(project)
    suspend fun deleteProjectById(projectId: Int) = dao.deleteProjectById(projectId)
    suspend fun updateProject(project: Project) = dao.updateProject(project)

    suspend fun insertWorker(worker: Worker) = dao.insertWorker(worker)
    suspend fun deleteWorker(worker: Worker) = dao.deleteWorkerWithChildren(worker)
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
