# =============================================================================
# Loobby — regras de R8/ProGuard para build de release
# =============================================================================
# R8 (default-rules) já cobre Compose, Kotlin stdlib e Coroutines. As regras
# aqui são para libs que dependem de reflection / serialization e seriam
# quebradas pela ofuscação/shrink: Koin, Ktor, kotlinx.serialization, SQLDelight,
# Firebase Admin SDK e os modelos da própria app que viajam pela rede.
# =============================================================================

# ---- Atributos preservados globalmente ----
# Anotações são lidas em runtime por kotlinx.serialization e reflection.
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations, AnnotationDefault
# Mantém nomes de arquivo/linhas em stack traces (útil pra debug em release).
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# =============================================================================
# kotlinx.serialization
# =============================================================================
# Serializers gerados pelo plugin (classes XxxSerializer) são acessados via
# reflection. Sem isso, qualquer @Serializable quebra em runtime.
-keep,includedescriptorclasses class **$$serializer { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Modelos da app — DTOs de request/response, modelos de cache, tokens.
# Mantém todos os campos para garantir round-trip de JSON correto após R8.
-keep class app.loobby.**.data.model.** { *; }
-keep class app.loobby.**.dto.** { *; }
-keep class app.loobby.core.storage.StoredTokens { *; }
-keepclassmembers class app.loobby.** {
    @kotlinx.serialization.Serializable <fields>;
}

# =============================================================================
# Ktor (client core + engines + content negotiation)
# =============================================================================
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.debug.**
# Slf4j é dependência transitiva mas não é incluída — só silenciar.
-dontwarn org.slf4j.**

# =============================================================================
# Koin
# =============================================================================
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# =============================================================================
# SQLDelight
# =============================================================================
-keep class app.cash.sqldelight.** { *; }
-keep class app.loobby.db.** { *; }
-dontwarn app.cash.sqldelight.**

# =============================================================================
# Firebase / Google Sign-In / Credential Manager
# =============================================================================
# O SDK do Firebase já vem com regras consumidas — só garantir as classes do
# nosso service de FCM.
-keep class app.loobby.**.notifications.platform.** { *; }
# Credential Manager (Google Sign-In moderno) usa reflection para detectar
# providers no classpath.
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# =============================================================================
# Coroutines — serviço descoberto por classpath
# =============================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# =============================================================================
# Diversos — silenciar warnings de libs opcionais não embarcadas
# =============================================================================
-dontwarn java.lang.management.**
-dontwarn javax.naming.**
