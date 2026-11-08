package pope.project.util

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Instant

private val shortDays = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
private val fullDays = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
private val shortMonths = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

fun Int.pad2(): String = toString().padStart(2, '0')

/** "Lun 2 Nov" — header date, same format as the reference design. */
fun LocalDate.headerFormat(): String =
    "${shortDays[dayOfWeek.isoDayNumber - 1]} $day ${shortMonths[month.number - 1]}"

/** "01 Nov" — history / time-off dates. */
fun LocalDate.shortFormat(): String = "${day.pad2()} ${shortMonths[month.number - 1]}"

fun LocalTime.hhmm(): String = "${hour.pad2()}:${minute.pad2()}"

fun Instant.hhmm(timeZone: TimeZone): String = toLocalDateTime(timeZone).time.hhmm()

fun DayOfWeek.spanishName(): String = fullDays[isoDayNumber - 1]

/** 2.0 → "2 h", 2.5 → "2.5 h". */
fun formatHours(hours: Double): String {
    val tenths = (hours * 10).roundToInt()
    return if (tenths % 10 == 0) "${tenths / 10} h" else "${tenths / 10}.${tenths % 10} h"
}

/** Drops the state + postcode suffix: "14 Loch St, Nedlands WA 6009" → "14 Loch St, Nedlands". */
fun String.shortAddress(): String = replace(Regex("""\s+[A-Z]{2,3}\s+\d{4}$"""), "")
