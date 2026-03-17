package app.loobby.feature.events.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.EventType
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.userAvatarPlaceholder
import coil3.compose.AsyncImage
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    vm: EventDetailViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(eventId) {
        vm.load(eventId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────────
            TopAppBar(
                title = {
                    Text(
                        text = state.event?.name ?: "",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                windowInsets = WindowInsets(0)
            )

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            state.event?.let { event ->
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
//                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {

                    // ── Event info ────────────────────────────────────────────
                    item {
                        Spacer(Modifier.height(16.dp))
                        EventInfoCard(event = event)
                    }


                    // ── RSVP buttons ──────────────────────────────────────────
                    item {
                        Spacer(Modifier.height(16.dp))
                        RsvpButtonRow(
                            currentStatus = event.rsvpStatus,
                            acceptReserve = event.sport?.acceptReserve ?: false,
                            isLoading = state.isRsvpLoading,
                            onRsvp = { status -> vm.rsvp(eventId, status) }
                        )
                    }

                    // ── Attendee sections ─────────────────────────────────────
                    val grouped = state.rsvpsByStatus
                    val order = listOf(
                        RsvpStatus.YES,
                        RsvpStatus.RESERVE,
                        RsvpStatus.MAYBE,
                        RsvpStatus.NO,
                        RsvpStatus.PENDING
                    )

                    order.forEach { status ->
                        val list = grouped[status]
                        if (!list.isNullOrEmpty()) {
                            item(key = "header_$status") {
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = status.sectionLabel(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                )
                            }
                            items(list, key = { "${status}_${it.userId}" }) { rsvp ->
                                RsvpMemberRow(rsvp = rsvp)
                                if (list.last() != rsvp) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 52.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ─── Event info card ──────────────────────────────────────────────────────────

@Composable
private fun EventInfoCard(event: EventDomain) {
    val emoji = when (event.eventType) {
        EventType.SPORT -> "🏐"
        EventType.GAMEPLAY -> "🎮"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "$emoji ${event.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            event.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = event.scheduledDatetime.formatEventDate(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            // Sport-specific details
            event.sport?.let { sport ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                Column (
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sport.arena?.let { InfoChip("📍 $it") }
                    sport.maxPlayers?.let { InfoChip("👥 $it máx.") }
                    if (sport.durationMinutes > 0) {
                        InfoChip("🕗 ${sport.durationMinutes} min.")
                    }
                    if (sport.pricePerPlayer > 0) {
                        InfoChip("💰 R$ ${sport.pricePerPlayer}")
                    }
                }
            }

            // Confirmed count summary
            if (event.confirmedCount > 0) {
                Text(
                    text = "${event.confirmedCount} confirmado${if (event.confirmedCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun InfoChip(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ─── RSVP buttons ─────────────────────────────────────────────────────────────

@Composable
private fun RsvpButtonRow(
    currentStatus: RsvpStatus?,
    acceptReserve: Boolean,
    isLoading: Boolean,
    onRsvp: (RsvpStatus) -> Unit
) {
    // Estado local para feedback imediato ao clicar, sincronizado com currentStatus
    // quando a API responder e o evento for recarregado.
    var selectedStatus by remember(currentStatus) { mutableStateOf(currentStatus) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        Text(
            text = "Sua presença",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Linha superior: Vou | Não vou
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RsvpCard(
                modifier = Modifier.weight(1f),
                label = "Vou!",
                subtitle = "Confirmar presença",
                icon = {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                },
                isSelected = selectedStatus == RsvpStatus.YES,
                isLoading = isLoading && selectedStatus == RsvpStatus.YES,
                selectedBorderColor = Color(0xFF4CAF50),
                selectedBgColor = Color(0xFF1B3A1F),
                selectedLabelColor = Color(0xFF4CAF50),
                selectedIconBgColor = Color(0xFF2E7D32).copy(alpha = 0.4f),
                onClick = {
                    selectedStatus = RsvpStatus.YES
                    onRsvp(RsvpStatus.YES)
                }
            )

            RsvpCard(
                modifier = Modifier.weight(1f),
                label = "Não vou",
                subtitle = "Não poderei ir",
                icon = {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = null,
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(28.dp)
                    )
                },
                isSelected = selectedStatus == RsvpStatus.NO,
                isLoading = isLoading && selectedStatus == RsvpStatus.NO,
                selectedBorderColor = Color(0xFFEF5350),
                selectedBgColor = Color(0xFF3A1B1B),
                selectedLabelColor = Color(0xFFEF5350),
                selectedIconBgColor = Color(0xFFC62828).copy(alpha = 0.4f),
                onClick = {
                    selectedStatus = RsvpStatus.NO
                    onRsvp(RsvpStatus.NO)
                }
            )
        }

        // Linha inferior: Talvez (+ Reserva se aceitar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RsvpCard(
                modifier = Modifier.weight(1f),
                label = "Talvez",
                subtitle = "Ainda não sei",
                icon = {
                    Icon(
                        Icons.Outlined.Remove,
                        contentDescription = null,
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(28.dp)
                    )
                },
                isSelected = selectedStatus == RsvpStatus.MAYBE,
                isLoading = isLoading && selectedStatus == RsvpStatus.MAYBE,
                selectedBorderColor = Color(0xFFFFA726),
                selectedBgColor = Color(0xFF3A2C0A),
                selectedLabelColor = Color(0xFFFFA726),
                selectedIconBgColor = Color(0xFFF57F17).copy(alpha = 0.4f),
                onClick = {
                    selectedStatus = RsvpStatus.MAYBE
                    onRsvp(RsvpStatus.MAYBE)
                }
            )

            if (acceptReserve) {
                RsvpCard(
                    modifier = Modifier.weight(1f),
                    label = "Reserva",
                    subtitle = "Lista de espera",
                    icon = {
                        Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF7E57C2),
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    isSelected = selectedStatus == RsvpStatus.RESERVE,
                    isLoading = isLoading && selectedStatus == RsvpStatus.RESERVE,
                    selectedBorderColor = Color(0xFF7E57C2),
                    selectedBgColor = Color(0xFF1E1530),
                    selectedLabelColor = Color(0xFF7E57C2),
                    selectedIconBgColor = Color(0xFF512DA8).copy(alpha = 0.4f),
                    onClick = {
                        selectedStatus = RsvpStatus.RESERVE
                        onRsvp(RsvpStatus.RESERVE)
                    }
                )
            }
        }
    }
}

@Composable
private fun RsvpCard(
    modifier: Modifier = Modifier,
    label: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    isSelected: Boolean,
    isLoading: Boolean,
    selectedBorderColor: Color,
    selectedBgColor: Color,
    selectedLabelColor: Color,
    selectedIconBgColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable(enabled = !isLoading, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) selectedBorderColor
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) selectedBgColor
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Badge de selecionado (canto superior direito)

            // Conteúdo centralizado vertical e horizontalmente
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Círculo do ícone
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) selectedIconBgColor
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = selectedBorderColor
                        )
                    } else {
                        icon()
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) selectedLabelColor
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

// ─── RSVP member row ──────────────────────────────────────────────────────────

@Composable
private fun RsvpMemberRow(rsvp: RsvpDomain) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = rsvp.avatarUrl
                    ?: userAvatarPlaceholder(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Name + obs
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = rsvp.displayname ?: rsvp.username,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (rsvp.isOwner) {
                    Text(
                        text = "👑",
                        fontSize = 12.sp
                    )
                }
            }
            if (!rsvp.obs.isNullOrBlank()) {
                Text(
                    text = rsvp.obs,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Paid indicator
        if (rsvp.isPaid) {
            Surface(
                shape = RoundedCornerShape(corner = CornerSize(12.dp)),
                color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                modifier = Modifier.width(62.dp).height(28.dp)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = "Pago",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "Pago",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32),
                    )
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun RsvpStatus.sectionLabel(): String = when (this) {
    RsvpStatus.YES     -> "✅ Confirmados"
    RsvpStatus.NO      -> "❌ Não vão"
    RsvpStatus.MAYBE   -> "🤔 Talvez"
    RsvpStatus.RESERVE -> "🔄 Reservas"
    RsvpStatus.PENDING -> "⏳ Pendentes"
}

private fun String.formatEventDate(): String {
    return runCatching {
        val instant = Instant.parse(this)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = local.hour.toString().padStart(2, '0')
        val min = local.minute.toString().padStart(2, '0')
        val day = local.dayOfMonth.toString().padStart(2, '0')
        val month = local.monthNumber.toString().padStart(2, '0')
        val year = local.year
        when {
            local.date == now.date -> "Hoje, $hour:$min"
            local.year == now.year -> "$day/$month, $hour:$min"
            else -> "$day/$month/$year, $hour:$min"
        }
    }.getOrDefault(this)
}