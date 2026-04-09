package app.loobby.feature.groups.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.outlined.Boy
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
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
import app.loobby.feature.auth.presentation.AuthViewModel
import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.EventType
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.presentation.CreateEventSheet
import app.loobby.feature.groups.domain.model.GroupEventFilter
import app.loobby.userAvatarPlaceholder
import coil3.compose.AsyncImage
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.time.Clock.System.now

@Composable
fun GroupScreen(
    groupId: String,
    groupName: String,
    groupDescription: String? = null,
    onGroupNameClick: () -> Unit = {},
    onEventClick: (eventId: String, eventName: String) -> Unit = { _, _ -> },
    authVm: AuthViewModel = koinInject(),  // ← NOVO
    vm: GroupEventsViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()
    val authState by authVm.uiState.collectAsState()  // ← NOVO

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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {

        // ── Header ────────────────────────────────────────────────────────────
        GroupHeader(
            groupName = groupName,
            groupDescription = groupDescription,
            hasFullAccess = authState.hasFullAccess,  // ← NOVO
            onGroupNameClick = onGroupNameClick,
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
                    onClick = { onEventClick(event.id, event.name) },
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
    hasFullAccess: Boolean,  // ← NOVO
    onGroupNameClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onCreateEventClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clickable group name → opens GroupDetail
            Row(
                modifier = Modifier
                    .clickable(onClick = onGroupNameClick)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = groupName,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = "Detalhes do grupo",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

//            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//                IconButton(onClick = onSearchClick) {
//                    Icon(Icons.Outlined.Search, contentDescription = "Buscar")
//                }
//                BadgedBox(
//                    badge = {
//                        Badge(containerColor = MaterialTheme.colorScheme.error)
//                    }
//                ) {
//                    IconButton(onClick = onNotificationsClick) {
//                        Icon(Icons.Outlined.Notifications, contentDescription = "Notificações")
//                    }
//                }
//            }
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

        // ← NOVO: desabilita botão se não verificou email
        OutlinedButton(
            onClick = onCreateEventClick,
            enabled = hasFullAccess,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (hasFullAccess) "+ Criar Evento"
                else "Verifique seu email para criar eventos"
            )
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
        contentPadding = PaddingValues(horizontal = 12.dp),
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
    onClick: () -> Unit,
    onRsvpClick: () -> Unit
) {
    val isUpcoming = runCatching {
        kotlin.time.Instant.parse(event.scheduledDatetime) > now()
    }.getOrDefault(false)
    val isConfirmed = event.rsvpStatus == RsvpStatus.YES

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    if (event.scheduledDatetime < now().toString()) {
                        if (isConfirmed)
                            Icon(
                                imageVector = Icons.Filled.CheckCircleOutline,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(34.dp)
                            )
                    } else {
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
                    model = url ?: userAvatarPlaceholder(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        val extraPlayers = (confirmedCount-5)
        if (extraPlayers > 0) {
            Box(
                modifier = Modifier
                    .offset(x = (-(avatarUrls.size * 10)).dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$extraPlayers",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── Date formatting helper ───────────────────────────────────────────────────

private val DAYS_PTBR = mapOf(
    DayOfWeek.MONDAY    to "Seg",
    DayOfWeek.TUESDAY   to "Ter",
    DayOfWeek.WEDNESDAY to "Qua",
    DayOfWeek.THURSDAY  to "Qui",
    DayOfWeek.FRIDAY    to "Sex",
    DayOfWeek.SATURDAY  to "Sáb",
    DayOfWeek.SUNDAY    to "Dom"
)

private fun String.formatScheduled(): String {
    return runCatching {
        val instant = kotlin.time.Instant.parse(this)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = local.hour.toString().padStart(2, '0')
        val min = local.minute.toString().padStart(2, '0')

        val today = now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        val tomorrow = today.plus(DatePeriod(days = 1))

        when (local.date) {
            today -> "Hoje, $hour:$min"
            tomorrow -> "Amanhã, $hour:$min"
            else -> {
                val day = DAYS_PTBR[local.dayOfWeek]
                "$day, $hour:$min"
            }
        }
    }.getOrDefault(this)
}