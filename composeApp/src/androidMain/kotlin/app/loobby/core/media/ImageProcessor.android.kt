package app.loobby.core.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

actual object ImageProcessor {

    actual fun decode(bytes: ByteArray): ImageBitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
    }

    actual fun cropAndResize(
        bytes: ByteArray,
        cropX: Int,
        cropY: Int,
        cropSize: Int,
        targetSize: Int,
        quality: Int
    ): ByteArray {
        val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        // ── Clamp para não estourar os limites da imagem ──
        val safeX = cropX.coerceIn(0, (original.width - 1).coerceAtLeast(0))
        val safeY = cropY.coerceIn(0, (original.height - 1).coerceAtLeast(0))
        val maxSize = minOf(cropSize, original.width - safeX, original.height - safeY)
            .coerceAtLeast(1)

        // ── Crop ──
        val cropped = Bitmap.createBitmap(original, safeX, safeY, maxSize, maxSize)

        // ── Resize ──
        val resized = if (cropped.width != targetSize || cropped.height != targetSize) {
            Bitmap.createScaledBitmap(cropped, targetSize, targetSize, true)
        } else {
            cropped
        }

        // ── Encode JPEG ──
        val output = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, quality, output)

        // ── Liberar bitmaps intermediários ──
        if (resized !== cropped) resized.recycle()
        if (cropped !== original) cropped.recycle()
        original.recycle()

        return output.toByteArray()
    }
}