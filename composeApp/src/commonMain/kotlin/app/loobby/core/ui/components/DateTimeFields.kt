package app.loobby.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import app.loobby.core.util.DateTransformation
import app.loobby.core.util.TimeTransformation
import app.loobby.theme.Spacing
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

/**
 * Data e hora lado a lado. Os dois guardam só os dígitos (DDMMAAAA e HHMM); a
 * máscara cuida dos separadores. A data abre também um calendário a partir de hoje.
 */
@Composable
fun DateTimeFields(
    dateDigits: String,
    timeDigits: String,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    if (showPicker) {
        FutureDatePickerDialog(
            initialDigits = dateDigits,
            onPicked = { onDateChange(it); showPicker = false },
            onDismiss = { showPicker = false },
        )
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        OutlinedTextField(
            value = dateDigits,
            onValueChange = { onDateChange(it.filter(Char::isDigit).take(8)) },
            label = { Text("Data *") },
            placeholder = { Text("DD-MM-AAAA") },
            singleLine = true,
            visualTransformation = DateTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = {
                IconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = "Abrir calendário")
                }
            },
            modifier = Modifier.weight(1.2f),
        )
        OutlinedTextField(
            value = timeDigits,
            onValueChange = { onTimeChange(it.filter(Char::isDigit).take(4)) },
            label = { Text("Hora *") },
            placeholder = { Text("HH:MM") },
            singleLine = true,
            visualTransformation = TimeTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(0.8f),
        )
    }
}

/**
 * O calendário do Material entrega a data escolhida como meia-noite em UTC; aqui ela
 * volta a ser uma data de calendário, sem fuso, e sai como DDMMAAAA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FutureDatePickerDialog(
    initialDigits: String,
    onPicked: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val zone = TimeZone.currentSystemDefault()
    val today = remember { Clock.System.now().toLocalDateTime(zone).date }
    val initial = parseDateDigits(initialDigits) ?: today
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        selectableDates = remember(today) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcMillisToDate(utcTimeMillis) >= today

                override fun isSelectableYear(year: Int): Boolean = year >= today.year
            }
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onPicked(utcMillisToDate(it).toDigits()) } ?: onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    ) {
        DatePicker(state = state)
    }
}

private fun utcMillisToDate(millis: Long): LocalDate =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date

/** DDMMAAAA → data, ou null se incompleta ou inválida. */
fun parseDateDigits(digits: String): LocalDate? {
    if (digits.length != 8) return null
    return runCatching {
        LocalDate(
            year = digits.substring(4, 8).toInt(),
            month = digits.substring(2, 4).toInt(),
            day = digits.substring(0, 2).toInt(),
        )
    }.getOrNull()
}

/** Data → DDMMAAAA, o formato que os campos guardam. */
fun LocalDate.toDigits(): String =
    day.toString().padStart(2, '0') + month.number.toString().padStart(2, '0') + year.toString()
