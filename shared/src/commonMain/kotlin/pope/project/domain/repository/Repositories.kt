package pope.project.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import pope.project.domain.model.DayAvailability
import pope.project.domain.model.FinishResult
import pope.project.domain.model.HistoryEntry
import pope.project.domain.model.Incident
import pope.project.domain.model.IncidentType
import pope.project.domain.model.Job
import pope.project.domain.model.PhotoStage
import pope.project.domain.model.StaffProfile
import pope.project.domain.model.TimeOffRequest

interface AuthRepository {
    val session: StateFlow<StaffProfile?>
    suspend fun login(email: String, password: String): Result<StaffProfile>
    fun logout()
}

interface JobRepository {
    val todayJobs: StateFlow<List<Job>>
    val history: Flow<List<HistoryEntry>>
    fun job(id: String): Flow<Job?>
    suspend fun toggleTask(jobId: String, taskId: Int)
    suspend fun startJob(jobId: String): Boolean
    suspend fun finishJob(jobId: String): FinishResult
    suspend fun addPhoto(jobId: String, stage: PhotoStage, bytes: ByteArray)
    suspend fun removePhoto(jobId: String, photoId: Long)
    suspend fun markIncident(jobId: String)
}

interface IncidentRepository {
    val incidents: StateFlow<List<Incident>>
    suspend fun report(
        jobId: String,
        type: IncidentType,
        description: String,
        photo: ByteArray?,
    ): Result<Incident>
}

interface ProfileRepository {
    val availability: StateFlow<List<DayAvailability>>
    val timeOffRequests: StateFlow<List<TimeOffRequest>>
    suspend fun setAvailable(day: DayOfWeek, available: Boolean)
    suspend fun setHours(day: DayOfWeek, from: LocalTime, to: LocalTime): Boolean
    suspend fun requestTimeOff(from: LocalDate, to: LocalDate, reason: String): Result<TimeOffRequest>
}
