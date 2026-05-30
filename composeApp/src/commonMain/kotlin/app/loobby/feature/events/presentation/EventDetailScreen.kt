package app.loobby.feature.events.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuOpen
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Delete          // import ícone de excluir
import androidx.compose.material.icons.outlined.Edit            // import ícone de editar
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MoreVert        // import ícone "três pontinhos"
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SportsVolleyball
import androidx.compose.material.icons.outlined.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import app.loobby.theme.LoobbyColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.loobby.core.network.LocalIsOnline
import app.loobby.core.preferences.SharePreferencesRepository
import app.loobby.core.share.shareText
import app.loobby.core.util.rememberCopyToClipboard
import app.loobby.feature.events.domain.model.EventDomain
import app.loobby.feature.events.domain.model.RsvpDomain
import app.loobby.feature.events.domain.model.EventType
import app.loobby.feature.events.domain.model.RsvpStatus
import app.loobby.feature.events.domain.model.SportDomain
import app.loobby.feature.games.domain.model.GameDomain
import app.loobby.feature.groups.presentation.FeedViewModel    // para recarregar a sidebar ao sair de evento instantâneo
import app.loobby.theme.textLightShadow
import app.loobby.theme.textShadow
import app.loobby.userAvatarPlaceholder
import coil3.compose.AsyncImage
import io.ktor.client.request.invoke
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.time.Clock

