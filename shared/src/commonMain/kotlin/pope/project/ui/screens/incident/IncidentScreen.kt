package pope.project.ui.screens.incident

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pope.project.domain.model.IncidentType
import pope.project.domain.repository.IncidentRepository
import pope.project.domain.repository.JobRepository
import pope.project.platform.rememberCameraLauncher
import pope.project.ui.components.BackLink
import pope.project.ui.components.ButtonVariant
import pope.project.ui.components.FormField
import pope.project.ui.components.LocalToast
import pope.project.ui.components.OptionGrid
import pope.project.ui.components.PopeButton
import pope.project.ui.components.PopeCard
import pope.project.ui.components.PopeTextField
import pope.project.ui.components.dashedBorder
import pope.project.ui.icons.PopeIcons
import pope.project.ui.screens.detail.PhotoThumbnail
import pope.project.ui.theme.PopeTheme

class IncidentUiState(
    val clientName: String = "",
    val type: IncidentType = IncidentType.BROKEN_ITEM,
    val description: String = "",
    val photo: ByteArray? = null,
    val isSubmitting: Boolean = false,
    val showErrors: Boolean = false,
) {
    val descriptionError: String?
        get() = if (showErrors && description.isBlank()) "Describe brevemente qué ocurrió" else null

    fun copy(
        clientName: String = this.clientName,
        type: IncidentType = this.type,
        description: String = this.description,
        photo: ByteArray? = this.photo,
        isSubmitting: Boolean = this.isSubmitting,
        showErrors: Boolean = this.showErrors,
    ) = IncidentUiState(clientName, type, description, photo, isSubmitting, showErrors)
}

class IncidentViewModel(
    private val jobId: String,
    private val incidentRepository: IncidentRepository,
    jobRepository: JobRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(IncidentUiState())
    val state: StateFlow<IncidentUiState> = _state.asStateFlow()

    private val _submitted = Channel<Unit>(Channel.BUFFERED)
    val submitted: Flow<Unit> = _submitted.receiveAsFlow()

    init {
        viewModelScope.launch {
            val job = jobRepository.job(jobId).first()
            _state.update { it.copy(clientName = job?.client.orEmpty()) }
        }
    }

    fun selectType(type: IncidentType) = _state.update { it.copy(type = type) }
    fun onDescriptionChange(text: String) = _state.update { it.copy(description = text) }
    fun setPhoto(bytes: ByteArray?) = _state.update { it.copy(photo = bytes) }

    fun submit() {
        val current = _state.value
        if (current.isSubmitting) return
        if (current.description.isBlank()) {
            _state.update { it.copy(showErrors = true) }
            return
        }
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            incidentRepository.report(jobId, current.type, current.description, current.photo)
            _state.update { it.copy(isSubmitting = false) }
            _submitted.send(Unit)
        }
    }
}

@Composable
fun IncidentScreen(
    jobId: String,
    onClose: () -> Unit,
    viewModel: IncidentViewModel = koinViewModel(key = "incident-$jobId") { parametersOf(jobId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toast = LocalToast.current
    LaunchedEffect(viewModel) {
        viewModel.submitted.collect {
            toast.show("Incidencia enviada al supervisor")
            onClose()
        }
    }
    val camera = rememberCameraLauncher { bytes -> if (bytes != null) viewModel.setPhoto(bytes) }

    IncidentContent(
        state = state,
        onBack = onClose,
        onSelectType = viewModel::selectType,
        onDescriptionChange = viewModel::onDescriptionChange,
        onAttachPhoto = camera::launch,
        onRemovePhoto = { viewModel.setPhoto(null) },
        onSubmit = viewModel::submit,
    )
}

@Composable
fun IncidentContent(
    state: IncidentUiState,
    onBack: () -> Unit,
    onSelectType: (IncidentType) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAttachPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onSubmit: () -> Unit,
) {
    val c = PopeTheme.colors
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        BackLink("Volver al trabajo", onBack)
        PopeCard(Modifier.padding(horizontal = 16.dp)) {
            Column {
                Text("Reportar incidencia", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.fg)
                if (state.clientName.isNotEmpty()) Text(state.clientName, fontSize = 13.sp, color = c.fgMid)
            }
            FormField("Tipo de incidencia") {
                OptionGrid(
                    options = IncidentType.entries,
                    selected = state.type,
                    label = { it.label },
                    onSelect = onSelectType,
                )
            }
            FormField("Descripción") {
                PopeTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    placeholder = "Describe qué ocurrió, dónde y cómo…",
                    singleLine = false,
                    minHeight = 96.dp,
                    isError = state.descriptionError != null,
                )
                state.descriptionError?.let { Text(it, color = c.danger, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            }
            FormField("Foto (opcional)") {
                val photo = state.photo
                if (photo == null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .dashedBorder(c.border)
                            .clickable(onClick = onAttachPhoto)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(PopeIcons.Camera, contentDescription = null, tint = c.fgMid, modifier = Modifier.size(20.dp))
                        Text("Adjuntar foto", fontSize = 14.sp, color = c.fgMid)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        PhotoThumbnail(photo, Modifier.size(88.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Cambiar foto", color = c.accent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                modifier = Modifier.clickable(onClick = onAttachPhoto))
                            Text("Quitar", color = c.danger, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                modifier = Modifier.clickable(onClick = onRemovePhoto))
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PopeButton("Cancelar", onBack, Modifier.weight(1f), ButtonVariant.SECONDARY)
            PopeButton(
                "Enviar reporte",
                onSubmit,
                variant = ButtonVariant.DANGER_SOFT,
                icon = PopeIcons.Send,
                loading = state.isSubmitting,
            )
        }
    }
}
