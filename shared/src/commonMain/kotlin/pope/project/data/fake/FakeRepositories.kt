package pope.project.data.fake

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import pope.project.domain.model.DayAvailability
import pope.project.domain.model.Incident
import pope.project.domain.model.IncidentType
import pope.project.domain.model.StaffProfile
import pope.project.domain.model.TimeOffRequest
import pope.project.domain.model.TimeOffStatus
import pope.project.domain.repository.AuthRepository
import pope.project.domain.repository.IncidentRepository
import pope.project.domain.repository.JobRepository
import pope.project.domain.repository.ProfileRepository
import kotlin.time.Clock

/** Simulated network latency so loading states are visible. */
private const val FAKE_LATENCY_MS = 500L

class FakeAuthRepository(private val latencyMs: Long = FAKE_LATENCY_MS) : AuthRepository {
    private val _session = MutableStateFlow<StaffProfile?>(null)
    override val session: StateFlow<StaffProfile?> = _session.asStateFlow()

    override suspend fun login(email: String, password: String): Result<StaffProfile> {
        delay(latencyMs)
        if (!email.contains('@') || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Correo o contraseña no válidos"))
        }
        val profile = StaffProfile(
            id = "staff-maria",
            fullName = "María Rodríguez",
            role = "Limpiadora",
            email = email.trim(),
            phone = "+61 412 345 678",
        )
        _session.value = profile
        return Result.success(profile)
    }

    override fun logout() {
        _session.value = null
    }
}

class FakeIncidentRepository(
    private val jobRepository: JobRepository,
    private val clock: Clock,
    private val latencyMs: Long = FAKE_LATENCY_MS,
) : IncidentRepository {
    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    override val incidents: StateFlow<List<Incident>> = _incidents.asStateFlow()

    override suspend fun report(
        jobId: String,
        type: IncidentType,
        description: String,
        photo: ByteArray?,
    ): Result<Incident> {
        delay(latencyMs)
        val incident = Incident(
            id = "inc-${_incidents.value.size + 1}",
            jobId = jobId,
            type = type,
            description = description.trim(),
            photo = photo,
            createdAt = clock.now(),
        )
        _incidents.update { it + incident }
        jobRepository.markIncident(jobId)
        return Result.success(incident)
    }
}

class FakeProfileRepository(
    clock: Clock,
    timeZone: TimeZone,
    private val latencyMs: Long = FAKE_LATENCY_MS,
) : ProfileRepository {
    private val today = clock.todayIn(timeZone)

    private val _availability = MutableStateFlow(
        DayOfWeek.entries.map { day ->
            when (day) {
                DayOfWeek.SATURDAY -> DayAvailability(day, true, LocalTime(9, 0), LocalTime(13, 0))
                DayOfWeek.SUNDAY -> DayAvailability(day, false, LocalTime(9, 0), LocalTime(13, 0))
                else -> DayAvailability(day, true, LocalTime(8, 0), LocalTime(17, 0))
            }
        }
    )
    override val availability: StateFlow<List<DayAvailability>> = _availability.asStateFlow()

    private val _timeOff = MutableStateFlow(
        listOf(
            TimeOffRequest("to-1", today.plus(10, DateTimeUnit.DAY), today.plus(10, DateTimeUnit.DAY), "Cita médica", TimeOffStatus.APPROVED),
            TimeOffRequest("to-2", today.plus(20, DateTimeUnit.DAY), today.plus(24, DateTimeUnit.DAY), "Vacaciones familiares", TimeOffStatus.PENDING),
        )
    )
    override val timeOffRequests: StateFlow<List<TimeOffRequest>> = _timeOff.asStateFlow()

    override suspend fun setAvailable(day: DayOfWeek, available: Boolean) {
        _availability.update { list -> list.map { if (it.day == day) it.copy(available = available) else it } }
    }

    override suspend fun setHours(day: DayOfWeek, from: LocalTime, to: LocalTime): Boolean {
        if (from >= to) return false
        _availability.update { list -> list.map { if (it.day == day) it.copy(from = from, to = to) else it } }
        return true
    }

    override suspend fun requestTimeOff(from: LocalDate, to: LocalDate, reason: String): Result<TimeOffRequest> {
        if (to < from) return Result.failure(IllegalArgumentException("La fecha final es anterior a la inicial"))
        delay(latencyMs)
        val request = TimeOffRequest(
            id = "to-${_timeOff.value.size + 1}",
            from = from,
            to = to,
            reason = reason.trim().ifBlank { "Sin motivo" },
            status = TimeOffStatus.PENDING,
        )
        _timeOff.update { (it + request).sortedBy { r -> r.from } }
        return Result.success(request)
    }
}
