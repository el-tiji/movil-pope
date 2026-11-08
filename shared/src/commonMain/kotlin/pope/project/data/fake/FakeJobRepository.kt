package pope.project.data.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn
import pope.project.domain.model.ChecklistTask
import pope.project.domain.model.FinishResult
import pope.project.domain.model.HistoryEntry
import pope.project.domain.model.Job
import pope.project.domain.model.JobPhoto
import pope.project.domain.model.JobStatus
import pope.project.domain.model.PhotoStage
import pope.project.domain.model.TimeLog
import pope.project.domain.repository.JobRepository
import kotlin.math.roundToInt
import kotlin.time.Clock

/** In-memory job store seeded with the jobs from the reference design. */
class FakeJobRepository(
    private val clock: Clock,
    private val timeZone: TimeZone,
) : JobRepository {

    private val today: LocalDate = clock.todayIn(timeZone)
    private val jobs = MutableStateFlow(seedJobs(today))
    private val pastHistory = seedHistory(today)
    private var photoIds = 0L

    override val todayJobs: StateFlow<List<Job>> = jobs.asStateFlow()

    override val history: Flow<List<HistoryEntry>> = jobs.map { list ->
        val todayDone = list.filter { it.status == JobStatus.DONE }
            .sortedByDescending { it.startTime }
            .map { it.toHistoryEntry() }
        todayDone + pastHistory
    }

    override fun job(id: String): Flow<Job?> = jobs.map { list -> list.find { it.id == id } }

    override suspend fun toggleTask(jobId: String, taskId: Int) = update(jobId) { job ->
        if (job.status == JobStatus.DONE) job
        else job.copy(tasks = job.tasks.map { if (it.id == taskId) it.copy(done = !it.done) else it })
    }

    override suspend fun startJob(jobId: String): Boolean {
        val job = jobs.value.find { it.id == jobId } ?: return false
        if (job.status != JobStatus.PENDING) return false
        update(jobId) {
            it.copy(status = JobStatus.ACTIVE, timeLog = it.timeLog.copy(checkIn = clock.now()))
        }
        return true
    }

    override suspend fun finishJob(jobId: String): FinishResult {
        val job = jobs.value.find { it.id == jobId } ?: return FinishResult.NotStarted
        if (job.status != JobStatus.ACTIVE) return FinishResult.NotStarted
        if (job.remainingTasks > 0) return FinishResult.PendingTasks(job.remainingTasks)
        update(jobId) {
            it.copy(status = JobStatus.DONE, timeLog = it.timeLog.copy(checkOut = clock.now()))
        }
        return FinishResult.Completed
    }

    override suspend fun addPhoto(jobId: String, stage: PhotoStage, bytes: ByteArray) = update(jobId) {
        it.copy(photos = it.photos + JobPhoto(++photoIds, stage, bytes))
    }

    override suspend fun removePhoto(jobId: String, photoId: Long) = update(jobId) {
        it.copy(photos = it.photos.filterNot { photo -> photo.id == photoId })
    }

    override suspend fun markIncident(jobId: String) = update(jobId) { it.copy(hasIncident = true) }

    private fun update(jobId: String, transform: (Job) -> Job) {
        jobs.update { list -> list.map { if (it.id == jobId) transform(it) else it } }
    }

    private fun Job.toHistoryEntry(): HistoryEntry {
        val checkIn = timeLog.checkIn
        val checkOut = timeLog.checkOut
        val hours = if (checkIn != null && checkOut != null) {
            ((checkOut - checkIn).inWholeMinutes / 6.0).roundToInt() / 10.0
        } else estimatedHours
        return HistoryEntry(id, client, address, date, hours, hasIncident)
    }

    private fun at(date: LocalDate, hour: Int, minute: Int) =
        LocalDateTime(date, LocalTime(hour, minute)).toInstant(timeZone)

    private fun seedJobs(today: LocalDate): List<Job> = listOf(
        Job(
            id = "job-torres",
            client = "Familia Torres",
            address = "14 Loch St, Nedlands WA 6009",
            date = today,
            startTime = LocalTime(8, 0),
            estimatedHours = 2.0,
            type = "Limpieza regular",
            notes = "Mascota en casa (gato). Traer aspiradora para pelos.",
            status = JobStatus.DONE,
            tasks = tasks(
                "Aspirar dormitorios (x2)", "Limpiar baño principal", "Limpiar cocina y electrodomésticos",
                "Fregar suelos", "Limpiar espejos y vidrios", "Vaciar papeleras", "Desinfectar encimeras",
                "Limpiar sala de estar", doneCount = 8,
            ),
            timeLog = TimeLog(checkIn = at(today, 8, 2), checkOut = at(today, 10, 5)),
        ),
        Job(
            id = "job-nguyen",
            client = "Sr. Nguyen",
            address = "8 Bay View Tce, Claremont WA 6010",
            date = today,
            startTime = LocalTime(10, 30),
            estimatedHours = 3.0,
            type = "Limpieza profunda",
            notes = "Spring clean. Incluye horno y refrigerador por dentro.",
            status = JobStatus.ACTIVE,
            tasks = tasks(
                "Limpiar horno (interior)", "Limpiar refrigerador (interior)", "Aspirar toda la casa",
                "Fregar todos los suelos", "Limpiar baños (x2)", "Limpiar cocina a fondo",
                "Desinfectar encimeras", "Limpiar ventanas interiores", doneCount = 3,
            ),
            timeLog = TimeLog(checkIn = at(today, 10, 32)),
        ),
        Job(
            id = "job-apex",
            client = "Oficina Apex Capital",
            address = "120 St Georges Tce, Perth WA 6000",
            date = today,
            startTime = LocalTime(14, 0),
            estimatedHours = 2.5,
            type = "Limpieza comercial",
            notes = "Acceso por recepción, pedir a Jamie. Cocina y 3 baños.",
            status = JobStatus.PENDING,
            tasks = tasks(
                "Limpiar baños (x3)", "Limpiar cocina / lunch room", "Aspirar alfombras zona recepción",
                "Desinfectar escritorios y superficies", "Vaciar papeleras (toda la oficina)",
                "Limpiar microondas", "Pasar mopa suelos duros", "Reponer papel higiénico y jabón",
                doneCount = 0,
            ),
        ),
    )

    private fun tasks(vararg labels: String, doneCount: Int) =
        labels.mapIndexed { i, label -> ChecklistTask(i, label, done = i < doneCount) }

    private fun seedHistory(today: LocalDate) = listOf(
        HistoryEntry("h-1", "Sra. Campbell", "55 Stirling Hwy, Nedlands WA 6009", today.minus(1, DateTimeUnit.DAY), 2.5, false),
        HistoryEntry("h-2", "Familia Torres", "14 Loch St, Nedlands WA 6009", today.minus(2, DateTimeUnit.DAY), 2.0, false),
        HistoryEntry("h-3", "Oficina Apex Capital", "120 St Georges Tce, Perth WA 6000", today.minus(4, DateTimeUnit.DAY), 3.0, false),
        HistoryEntry("h-4", "Sr. Nguyen", "8 Bay View Tce, Claremont WA 6010", today.minus(5, DateTimeUnit.DAY), 3.0, true),
    )
}
