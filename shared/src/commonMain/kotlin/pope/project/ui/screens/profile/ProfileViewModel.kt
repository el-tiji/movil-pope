package pope.project.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import pope.project.domain.model.DayAvailability
import pope.project.domain.model.HistoryEntry
import pope.project.domain.model.StaffProfile
import pope.project.domain.model.TimeOffRequest
import pope.project.domain.repository.AuthRepository
import pope.project.domain.repository.JobRepository
import pope.project.domain.repository.ProfileRepository
import kotlin.time.Clock

data class ProfileUiState(
    val profile: StaffProfile? = null,
    val hoursThisWeek: Double = 0.0,
    val jobsThisWeek: Int = 0,
    val availability: List<DayAvailability> = emptyList(),
    val timeOffRequests: List<TimeOffRequest> = emptyList(),
    val isSubmittingTimeOff: Boolean = false,
)

/** Monday of the ISO week containing [date]. */
fun startOfWeek(date: LocalDate): LocalDate = date.minus(date.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)

fun weeklyStats(history: List<HistoryEntry>, today: LocalDate): Pair<Double, Int> {
    val monday = startOfWeek(today)
    val week = history.filter { it.date in monday..today }
    return week.sumOf { it.hoursWorked } to week.size
}

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    jobRepository: JobRepository,
    clock: Clock,
    timeZone: TimeZone,
) : ViewModel() {
    val today: LocalDate = clock.todayIn(timeZone)
    private val submitting = MutableStateFlow(false)

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    val state: StateFlow<ProfileUiState> = combine(
        authRepository.session,
        jobRepository.history,
        profileRepository.availability,
        profileRepository.timeOffRequests,
        submitting,
    ) { profile, history, availability, timeOff, isSubmitting ->
        val (hours, jobs) = weeklyStats(history, today)
        ProfileUiState(profile, hours, jobs, availability, timeOff, isSubmitting)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState(profile = authRepository.session.value))

    fun setAvailable(day: DayOfWeek, available: Boolean) {
        viewModelScope.launch { profileRepository.setAvailable(day, available) }
    }

    fun setHours(day: DayOfWeek, from: LocalTime, to: LocalTime) {
        viewModelScope.launch {
            if (!profileRepository.setHours(day, from, to)) {
                _messages.send("La hora de inicio debe ser anterior a la de fin")
            }
        }
    }

    fun requestTimeOff(from: LocalDate, to: LocalDate, reason: String, onDone: () -> Unit) {
        if (submitting.value) return
        submitting.value = true
        viewModelScope.launch {
            val result = profileRepository.requestTimeOff(from, to, reason)
            submitting.value = false
            result.onSuccess {
                _messages.send("Solicitud enviada")
                onDone()
            }.onFailure { _messages.send(it.message ?: "No se pudo enviar la solicitud") }
        }
    }

    fun logout() = authRepository.logout()
}
