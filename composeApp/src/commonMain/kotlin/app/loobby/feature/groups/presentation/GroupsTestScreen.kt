package app.loobby.feature.groups.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

@Composable
fun GroupsTestScreen() {
    val vm: GroupsViewModel = koinInject()
    val state by vm.uiState.collectAsState()

    var createName by remember { mutableStateOf("") }
    var createImageUrl by remember { mutableStateOf("") }
    var groupId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Groups – Manual Test", style = MaterialTheme.typography.titleLarge)

        if (state.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        state.lastMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        Text("Create Group", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = createName,
            onValueChange = { createName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            singleLine = true
        )

        OutlinedTextField(
            value = createImageUrl,
            onValueChange = { createImageUrl = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("ImageUrl (optional)") },
            singleLine = true
        )

        Button(
            onClick = { vm.create(createName.trim(), createImageUrl.trim().takeIf { it.isNotBlank() }) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("POST /groups")
        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.refreshMyGroups() }, modifier = Modifier.weight(1f)) {
                Text("GET /groups")
            }
        }

        state.groups.forEach { g ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(g.name, style = MaterialTheme.typography.titleMedium)
                    Text("id: ${g.id}")
                    Text("invite: ${g.inviteCode}")
                    Text("owner: ${g.ownerId}")

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { groupId = g.id }) { Text("Use ID") }
                        OutlinedButton(onClick = { vm.loadMembers(g.id) }) { Text("Members") }
                    }
                }
            }
        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        Text("Group Actions", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = groupId,
            onValueChange = { groupId = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("GroupId") },
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.loadGroup(groupId.trim()) }, modifier = Modifier.weight(1f)) {
                Text("GET /groups/{id}")
            }
            Button(onClick = { vm.join(groupId.trim()) }, modifier = Modifier.weight(1f)) {
                Text("JOIN")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.leave(groupId.trim()) }, modifier = Modifier.weight(1f)) {
                Text("LEAVE")
            }
            Button(onClick = { vm.loadMembers(groupId.trim()) }, modifier = Modifier.weight(1f)) {
                Text("GET members")
            }
        }

        state.selectedGroup?.let { g ->
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Text("Selected Group", style = MaterialTheme.typography.titleMedium)
            Text("Name: ${g.name}")
            Text("Invite: ${g.inviteCode}")
            Text("ImageUrl: ${g.imageUrl ?: "-"}")
        }

        if (state.members.isNotEmpty()) {
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Text("Members", style = MaterialTheme.typography.titleMedium)
            state.members.forEach { m ->
                Text(
                    text = "${if (m.isOwner) "👑 " else ""}${m.username} (${m.userId})",
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}