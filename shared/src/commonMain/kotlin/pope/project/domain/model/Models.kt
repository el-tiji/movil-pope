package pope.project.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Instant

enum class JobStatus { PENDING, ACTIVE, DONE }

data class ChecklistTask(
    val id: Int,
    val label: String,
    val done: Boolean = false,
)

enum class PhotoStage { BEFORE, AFTER }

/** Photo taken by the staff member. Plain class (not data) because it wraps a ByteArray. */
class JobPhoto(
    val id: Long,
    val stage: PhotoStage,
    val bytes: ByteArray,
)

data class GeoPoint(val latitude: Double, val longitude: Double)

data class TimeLog(
    val checkIn: Instant? = null,
    val checkOut: Instant? = null,
    /** Reserved for GPS verification of the check-in (not implemented yet). */
    val checkInLocation: GeoPoint? = null,
)

data class Job(
    val id: String,
    val client: String,
    val address: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val estimatedHours: Double,
    val type: String,
    val notes: String?,
    val status: JobStatus,
    val tasks: List<ChecklistTask>,
    val timeLog: TimeLog = TimeLog(),
    val photos: List<JobPhoto> = emptyList(),
    val hasIncident: Boolean = false,
) {
    val completedTasks: Int get() = tasks.count { it.done }
    val totalTasks: Int get() = tasks.size
    val remainingTasks: Int get() = totalTasks - completedTasks
    val progress: Float get() = if (totalTasks == 0) 0f else completedTasks.toFloat() / totalTasks
}

enum class IncidentType(val label: String) {
    BROKEN_ITEM("Objeto roto"),
    ACCESS_DENIED("Acceso negado"),
    PRE_EXISTING_DAMAGE("Daño preexistente"),
    MISSING_PRODUCT("Producto faltante"),
}

class Incident(
    val id: String,
    val jobId: String,
    val type: IncidentType,
    val description: String,
    val photo: ByteArray?,
    val createdAt: Instant,
)

data class HistoryEntry(
    val jobId: String,
    val client: String,
    val address: String,
    val date: LocalDate,
    val hoursWorked: Double,
    val hadIncident: Boolean,
)

data class StaffProfile(
    val id: String,
    val fullName: String,
    val role: String,
    val email: String,
    val phone: String,
) {
    val initials: String
        get() = fullName.split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }
}

data class DayAvailability(
    val day: DayOfWeek,
    val available: Boolean,
    val from: LocalTime,
    val to: LocalTime,
)

enum class TimeOffStatus(val label: String) {
    PENDING("Pendiente"),
    APPROVED("Aprobada"),
    REJECTED("Rechazada"),
}

data class TimeOffRequest(
    val id: String,
    val from: LocalDate,
    val to: LocalDate,
    val reason: String,
    val status: TimeOffStatus,
)

sealed interface FinishResult {
    data object Completed : FinishResult
    data object NotStarted : FinishResult
    data class PendingTasks(val remaining: Int) : FinishResult
}
