package pope.project.ui.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pope.project.domain.model.Job
import pope.project.domain.model.JobPhoto
import pope.project.domain.model.JobStatus
import pope.project.domain.model.PhotoStage
import pope.project.platform.rememberCameraLauncher
import pope.project.ui.components.BackLink
import pope.project.ui.components.ButtonVariant
import pope.project.ui.components.ChecklistItem
import pope.project.ui.components.ChecklistProgress
import pope.project.ui.components.DetailRow
import pope.project.ui.components.EmptyState
import pope.project.ui.components.LocalToast
import pope.project.ui.components.PopeButton
import pope.project.ui.components.PopeCard
import pope.project.ui.components.PopeDivider
import pope.project.ui.components.Section
import pope.project.ui.components.SectionLabel
import pope.project.ui.components.StatusChip
import pope.project.ui.components.dashedBorder
import pope.project.ui.icons.PopeIcons
import pope.project.ui.theme.PopeTheme
import pope.project.util.formatHours
import pope.project.util.hhmm

@Composable
fun JobDetailScreen(
    jobId: String,
    onBack: () -> Unit,
    onReportIncident: () -> Unit,
    viewModel: JobDetailViewModel = koinViewModel(key = "detail-$jobId") { parametersOf(jobId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toast = LocalToast.current
    LaunchedEffect(viewModel) { viewModel.messages.collect { toast.show(it) } }

    var pendingStage by remember { mutableStateOf(PhotoStage.BEFORE) }
    val camera = rememberCameraLauncher { bytes ->
        if (bytes != null) viewModel.addPhoto(pendingStage, bytes)
    }

    JobDetailContent(
        state = state,
        timeZone = koinInject(),
        onBack = onBack,
        onStart = viewModel::start,
        onFinish = viewModel::finish,
        onReportIncident = onReportIncident,
        onToggleTask = viewModel::toggleTask,
        onAddPhoto = { stage -> pendingStage = stage; camera.launch() },
        onRemovePhoto = viewModel::removePhoto,
    )
}

@Composable
fun JobDetailContent(
    state: JobDetailUiState,
    timeZone: TimeZone,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onFinish: () -> Unit,
    onReportIncident: () -> Unit,
    onToggleTask: (Int) -> Unit,
    onAddPhoto: (PhotoStage) -> Unit,
    onRemovePhoto: (Long) -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        BackLink("Mis trabajos", onBack)
        val job = state.job
        when {
            state.isLoading -> Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PopeTheme.colors.accent)
            }
            job == null -> EmptyState("Trabajo no encontrado", "Puede que haya sido reasignado.", PopeIcons.Note)
            else -> {
                JobInfoCard(job, timeZone, Modifier.padding(horizontal = 16.dp))
                ActionRow(job, onStart, onFinish, onReportIncident)
                Section(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)) {
                    SectionLabel("Checklist de limpieza")
                    ChecklistProgress(job.completedTasks, job.totalTasks)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        job.tasks.forEach { task ->
                            ChecklistItem(task, onToggle = { onToggleTask(task.id) }, enabled = state.canEditChecklist)
                        }
                    }
                }
                Section(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp)) {
                    SectionLabel("Fotos")
                    PhotoRow("Antes", job.photos.filter { it.stage == PhotoStage.BEFORE }, { onAddPhoto(PhotoStage.BEFORE) }, onRemovePhoto)
                    PhotoRow("Después", job.photos.filter { it.stage == PhotoStage.AFTER }, { onAddPhoto(PhotoStage.AFTER) }, onRemovePhoto)
                }
            }
        }
    }
}

@Composable
private fun JobInfoCard(job: Job, timeZone: TimeZone, modifier: Modifier = Modifier) {
    val c = PopeTheme.colors
    PopeCard(modifier) {
        Text(job.client, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.fg)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip(job.status)
            if (job.hasIncident) pope.project.ui.components.PopeChip("Incidencia reportada", pope.project.ui.components.ChipVariant.DANGER)
        }
        PopeDivider()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DetailRow(PopeIcons.Pin) { Text(job.address, fontSize = 14.sp, color = c.fgMid) }
            DetailRow(PopeIcons.Clock) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = c.fg, fontWeight = FontWeight.Bold)) { append(job.startTime.hhmm()) }
                        append(" · ${formatHours(job.estimatedHours)} estimadas")
                    },
                    fontSize = 14.sp,
                    color = c.fgMid,
                )
            }
            DetailRow(PopeIcons.Briefcase) { Text(job.type, fontSize = 14.sp, color = c.fgMid) }
            if (!job.notes.isNullOrBlank()) {
                DetailRow(PopeIcons.Note) {
                    Text(job.notes, fontSize = 13.sp, fontStyle = FontStyle.Italic, color = c.fgMid)
                }
            }
            if (job.timeLog.checkIn != null) {
                DetailRow(PopeIcons.LogIn) {
                    Text(
                        "Entrada ${job.timeLog.checkIn.hhmm(timeZone)} · Salida ${job.timeLog.checkOut?.hhmm(timeZone) ?: "—"}",
                        fontFamily = PopeTheme.mono,
                        fontSize = 13.sp,
                        color = c.fgMid,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionRow(job: Job, onStart: () -> Unit, onFinish: () -> Unit, onReportIncident: () -> Unit) {
    val c = PopeTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when (job.status) {
            JobStatus.PENDING -> PopeButton("Iniciar limpieza", onStart, Modifier.weight(1f), ButtonVariant.PRIMARY, PopeIcons.Play)
            JobStatus.ACTIVE -> PopeButton("Finalizar limpieza", onFinish, Modifier.weight(1f), ButtonVariant.OK, PopeIcons.Check)
            JobStatus.DONE -> Row(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.okBg)
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(PopeIcons.Check, contentDescription = null, tint = c.ok, modifier = Modifier.size(16.dp))
                Text("Trabajo completado", color = c.ok, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        if (job.status != JobStatus.DONE) {
            PopeButton("!", onReportIncident, variant = ButtonVariant.DANGER_SOFT)
        }
    }
}

@Composable
private fun PhotoRow(
    label: String,
    photos: List<JobPhoto>,
    onAdd: () -> Unit,
    onRemove: (Long) -> Unit,
) {
    val c = PopeTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.fgMid)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(photos, key = { it.id }) { photo ->
                Box(Modifier.size(76.dp)) {
                    PhotoThumbnail(photo.bytes, Modifier.size(76.dp))
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .clickable { onRemove(photo.id) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(PopeIcons.Close, contentDescription = "Eliminar foto", tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .dashedBorder(c.border)
                        .clickable(onClick = onAdd),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(PopeIcons.Camera, contentDescription = null, tint = c.fgMid, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Añadir", fontSize = 11.sp, color = c.fgMid, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PhotoThumbnail(bytes: ByteArray, modifier: Modifier = Modifier) {
    val bitmap: ImageBitmap? = remember(bytes) { runCatching { bytes.decodeToImageBitmap() }.getOrNull() }
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(PopeTheme.colors.accentLt)) {
        if (bitmap != null) {
            Image(bitmap, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
    }
}
