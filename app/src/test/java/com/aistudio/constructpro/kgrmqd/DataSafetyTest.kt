package com.aistudio.constructpro.kgrmqd

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aistudio.constructpro.kgrmqd.data.AppDatabase
import com.aistudio.constructpro.kgrmqd.data.Attendance
import com.aistudio.constructpro.kgrmqd.data.BackupData
import com.aistudio.constructpro.kgrmqd.data.Project
import com.aistudio.constructpro.kgrmqd.data.Task
import com.aistudio.constructpro.kgrmqd.data.Transaction
import com.aistudio.constructpro.kgrmqd.data.Worker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DataSafetyTest {
    private var db: AppDatabase? = null

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db?.close()
    }

    @Test
    fun deletingProjectRemovesProjectChildren() = runBlocking {
        val dao = db!!.constructionDao()
        val projectId = dao.insertProject(Project(name = "Tower A", location = "Pune", budget = 1000.0, status = "Active")).toInt()
        val workerId = dao.insertWorker(Worker(name = "Ravi", role = "Mason", shift = "Day", wageRate = 500.0, avatarColor = 0)).toInt()
        dao.insertTask(Task(projectId = projectId, title = "Foundation", priority = "High", status = "To Do", dueDate = "2026-06-10", assignee = "Ravi"))
        dao.insertTransaction(Transaction(projectId = projectId, type = "Money Out", amount = 250.0, category = "Labor", description = "Advance", date = "2026-06-02", partyId = workerId, partyName = "Ravi"))
        dao.insertAttendance(Attendance(workerId = workerId, projectId = projectId, date = "2026-06-02", status = "Present", overtimeHours = 0.0))

        dao.deleteProjectWithChildren(Project(id = projectId, name = "Tower A", location = "Pune", budget = 1000.0, status = "Active"))

        assertTrue(dao.getAllProjects().first().isEmpty())
        assertTrue(dao.getAllTasks().first().isEmpty())
        assertTrue(dao.getAllTransactions().first().isEmpty())
        assertTrue(dao.getAllAttendance().first().isEmpty())
        assertEquals(1, dao.getAllWorkers().first().size)
    }

    @Test
    fun fullRestoreReplacesDataTransactionally() = runBlocking {
        val dao = db!!.constructionDao()
        val backup = BackupData(
            projects = listOf(Project(id = 7, name = "Market Complex", location = "Mumbai", budget = 5000.0, status = "Active")),
            workers = listOf(Worker(id = 4, name = "Anita", role = "Engineer", shift = "Day", wageRate = 1200.0, avatarColor = 1)),
            tasks = listOf(Task(id = 9, projectId = 7, title = "Survey", priority = "Medium", status = "Done", dueDate = "2026-06-05", assignee = "Anita")),
            transactions = listOf(Transaction(id = 11, projectId = 7, type = "Money In", amount = 10000.0, category = "Client Advance", description = "Initial payment", date = "2026-06-02", partyId = 4, partyName = "Anita"))
        )

        dao.replaceAllData(backup)

        assertEquals("Market Complex", dao.getAllProjects().first().single().name)
        assertEquals("Anita", dao.getAllWorkers().first().single().name)
        assertEquals("Survey", dao.getAllTasks().first().single().title)
        assertEquals(10000.0, dao.getAllTransactions().first().single().amount, 0.0)
    }
}
