package app.loobby.feature.gameplays.data.mapper

import app.loobby.feature.gameplays.data.model.CommentResponse
import app.loobby.feature.gameplays.data.model.CountsResponse
import app.loobby.feature.gameplays.data.model.GameRefResponse
import app.loobby.feature.gameplays.data.model.GameplayResponse
import app.loobby.feature.gameplays.data.model.GameplaySummaryResponse
import app.loobby.feature.gameplays.data.model.InvitePreviewResponse
import app.loobby.feature.gameplays.data.model.ParticipantResponse
import app.loobby.feature.gameplays.data.model.PermissionsResponse
import app.loobby.feature.gameplays.data.model.RoundVotesResponse
import app.loobby.feature.gameplays.data.model.UserRefResponse
import app.loobby.feature.gameplays.data.model.VotingGameResponse
import app.loobby.feature.gameplays.data.model.VotingResponse
import app.loobby.feature.gameplays.domain.model.GameInfo
import app.loobby.feature.gameplays.domain.model.Gameplay
import app.loobby.feature.gameplays.domain.model.GameplayComment
import app.loobby.feature.gameplays.domain.model.GameplayInvitePreview
import app.loobby.feature.gameplays.domain.model.GameplayMode
import app.loobby.feature.gameplays.domain.model.GameplayPermissions
import app.loobby.feature.gameplays.domain.model.GameplayStatus
import app.loobby.feature.gameplays.domain.model.GameplaySummary
import app.loobby.feature.gameplays.domain.model.GameplayUser
import app.loobby.feature.gameplays.domain.model.Participant
import app.loobby.feature.gameplays.domain.model.ParticipationCounts
import app.loobby.feature.gameplays.domain.model.ParticipationStatus
import app.loobby.feature.gameplays.domain.model.Platform
import app.loobby.feature.gameplays.domain.model.RoundVotes
import app.loobby.feature.gameplays.domain.model.Voting
import app.loobby.feature.gameplays.domain.model.VotingGame
import app.loobby.feature.gameplays.domain.model.VotingResult
import kotlin.time.Instant

// Resposta da API → domínio. Valor que o app não conhece (uma plataforma nova, por
// exemplo) é descartado ou cai no padrão, em vez de derrubar a tela.

fun GameplayResponse.toDomain(): Gameplay = Gameplay(
    id = id,
    groupId = groupId,
    groupName = groupName,
    name = name,
    description = description?.takeIf { it.isNotBlank() },
    mode = GameplayMode.fromId(mode),
    platforms = platforms.toPlatforms(),
    scheduledAt = Instant.parse(scheduledAt),
    autoEndAt = Instant.parse(autoEndAt),
    status = GameplayStatus.fromId(status),
    endedAt = endedAt?.let(Instant::parse),
    endedBy = endedBy?.toDomain(),
    inviteCode = inviteCode,
    creator = creator.toDomain(),
    createdAt = Instant.parse(createdAt),
    myStatus = ParticipationStatus.fromId(myStatus),
    permissions = permissions.toDomain(),
    counts = counts.toDomain(),
    participants = participants.map { it.toDomain() },
    game = game?.toDomain(),
    voting = voting?.toDomain(),
    gameOwners = game?.owners.orEmpty().map { it.toDomain() },
)

fun GameplaySummaryResponse.toDomain(): GameplaySummary = GameplaySummary(
    id = id,
    groupId = groupId,
    groupName = groupName,
    name = name,
    mode = GameplayMode.fromId(mode),
    platforms = platforms.toPlatforms(),
    scheduledAt = Instant.parse(scheduledAt),
    autoEndAt = Instant.parse(autoEndAt),
    status = GameplayStatus.fromId(status),
    endedAt = endedAt?.let(Instant::parse),
    creator = creator.toDomain(),
    myStatus = ParticipationStatus.fromId(myStatus),
    goingCount = goingCount,
    goingAvatars = goingAvatars,
    game = game?.toDomain(),
    leading = leading.map { it.toDomain() },
    tie = tie,
)

fun InvitePreviewResponse.toDomain(): GameplayInvitePreview = GameplayInvitePreview(
    id = id,
    name = name,
    mode = GameplayMode.fromId(mode),
    platforms = platforms.toPlatforms(),
    scheduledAt = Instant.parse(scheduledAt),
    status = GameplayStatus.fromId(status),
    creator = creator.toDomain(),
    goingCount = goingCount,
    joined = joined,
)

fun CommentResponse.toDomain(): GameplayComment = GameplayComment(
    id = id,
    gameplayId = gameplayId,
    author = author.toDomain(),
    body = body,
    createdAt = Instant.parse(createdAt),
    canDelete = canDelete,
    reportedByMe = reportedByMe,
)

private fun UserRefResponse.toDomain() = GameplayUser(
    id = id,
    username = username,
    displayname = displayname,
    avatarUrl = avatarUrl,
)

private fun GameRefResponse.toDomain() = GameInfo(catalogId = gameId, name = name, coverUrl = coverUrl)

private fun PermissionsResponse.toDomain() = GameplayPermissions(
    respond = respond,
    vote = vote,
    editGames = editGames,
    comment = comment,
    manage = manage,
    configureVoting = configureVoting,
    tiebreak = tiebreak,
    leave = leave,
    moderate = moderate,
    delete = delete,
)

private fun CountsResponse.toDomain() = ParticipationCounts(going = going, notGoing = notGoing, pending = pending)

private fun ParticipantResponse.toDomain() = Participant(
    user = user.toDomain(),
    status = ParticipationStatus.fromId(status),
    isAnonymous = isAnonymous,
)

private fun VotingResponse.toDomain() = Voting(
    finalists = finalists,
    multiVote = multiVote,
    locked = locked,
    round = round,
    games = games.map { it.toDomain() },
    result = VotingResult(round = result.round, finalistIds = result.finalistIds, tie = result.tie),
)

private fun VotingGameResponse.toDomain() = VotingGame(
    id = id,
    catalogId = gameId,
    name = name,
    coverUrl = coverUrl,
    addedBy = addedBy,
    candidate = candidate,
    myVote = myVote,
    rounds = rounds.map { it.toDomain() },
    owners = owners.map { it.toDomain() },
)

private fun RoundVotesResponse.toDomain() = RoundVotes(
    round = round,
    votes = votes,
    voters = voters.map { it.toDomain() },
)

private fun List<String>.toPlatforms(): List<Platform> = mapNotNull(Platform::fromId)
