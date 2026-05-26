package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object DataIO {

    // ==========================================
    // 1. CSV EXPORT FOR TRANSACTIONS
    // ==========================================
    fun exportTransactionsCSV(context: Context, transactions: List<Transaction>, projectName: String) {
        try {
            val csvHeader = "ID,Project,Type,Amount,Category,Description,Date\n"
            val csvBody = transactions.joinToString("\n") { t ->
                "${t.id},\"${projectName.replace("\"", "\"\"")}\",${t.type},${t.amount},\"${t.category.replace("\"", "\"\"")}\",\"${t.description.replace("\"", "\"\"")}\",${t.date}"
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
            val root = JSONObject()

            // Projects Array
            val projectsArray = JSONArray()
            projects.forEach { p ->
                val pObj = JSONObject()
                pObj.put("id", p.id)
                pObj.put("name", p.name)
                pObj.put("location", p.location)
                pObj.put("budget", p.budget)
                pObj.put("status", p.status)
                projectsArray.put(pObj)
            }
            root.put("projects", projectsArray)

            // Workers Array
            val workersArray = JSONArray()
            workers.forEach { w ->
                val wObj = JSONObject()
                wObj.put("id", w.id)
                wObj.put("name", w.name)
                wObj.put("role", w.role)
                wObj.put("shift", w.shift)
                wObj.put("wageRate", w.wageRate)
                wObj.put("avatarColor", w.avatarColor)
                workersArray.put(wObj)
            }
            root.put("workers", workersArray)

            // Tasks Array
            val tasksArray = JSONArray()
            tasks.forEach { t ->
                val tObj = JSONObject()
                tObj.put("id", t.id)
                tObj.put("projectId", t.projectId)
                tObj.put("title", t.title)
                tObj.put("priority", t.priority)
                tObj.put("status", t.status)
                tObj.put("dueDate", t.dueDate)
                tObj.put("assignee", t.assignee)
                tasksArray.put(tObj)
            }
            root.put("tasks", tasksArray)

            // Transactions Array
            val txArray = JSONArray()
            transactions.forEach { tx ->
                val txObj = JSONObject()
                txObj.put("id", tx.id)
                txObj.put("projectId", tx.projectId)
                txObj.put("type", tx.type)
                txObj.put("amount", tx.amount)
                txObj.put("category", tx.category)
                txObj.put("description", tx.description)
                txObj.put("date", tx.date)
                txArray.put(txObj)
            }
            root.put("transactions", txArray)

            // Attendance Array
            val attArray = JSONArray()
            attendance.forEach { a ->
                val aObj = JSONObject()
                aObj.put("id", a.id)
                aObj.put("workerId", a.workerId)
                aObj.put("projectId", a.projectId)
                aObj.put("date", a.date)
                aObj.put("status", a.status)
                aObj.put("overtimeHours", a.overtimeHours)
                attArray.put(aObj)
            }
            root.put("attendance", attArray)

            // MOMs Array
            val mArray = JSONArray()
            moms.forEach { m ->
                val mObj = JSONObject()
                mObj.put("id", m.id)
                mObj.put("projectId", m.projectId)
                mObj.put("title", m.title)
                mObj.put("content", m.content)
                mObj.put("date", m.date)
                mArray.put(mObj)
            }
            root.put("moms", mArray)

            // Payroll Array
            val pyArray = JSONArray()
            payroll.forEach { py ->
                val pyObj = JSONObject()
                pyObj.put("id", py.id)
                pyObj.put("workerId", py.workerId)
                pyObj.put("projectId", py.projectId)
                pyObj.put("date", py.date)
                pyObj.put("wagesPaid", py.wagesPaid)
                pyObj.put("status", py.status)
                pyArray.put(pyObj)
            }
            root.put("payroll", pyArray)

            // Estimates Array
            val estArray = JSONArray()
            estimates.forEach { est ->
                val estObj = JSONObject()
                estObj.put("id", est.id)
                estObj.put("projectId", est.projectId)
                estObj.put("itemName", est.itemName)
                estObj.put("quantity", est.quantity)
                estObj.put("unit", est.unit)
                estObj.put("rate", est.rate)
                estObj.put("totalCost", est.totalCost)
                estArray.put(estObj)
            }
            root.put("estimates", estArray)

            val jsonString = root.toString(2)
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
            val root = JSONObject(jsonString)

            // Import Projects
            if (root.has("projects")) {
                val array = root.getJSONArray("projects")
                for (i in 0 until array.length()) {
                    val p = array.getJSONObject(i)
                    dao.insertProject(
                        Project(
                            id = p.optInt("id", 0),
                            name = p.getString("name"),
                            location = p.getString("location"),
                            budget = p.getDouble("budget"),
                            status = p.getString("status")
                        )
                    )
                }
            }

            // Import Workers
            if (root.has("workers")) {
                val array = root.getJSONArray("workers")
                for (i in 0 until array.length()) {
                    val w = array.getJSONObject(i)
                    dao.insertWorker(
                        Worker(
                            id = w.optInt("id", 0),
                            name = w.getString("name"),
                            role = w.getString("role"),
                            shift = w.getString("shift"),
                            wageRate = w.getDouble("wageRate"),
                            avatarColor = w.getInt("avatarColor")
                        )
                    )
                }
            }

            // Import Tasks
            if (root.has("tasks")) {
                val array = root.getJSONArray("tasks")
                for (i in 0 until array.length()) {
                    val t = array.getJSONObject(i)
                    dao.insertTask(
                        Task(
                            id = t.optInt("id", 0),
                            projectId = t.getInt("projectId"),
                            title = t.getString("title"),
                            priority = t.getString("priority"),
                            status = t.getString("status"),
                            dueDate = t.getString("dueDate"),
                            assignee = t.getString("assignee")
                        )
                    )
                }
            }

            // Import Transactions
            if (root.has("transactions")) {
                val array = root.getJSONArray("transactions")
                for (i in 0 until array.length()) {
                    val tx = array.getJSONObject(i)
                    dao.insertTransaction(
                        Transaction(
                            id = tx.optInt("id", 0),
                            projectId = tx.getInt("projectId"),
                            type = tx.getString("type"),
                            amount = tx.getDouble("amount"),
                            category = tx.getString("category"),
                            description = tx.getString("description"),
                            date = tx.getString("date")
                        )
                    )
                }
            }

            // Import Attendance
            if (root.has("attendance")) {
                val array = root.getJSONArray("attendance")
                for (i in 0 until array.length()) {
                    val a = array.getJSONObject(i)
                    dao.insertAttendance(
                        Attendance(
                            id = a.optInt("id", 0),
                            workerId = a.getInt("workerId"),
                            projectId = a.getInt("projectId"),
                            date = a.getString("date"),
                            status = a.getString("status"),
                            overtimeHours = a.getDouble("overtimeHours")
                        )
                    )
                }
            }

            // Import MOMs
            if (root.has("moms")) {
                val array = root.getJSONArray("moms")
                for (i in 0 until array.length()) {
                    val m = array.getJSONObject(i)
                    dao.insertMOM(
                        MOM(
                            id = m.optInt("id", 0),
                            projectId = m.getInt("projectId"),
                            title = m.getString("title"),
                            content = m.getString("content"),
                            date = m.getString("date")
                        )
                    )
                }
            }

            // Import Payroll
            if (root.has("payroll")) {
                val array = root.getJSONArray("payroll")
                for (i in 0 until array.length()) {
                    val py = array.getJSONObject(i)
                    dao.insertPayroll(
                        Payroll(
                            id = py.optInt("id", 0),
                            workerId = py.getInt("workerId"),
                            projectId = py.getInt("projectId"),
                            date = py.getString("date"),
                            wagesPaid = py.getDouble("wagesPaid"),
                            status = py.getString("status")
                        )
                    )
                }
            }

            // Import Estimates
            if (root.has("estimates")) {
                val array = root.getJSONArray("estimates")
                for (i in 0 until array.length()) {
                    val est = array.getJSONObject(i)
                    dao.insertEstimate(
                        Estimate(
                            id = est.optInt("id", 0),
                            projectId = est.getInt("projectId"),
                            itemName = est.getString("itemName"),
                            quantity = est.getDouble("quantity"),
                            unit = est.getString("unit"),
                            rate = est.getDouble("rate"),
                            totalCost = est.getDouble("totalCost")
                        )
                    )
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
