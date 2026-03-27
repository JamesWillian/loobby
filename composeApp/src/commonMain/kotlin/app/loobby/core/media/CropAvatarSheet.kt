package app.loobby.core.media

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * Resultado do recorte — contém os bytes JPEG processados.
 */
data class CroppedResult(
    val bytes: ByteArray,
    val fileName: String
)

/**
 * Sheet/tela de recorte de avatar.
 *
 * Exibe a imagem com um overlay quadrado centralizado.
 * O usuário pode arrastar (pan) e dar pinch-to-zoom na imagem.
 * Ao confirmar, a região visível no quadrado é recortada e redimensionada.
 *
 * @param imageBytes bytes da imagem original selecionada
 * @param onConfirm callback com o resultado cortado + redimensionado
 * @param onDismiss callback para fechar sem cortar
 * @param targetSize tamanho final do avatar em pixels (default 512)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropAvatarSheet(
    imageBytes: ByteArray,
    onConfirm: (CroppedResult) -> Unit,
    onDismiss: () -> Unit,
    targetSize: Int = 512
) {
    // ── Decodifica a imagem para exibição ──
    val imageBitmap = remember(imageBytes) {
        ImageProcessor.decode(imageBytes)
    }

    val imgW = imageBitmap.width.toFloat()
    val imgH = imageBitmap.height.toFloat()

    // ── Estado do container ──
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // ── Tamanho do crop box (80% do menor lado) ──
    val cropBoxSize by remember(containerSize) {
        derivedStateOf {
            if (containerSize.width == 0 || containerSize.height == 0) 0f
            else minOf(containerSize.width, containerSize.height) * 0.82f
        }
    }

    // ── baseScale: escala para que o menor lado da imagem cubra o crop box ──
    val baseScale by remember(containerSize, imageBitmap) {
        derivedStateOf {
            if (cropBoxSize == 0f || imgW == 0f || imgH == 0f) 1f
            else {
                val scaleW = cropBoxSize / imgW
                val scaleH = cropBoxSize / imgH
                maxOf(scaleW, scaleH)
            }
        }
    }

    // ── Gestos do usuário ──
    var userScale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val totalScale by derivedStateOf { baseScale * userScale }

    // ── Constrains: limitar offset para que a imagem sempre cubra o crop box ──
    fun constrainOffset(ox: Float, oy: Float, scale: Float): Pair<Float, Float> {
        val drawW = imgW * scale
        val drawH = imgH * scale
        val halfCrop = cropBoxSize / 2f
        // A imagem deve se estender além do crop box em todas as direções
        val maxOffX = (drawW / 2f - halfCrop).coerceAtLeast(0f)
        val maxOffY = (drawH / 2f - halfCrop).coerceAtLeast(0f)
        return ox.coerceIn(-maxOffX, maxOffX) to oy.coerceIn(-maxOffY, maxOffY)
    }

    // ── Estado de processamento ──
    var isProcessing by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // ── Top bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Fechar",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Recortar foto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                // Espaço balanceado
                Spacer(Modifier.size(48.dp))
            }

            // ── Área de crop ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged { containerSize = it }
                    .pointerInput(baseScale) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (userScale * zoom).coerceIn(1f, 5f)
                            val newTotalScale = baseScale * newScale
                            val (cx, cy) = constrainOffset(
                                offsetX + pan.x,
                                offsetY + pan.y,
                                newTotalScale
                            )
                            userScale = newScale
                            offsetX = cx
                            offsetY = cy
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (containerSize.width > 0 && containerSize.height > 0) {

                    val cw = containerSize.width.toFloat()
                    val ch = containerSize.height.toFloat()
                    val cropLeft = (cw - cropBoxSize) / 2f
                    val cropTop = (ch - cropBoxSize) / 2f

                    // ── Imagem ──
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val drawW = imgW * totalScale
                        val drawH = imgH * totalScale
                        val imgLeft = (cw - drawW) / 2f + offsetX
                        val imgTop = (ch - drawH) / 2f + offsetY

                        drawImage(
                            image = imageBitmap,
                            srcOffset = IntOffset.Zero,
                            srcSize = IntSize(imageBitmap.width, imageBitmap.height),
                            dstOffset = IntOffset(imgLeft.toInt(), imgTop.toInt()),
                            dstSize = IntSize(drawW.toInt(), drawH.toInt())
                        )
                    }

                    // ── Overlay escuro com recorte quadrado ──
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    ) {
                        // Fundo escuro
                        drawRect(Color.Black.copy(alpha = 0.55f))

                        // Recorta o quadrado (limpa o overlay nessa região)
                        drawRect(
                            color = Color.Transparent,
                            topLeft = Offset(cropLeft, cropTop),
                            size = Size(cropBoxSize, cropBoxSize),
                            blendMode = BlendMode.Clear
                        )

                        // Borda branca do crop box
                        drawRect(
                            color = Color.White.copy(alpha = 0.7f),
                            topLeft = Offset(cropLeft, cropTop),
                            size = Size(cropBoxSize, cropBoxSize),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 2f
                            )
                        )
                    }

                    // ── Preview circular sutil (indicação de como ficará o avatar) ──
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = cropBoxSize / 2f
                        val centerX = cw / 2f
                        val centerY = ch / 2f

                        drawCircle(
                            color = Color.White.copy(alpha = 0.15f),
                            radius = radius,
                            center = Offset(centerX, centerY),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                        )
                    }
                }
            }

            // ── Botão confirmar ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (isProcessing) return@Button
                        isProcessing = true

                        val cw = containerSize.width.toFloat()
                        val ch = containerSize.height.toFloat()

                        // Mapear crop box de volta para coordenadas da imagem original
                        val srcCropSize = (cropBoxSize / totalScale).toInt()
                        val srcCropX = ((imgW / 2f) - (cropBoxSize / 2f + offsetX) / totalScale).toInt()
                        val srcCropY = ((imgH / 2f) - (cropBoxSize / 2f + offsetY) / totalScale).toInt()

                        val result = ImageProcessor.cropAndResize(
                            bytes = imageBytes,
                            cropX = srcCropX,
                            cropY = srcCropY,
                            cropSize = srcCropSize,
                            targetSize = targetSize
                        )

                        onConfirm(
                            CroppedResult(
                                bytes = result,
                                fileName = "avatar.jpg"
                            )
                        )
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            "Confirmar",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}