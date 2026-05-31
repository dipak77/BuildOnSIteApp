package com.example

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.data.AppDatabase
import com.example.data.BackupData
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AutoBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("AutoBackupWorker", "Starting daily auto-backup to Google Drive...")

        val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
        if (account == null) {
            Log.w("AutoBackupWorker", "No Google account found. Skipping backup.")
            return Result.failure()
        }

        return try {
            val scope = "oauth2:https://www.googleapis.com/auth/drive.file"
            val accountObj = account.account ?: android.accounts.Account(account.email ?: "", "com.google")
            
            val token = GoogleAuthUtil.getToken(applicationContext, accountObj, scope)
            if (token.isNullOrBlank()) {
                Log.e("AutoBackupWorker", "Failed to retrieve OAuth token.")
                return Result.failure()
            }

            val dao = AppDatabase.getDatabase(applicationContext).constructionDao()
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

            val dateStr = SimpleDateFormat("yyyy_MM_dd", Locale.US).format(Date())
            val fileName = "ConstructPro_AutoBackup_$dateStr.json"
            val mediaTypeJson = "application/json; charset=UTF-8".toMediaType()
            
            val metadata = """{"name": "$fileName", "mimeType": "application/json"}"""

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(
                    Headers.Builder().add("Content-Type", "application/json; charset=UTF-8").build(),
                    RequestBody.create(mediaTypeJson, metadata)
                )
                .addPart(
                    Headers.Builder().add("Content-Type", "application/json").build(),
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
                Log.i("AutoBackupWorker", "Auto-backup successfully uploaded: $fileName")
                response.close()
                Result.success()
            } else {
                val errorBody = response.body?.string() ?: ""
                Log.e("AutoBackupWorker", "Google Drive API upload failed: ${response.code} $errorBody")
                response.close()
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("AutoBackupWorker", "Auto-backup execution failed", e)
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            val initialDelay = calendar.timeInMillis - now
            
            Log.d("AutoBackupWorker", "Scheduling nightly auto-backup. Initial delay: ${initialDelay / 1000}s")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val backupWorkRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "GoogleDriveAutoBackup",
                ExistingPeriodicWorkPolicy.KEEP,
                backupWorkRequest
            )
        }
    }
}
