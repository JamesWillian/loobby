package app.loobby.feature.gameplays.presentation.detail

import androidx.compose.runtime.Composable
import app.loobby.core.ui.components.ConfirmDialog
import app.loobby.feature.gameplays.presentation.detail.components.ReportCommentDialog
import app.loobby.feature.gameplays.presentation.model.BallotEntry

/** O diálogo aberto na tela da gameplay; null = nenhum. */
sealed interface DetailDialog {
    data object End : DetailDialog
    data class Delete(val isEnded: Boolean) : DetailDialog
    data object Leave : DetailDialog
    data class RemoveGame(val entry: BallotEntry) : DetailDialog
    data class DeleteComment(val commentId: String) : DetailDialog
    data class ReportComment(val commentId: String) : DetailDialog
}

/** Mostra o [dialog] da vez e entrega a confirmação ao ViewModel. */
@Composable
fun GameplayDetailDialogHost(
    dialog: DetailDialog?,
    vm: GameplayDetailViewModel,
    onDismiss: () -> Unit,
) {
    when (dialog) {
        null -> Unit
        DetailDialog.End -> ConfirmDialog(
            title = "Encerrar a gameplay?",
            message = "Ninguém mais responde, vota ou comenta. Ela continua aparecendo para consulta.",
            confirmLabel = "Encerrar",
            destructive = true,
            onConfirm = { onDismiss(); vm.end() },
            onDismiss = onDismiss,
        )
        is DetailDialog.Delete -> ConfirmDialog(
            title = "Excluir a gameplay?",
            message = if (dialog.isEnded) {
                "Ela some para todo mundo, inclusive do Histórico de quem participou. Não dá para desfazer."
            } else {
                "Ela some para todo mundo, com os votos e os comentários. Quem marcou Vou recebe um aviso. " +
                    "Não dá para desfazer."
            },
            confirmLabel = "Excluir",
            destructive = true,
            onConfirm = { onDismiss(); vm.delete() },
            onDismiss = onDismiss,
        )
        DetailDialog.Leave -> ConfirmDialog(
            title = "Sair da gameplay?",
            message = "Você deixa de ver esta gameplay. Para voltar, precisa do convite de novo.",
            confirmLabel = "Sair",
            destructive = true,
            onConfirm = { onDismiss(); vm.leave() },
            onDismiss = onDismiss,
        )
        is DetailDialog.RemoveGame -> ConfirmDialog(
            title = "Tirar ${dialog.entry.name} da votação?",
            message = "Os votos nesse jogo também saem.",
            confirmLabel = "Tirar",
            destructive = true,
            onConfirm = { onDismiss(); vm.removeGame(dialog.entry.votingGameId) },
            onDismiss = onDismiss,
        )
        is DetailDialog.DeleteComment -> ConfirmDialog(
            title = "Apagar o comentário?",
            message = "Ele some para todo mundo.",
            confirmLabel = "Apagar",
            destructive = true,
            onConfirm = { onDismiss(); vm.deleteComment(dialog.commentId) },
            onDismiss = onDismiss,
        )
        is DetailDialog.ReportComment -> ReportCommentDialog(
            onConfirm = { reason -> onDismiss(); vm.reportComment(dialog.commentId, reason) },
            onDismiss = onDismiss,
        )
    }
}
