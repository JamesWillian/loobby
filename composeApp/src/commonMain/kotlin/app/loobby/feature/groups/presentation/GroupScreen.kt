package app.loobby.feature.groups.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.EventType
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.presentation.CreateEventSheet
import app.loobby.feature.groups.domain.model.GroupEventFilter
import coil3.compose.AsyncImage
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.time.Clock.System.now

@Composable
fun GroupScreen(
    groupId: String,
    groupName: String,
    groupDescription: String? = null,
    vm: GroupEventsViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(groupId) {
        vm.loadEvents(groupId)
    }

    var showCreateSheet by remember { mutableStateOf(false) }

    if (showCreateSheet) {
        CreateEventSheet(
            groupId = groupId,
            onDismiss = { showCreateSheet = false },
            onEventCreated = {
                showCreateSheet = false
                vm.loadEvents(groupId)
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Header ────────────────────────────────────────────────────────────
        GroupHeader(
            groupName = groupName,
            groupDescription = groupDescription,
            onSearchClick = { /* TODO */ },
            onNotificationsClick = { /* TODO */ },
            onCreateEventClick = { showCreateSheet = true }
        )

        // ── Filter chips ──────────────────────────────────────────────────────
        FilterChipRow(
            activeFilter = state.activeFilter,
            onFilterSelected = vm::setFilter
        )

        // ── Loading / error ───────────────────────────────────────────────────
        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        state.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // ── Events list ───────────────────────────────────────────────────────
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.filteredEvents, key = { it.id }) { event ->
                EventCard(
                    event = event,
                    onRsvpClick = {
                        vm.rsvp(
                            eventId = event.id,
                            status = when (event.rsvpStatus) {
                                RsvpStatus.YES -> RsvpStatus.NO
                                RsvpStatus.NO -> RsvpStatus.YES
                                else -> RsvpStatus.YES
                            }
                        )
                    }
                )
            }

            if (state.filteredEvents.isEmpty() && !state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nenhum evento nesta categoria",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun GroupHeader(
    groupName: String,
    groupDescription: String?,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onCreateEventClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$groupName >",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Outlined.Search, contentDescription = "Buscar")
                }
                BadgedBox(
                    badge = {
                        Badge(containerColor = MaterialTheme.colorScheme.error)
                    }
                ) {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notificações")
                    }
                }
            }
        }

        if (groupDescription != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = groupDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onCreateEventClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Criar Evento")
        }
    }
}

// ─── Filter chips ─────────────────────────────────────────────────────────────

private data class FilterOption(val filter: GroupEventFilter, val label: String)

private val filterOptions = listOf(
    FilterOption(GroupEventFilter.TODAY, "Para hoje"),
    FilterOption(GroupEventFilter.UPCOMING, "Em breve"),
    FilterOption(GroupEventFilter.FINISHED, "Finalizados"),
    FilterOption(GroupEventFilter.CONFIRMED, "Confirmados"),
)

@Composable
private fun FilterChipRow(
    activeFilter: GroupEventFilter,
    onFilterSelected: (GroupEventFilter) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filterOptions) { option ->
            val selected = activeFilter == option.filter
            FilterChip(
                selected = selected,
                onClick = { onFilterSelected(option.filter) },
                label = { Text(option.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

// ─── Event card ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EventCard(
    event: EventDomain,
    onRsvpClick: () -> Unit
) {
    val isUpcoming = runCatching {
        kotlin.time.Instant.parse(event.scheduledDatetime) > now()
    }.getOrDefault(false)
    val isConfirmed = event.rsvpStatus == RsvpStatus.YES

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = if (isUpcoming) 1f else 0.6f
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Event name with emoji prefix
            val emoji = when (event.eventType) {
                EventType.SPORT -> "🏐"
                EventType.GAMEPLAY -> "🎮"
            }
            Text(
                text = "$emoji ${event.name}",
                style = MaterialTheme.typography.titleMediumEmphasized.copy(fontWeight = FontWeight.SemiBold),
                fontSize = 18.sp
            )

            Spacer(Modifier.height(6.dp))

            Box {
                Column {

                    // Date/time
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = event.scheduledDatetime.formatScheduled(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Attendee avatars
                    AttendeesRow(
                        avatarUrls = event.confirmedAvatars.orEmpty(),
                        confirmedCount = event.confirmedCount
                    )
                }

                Box(modifier = Modifier.fillMaxHeight().align(Alignment.BottomEnd)) {
                    // RSVP button
                    Button(
                        modifier = Modifier.align(Alignment.BottomEnd).fillMaxHeight(),
                        onClick = onRsvpClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isConfirmed -> Color(0xFF2E7D32)  // verde confirmado
                                isUpcoming -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Vou", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─── Attendees row ────────────────────────────────────────────────────────────

@Composable
private fun AttendeesRow(avatarUrls: List<String?>, confirmedCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {

        avatarUrls.forEachIndexed { index, url ->
            Box(
                modifier = Modifier
                    .offset(x = (-index * 10).dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = url ?: "https://upload.wikimedia.org/wikipedia/commons/8/89/Portrait_Placeholder.png",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (confirmedCount > 0) {
            Box(
                modifier = Modifier
                    .offset(x = (-(avatarUrls.size * 10)).dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$confirmedCount",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── Date formatting helper ───────────────────────────────────────────────────

private fun String.formatScheduled(): String {
    return runCatching {
        val instant = Instant.parse(this)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = local.hour.toString().padStart(2, '0')
        val min = local.minute.toString().padStart(2, '0')

        val today = now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        val tomorrow = kotlinx.datetime.LocalDate(today.year, today.month, today.dayOfMonth + 1)

        when (local.date) {
            today -> "Hoje, $hour:$min"
            tomorrow -> "Amanhã, $hour:$min"
            else -> {
                val day = local.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                "$day, $hour:$min"
            }
        }
    }.getOrDefault(this)
}