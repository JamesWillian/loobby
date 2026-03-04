package app.loobby.feature.groups.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.loobby.feature.groups.domain.model.GroupEventFilter
import app.loobby.feature.groups.ui.GroupEventCard
import org.koin.compose.koinInject

@Composable
fun GroupScreen(
    groupId: String,
    groupName: String,
    vm: GroupEventsViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(groupId) {
        vm.loadForGroup(groupId, groupName)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        GroupScreenHeader(
            groupName = state.groupName.ifBlank { groupName },
            onSearchClick = { /* TODO */ },
            onNotificationsClick = { /* TODO */ }
        )

        GroupFilterRow(
            activeFilter = state.activeFilter,
            onFilterSelected = { vm.setFilter(it) }
        )

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

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.filteredEvents, key = { it.id }) { event ->
                GroupEventCard(event = event)
            }

            if (state.filteredEvents.isEmpty() && !state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum evento aqui ainda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupScreenHeader(
    groupName: String,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = ">",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Pesquisar",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        BadgedBox(
            badge = {
                Badge(containerColor = MaterialTheme.colorScheme.error)
            }
        ) {
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notificações",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun GroupFilterRow(
    activeFilter: GroupEventFilter,
    onFilterSelected: (GroupEventFilter) -> Unit
) {
    val filters = listOf(
        GroupEventFilter.TODAY to "Para hoje",
        GroupEventFilter.UPCOMING to "Em breve",
        GroupEventFilter.FINISHED to "Finalizados",
        GroupEventFilter.CONFIRMED to "Confirmados",
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(filters) { (filter, label) ->
            val isActive = activeFilter == filter
            FilterChip(
                selected = isActive,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = label,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        }
    }

    Spacer(Modifier.height(4.dp))
}