package com.aistudio.constructpro.kgrmqd.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.FileOutputStream

@JsonClass(generateAdapter = true)
data class BackupData(
    val projects: List<Project> = emptyList(),
    val workers: List<Worker> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val attendance: List<Attendance> = emptyList(),
    val moms: List<MOM> = emptyList(),
    val payroll: List<Payroll> = emptyList(),
    val estimates: List<Estimate> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ProjectBackupData(
    val project: Project,
    val workers: List<Worker> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val attendance: List<Attendance> = emptyList(),
    val moms: List<MOM> = emptyList(),
    val payroll: List<Payroll> = emptyList(),
    val estimates: List<Estimate> = emptyList()
)

object DataIO {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val backupAdapter = moshi.adapter(BackupData::class.java)
    private val projectBackupAdapter = moshi.adapter(ProjectBackupData::class.java)

    // ==========================================
    // 1. CSV EXPORT FOR TRANSACTIONS
    // ==========================================
    fun exportTransactionsCSV(context: Context, transactions: List<Transaction>, projectName: String) {
        try {
            val csvHeader = "ID,Project,Type,Amount,Category,Description,Date,Party,Reference,Payment Method\n"
            val csvBody = transactions.joinToString("\n") { t ->
                val pName = t.partyName ?: ""
                val ref = t.reference
                val payM = t.paymentMethod
                "${t.id},\"${projectName.replace("\"", "\"\"")}\",${t.type},${t.amount},\"${t.category.replace("\"", "\"\"")}\",\"${t.description.replace("\"", "\"\"")}\",${t.date},\"${pName.replace("\"", "\"\"")}\",\"${ref.replace("\"", "\"\"")}\",\"${payM.replace("\"", "\"\"")}\""
            }
            val csvContent = csvHeader + csvBody

            val fileName = "Transactions_${projectName.replace(" ", "_")}_${System.currentTimeMillis()}.csv"
            val cacheFile = File(context.cacheDir, fileName)
            FileOutputStream(cacheFile).use { out ->
                out.write(csvContent.toByteArray())
            }

            shareFile(context, cacheFile, "text/csv", "Share Transactions CSV")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // ==========================================
    // 2. EXPORT ALL DATABASE TO JSON
    // ==========================================
    fun exportBackupJSON(
        context: Context,
        projects: List<Project>,
        workers: List<Worker>,
        tasks: List<Task>,
        transactions: List<Transaction>,
        attendance: List<Attendance>,
        moms: List<MOM>,
        payroll: List<Payroll>,
        estimates: List<Estimate>
    ) {
        try {
            val backupData = BackupData(
                projects = projects,
                workers = workers,
                tasks = tasks,
                transactions = transactions,
                attendance = attendance,
                moms = moms,
                payroll = payroll,
                estimates = estimates
            )
            val jsonString = backupAdapter.indent("  ").toJson(backupData)
            val fileName = "ConstructPro_Backup_${System.currentTimeMillis()}.json"
            val cacheFile = File(context.cacheDir, fileName)
            FileOutputStream(cacheFile).use { out ->
                out.write(jsonString.toByteArray())
            }

            shareFile(context, cacheFile, "application/json", "Export Database Backup")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting database backup: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // ==========================================
    // 3. SYSTEM SHARE DISPATCHER
    // ==========================================
    private fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    // ==========================================
    // 4. IMPORT DATABASE WITH PARSING
    // ==========================================
    suspend fun importBackupJSON(
        jsonString: String,
        dao: ConstructionDao
    ): Boolean {
        return try {
            val backupData = backupAdapter.fromJson(jsonString) ?: return false
            dao.replaceAllData(backupData.sanitized())

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ==========================================
    // 5. EXPORT SINGLE PROJECT BACKUP TO JSON
    // ==========================================
    fun exportProjectBackupJSON(
        context: Context,
        project: Project,
        workers: List<Worker>,
        tasks: List<Task>,
        transactions: List<Transaction>,
        attendance: List<Attendance>,
        moms: List<MOM>,
        payroll: List<Payroll>,
        estimates: List<Estimate>
    ) {
        try {
            val projectBackup = ProjectBackupData(
                project = project,
                workers = workersForProject(workers = workers, transactions = transactions, attendance = attendance, payroll = payroll, projectId = project.id),
                tasks = tasks.filter { it.projectId == project.id },
                transactions = transactions.filter { it.projectId == project.id },
                attendance = attendance.filter { it.projectId == project.id },
                moms = moms.filter { it.projectId == project.id },
                payroll = payroll.filter { it.projectId == project.id },
                estimates = estimates.filter { it.projectId == project.id }
            )
            val jsonString = projectBackupAdapter.indent("  ").toJson(projectBackup)
            val fileName = "Project_${project.name.replace(" ", "_")}_Backup_${System.currentTimeMillis()}.json"
            val cacheFile = File(context.cacheDir, fileName)
            FileOutputStream(cacheFile).use { out ->
                out.write(jsonString.toByteArray())
            }

            shareFile(context, cacheFile, "application/json", "Export Project Database Backup")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting database project backup: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // ==========================================
    // 6. IMPORT SINGLE PROJECT BACKUP FROM JSON
    // ==========================================
    suspend fun importProjectBackupJSON(
        jsonString: String,
        dao: ConstructionDao
    ): Boolean {
        return try {
            val projectBackup = projectBackupAdapter.fromJson(jsonString) ?: return false
            val insertedProj = projectBackup.project
            val pId = dao.insertProject(
                Project(
                    name = insertedProj.name,
                    location = insertedProj.location,
                    budget = insertedProj.budget,
                    status = insertedProj.status,
                    customBackground = insertedProj.customBackground
                )
            ).toInt()

            val workerIdMap = mutableMapOf<Int, Int>()
            projectBackup.workers.forEach { worker ->
                val newId = dao.insertWorker(worker.copy(id = 0)).toInt()
                workerIdMap[worker.id] = newId
            }

            val updatedTasks = projectBackup.tasks.map { it.copy(id = 0, projectId = pId) }
            val updatedTransactions = projectBackup.transactions.map {
                it.copy(
                    id = 0,
                    projectId = pId,
                    partyId = it.partyId?.let { oldId -> workerIdMap[oldId] },
                    partyName = it.partyName
                )
            }
            val updatedAttendance = projectBackup.attendance.mapNotNull {
                val newWorkerId = workerIdMap[it.workerId] ?: return@mapNotNull null
                it.copy(id = 0, workerId = newWorkerId, projectId = pId)
            }
            val updatedMoms = projectBackup.moms.map { it.copy(id = 0, projectId = pId) }
            val updatedPayroll = projectBackup.payroll.mapNotNull {
                val newWorkerId = workerIdMap[it.workerId] ?: return@mapNotNull null
                it.copy(id = 0, workerId = newWorkerId, projectId = pId)
            }
            val updatedEstimates = projectBackup.estimates.map { it.copy(id = 0, projectId = pId) }

            if (updatedTasks.isNotEmpty()) dao.insertAllTasks(updatedTasks)
            if (updatedTransactions.isNotEmpty()) dao.insertAllTransactions(updatedTransactions)
            if (updatedAttendance.isNotEmpty()) dao.insertAllAttendance(updatedAttendance)
            if (updatedMoms.isNotEmpty()) dao.insertAllMOMs(updatedMoms)
            if (updatedPayroll.isNotEmpty()) dao.insertAllPayroll(updatedPayroll)
            if (updatedEstimates.isNotEmpty()) dao.insertAllEstimates(updatedEstimates)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun workersForProject(
        workers: List<Worker>,
        transactions: List<Transaction>,
        attendance: List<Attendance>,
        payroll: List<Payroll>,
        projectId: Int
    ): List<Worker> {
        val workerIds = buildSet {
            transactions.filter { it.projectId == projectId }.mapNotNullTo(this) { it.partyId }
            attendance.filter { it.projectId == projectId }.mapTo(this) { it.workerId }
            payroll.filter { it.projectId == projectId }.mapTo(this) { it.workerId }
        }
        return workers.filter { it.id in workerIds }
    }

    private fun BackupData.sanitized(): BackupData {
        val projectIds = projects.map { it.id }.toSet()
        val workerIds = workers.map { it.id }.toSet()
        return copy(
            tasks = tasks.filter { it.projectId in projectIds },
            transactions = transactions
                .filter { it.projectId in projectIds }
                .map { tx -> if (tx.partyId == null || tx.partyId in workerIds) tx else tx.copy(partyId = null) },
            attendance = attendance.filter { it.projectId in projectIds && it.workerId in workerIds },
            moms = moms.filter { it.projectId in projectIds },
            payroll = payroll.filter { it.projectId in projectIds && it.workerId in workerIds },
            estimates = estimates.filter { it.projectId in projectIds }
        )
    }
}