// Altura visível no estado colapsado: título + espaçamentos + linha de Vou/Não vou
private val SHEET_PEEK_HEIGHT = 162.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    onOpenTeams: () -> Unit = {},
    // Abre a tela "Formação dos Times" (somente leitura) — usada para usuários
    // comuns, que não são donos do grupo nem criadores do evento.
    onOpenTeamsReport: () -> Unit = {},
    // Controla se o botão de voltar aparece na top bar.
    // false quando a tela é aberta pelo sidebar (Evento Rápido) e não há rota anterior.
    showBackButton: Boolean = true,
    vm: EventDetailViewModel = koinInject(),
    // Singleton — mesma instância usada pela sidebar (GroupSidebar via AppShell).
    // Usado apenas pra disparar refresh da sidebar quando o usuário sai de um
    // evento instantâneo (o evento deve sumir da lista de feed).
    feedVm: FeedViewModel = koinInject()
) {
    val state by vm.uiState.collectAsState()

    // Todas as ações de escrita (RSVP, editar, excluir, pago, observação) são
    // bloqueadas quando offline. Compartilhar e copiar código de convite
    // permanecem habilitados porque são operações locais/SO.
    val isOnline = LocalIsOnline.current

    // controla visibilidade do diálogo de compartilhamento
    var showShareDialog by remember { mutableStateOf(false) }
    // controla diálogo de confirmação de exclusão
    var showDeleteDialog by remember { mutableStateOf(false) }
    // controla diálogo de confirmação de remoção da própria presença
    var showRemoveRsvpDialog by remember { mutableStateOf(false) }
    // controla expansão do menu de mais opções (três pontinhos)
    var showMoreMenu by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        vm.load(eventId)
    }

    // navegar de volta quando excluir com sucesso
    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            onBack()
        }
    }

    // diálogo de confirmação de compartilhamento
    if (showShareDialog && state.event != null) {
        ShareDialog(
            event = state.event!!,
            rsvps = state.rsvps,
            onShare = { textToShare ->
                showShareDialog = false
                shareText(textToShare)
            },
            onDismiss = { showShareDialog = false }
        )
    }

    // diálogo de confirmação de exclusão
    if (showDeleteDialog && state.event != null) {
        DeleteEventDialog(
            eventName = state.event!!.name,
            isDeleting = state.isDeleting,
            onConfirm = {
                vm.delete(eventId)
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // diálogo de confirmação de remover presença (RSVP do próprio usuário)
    if (showRemoveRsvpDialog && state.event != null) {
        RemoveRsvpDialog(
            isInstant = state.event!!.isInstant,
            isRemoving = state.isRemovingRsvp,
            onConfirm = {
                vm.removeMyRsvp(eventId)
            },
            onDismiss = { showRemoveRsvpDialog = false }
        )
    }

    // Pós-sucesso ao remover a própria presença: fecha o diálogo e, se o evento
    // era instantâneo, recarrega o feed da sidebar (o evento deixa de aparecer
    // ali porque o usuário não tem mais RSVP) e navega de volta. Para eventos de
    // grupo, o usuário continua vendo o evento normalmente — só a presença foi
    // removida — então não precisa navegar.
    LaunchedEffect(state.removeRsvpSuccess) {
        if (state.removeRsvpSuccess) {
            showRemoveRsvpDialog = false
            if (state.event?.isInstant == true) {
                feedVm.refreshFeed()
                onBack()
            }
        }
    }

    // sheet de edição do evento
    if (state.showEditSheet && state.event != null) {
        CreateEventSheet(
            groupId = state.event!!.groupId,
            onDismiss = { vm.hideEditSheet() },
            onEventCreated = { vm.onEventUpdated(eventId) },
            editEvent = state.event
        )
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true   // sheet nunca some da tela
        )
    )

    // ── Estado do botão "Copiar código de convite" (apenas para eventos instantâneos) ──
    val copyToClipboard = rememberCopyToClipboard()
    var copiedSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(copiedSnackbar) {
        if (copiedSnackbar) {
            scaffoldState.snackbarHostState.showSnackbar("Código copiado!")
            copiedSnackbar = false
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetPeekHeight = SHEET_PEEK_HEIGHT,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetTonalElevation = 4.dp,
        sheetShadowElevation = 8.dp,
        sheetContent = {
            if (state.isLoading) {
//                CircularWavyProgressIndicator(modifier = Modifier.align(alignment = Alignment.CenterHorizontally))
            } else {
                // Quando o evento está finalizado, mantemos a sheet com todos os
                // campos visíveis (Gerenciar Times, Pagamento, Observação) e
                // apenas os botões de RSVP recebem overlay com texto
                // "Evento Finalizado!" + blur + clique desabilitado.
                RsvpSheetContent(
                    currentStatus = state.event?.rsvpStatus,
                    acceptReserve = state.event?.sport?.acceptReserve ?: false,
                    pricePerPlayer = state.event?.sport?.pricePerPlayer ?: 0.0,
                    canManageTeams = state.canManage,
                    onOpenTeams = onOpenTeams,
                    onOpenTeamsReport = onOpenTeamsReport,
                    isPaid = state.isPaid,
                    onPaidChange = { vm.setPaid(eventId, it) },
                    obs = state.obs,
                    onObsChange = { vm.setObs(eventId, it) },
                    isObsSaved = state.isObsSaved,
                    isLoading = state.isRsvpLoading,
                    isOnline = isOnline,
                    isFinished = state.isFinished,
                    onRsvp = { status -> vm.rsvp(eventId, status) }
                )
            }
        }
    ) { innerPadding ->
        // innerPadding é intencionalmente ignorado: o hero desenha edge-to-edge sob
        // a status bar e cuida dos próprios insets. O parâmetro é mantido apenas
        // para a assinatura do slot de conteúdo do BottomSheetScaffold.
        Box(modifier = Modifier.fillMaxSize()) {
            state.event?.let { event ->
                EventDetailContent(
                    event = event,
                    game = state.game,
                    rsvpsByStatus = state.rsvpsByStatus,
                    showBackButton = showBackButton,
                    isOnline = isOnline,
                    canManage = state.canManage,
                    showMoreMenu = showMoreMenu,
                    onMoreMenuChange = { showMoreMenu = it },
                    onBack = onBack,
                    onShare = { showShareDialog = true },
                    onEdit = { vm.showEditSheet() },
                    onDelete = { showDeleteDialog = true },
                    onRemoveRsvp = { showRemoveRsvpDialog = true },
                    onCopyInvite = {
                        copyToClipboard(event.inviteCode)
                        copiedSnackbar = true
                    }
                )
            }

            // Durante o carregamento/erro (evento ainda null) mantém um botão de
            // voltar acessível no topo, sob a status bar.
            if (state.event == null && showBackButton) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
//                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(8.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                }
            }

            if (state.isLoading) {
                LinearWavyProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }

            state.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }
    }
}

// ─── Conteúdo: hero colapsável + lista de participantes ─────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDetailContent(
    event: EventDomain,
    game: GameDomain?,
    rsvpsByStatus: Map<RsvpStatus, List<RsvpDomain>>,
    showBackButton: Boolean,
    isOnline: Boolean,
    canManage: Boolean,
    showMoreMenu: Boolean,
    onMoreMenuChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRemoveRsvp: () -> Unit,
    onCopyInvite: () -> Unit
) {
    val density = LocalDensity.current
    val statusBarTopPx = WindowInsets.statusBars.getTop(density).toFloat()

    // Alturas do hero: expandido (com imagem grande) → colapsado (TopAppBar).
    // Ambas incluem a área da status bar, já que o hero desenha edge-to-edge.
    val expandedPx = with(density) { 300.dp.toPx() }
    val collapsedPx = with(density) { 56.dp.toPx() }

    var heroHeightPx by remember(expandedPx) { mutableFloatStateOf(expandedPx) }

    // Conecta o scroll da lista ao hero: rolar para cima encolhe o hero antes de
    // mover a lista (onPreScroll); rolar para baixo expande o hero só depois que a
    // lista chegou ao topo (onPostScroll). Esse é o padrão clássico de toolbar
    // colapsável e evita o bug de expandir o header no meio da lista.
    val connection = remember(expandedPx, collapsedPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0f) {
                    val newHeight = (heroHeightPx + delta).coerceAtLeast(collapsedPx)
                    val consumed = newHeight - heroHeightPx
                    heroHeightPx = newHeight
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (delta > 0f) {
                    val newHeight = (heroHeightPx + delta).coerceAtMost(expandedPx)
                    val consumedY = newHeight - heroHeightPx
                    heroHeightPx = newHeight
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }
        }
    }

    val collapseFraction =
        ((expandedPx - heroHeightPx) / (expandedPx - collapsedPx)).coerceIn(0f, 1f)
    val heroHeightDp = with(density) { heroHeightPx.toDp() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(connection)
    ) {
        // ── Lista (fica atrás do hero; começa logo abaixo dele) ──────────────
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = heroHeightDp + 12.dp,
                bottom = SHEET_PEEK_HEIGHT + 8.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            // Detalhes de esporte (chips) — só para eventos de esporte.
            event.sport?.let { sport ->
                item { SportDetailsCard(sport) }
            }

            // Código de convite (somente em eventos instantâneos).
            if (event.isInstant) {
                item {
                    Spacer(Modifier.height(12.dp))
                    EventInviteCodeCard(inviteCode = event.inviteCode, onCopy = onCopyInvite)
                }
            }

            // Seções de participantes.
            val order = listOf(
                RsvpStatus.YES,
                RsvpStatus.RESERVE,
                RsvpStatus.MAYBE,
                RsvpStatus.NO,
                RsvpStatus.PENDING
            )
            order.forEach { status ->
                val list = rsvpsByStatus[status]
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

        // ── Hero colapsável (sobreposto, na frente da lista) ─────────────────
        EventHero(
            heightDp = heroHeightDp,
            collapseFraction = collapseFraction,
            statusBarTopPx = statusBarTopPx,
            event = event,
            game = game,
            showBackButton = showBackButton,
            isOnline = isOnline,
            canManage = canManage,
            showMoreMenu = showMoreMenu,
            onMoreMenuChange = onMoreMenuChange,
            onBack = onBack,
            onShare = onShare,
            onEdit = onEdit,
            onDelete = onDelete,
            onRemoveRsvp = onRemoveRsvp
        )
    }
}

