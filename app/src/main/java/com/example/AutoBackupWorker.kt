package com.example

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.data.AppDatabase
import com.example.data.BackupData
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class BackupLogItem(
    val timestamp: Long,
    val dateString: String,
    val status: String,
    val details: String
)

object BackupLogger {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    
    @JsonClass(generateAdapter = true)
    data class LogListWrapper(val logs: List<BackupLogItem> = emptyList())
    
    private val listAdapter = moshi.adapter(LogListWrapper::class.java)

    fun readLogs(context: Context): List<BackupLogItem> {
        return try {
            val file = File(context.filesDir, "backup_logs.json")
            if (!file.exists()) return emptyList()
            val json = file.readText()
            listAdapter.fromJson(json)?.logs ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun addLog(context: Context, status: String, details: String) {
        try {
            val file = File(context.filesDir, "backup_logs.json")
            val currentLogs = readLogs(context).toMutableList()
            
            val newLog = BackupLogItem(
                timestamp = System.currentTimeMillis(),
                dateString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                status = status,
                details = details
            )
            
            currentLogs.add(0, newLog)
            
            // Limit activities to the last 7 days (last week) and keep at most the last 10 entries
            val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            val filteredLogs = currentLogs.filter { it.timestamp >= oneWeekAgo }
            val trimmedLogs = filteredLogs.take(10)
            
            val json = listAdapter.indent("  ").toJson(LogListWrapper(trimmedLogs))
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class UploadResult(val success: Boolean, val message: String)

class AutoBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("AutoBackupWorker", "Starting scheduled auto-backup to Google Drive...")
        val uploadRes = performBackupUpload(applicationContext)
        return if (uploadRes.success) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        suspend fun performBackupUpload(context: Context): UploadResult = withContext(Dispatchers.IO) {
            Log.d("AutoBackupWorker", "Starting auto-backup upload to Google Drive...")

            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                Log.w("AutoBackupWorker", "No Google account found. Skipping backup.")
                val msg = "Account not found. Please log in."
                BackupLogger.addLog(context, "Failure", msg)
                return@withContext UploadResult(false, msg)
            }

            val hasScope = GoogleSignIn.hasPermissions(
                account,
                com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/drive.file")
            )
            if (!hasScope) {
                Log.w("AutoBackupWorker", "Drive scope not consented. Skipping backup.")
                val msg = "Drive permission not consented."
                BackupLogger.addLog(context, "Failure", msg)
                return@withContext UploadResult(false, msg)
            }

            try {
                val scope = "oauth2:https://www.googleapis.com/auth/drive.file"
                val accountObj = account.account ?: android.accounts.Account(account.email ?: "", "com.google")
                
                val token = GoogleAuthUtil.getToken(context, accountObj, scope)
                if (token.isNullOrBlank()) {
                    Log.e("AutoBackupWorker", "Failed to retrieve OAuth token.")
                    val msg = "OAuth token retrieval failed."
                    BackupLogger.addLog(context, "Failure", msg)
                    return@withContext UploadResult(false, msg)
                }

                val dao = AppDatabase.getDatabase(context).constructionDao()
                val projects = dao.getAllProjects().first()
                val workers = dao.getAllWorkers().first()
                val tasks = dao.getAllTasks().first()
                val transactions = dao.getAllTransactions().first()
                val attendance = dao.getAllAttendance().first()
                val moms = dao.getAllMOMs().first()
                val payroll = dao.getAllPayroll().first()
                val estimates = dao.getAllEstimates().first()

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

                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val jsonString = moshi.adapter(BackupData::class.java).indent("  ").toJson(backupData)

                val dateStr = SimpleDateFormat("yyyy_MM_dd_HHmmss", Locale.US).format(Date())
                val fileName = "ConstructPro_AutoBackup_$dateStr.json"
                val mediaTypeJson = "application/json; charset=UTF-8".toMediaType()
                val mediaTypeRelated = "multipart/related".toMediaType()
                
                val metadata = """{"name": "$fileName", "mimeType": "application/json"}"""

                val requestBody = MultipartBody.Builder()
                    .setType(mediaTypeRelated)
                    .addPart(
                        RequestBody.create(mediaTypeJson, metadata)
                    )
                    .addPart(
                        RequestBody.create("application/json".toMediaType(), jsonString)
                    )
                    .build()

                val request = Request.Builder()
                    .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
                    .header("Authorization", "Bearer $token")
                    .post(requestBody)
                    .build()

                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val msg = "Synced file: $fileName"
                    Log.i("AutoBackupWorker", "Auto-backup successfully uploaded: $fileName")
                    response.close()
                    BackupLogger.addLog(context, "Success", msg)
                    UploadResult(true, msg)
                } else {
                    val errorBody = response.body?.string() ?: ""
                    var errorDetail = ""
                    try {
                        val keyword = "\"message\":"
                        val index = errorBody.indexOf(keyword)
                        if (index != -1) {
                            val start = errorBody.indexOf("\"", index + keyword.length)
                            if (start != -1) {
                                val end = errorBody.indexOf("\"", start + 1)
                                if (end != -1) {
                                    errorDetail = ": " + errorBody.substring(start + 1, end)
                                }
                            }
                        }
                    } catch (t: Throwable) {}
                    
                    val msg = "Drive API error ${response.code}$errorDetail"
                    Log.e("AutoBackupWorker", "Google Drive API upload failed: ${response.code} $errorBody")
                    response.close()
                    BackupLogger.addLog(context, "Failure", msg)
                    UploadResult(false, msg)
                }
            } catch (e: Exception) {
                val msg = "Error: ${e.localizedMessage ?: "Unknown error"}"
                Log.e("AutoBackupWorker", "Auto-backup execution failed", e)
                BackupLogger.addLog(context, "Failure", msg)
                UploadResult(false, msg)
            }
        }

        fun schedule(context: Context) {
            val prefs = context.getSharedPreferences("constructpro_prefs", Context.MODE_PRIVATE)
            val isEnabled = prefs.getBoolean("drive_auto_backup_enabled", false)
            if (!isEnabled) {
                WorkManager.getInstance(context).cancelUniqueWork("GoogleDriveAutoBackup")
                return
            }

            val frequency = prefs.getString("drive_auto_backup_frequency", "Daily") ?: "Daily"
            val customHours = prefs.getInt("drive_auto_backup_custom_hours", 12)

            val workRequest = when (frequency) {
                "Hourly" -> {
                    PeriodicWorkRequestBuilder<AutoBackupWorker>(1, TimeUnit.HOURS)
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .build()
                }
                "Weekly" -> {
                    val delay = calculateMidnightDelay()
                    PeriodicWorkRequestBuilder<AutoBackupWorker>(7, TimeUnit.DAYS)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .build()
                }
                "Custom" -> {
                    val hours = customHours.coerceAtLeast(1)
                    PeriodicWorkRequestBuilder<AutoBackupWorker>(hours.toLong(), TimeUnit.HOURS)
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .build()
                }
                else -> { // "Daily"
                    val delay = calculateMidnightDelay()
                    PeriodicWorkRequestBuilder<AutoBackupWorker>(24, TimeUnit.HOURS)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .build()
                }
            }

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "GoogleDriveAutoBackup",
                ExistingPeriodicWorkPolicy.UPDATE, // UPDATE to seamlessly apply new frequency/constraints
                workRequest
            )
        }

        private fun calculateMidnightDelay(): Long {
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis - now
        }
    }
}
