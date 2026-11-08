package pope.project.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import pope.project.domain.model.DayAvailability
import pope.project.domain.model.TimeOffRequest
import pope.project.domain.model.TimeOffStatus
import pope.project.ui.components.Avatar
import pope.project.ui.components.ButtonVariant
import pope.project.ui.components.ChipVariant
import pope.project.ui.components.FormField
import pope.project.ui.components.LocalToast
import pope.project.ui.components.PopeButton
import pope.project.ui.components.PopeCard
import pope.project.ui.components.PopeChip
import pope.project.ui.components.PopeDivider
import pope.project.ui.components.PopeTextField
import pope.project.ui.components.SectionLabel
import pope.project.ui.icons.PopeIcons
import pope.project.ui.theme.LocalThemeController
import pope.project.ui.theme.PopeTheme
import pope.project.util.formatHours
import pope.project.util.hhmm
import pope.project.util.shortFormat
import pope.project.util.spanishName
import kotlin.time.Instant

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toast = LocalToast.current
    val theme = LocalThemeController.current
    LaunchedEffect(viewModel) { viewModel.messages.collect { toast.show(it) } }

    var showSheet by remember { mutableStateOf(false) }

    ProfileContent(
        state = state,
        isNight = theme.isNight,
        onNightChange = theme::updateNight,
        onToggleDay = viewModel::setAvailable,
        onChangeHours = viewModel::setHours,
        onNewTimeOff = { showSheet = true },
        onLogout = viewModel::logout,
    )

    if (showSheet) {
        TimeOffSheet(
            today = viewModel.today,
            isSubmitting = state.isSubmittingTimeOff,
            onDismiss = { showSheet = false },
            onSubmit = { from, to, reason -> viewModel.requestTimeOff(from, to, reason) { showSheet = false } },
        )
    }
}

@Composable
fun ProfileContent(
    state: ProfileUiState,
    isNight: Boolean,
    onNightChange: (Boolean) -> Unit,
    onToggleDay: (DayOfWeek, Boolean) -> Unit,
    onChangeHours: (DayOfWeek, LocalTime, LocalTime) -> Unit,
    onNewTimeOff: () -> Unit,
    onLogout: () -> Unit,
) {
    val c = PopeTheme.colors
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionLabel("Mi perfil")
        state.profile?.let { profile ->
            PopeCard {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(profile.initials, size = 56.dp)
                    Column(Modifier.weight(1f)) {
                        Text(profile.fullName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.fg)
                        Text(
                            "${profile.role} · Pope Cleaning".uppercase(),
                            fontSize = 11.sp, letterSpacing = 0.06.em, color = c.fgDim,
                        )
                    }
                }
                PopeDivider()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(profile.email, fontSize = 14.sp, color = c.fgMid)
                    Text(profile.phone, fontSize = 14.sp, color = c.fgMid, fontFamily = PopeTheme.mono)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Horas · semana", formatHours(state.hoursThisWeek), Modifier.weight(1f))
            StatTile("Trabajos · semana", state.jobsThisWeek.toString(), Modifier.weight(1f))
        }

        Spacer(Modifier.size(4.dp))
        SectionLabel("Disponibilidad semanal")
        PopeCard(padding = PaddingValues(vertical = 6.dp), spacing = 0.dp) {
            state.availability.forEachIndexed { index, day ->
                if (index > 0) PopeDivider()
                AvailabilityRow(day, onToggleDay, onChangeHours)
            }
        }

        Spacer(Modifier.size(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("Solicitudes de ausencia", Modifier.weight(1f))
            Row(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onNewTimeOff).padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(PopeIcons.Plus, contentDescription = null, tint = c.accent, modifier = Modifier.size(16.dp))
                Text("Nueva", color = c.accent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
        if (state.timeOffRequests.isEmpty()) {
            Text("No tienes solicitudes.", fontSize = 14.sp, color = c.fgMid)
        }
        state.timeOffRequests.forEach { TimeOffCard(it) }

        Spacer(Modifier.size(4.dp))
        SectionLabel("Preferencias")
        PopeCard(padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(if (isNight) PopeIcons.Moon else PopeIcons.Sun, contentDescription = null, tint = c.fgMid, modifier = Modifier.size(18.dp))
                Text("Modo noche", fontSize = 15.sp, color = c.fg, modifier = Modifier.weight(1f))
                PopeSwitch(isNight, onNightChange)
            }
        }

        PopeButton(
            "Cerrar sesión",
            onLogout,
            Modifier.fillMaxWidth().padding(top = 8.dp),
            ButtonVariant.DANGER_SOFT,
            PopeIcons.LogOut,
        )
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    val c = PopeTheme.colors
    PopeCard(modifier, radius = 16.dp, padding = PaddingValues(16.dp), spacing = 6.dp) {
        Text(label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08.em, color = c.fgDim, maxLines = 2)
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = c.fg)
    }
}

@Composable
private fun PopeSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val c = PopeTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = c.surface,
            checkedTrackColor = c.accent,
            checkedBorderColor = c.accent,
            uncheckedThumbColor = c.fgDim,
            uncheckedTrackColor = c.bg,
            uncheckedBorderColor = c.border,
        ),
    )
}

