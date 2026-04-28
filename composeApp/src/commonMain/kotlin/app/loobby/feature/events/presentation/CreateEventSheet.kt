package app.loobby.feature.events.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth  // calendar icon
import androidx.compose.material.icons.outlined.SportsVolleyball
import androidx.compose.material.icons.outlined.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.loobby.core.util.DateTransformation
import app.loobby.core.util.TimeTransformation
import app.loobby.feature.events.domain.model.EventDomain // import
import app.loobby.feature.events.domain.model.EventType
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventSheet(
    groupId: String?,
    onDismiss: () -> Unit,
    onEventCreated: () -> Unit,
    editEvent: EventDomain? = null, // evento para edição (null = modo criação)
    vm: CreateEventViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()

    // se editEvent != null, carrega dados no VM para edição
    LaunchedEffect(editEvent) {
        if (editEvent != null) {
            vm.loadForEdit(editEvent)
        }
    }

    // Dismiss and refresh when creation/update succeeds
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            vm.reset()
            onEventCreated()
        }
    }

    // Confirmação ao fechar com rascunho (campos preenchidos não submetidos).
    var showDiscardDialog by remember { mutableStateOf(false) }
    // Ação pendente de fechamento (chamada apenas se o usuário confirmar o descarte).
    var pendingDismiss by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Snapshot atualizado de isDirty para uso dentro de lambdas memorizadas
    // (confirmValueChange é capturado no remember inicial do sheet state).
    val isDirty by rememberUpdatedState(state.isDirty)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            if (newValue == SheetValue.Hidden && isDirty) {
                // Bloqueia o fechamento e mostra o diálogo. O "Descartar" do diálogo
                // chama onDismiss() diretamente, dispensando o sheet.
                pendingDismiss = {
                    vm.reset()
                    onDismiss()
                }
                showDiscardDialog = true
                false
            } else {
                true
            }
        }
    )

    fun attemptDismiss(action: () -> Unit) {
        if (state.isDirty) {
            pendingDismiss = action
            showDiscardDialog = true
        } else {
            action()
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = {
                showDiscardDialog = false
                pendingDismiss = null
            },
            title = { Text("Descartar evento?") },
            text = { Text("As informações preenchidas serão perdidas.") },
            confirmButton = {
                TextButton(onClick = {
                    val action = pendingDismiss
                    showDiscardDialog = false
                    pendingDismiss = null
                    action?.invoke()
                }) { Text("Descartar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    pendingDismiss = null
                }) { Text("Continuar editando") }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            // Caminho "limpo" (sem rascunho): confirmValueChange já bloqueou o
            // fechamento sujo, então aqui é seguro descartar e dispensar.
            vm.reset()
            onDismiss()
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // título dinâmico baseado no modo
            Text(
                text = if (state.isEditMode) "Editar Evento" else "Novo Evento",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            // no modo edição, tipo já está selecionado e não pode ser alterado
            if (state.selectedType == null && !state.isEditMode) {
                // ── Step 1: choose type ──────────────────────────────────────
                TypeSelectionStep(onTypeSelected = vm::selectType)
            } else {
                // ── Step 2: fill details ─────────────────────────────────────
                EventDetailsStep(
                    state = state,
                    onNameChange = vm::onNameChange,
                    onDescriptionChange = vm::onDescriptionChange,
                    onDateChange = vm::onDateChange,
                    onTimeChange = vm::onTimeChange,
                    onDurationChange = vm::onDurationChange,
                    onArenaChange = vm::onArenaChange,
                    onPriceChange = vm::onPriceChange,
                    onMaxPlayersChange = vm::onMaxPlayersChange,
                    onAcceptReserveChange = vm::onAcceptReserveChange,
                    onGameNameChange = vm::onGameNameChange,
                    onGameIdChange = vm::onGameIdChange,
                    onSubmit = { vm.submit(groupId) },
                    // no modo edição, "Voltar" fecha o sheet em vez de voltar à seleção de tipo
                    onBack = {
                        attemptDismiss {
                            if (state.isEditMode) {
                                vm.reset()
                                onDismiss()
                            } else {
                                vm.reset()
                            }
                        }
                    }
                )
            }
        }
    }
}

// ─── Step 1: Type Selection ───────────────────────────────────────────────────

@Composable
private fun TypeSelectionStep(onTypeSelected: (EventType) -> Unit) {
    Text(
        "Qual tipo de evento?",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EventTypeCard(
            label = "Esporte",
            icon = { Icon(Icons.Outlined.SportsVolleyball, contentDescription = null, modifier = Modifier.size(32.dp)) },
            onClick = { onTypeSelected(EventType.SPORT) },
            modifier = Modifier.weight(1f)
        )
        EventTypeCard(
            label = "Gameplay",
            icon = { Icon(Icons.Outlined.VideogameAsset, contentDescription = null, modifier = Modifier.size(32.dp)) },
            onClick = { onTypeSelected(EventType.GAMEPLAY) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EventTypeCard(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// ─── Step 2: Event Details ────────────────────────────────────────────────────

@Composable
private fun EventDetailsStep(
    state: CreateEventUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onArenaChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onMaxPlayersChange: (String) -> Unit,
    onAcceptReserveChange: (Boolean) -> Unit,
    onGameNameChange: (String) -> Unit,
    onGameIdChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val typeLabel = if (state.selectedType == EventType.SPORT) "🏐 Esporte" else "🎮 Gameplay"

    Row(verticalAlignment = Alignment.CenterVertically) {
        // no modo edição, mostra "← Cancelar" em vez de "← Voltar"
        TextButton(onClick = onBack) {
            Text(if (state.isEditMode) "← Cancelar" else "← Voltar")
        }
        Spacer(Modifier.width(8.dp))
        Text(typeLabel, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
    }

    // ── Common fields ─────────────────────────────────────────────────────────
    OutlinedTextField(
        value = state.name,
        onValueChange = onNameChange,
        label = { Text("Nome do evento *") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = state.description,
        onValueChange = onDescriptionChange,
        label = { Text("Descrição") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 3
    )

    // DatePickerDialog state — só permite datas a partir de hoje (no fuso do usuário).
    var showDatePicker by remember { mutableStateOf(false) }
    val futureOnlySelectableDates = remember {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val tz = TimeZone.currentSystemDefault()
                // O DatePicker do Material3 entrega o "rótulo" da data como meia-noite UTC,
                // então interpretamos esse millis como uma data calendárica em UTC e
                // comparamos com a data de hoje no fuso local do usuário.
                val pickedDate = Instant.fromEpochMilliseconds(utcTimeMillis)
                    .toLocalDateTime(TimeZone.UTC).date
                val today = Clock.System.now().toLocalDateTime(tz).date
                return pickedDate >= today
            }

            override fun isSelectableYear(year: Int): Boolean {
                val tz = TimeZone.currentSystemDefault()
                val currentYear = Clock.System.now().toLocalDateTime(tz).year
                return year >= currentYear
            }
        }
    }

    // Pré-seleção: usa a data já preenchida no formulário (modo edição ou após o usuário
    // ter escolhido algo) ou, em último caso, hoje (no fuso do usuário). O Material3
    // espera meia-noite UTC do dia "rotulado".
    val initialSelectedDateMillis = remember(state.scheduledDate) {
        val tz = TimeZone.currentSystemDefault()
        val fromForm = if (state.scheduledDate.length == 8) {
            runCatching {
                val dd = state.scheduledDate.substring(0, 2).toInt()
                val mm = state.scheduledDate.substring(2, 4).toInt()
                val yyyy = state.scheduledDate.substring(4, 8).toInt()
                LocalDate(yyyy, mm, dd).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            }.getOrNull()
        } else null
        fromForm ?: Clock.System.now()
            .toLocalDateTime(tz).date
            .atStartOfDayIn(TimeZone.UTC)
            .toEpochMilliseconds()
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis,
        selectableDates = futureOnlySelectableDates
    )

    // show DatePickerDialog when triggered
    if (showDatePicker) {
        @OptIn(ExperimentalMaterial3Api::class)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Convert millis to DD-MM-YYYY
                        val totalDays = millis / 86400000L
                        val y: Int
                        val m: Int
                        val d: Int
                        var year = 1970; var daysLeft = totalDays.toInt()
                        while (true) {
                            val diy = if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 366 else 365
                            if (daysLeft < diy) break
                            daysLeft -= diy; year++
                        }
                        y = year
                        val leap = y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)
                        val dim = intArrayOf(31,if(leap)29 else 28,31,30,31,30,31,31,30,31,30,31)
                        var month = 0
                        while (daysLeft >= dim[month]) { daysLeft -= dim[month]; month++ }
                        m = month + 1; d = daysLeft + 1
                        val dd = d.toString().padStart(2, '0')
                        val mm = m.toString().padStart(2, '0')
                        onDateChange("$dd$mm$y")
                    }
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // date field with calendar icon and DD-MM-YYYY placeholder
        OutlinedTextField(
            value = state.scheduledDate,
            onValueChange = onDateChange,
            label = { Text("Data *") },
            placeholder = { Text("DD-MM-YYYY") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            visualTransformation = DateTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = "Abrir calendário")
                }
            }
        )
        // time field with HH:MM placeholder
        OutlinedTextField(
            value = state.scheduledTime,
            onValueChange = onTimeChange,
            label = { Text("Hora *") },
            placeholder = { Text("HH:MM") },
            modifier = Modifier.weight(0.8f),
            singleLine = true,
            visualTransformation = TimeTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }

    HorizontalDivider()

    // ── Type-specific fields ──────────────────────────────────────────────────
    when (state.selectedType) {
        EventType.SPORT -> SportFields(
            state = state,
            onDurationChange = onDurationChange,
            onArenaChange = onArenaChange,
            onPriceChange = onPriceChange,
            onMaxPlayersChange = onMaxPlayersChange,
            onAcceptReserveChange = onAcceptReserveChange
        )
        EventType.GAMEPLAY -> GameplayFields(
            state = state,
            onGameNameChange = onGameNameChange,
            onGameIdChange = onGameIdChange
        )
        null -> Unit
    }

    // ── Submit ────────────────────────────────────────────────────────────────
    // Guarda de offline fica no ActionSheet (entrada para criar) e no ícone de
    // editar do EventDetailScreen. Se a sheet foi aberta é porque havia rede,
    // então o botão aqui não precisa re-checar isOnline.
    if (state.errorMessage != null) {
        Text(
            text = state.errorMessage!!,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }

    Button(
        onClick = onSubmit,
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoading
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
        } else {
            val label = if (state.isEditMode) "Salvar Alterações" else "Criar Evento"
            Text(label, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SportFields(
    state: CreateEventUiState,
    onDurationChange: (String) -> Unit,
    onArenaChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onMaxPlayersChange: (String) -> Unit,
    onAcceptReserveChange: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = state.durationMinutes,
        onValueChange = onDurationChange,
        label = { Text("Duração (minutos) *") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    OutlinedTextField(
        value = state.arena,
        onValueChange = onArenaChange,
        label = { Text("Local / Quadra") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = state.pricePerPlayer,
            onValueChange = onPriceChange,
            label = { Text("Preço/Pessoa") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
            value = state.maxPlayers,
            onValueChange = onMaxPlayersChange,
            label = { Text("Máx. jogadores") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(checked = state.acceptReserve, onCheckedChange = onAcceptReserveChange)
        Spacer(Modifier.width(4.dp))
        Text("Aceitar reservas", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun GameplayFields(
    state: CreateEventUiState,
    onGameNameChange: (String) -> Unit,
    onGameIdChange: (String) -> Unit
) {
    OutlinedTextField(
        value = state.gameName,
        onValueChange = onGameNameChange,
        label = { Text("Nome do jogo *") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    OutlinedTextField(
        value = state.gameId,
        onValueChange = onGameIdChange,
        label = { Text("ID do jogo (opcional)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}