// ─── Hero colapsável ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventHero(
    heightDp: Dp,
    collapseFraction: Float,
    statusBarTopPx: Float,
    event: EventDomain,
    game: GameDomain?,
    showBackButton: Boolean,
    isOnline: Boolean,
    canManage: Boolean,
    showMoreMenu: Boolean,
    onMoreMenuChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRemoveRsvp: () -> Unit
) {
    val density = LocalDensity.current
    val statusBarTopDp = with(density) { statusBarTopPx.toDp() }

    // As infos sobre a imagem somem um pouco antes do colapso total; o título
    // compacto da TopAppBar aparece em seguida — garantindo um título sempre visível.
    val expandedAlpha = (1f - collapseFraction * 1.4f).coerceIn(0f, 1f)
    val collapsedAlpha = ((collapseFraction - 0.4f) / 0.6f).coerceIn(0f, 1f)
    val titleTextShadow =
        Shadow(
            color = Color.Black
                .copy(alpha = (collapsedAlpha - 0.1f).coerceAtLeast(0f)), // Opacidade da sombra
            offset = Offset(x = 2f, y = 4f),        // Deslocamento (X para os lados, Y para baixo)
            blurRadius = 8f                         // Quão esfumaçada ela é
        )

    val imageUrl = game?.backgroundImage

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp)
            .clipToBounds()
    ) {
        // Fundo: capa do jogo (RAWG) ou placeholder em gradiente do tema.
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (event.eventType == EventType.SPORT)
                        Icons.Outlined.SportsVolleyball else Icons.Outlined.VideogameAsset,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(96.dp)
                )
            }
        }

        // Scrim escuro (topo p/ botões e base p/ textos) — legibilidade.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Black.copy(alpha = 0.45f),
                            0.30f to Color.Transparent,
                            0.60f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.78f)
                        )
                    )
                )
        )

        // ── Linha superior: botões circulares + título compacto ──────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                HeroCircleButton(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    onClick = onBack
                )
            }

            // Título compacto — aparece conforme o hero colapsa.
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleMedium
                    .copy(
                        fontWeight = FontWeight.Bold,
                        shadow = titleTextShadow
                    ),
                color = Color.White.copy(alpha = collapsedAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            HeroCircleButton(
                icon = Icons.Outlined.Share,
                contentDescription = "Compartilhar evento",
                onClick = onShare
            )
            Spacer(Modifier.width(8.dp))
            Box {
                HeroCircleButton(
                    icon = Icons.Outlined.MoreVert,
                    contentDescription = "Mais opções",
                    onClick = { onMoreMenuChange(true) }
                )
                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { onMoreMenuChange(false) }
                ) {
                    if (canManage) {
                        DropdownMenuItem(
                            text = { Text("Editar evento") },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            enabled = isOnline,
                            onClick = {
                                onMoreMenuChange(false)
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Excluir evento",
                                    color = if (isOnline) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = if (isOnline) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                                )
                            },
                            enabled = isOnline,
                            onClick = {
                                onMoreMenuChange(false)
                                onDelete()
                            }
                        )
                    }

                    val hasRsvp = event.rsvpStatus != null
                    val removeLabel = if (event.isInstant)
                        "Remover presença e sair do evento"
                    else
                        "Remover presença"
                    DropdownMenuItem(
                        text = { Text(removeLabel) },
                        leadingIcon = { Icon(Icons.Outlined.Remove, contentDescription = null) },
                        enabled = isOnline && hasRsvp,
                        onClick = {
                            onMoreMenuChange(false)
                            onRemoveRsvp()
                        }
                    )
                }
            }
        }

        // ── Infos expandidas (sobre a imagem, ancoradas na base) ─────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp)
                .graphicsLayer { alpha = expandedAlpha },
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val emoji = if (event.eventType == EventType.SPORT) "🏐" else "🎮"
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = event.name,
                style = MaterialTheme.typography.headlineSmall
                    .copy(
                        fontWeight = FontWeight.Bold,
                        shadow = textShadow
                    ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            event.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium
                        .copy(shadow = textLightShadow),
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Nome do jogo (gameplay) — ícone de controle à esquerda, acima da data.
            event.gameplay?.gameName?.takeIf { it.isNotBlank() }?.let { gameName ->
                HeroInfoLine(icon = Icons.Outlined.VideogameAsset, text = gameName)
            }

            // Data/hora — ícone de calendário à esquerda.
            HeroInfoLine(
                icon = Icons.Outlined.CalendarToday,
                text = event.scheduledDatetime.formatEventDate()
            )

            if (event.confirmedCount > 0) {
                Text(
                    text = "${event.confirmedCount} confirmado${if (event.confirmedCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun HeroCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun HeroInfoLine(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─── Sport details card ─────────────────────────────────────────────────────
// Chips de detalhes específicos de esporte (arena, vagas, duração, preço).
// As infos gerais do evento (nome, descrição, data, confirmados) agora ficam
// sobre a imagem no hero — este card cobre apenas o que é exclusivo de esporte.

@Composable
private fun SportDetailsCard(sport: SportDomain) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        FlowRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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

// ─── Invite code card (eventos instantâneos) ─────────────────────────────────
// Mesmo padrão visual do bloco de código de convite do GroupDetailScreen:
// label pequena em onSurfaceVariant + código em bold na cor primary com
// letterSpacing de 2.sp + IconButton com ícone ContentCopy ao lado direito.
@Composable
private fun EventInviteCodeCard(
    inviteCode: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Código de convite",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = inviteCode,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onCopy) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = "Copiar código",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

// ─── RSVP buttons ─────────────────────────────────────────────────────────────

@Composable
private fun RsvpSheetContent(
    currentStatus: RsvpStatus?,
    acceptReserve: Boolean,
    pricePerPlayer: Double,
    // true quando o usuário é dono do grupo ou criador do evento. Define se o
    // botão abre a tela de gerenciamento (TeamsScreen) ou apenas a formação
    // somente leitura (TeamsReportScreen).
    canManageTeams: Boolean = false,
    onOpenTeams: () -> Unit = {},
    onOpenTeamsReport: () -> Unit = {},
    isPaid: Boolean,
    onPaidChange: (Boolean) -> Unit,
    obs: String,
    onObsChange: (String) -> Unit,
    isObsSaved: Boolean,
    isLoading: Boolean,
    // Quando offline, todas as ações de escrita (RSVP, pago, observação) são
    // bloqueadas. "Gerenciar Times" permanece habilitado porque é navegação —
    // a tela destino tem seu próprio guard de offline.
    isOnline: Boolean,
    // Quando true (evento já passou da data/hora final), os botões de RSVP
    // recebem um overlay com texto "Evento Finalizado!" + blur + clique
    // bloqueado. Demais campos (Gerenciar Times, Pagamento, Observação)
    // permanecem visíveis e interativos normalmente.
    isFinished: Boolean = false,
    onRsvp: (RsvpStatus) -> Unit
) {
    // Estado local para feedback imediato ao clicar, sincronizado com currentStatus
    // quando a API responder e o evento for recarregado.
    var selectedStatus by remember(currentStatus) { mutableStateOf(currentStatus) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        if (selectedStatus == RsvpStatus.YES || selectedStatus == RsvpStatus.RESERVE) {
            // Donos do grupo / criadores do evento veem "Gerenciar Times" e vão
            // para a tela de gerenciamento. Usuários comuns veem "Exibir Times"
            // e vão direto para a tela de Formação dos Times (somente leitura).
            val teamsButtonLabel = if (canManageTeams) "Gerenciar Times" else "Exibir Times"
            val onTeamsButtonClick = if (canManageTeams) onOpenTeams else onOpenTeamsReport
            Button(
                onClick = onTeamsButtonClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    Icons.Outlined.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(teamsButtonLabel, fontWeight = FontWeight.SemiBold)
            }
        }

        SheetSectionLabel("SUA PRESENÇA")

        // Bloco de botões de RSVP. Quando o evento está finalizado, a área
        // recebe blur + overlay com texto "Evento Finalizado!" e o clique nos
        // cards é bloqueado tanto pelo `enabled = false` quanto por uma camada
        // invisível clicável que captura o toque sem fazer nada.
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isFinished) Modifier.blur(8.dp) else Modifier),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                                tint = LoobbyColors.ConfirmedLight,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        isSelected = selectedStatus == RsvpStatus.YES,
                        isLoading = isLoading && selectedStatus == RsvpStatus.YES,
                        enabled = isOnline && !isFinished,
                        selectedBorderColor = LoobbyColors.ConfirmedLight,
                        selectedBgColor = LoobbyColors.ConfirmedBg,
                        selectedLabelColor = LoobbyColors.ConfirmedLight,
                        selectedIconBgColor = LoobbyColors.Confirmed.copy(alpha = 0.4f),
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
                                tint = LoobbyColors.Declined,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        isSelected = selectedStatus == RsvpStatus.NO,
                        isLoading = isLoading && selectedStatus == RsvpStatus.NO,
                        enabled = isOnline && !isFinished,
                        selectedBorderColor = LoobbyColors.Declined,
                        selectedBgColor = LoobbyColors.DeclinedBg,
                        selectedLabelColor = LoobbyColors.Declined,
                        selectedIconBgColor = LoobbyColors.DeclinedIcon.copy(alpha = 0.4f),
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
                                tint = LoobbyColors.Maybe,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        isSelected = selectedStatus == RsvpStatus.MAYBE,
                        isLoading = isLoading && selectedStatus == RsvpStatus.MAYBE,
                        enabled = isOnline && !isFinished,
                        selectedBorderColor = LoobbyColors.Maybe,
                        selectedBgColor = LoobbyColors.MaybeBg,
                        selectedLabelColor = LoobbyColors.Maybe,
                        selectedIconBgColor = LoobbyColors.MaybeIcon.copy(alpha = 0.4f),
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
                                    tint = LoobbyColors.TeamsAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            isSelected = selectedStatus == RsvpStatus.RESERVE,
                            isLoading = isLoading && selectedStatus == RsvpStatus.RESERVE,
                            enabled = isOnline && !isFinished,
                            selectedBorderColor = LoobbyColors.TeamsAccent,
                            selectedBgColor = LoobbyColors.TeamsBg,
                            selectedLabelColor = LoobbyColors.TeamsAccent,
                            selectedIconBgColor = LoobbyColors.TeamsIcon.copy(alpha = 0.4f),
                            onClick = {
                                selectedStatus = RsvpStatus.RESERVE
                                onRsvp(RsvpStatus.RESERVE)
                            }
                        )
                    }
                }
            }

            // Overlay exibido apenas quando o evento está finalizado.
            // - Camada `matchParentSize().clickable {}` captura o toque sobre
            //   a região dos botões de RSVP (e o `indication = null` evita
            //   ripple visual sobre o overlay).
            // - O Surface central exibe o texto "Evento Finalizado!" com
            //   contraste sobre o conteúdo borrado por trás.
            if (isFinished) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "Evento Finalizado!",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Seção de Pagamento — só aparece quando selecionado "Vou"
            if (selectedStatus == RsvpStatus.YES && pricePerPlayer > 0) {
                SheetSectionLabel("PAGAMENTO")
                PaymentToggleRow(
                    price = pricePerPlayer,
                    isPaid = isPaid,
                    onToggle = { onPaidChange(it) },
                    enabled = isOnline
                )
            }

            // seção de Observação — aparece quando qualquer status estiver selecionado
            if (selectedStatus != null) {
                SheetSectionLabel("OBSERVAÇÃO")
                ObsRow(
                    obs = obs,
                    onObsChange = onObsChange,
                    isSaved = isObsSaved,
                    enabled = isOnline
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
    // Quando false (ex.: offline), o card fica esmaecido e não responde ao clique.
    enabled: Boolean = true,
    selectedBorderColor: Color,
    selectedBgColor: Color,
    selectedLabelColor: Color,
    selectedIconBgColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(70.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f }
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
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

            // Conteúdo centralizado vertical e horizontalmente
            Row(
                modifier = Modifier.padding(8.dp).fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Círculo do ícone
                Box(
                    modifier = Modifier
                        .size(36.dp)
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

                Spacer(Modifier.width(8.dp))

                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) selectedLabelColor
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    autoSize = TextAutoSize.StepBased(12.sp,18.sp),
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
                color = LoobbyColors.Confirmed.copy(alpha = 0.12f),
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
                        tint = LoobbyColors.Confirmed,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "Pago",
                        style = MaterialTheme.typography.bodySmall,
                        color = LoobbyColors.Confirmed,
                    )
                }
            }
        }
    }
}

