package app.loobby.core.network

/**
 * Lançada quando uma operação de escrita (create/update/delete/confirm) é
 * tentada com o aparelho sem conexão de rede.
 *
 * O app trabalha em modo "offline somente leitura": dados já carregados
 * continuam visíveis, mas qualquer mutação é bloqueada até voltar online.
 * ViewModels devem capturar esta exceção e converter em feedback visual
 * para o usuário (snackbar/toast), NÃO enfileirar a operação.
 */
class OfflineException(
    message: String = "Você precisa estar online para realizar esta ação."
) : Exception(message)
