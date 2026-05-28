package app.loobby.feature.auth.domain.usecase

import app.loobby.core.storage.StoredTokens
import app.loobby.feature.auth.data.model.AuthResponse
import app.loobby.feature.auth.data.model.UserMeResponse
import app.loobby.feature.auth.data.model.UserProfileResponse
import app.loobby.feature.auth.domain.model.AuthSession
import app.loobby.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * ──────────────────────────────────────────────────────────────────────────
 *  EXEMPLO 3 — Testando um Use Case que depende de um Repository.
 * ──────────────────────────────────────────────────────────────────────────
 *
 * Aqui aparecem dois conceitos novos:
 *
 *  1. **runTest { ... }** — vem de `kotlinx-coroutines-test`. É o jeito
 *     correto de testar funções `suspend`. Ele cria um scheduler virtual
 *     que executa coroutines imediatamente, mesmo quando há `delay`.
 *
 *  2. **Fake Repository** — em vez de usar uma lib de mock (Mockk, Mockito),
 *     escrevemos uma implementação "de mentira" da interface
 *     `AuthRepository`. Em Kotlin Multiplatform isso é o padrão recomendado,
 *     porque Mockk não roda em todos os targets.
 *
 *     Um Fake:
 *      - Implementa a interface real.
 *      - Tem comportamento controlável (você define o que retorna).
 *      - Tem "espiões" — flags/contadores que o teste lê depois para
 *        verificar o que foi chamado.
 *
 * O que o `LoginUseCase` faz:
 *  - Se o usuário atual é anônimo, ele salva o `refreshToken` anônimo antes
 *    de chamar `repository.login(...)` (para permitir migração depois).
 *  - Se não é anônimo, vai direto pro login.
 *
 * Esses são os 2 caminhos que vamos testar.
 */
class LoginUseCaseTest {

    // ── Caminho 1: usuário anônimo ────────────────────────────────────────
    @Test
    fun login_quandoUsuarioAnonimo_salvaTokenAnonimoAntesDeLogar() = runTest {
        // Arrange
        val tokensAtuais = StoredTokens(
            accessToken = "access-anon",
            refreshToken = "refresh-anon",
            userId = "anon-1",
            username = "anon",
            roles = listOf("ANON")
        )
        val fakeRepo = FakeAuthRepository(
            isAnonymous = true,
            storedTokens = tokensAtuais,
            loginResponse = authResponseDummy()
        )
        val useCase = LoginUseCase(fakeRepo)

        // Act
        val resposta = useCase("user@email.com", "senha123")

        // Assert
        // 1) O token anônimo foi salvo com o refreshToken atual.
        assertEquals("refresh-anon", fakeRepo.savedAnonymousToken)
        // 2) O login no repositório foi chamado com os parâmetros corretos.
        assertEquals("user@email.com" to "senha123", fakeRepo.lastLoginCall)
        // 3) A resposta do repositório foi propagada.
        assertEquals("access-novo", resposta.accessToken)
    }

    // ── Caminho 2: usuário NÃO anônimo ────────────────────────────────────
    @Test
    fun login_quandoUsuarioNaoAnonimo_naoSalvaTokenAnonimo() = runTest {
        val fakeRepo = FakeAuthRepository(
            isAnonymous = false,
            storedTokens = null,
            loginResponse = authResponseDummy()
        )
        val useCase = LoginUseCase(fakeRepo)

        useCase("a@b.com", "x")

        // O ramo do `if` não deve ter sido executado.
        assertNull(fakeRepo.savedAnonymousToken)
        assertEquals("a@b.com" to "x", fakeRepo.lastLoginCall)
    }

    // ── Caminho 3: anônimo mas sem tokens armazenados ─────────────────────
    @Test
    fun login_quandoAnonimoSemTokens_naoQuebraESegueLogin() = runTest {
        // Esse é um caso "estranho" mas possível: o repo diz que é anônimo
        // mas o `storedTokensFlow` está vazio. O código usa `.first()` que
        // emite null, e o `.let { }` simplesmente não executa o bloco.
        // O teste documenta que isso NÃO quebra.
        val fakeRepo = FakeAuthRepository(
            isAnonymous = true,
            storedTokens = null,
            loginResponse = authResponseDummy()
        )
        val useCase = LoginUseCase(fakeRepo)

        useCase("a@b.com", "x")

        assertNull(fakeRepo.savedAnonymousToken)
        assertTrue(fakeRepo.lastLoginCall != null, "login deveria ter sido chamado")
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private fun authResponseDummy() = AuthResponse(
        accessToken = "access-novo",
        refreshToken = "refresh-novo",
        expiresIn = 3600L,
        userId = "user-1",
        username = "joao",
        roles = listOf("USER")
    )
}

/**
 * Fake Repository — implementa só o que o teste precisa.
 *
 * Para os métodos que não usamos neste teste, lançamos
 * `NotImplementedError`. Se algum dia outro teste passar a usá-los, ele
 * vai falhar com uma mensagem clara dizendo o que falta implementar.
 *
 * As propriedades públicas (`savedAnonymousToken`, `lastLoginCall`)
 * funcionam como "espiões": o teste lê elas depois do `act` pra verificar
 * o que foi chamado.
 */
private class FakeAuthRepository(
    private val isAnonymous: Boolean,
    storedTokens: StoredTokens?,
    private val loginResponse: AuthResponse
) : AuthRepository {

    // Espiões
    var savedAnonymousToken: String? = null
        private set
    var lastLoginCall: Pair<String, String>? = null
        private set

    override val storedTokensFlow: Flow<StoredTokens?> =
        MutableStateFlow(storedTokens)

    override val sessionFlow: Flow<AuthSession?> = MutableStateFlow(null)

    override suspend fun isAnonymous(): Boolean = isAnonymous

    override suspend fun saveAnonymousToken(anonymousToken: String) {
        savedAnonymousToken = anonymousToken
    }

    override suspend fun login(email: String, password: String): AuthResponse {
        lastLoginCall = email to password
        return loginResponse
    }

    // ── Não usados neste teste ─────────────────────────────────────────────
    override suspend fun initializeAnonymousIfNeeded(): AuthSession = notUsed()
    override suspend fun loginWithGoogle(idToken: String): AuthResponse = notUsed()
    override suspend fun register(email: String, password: String): AuthResponse = notUsed()
    override suspend fun currentUserId(): String? = notUsed()
    override suspend fun getSavedAnonymousToken(): String? = notUsed()
    override suspend fun clearSavedAnonymousToken() = notUsed()
    override suspend fun logout() = notUsed()
    override suspend fun refreshIfPossible(): AuthSession? = notUsed()
    override suspend fun recoverAnonymous(anonymousToken: String): AuthSession = notUsed()
    override suspend fun resendVerification() = notUsed()
    override suspend fun forgotPassword(email: String) = notUsed()
    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) = notUsed()
    override suspend fun getProfile(): UserMeResponse = notUsed()
    override suspend fun updateProfile(
        username: String?,
        displayname: String?
    ): UserProfileResponse = notUsed()
    override suspend fun uploadAvatar(
        imageBytes: ByteArray,
        fileName: String
    ): UserProfileResponse = notUsed()
    override suspend fun deleteAccount(password: String) = notUsed()

    private fun notUsed(): Nothing =
        throw NotImplementedError("Este método do Fake não foi usado por este teste.")
}