// ─── Sheet section label ──────────────────────────────────────────────────────

// ALTERAÇÃO: label de seção no estilo das imagens (texto pequeno em caps)
@Composable
private fun SheetSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

// ─── Payment toggle row ───────────────────────────────────────────────────────

// ALTERAÇÃO: linha de pagamento com ícone, texto e switch — visível só quando "Vou"
@Composable
private fun PaymentToggleRow(
    price: Double,
    isPaid: Boolean,
    onToggle: (Boolean) -> Unit,
    // Quando offline, o switch não dispara rede; desabilita o controle.
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Ícone
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LoobbyColors.ConfirmedBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CreditCard,
                    contentDescription = null,
                    tint = LoobbyColors.ConfirmedLight,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sua contribuição",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val whole = price.toLong()
                val cents = ((price - whole) * 100).toLong()
                val formattedPrice = "$whole,${cents.toString().padStart(2, '0')}"
                Text(
                    text = "R$ $formattedPrice por pessoa",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = isPaid,
                onCheckedChange = onToggle,
                enabled = enabled
            )
        }
    }
}

// ─── Observation row ──────────────────────────────────────────────────────────

@Composable
private fun ObsRow(
    obs: String,
    onObsChange: (String) -> Unit,
    isSaved: Boolean,
    // Quando offline, o text field entra em enabled=false — o valor permanece
    // visível (se já salvo), mas o usuário não consegue editar/disparar o save.
    enabled: Boolean = true
) {
    val maxChars = 100

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Ícone
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Deixe uma mensagem",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Opcional",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = obs,
                onValueChange = { if (it.length <= maxChars) onObsChange(it) },
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 90.dp),
                placeholder = {
                    Text(
                        text = "Ex: Vou chegar um pouco atrasado, posso levar sorvete...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // "Salvo!" aparece só quando isSaved = true
                        if (isSaved) {
                            Text(
                                text = "Salvo!",
                                style = MaterialTheme.typography.labelSmall,
                                color = LoobbyColors.ConfirmedLight
                            )
                        } else {
                            Spacer(Modifier.width(1.dp))
                        }
                        Text(
                            text = "${obs.length} / $maxChars",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )
        }
    }
}

