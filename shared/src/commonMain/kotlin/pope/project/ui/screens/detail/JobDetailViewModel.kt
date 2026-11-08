package pope.project.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pope.project.domain.model.FinishResult
import pope.project.domain.model.Job
import pope.project.domain.model.JobStatus
import pope.project.domain.model.PhotoStage
import pope.project.domain.repository.JobRepository

data class JobDetailUiState(
    val isLoading: Boolean = true,
    val job: Job? = null,
) {
    val canEditChecklist: Boolean get() = job != null && job.status != JobStatus.DONE
}

class JobDetailViewModel(
    private val jobId: String,
    private val jobRepository: JobRepository,
) : ViewModel() {

    val state: StateFlow<JobDetailUiState> = jobRepository.job(jobId)
        .map { JobDetailUiState(isLoading = false, job = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JobDetailUiState())

    private val _messages = Channel<String>(Channel.BUFFERED)
    /** One-shot toast messages. */
    val messages: Flow<String> = _messages.receiveAsFlow()

    fun toggleTask(taskId: Int) {
        viewModelScope.launch { jobRepository.toggleTask(jobId, taskId) }
    }

    fun start() {
        viewModelScope.launch {
            if (jobRepository.startJob(jobId)) _messages.send("Limpieza iniciada · entrada registrada")
        }
    }

    fun finish() {
        viewModelScope.launch {
            when (val result = jobRepository.finishJob(jobId)) {
                FinishResult.Completed -> _messages.send("¡Trabajo completado!")
                FinishResult.NotStarted -> _messages.send("Inicia la limpieza primero")
                is FinishResult.PendingTasks -> _messages.send(
                    if (result.remaining == 1) "Aún queda 1 tarea pendiente"
                    else "Aún quedan ${result.remaining} tareas pendientes"
                )
            }
        }
    }

    fun addPhoto(stage: PhotoStage, bytes: ByteArray) {
        viewModelScope.launch {
            jobRepository.addPhoto(jobId, stage, bytes)
            _messages.send("Foto añadida")
        }
    }

    fun removePhoto(photoId: Long) {
        viewModelScope.launch { jobRepository.removePhoto(jobId, photoId) }
    }
}
