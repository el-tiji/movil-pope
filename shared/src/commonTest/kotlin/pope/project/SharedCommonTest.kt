package pope.project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import pope.project.data.fake.FakeJobRepository
import pope.project.domain.model.FinishResult
import pope.project.domain.model.HistoryEntry
import pope.project.domain.model.JobStatus
import pope.project.domain.model.StaffProfile
import pope.project.ui.screens.detail.JobDetailViewModel
import pope.project.ui.screens.profile.weeklyStats
import pope.project.ui.theme.resolveInitialNight
import pope.project.util.formatHours
import pope.project.util.headerFormat
import pope.project.util.shortAddress
import pope.project.util.shortFormat
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

private val tz = TimeZone.UTC
// Monday 2 Nov 2026, 11:00
private val fixedNow: Instant = LocalDateTime(2026, 11, 2, 11, 0).toInstant(tz)
private val fixedClock = object : Clock {
    override fun now(): Instant = fixedNow
}

class FakeJobRepositoryTest {
    private fun repo() = FakeJobRepository(fixedClock, tz)

    @Test
    fun seedMatchesReferenceDesign() {
        val jobs = repo().todayJobs.value
        assertEquals(listOf(JobStatus.DONE, JobStatus.ACTIVE, JobStatus.PENDING), jobs.map { it.status })
        assertEquals(listOf(8, 3, 0), jobs.map { it.completedTasks })
    }

    @Test
    fun startMovesPendingToActiveAndRecordsCheckIn() = runTest {
        val repo = repo()
        assertTrue(repo.startJob("job-apex"))
        val job = repo.todayJobs.value.first { it.id == "job-apex" }
        assertEquals(JobStatus.ACTIVE, job.status)
        assertEquals(fixedNow, job.timeLog.checkIn)
        assertFalse(repo.startJob("job-apex"), "cannot start twice")
    }

    @Test
    fun finishIsBlockedWhileTasksRemain() = runTest {
        val repo = repo()
        assertEquals(FinishResult.PendingTasks(5), repo.finishJob("job-nguyen"))
        assertEquals(FinishResult.NotStarted, repo.finishJob("job-apex"))
    }

    @Test
    fun finishingAllTasksCompletesJobAndAddsItToHistory() = runTest {
        val repo = repo()
        (3..7).forEach { repo.toggleTask("job-nguyen", it) }
        assertEquals(FinishResult.Completed, repo.finishJob("job-nguyen"))
        val job = repo.todayJobs.value.first { it.id == "job-nguyen" }
        assertEquals(JobStatus.DONE, job.status)
        assertNotNull(job.timeLog.checkOut)
        assertTrue(repo.history.first().any { it.jobId == "job-nguyen" })
    }

    @Test
    fun doneJobsChecklistIsLocked() = runTest {
        val repo = repo()
        repo.toggleTask("job-torres", 0)
        assertEquals(8, repo.todayJobs.value.first { it.id == "job-torres" }.completedTasks)
    }

    @Test
    fun incidentIsFlaggedOnJob() = runTest {
        val repo = repo()
        repo.markIncident("job-apex")
        assertTrue(repo.todayJobs.value.first { it.id == "job-apex" }.hasIncident)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class JobDetailViewModelTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun finishWithPendingTasksEmitsToast() = runTest {
        val vm = JobDetailViewModel("job-nguyen", FakeJobRepository(fixedClock, tz))
        val messages = mutableListOf<String>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { vm.messages.collect { messages += it } }
        vm.finish()
        assertEquals(listOf("Aún quedan 5 tareas pendientes"), messages)
        job.cancel()
    }
}

class ThemeAndFormatTest {
    @Test
    fun themeFollowsSavedValueThenHour() {
        assertTrue(resolveInitialNight("night", 10))
        assertFalse(resolveInitialNight("day", 23))
        assertFalse(resolveInitialNight(null, 6))
        assertFalse(resolveInitialNight(null, 17))
        assertTrue(resolveInitialNight(null, 18))
        assertTrue(resolveInitialNight(null, 5))
    }

    @Test
    fun spanishDates() {
        assertEquals("Lun 2 Nov", LocalDate(2026, 11, 2).headerFormat())
        assertEquals("01 Nov", LocalDate(2026, 11, 1).shortFormat())
        assertEquals("Mié 7 Ene", LocalDate(2026, 1, 7).headerFormat())
    }

    @Test
    fun hoursAndAddresses() {
        assertEquals("2 h", formatHours(2.0))
        assertEquals("2.5 h", formatHours(2.5))
        assertEquals("14 Loch St, Nedlands", "14 Loch St, Nedlands WA 6009".shortAddress())
    }

    @Test
    fun weeklyStatsCountOnlyCurrentWeek() {
        val today = LocalDate(2026, 11, 4) // Wednesday
        val history = listOf(
            HistoryEntry("a", "A", "x", LocalDate(2026, 11, 2), 2.0, false), // Mon
            HistoryEntry("b", "B", "x", LocalDate(2026, 11, 4), 1.5, false), // Wed
            HistoryEntry("c", "C", "x", LocalDate(2026, 11, 1), 3.0, false), // previous Sunday
        )
        assertEquals(3.5 to 2, weeklyStats(history, today))
    }

    @Test
    fun initials() {
        assertEquals("MR", StaffProfile("1", "María Rodríguez", "", "", "").initials)
    }
}
