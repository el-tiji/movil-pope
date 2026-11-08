package pope.project.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pope.project.domain.model.ChecklistTask
import pope.project.domain.model.HistoryEntry
import pope.project.domain.model.Job
import pope.project.domain.model.JobStatus
import pope.project.ui.icons.PopeIcons
import pope.project.ui.theme.PopeColors
import pope.project.ui.theme.PopeTheme
import pope.project.util.formatHours
import pope.project.util.hhmm
import pope.project.util.shortAddress
import pope.project.util.shortFormat

fun PopeColors.statusColor(status: JobStatus): Color = when (status) {
    JobStatus.PENDING -> accent
    JobStatus.ACTIVE -> warn
    JobStatus.DONE -> ok
}

@Composable
fun StatusChip(status: JobStatus, modifier: Modifier = Modifier) = when (status) {
    JobStatus.DONE -> PopeChip("Completado", ChipVariant.OK, modifier, icon = PopeIcons.Check)
    JobStatus.ACTIVE -> PopeChip("En progreso", ChipVariant.WARN, modifier, icon = PopeIcons.Play)
    JobStatus.PENDING -> PopeChip("Pendiente", ChipVariant.NEUTRAL, modifier)
}

/* ─── .job-card ─── */
@Composable
fun JobCard(job: Job, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = PopeTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(16.dp)
    val statusColor = c.statusColor(job.status)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.surface)
            .border(if (pressed) 2.dp else 1.dp, if (pressed) c.accent else c.border, shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    ) {
        Box(Modifier.fillMaxWidth().height(4.dp).background(statusColor))
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        job.startTime.hhmm(),
                        fontFamily = PopeTheme.mono,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = c.fgMid,
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        job.client,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.fg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(job.address, fontSize = 13.sp, color = c.fgMid, modifier = Modifier.padding(top = 2.dp))
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusChip(job.status)
                PopeChip("${formatHours(job.estimatedHours)} estimadas")
                if (job.status == JobStatus.ACTIVE) PopeChip("${job.completedTasks} / ${job.totalTasks} tareas")
                if (job.hasIncident) PopeChip("Incidencia", ChipVariant.DANGER)
            }
        }
    }
}

/* ─── .check-progress ─── */
@Composable
fun ChecklistProgress(done: Int, total: Int, modifier: Modifier = Modifier) {
    val c = PopeTheme.colors
    val fraction by animateFloatAsState(if (total == 0) 0f else done.toFloat() / total)
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f).height(6.dp).clip(CircleShape).background(c.border)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).clip(CircleShape).background(c.ok))
        }
        Text("$done / $total", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.fgMid)
    }
}

/* ─── .check-item ─── */
@Composable
fun ChecklistItem(task: ChecklistTask, onToggle: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier) {
    val c = PopeTheme.colors
    val boxBg by animateColorAsState(if (task.done) c.ok else Color.Transparent)
    val boxBorder by animateColorAsState(if (task.done) c.ok else c.border)
    val boxShape = RoundedCornerShape(6.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(22.dp).clip(boxShape).background(boxBg).border(2.dp, boxBorder, boxShape),
            contentAlignment = Alignment.Center,
        ) {
            if (task.done) Icon(PopeIcons.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
        }
        Text(
            task.label,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            color = if (task.done) c.fgDim else c.fg,
            textDecoration = if (task.done) TextDecoration.LineThrough else null,
        )
    }
}

/* ─── .hist-card ─── */
@Composable
fun HistoryCard(entry: HistoryEntry, modifier: Modifier = Modifier) {
    val c = PopeTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.surface)
            .border(1.dp, c.border, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (entry.hadIncident) {
                PopeChip("!", ChipVariant.DANGER, compact = true)
            } else {
                PopeChip("", ChipVariant.OK, compact = true, icon = PopeIcons.Check)
            }
            Text(entry.date.shortFormat(), fontFamily = PopeTheme.mono, fontSize = 12.sp, color = c.fgDim)
        }
        Column(Modifier.weight(1f)) {
            Text(entry.client, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c.fg)
            val detail = if (entry.hadIncident) "Incidencia reportada" else formatHours(entry.hoursWorked)
            Text(
                "${entry.address.shortAddress()} — $detail",
                fontSize = 12.sp,
                color = c.fgMid,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
