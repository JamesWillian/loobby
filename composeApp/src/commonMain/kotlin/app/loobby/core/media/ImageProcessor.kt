package app.loobby.core.media

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Processador de imagem multiplataforma.
 * Cada plataforma implementa via expect/actual.
 */
expect object ImageProcessor {

    /**
     * Decodifica ByteArray → ImageBitmap (para exibir na UI de crop).
     */
    fun decode(bytes: ByteArray): ImageBitmap

    /**
     * Processa a imagem para upload:
     * 1. Decodifica os bytes originais
     * 2. Recorta a região definida por (cropX, cropY, cropSize) em pixels da imagem original
     * 3. Redimensiona para [targetSize] x [targetSize]
     * 4. Recodifica como JPEG com qualidade [quality] (0–100)
     *
     * @return ByteArray do JPEG final, pronto para upload
     */
    fun cropAndResize(
        bytes: ByteArray,
        cropX: Int,
        cropY: Int,
        cropSize: Int,
        targetSize: Int = 512,
        quality: Int = 80
    ): ByteArray
}