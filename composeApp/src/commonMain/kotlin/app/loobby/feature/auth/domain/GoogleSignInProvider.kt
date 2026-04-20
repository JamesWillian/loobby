package app.loobby.feature.auth.domain

import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
@kotlin.native.ObjCName("GoogleSignInProvider")
interface GoogleSignInProvider {
    @Throws(Throwable::class)
    suspend fun signIn(): String // retorna o idToken ou Throwable
}