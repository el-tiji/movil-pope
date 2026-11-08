package pope.project.ui.screens.history

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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.koin.compose.viewmodel.koinViewModel
import pope.project.domain.model.HistoryEntry
import pope.project.domain.repository.JobRepository
import pope.project.ui.components.EmptyState
import pope.project.ui.components.HistoryCard
import pope.project.ui.components.SectionLabel
import pope.project.ui.icons.PopeIcons
import kotlin.time.Clock

data class HistoryUiState(val entries: List<HistoryEntry> = emptyList())

class HistoryViewModel(jobRepository: JobRepository, clock: Clock, timeZone: TimeZone) : ViewModel() {
    private val since = clock.todayIn(timeZone).minus(7, DateTimeUnit.DAY)

    val state: StateFlow<HistoryUiState> = jobRepository.history
        .map { list -> HistoryUiState(list.filter { it.date >= since }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())
}

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HistoryContent(state)
}

@Composable
fun HistoryContent(state: HistoryUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SectionLabel("Últimos 7 días") }
        if (state.entries.isEmpty()) {
            item { EmptyState("Sin historial", "Aquí verás los trabajos que completes.", PopeIcons.Clock) }
        }
        items(state.entries, key = { it.jobId + it.date }) { entry -> HistoryCard(entry) }
    }
}
