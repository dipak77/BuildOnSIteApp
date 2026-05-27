package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ConstructPro", appName)
  }

  @Test
  fun `database initialization and seeding works correctly`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    val dao = db.constructionDao()

    // Manually trigger seed database to verify queries and schema constraints
    AppDatabase.seedDatabase(dao)

    val projects = dao.getAllProjects().first()
    assertTrue(projects.isNotEmpty())
    assertEquals("Skyline Corporate Tower", projects.find { it.name == "Skyline Corporate Tower" }?.name)

    val workers = dao.getAllWorkers().first()
    assertTrue(workers.isNotEmpty())
    assertEquals("John Carter", workers.find { it.name == "John Carter" }?.name)
  }
}