// ─── Share dialog ─────────────────────────────────────────────────────────────

// Diálogo de compartilhamento com personalização. Mantemos o formato de
// AlertDialog (visual de "dialog") mas com conteúdo customizado, oferecendo:
//  • toggle emoji/texto para identificar os campos do evento
//  • toggle de inclusão da lista de presença + chips multi-seleção dos status
//    (apenas status que têm participantes; todos marcados por padrão)
//  • toggle de exibição de confirmação de pagamento (desmarcado por padrão)
//  • toggle de exibição dos comentários (obs) dos participantes (desmarcado
//    por padrão)
//  • pré-visualização ao vivo (somente leitura, com altura para ~7 linhas e
//    rolagem interna)
@Composable
private fun ShareDialog(
    event: EventDomain,
    rsvps: List<RsvpDomain>,
    onShare: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Repositório de preferências locais (Settings). É lido a cada abertura
    // do diálogo (ao entrar em composição) e atualizado a cada toggle.
    val sharePrefs: SharePreferencesRepository = koinInject()

    // Status oferecidos no chip-list (sem PENDING — fora do escopo do diálogo).
    val candidateStatuses = remember {
        listOf(RsvpStatus.YES, RsvpStatus.RESERVE, RsvpStatus.MAYBE, RsvpStatus.NO)
    }
    // Apenas status que possuem participantes aparecem como chip.
    val availableStatuses = remember(rsvps) {
        candidateStatuses.filter { status -> rsvps.any { it.status == status } }
    }
    val hasAnyRsvp = availableStatuses.isNotEmpty()

    // Estado inicial vindo das preferências do usuário (com defaults para
    // primeira execução).
    var useEmoji by remember { mutableStateOf(sharePrefs.getUseEmoji()) }
    // Master checkbox da lista — clamp por hasAnyRsvp (se não há ninguém,
    // não faz sentido marcar).
    var includeList by remember(hasAnyRsvp) {
        mutableStateOf(sharePrefs.getIncludeList() && hasAnyRsvp)
    }
    var includePayment by remember { mutableStateOf(sharePrefs.getIncludePayment()) }
    var includeObservations by remember { mutableStateOf(sharePrefs.getIncludeObservations()) }
    // Status persistidos (interseção com os disponíveis no evento atual).
    // Se nunca foi configurado, todos os disponíveis vêm marcados.
    var selectedStatuses by remember(availableStatuses) {
        val saved = sharePrefs.getSelectedStatuses()
        val initial = if (saved == null) {
            availableStatuses.toSet()
        } else {
            saved.intersect(availableStatuses.toSet())
        }
        mutableStateOf(initial)
    }

    val effectiveStatuses = if (includeList) selectedStatuses else emptySet()

    val previewText = remember(
        event, rsvps, useEmoji, effectiveStatuses, includePayment, includeObservations
    ) {
        buildShareText(
            event = event,
            rsvps = rsvps,
            useEmoji = useEmoji,
            includedStatuses = effectiveStatuses,
            includePaymentConfirm = includePayment,
            includeObservations = includeObservations
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compartilhar evento") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // ── Identificação dos campos: emoji ou texto ──────────────────
                CheckboxRow(
                    checked = useEmoji,
                    onCheckedChange = {
                        useEmoji = it
                        sharePrefs.setUseEmoji(it)
                    },
                    label = "Usar emojis"
                )

                // ── Lista de presença ────────────────────────────────────────
                CheckboxRow(
                    checked = includeList && hasAnyRsvp,
                    onCheckedChange = {
                        includeList = it
                        sharePrefs.setIncludeList(it)
                    },
                    enabled = hasAnyRsvp,
                    label = if (hasAnyRsvp) "Incluir lista de presença"
                            else "Incluir lista de presença (sem participantes)"
                )

                if (includeList && hasAnyRsvp) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        availableStatuses.forEach { status ->
                            val isSelected = status in selectedStatuses
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val newSet = if (isSelected)
                                        selectedStatuses - status
                                    else
                                        selectedStatuses + status
                                    selectedStatuses = newSet
                                    sharePrefs.setSelectedStatuses(newSet)
                                },
                                label = { Text(status.shareChipLabel()) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                )
                            )
                        }
                    }
                }

                // ── Confirmação de pagamento ─────────────────────────────────
                event.sport?.pricePerPlayer?.let { pricePerPlayer ->
                    if (pricePerPlayer > 0.0)
                        CheckboxRow(
                            checked = includePayment,
                            onCheckedChange = {
                                includePayment = it
                                sharePrefs.setIncludePayment(it)
                            },
                            label = "Exibir confirmação de pagamento"
                        )
                }

                // ── Comentários dos participantes ────────────────────────────
                CheckboxRow(
                    checked = includeObservations,
                    onCheckedChange = {
                        includeObservations = it
                        sharePrefs.setIncludeObservations(it)
                    },
                    label = "Exibir comentários"
                )

                Spacer(Modifier.height(4.dp))

                // ── Pré-visualização ─────────────────────────────────────────
                Text(
                    text = "Pré-visualização",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Altura fixa para garantir ~7 linhas visíveis e permitir
                // rolagem interna do TextField quando o conteúdo for maior.
                OutlinedTextField(
                    value = previewText,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onShare(
                        buildString {
                            appendLine(previewText)
                            appendLine("_Criado pelo app Loobby_")
                        }
                    )
                }
            ) {
                Text("Compartilhar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/** Linha "checkbox + label" clicável (a linha inteira aciona o toggle). */
@Composable
private fun CheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    enabled: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            color = if (enabled)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Rótulo curto de cada status para uso no chip-list do diálogo. */
private fun RsvpStatus.shareChipLabel(): String = when (this) {
    RsvpStatus.YES     -> "Confirmados"
    RsvpStatus.RESERVE -> "Reservas"
    RsvpStatus.MAYBE   -> "Talvez"
    RsvpStatus.NO      -> "Não vão"
    RsvpStatus.PENDING -> "Pendentes"
}

// ─── Delete confirmation dialog ──────────────────────────────────────────────

// diálogo de confirmação para excluir evento
@Composable
private fun DeleteEventDialog(
    eventName: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = { Text("Excluir evento?") },
        text = {
            Text("Tem certeza que deseja excluir \"$eventName\"? Essa ação não pode ser desfeita. Todos os RSVPs e dados do evento serão removidos.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Excluir", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text("Cancelar")
            }
        }
    )
}

// ─── Remove RSVP confirmation dialog ─────────────────────────────────────────

// Diálogo de confirmação para remover a própria presença (RSVP) do evento.
// Em eventos instantâneos, removendo o RSVP o usuário também "sai" da lista
// — explicitamos isso no texto para o usuário entender o efeito.
@Composable
private fun RemoveRsvpDialog(
    isInstant: Boolean,
    isRemoving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = if (isInstant) "Sair do evento?" else "Remover presença?"
    val body = if (isInstant)
        "Sua presença será removida e você sairá deste evento instantâneo. " +
        "Para entrar novamente, você precisará do código de convite."
    else
        "Tem certeza que deseja remover sua presença deste evento? " +
        "Você poderá confirmar novamente a qualquer momento."

    AlertDialog(
        onDismissRequest = { if (!isRemoving) onDismiss() },
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isRemoving,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isRemoving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Remover", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isRemoving
            ) {
                Text("Cancelar")
            }
        }
    )
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