private val hourOptions = (5..22).map { LocalTime(it, 0) }

@Composable
private fun AvailabilityRow(
    day: DayAvailability,
    onToggle: (DayOfWeek, Boolean) -> Unit,
    onChangeHours: (DayOfWeek, LocalTime, LocalTime) -> Unit,
) {
    val c = PopeTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            day.day.spanishName(),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (day.available) c.fg else c.fgDim,
            modifier = Modifier.weight(1f),
        )
        if (day.available) {
            TimeChip(day.from) { onChangeHours(day.day, it, day.to) }
            Text("–", color = c.fgMid)
            TimeChip(day.to) { onChangeHours(day.day, day.from, it) }
        } else {
            Text("No disponible", fontSize = 13.sp, color = c.fgDim)
        }
        Spacer(Modifier.width(4.dp))
        PopeSwitch(day.available) { onToggle(day.day, it) }
    }
}

@Composable
private fun TimeChip(time: LocalTime, onSelect: (LocalTime) -> Unit) {
    val c = PopeTheme.colors
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(
            time.hhmm(),
            fontFamily = PopeTheme.mono,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = c.accent,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(c.accentLt)
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 5.dp),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = c.surface) {
            hourOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.hhmm(), fontFamily = PopeTheme.mono, color = c.fg) },
                    onClick = { expanded = false; onSelect(option) },
                )
            }
        }
    }
}

@Composable
private fun TimeOffCard(request: TimeOffRequest) {
    val c = PopeTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.surface)
            .border(1.dp, c.border, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            val range = if (request.from == request.to) request.from.shortFormat()
            else "${request.from.shortFormat()} – ${request.to.shortFormat()}"
            Text(range, fontFamily = PopeTheme.mono, fontSize = 13.sp, color = c.fgMid)
            Text(request.reason, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c.fg)
        }
        val variant = when (request.status) {
            TimeOffStatus.PENDING -> ChipVariant.WARN
            TimeOffStatus.APPROVED -> ChipVariant.OK
            TimeOffStatus.REJECTED -> ChipVariant.DANGER
        }
        PopeChip(request.status.label, variant)
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
private fun Long.utcMillisToDate(): LocalDate = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date

@Composable
private fun TimeOffSheet(
    today: LocalDate,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (LocalDate, LocalDate, String) -> Unit,
) {
    val c = PopeTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var from by remember { mutableStateOf<LocalDate?>(null) }
    var to by remember { mutableStateOf<LocalDate?>(null) }
    var reason by remember { mutableStateOf("") }
    var showPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = c.surface) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 16.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Nueva solicitud de ausencia", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.fg)
            FormField("Fechas") {
                val label = when {
                    from == null -> "Selecciona las fechas"
                    to == null || to == from -> from!!.shortFormat()
                    else -> "${from!!.shortFormat()} – ${to!!.shortFormat()}"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(c.bg)
                        .border(1.5.dp, c.border, RoundedCornerShape(12.dp))
                        .clickable { showPicker = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(PopeIcons.Calendar, contentDescription = null, tint = c.fgMid, modifier = Modifier.size(18.dp))
                    Text(label, fontSize = 15.sp, color = if (from == null) c.fgDim else c.fg, fontFamily = if (from == null) null else PopeTheme.mono)
                }
            }
            FormField("Motivo") {
                PopeTextField(reason, { reason = it }, placeholder = "Ej. cita médica, vacaciones…", singleLine = false, minHeight = 72.dp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PopeButton("Cancelar", onDismiss, Modifier.weight(1f), ButtonVariant.SECONDARY)
                PopeButton(
                    "Enviar solicitud",
                    onClick = { from?.let { start -> onSubmit(start, to ?: start, reason) } },
                    modifier = Modifier.weight(1f),
                    enabled = from != null,
                    loading = isSubmitting,
                )
            }
        }
    }

    if (showPicker) {
        val todayMillis = remember(today) { today.toUtcMillis() }
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = from?.toUtcMillis(),
            initialSelectedEndDateMillis = to?.toUtcMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayMillis
                override fun isSelectableYear(year: Int) = year >= today.year
            },
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        from = pickerState.selectedStartDateMillis?.utcMillisToDate()
                        to = pickerState.selectedEndDateMillis?.utcMillisToDate()
                        showPicker = false
                    },
                    enabled = pickerState.selectedStartDateMillis != null,
                ) { Text("Aceptar", color = c.accent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar", color = c.fgMid) }
            },
        ) {
            DateRangePicker(
                state = pickerState,
                modifier = Modifier.weight(1f),
                showModeToggle = false,
                title = { Text("Fechas de ausencia", modifier = Modifier.padding(start = 24.dp, top = 16.dp), color = c.fgMid) },
            )
        }
    }
}
