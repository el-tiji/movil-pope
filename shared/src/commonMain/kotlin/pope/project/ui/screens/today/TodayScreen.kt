package pope.project.ui.screens.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.compose.viewmodel.koinViewModel
import pope.project.domain.model.Job
import pope.project.domain.model.JobStatus
import pope.project.domain.repository.JobRepository
import pope.project.ui.components.EmptyState
import pope.project.ui.components.JobCard
import pope.project.ui.components.SectionLabel
import pope.project.ui.icons.PopeIcons

data class TodayUiState(val jobs: List<Job> = emptyList()) {
    val allDone: Boolean get() = jobs.isNotEmpty() && jobs.all { it.status == JobStatus.DONE }
}

class TodayViewModel(jobRepository: JobRepository) : ViewModel() {
    val state: StateFlow<TodayUiState> = jobRepository.todayJobs
        .map { jobs -> TodayUiState(jobs.sortedBy { it.startTime }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState(jobRepository.todayJobs.value))
}

@Composable
fun TodayScreen(onOpenJob: (String) -> Unit, viewModel: TodayViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TodayContent(state, onOpenJob)
}

@Composable
fun TodayContent(state: TodayUiState, onOpenJob: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            val n = state.jobs.size
            SectionLabel("Turno de hoy · $n ${if (n == 1) "trabajo" else "trabajos"}")
        }
        if (state.jobs.isEmpty()) {
            item {
                EmptyState("Sin trabajos hoy", "No tienes limpiezas asignadas. ¡Disfruta tu día!", PopeIcons.Calendar)
            }
        }
        items(state.jobs, key = { it.id }) { job ->
            JobCard(job, onClick = { onOpenJob(job.id) }, modifier = Modifier.animateItem())
        }
        if (state.allDone) {
            item {
                EmptyState("¡Turno completado!", "Has terminado todos los trabajos de hoy.", PopeIcons.Check)
            }
        }
    }
}
