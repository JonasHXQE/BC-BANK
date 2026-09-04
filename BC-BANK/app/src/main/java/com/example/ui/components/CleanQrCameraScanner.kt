package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningYellow
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Clean, high-performance CameraX QR Code & Barcode Scanner for BC-BANK.
 * Uses high-precision multi-angle luminance processing to instantly detect codes.
 */
@Composable
fun CleanQrCameraScanner(
    title: String = "Escanear Código QR",
    subtitle: String = "Enfoca el código QR con la cámara para procesar de inmediato",
    modifier: Modifier = Modifier,
    viewfinderHeight: androidx.compose.ui.unit.Dp = 280.dp,
    onQrDetected: (rawQr: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var isFlashOn by remember { mutableStateOf(false) }
    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    val isScanned = remember { AtomicBoolean(false) }
    var showManualInputDialog by remember { mutableStateOf(false) }
    var manualQrInputText by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 220f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = TextSecondary
            )

            // Viewfinder Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(viewfinderHeight)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .border(2.dp, EmeraldPrimary, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            val cameraExecutor = Executors.newSingleThreadExecutor()
                            val mainHandler = Handler(Looper.getMainLooper())

                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setTargetResolution(Size(1280, 720))
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()

                                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                        if (!isScanned.get()) {
                                            processImageProxyWithZXing(imageProxy) { qrResult ->
                                                if (isScanned.compareAndSet(false, true)) {
                                                    triggerHapticFeedback(ctx)
                                                    mainHandler.post {
                                                        onQrDetected(qrResult)
                                                    }
                                                }
                                            }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }

                                    cameraProvider.unbindAll()
                                    val camera = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageAnalysis
                                    )
                                    cameraInstance = camera
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Flash state sync
                    LaunchedEffect(isFlashOn) {
                        try {
                            if (cameraInstance?.cameraInfo?.hasFlashUnit() == true) {
                                cameraInstance?.cameraControl?.enableTorch(isFlashOn)
                            }
                        } catch (_: Exception) {}
                    }

                    // Scanning Laser Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .offset(y = (laserOffset - 110).dp)
                            .background(EmeraldPrimary)
                    )

                    // Corner Guide Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Apunta el código dentro del marco",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = EmeraldLight
                        )
                    }
                } else {
                    // No permission fallback screen
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Permiso de Cámara Requerido",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Para escanear códigos QR y procesar pagos o retiros con la cámara de tu dispositivo, concede el permiso de acceso.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = TextSecondary
                        )
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("request_camera_permission_btn")
                        ) {
                            Text("Habilitar Cámara", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Controls Row: Torch Flashlight & Manual Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isFlashOn = !isFlashOn },
                    enabled = hasCameraPermission,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, BorderDark, CircleShape)
                        .testTag("scanner_flash_toggle")
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = if (isFlashOn) WarningYellow else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                OutlinedButton(
                    onClick = { showManualInputDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ingresar Código Manualmente", fontSize = 12.sp)
                }
            }
        }
    }

    // Manual QR String Dialog
    if (showManualInputDialog) {
        Dialog(onDismissRequest = { showManualInputDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ingresar Código o Enlace",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        IconButton(
                            onClick = { showManualInputDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                        }
                    }

                    OutlinedTextField(
                        value = manualQrInputText,
                        onValueChange = { manualQrInputText = it },
                        label = { Text("Código de pago, teléfono o CCI") },
                        placeholder = { Text("Ej. 987654321 o BCBANK:...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (manualQrInputText.isNotBlank()) {
                                showManualInputDialog = false
                                triggerHapticFeedback(context)
                                onQrDetected(manualQrInputText.trim())
                            }
                        },
                        enabled = manualQrInputText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Procesar Código", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Fast, multi-orientation ZXing frame decoder.
 * Handles row strides, landscape/portrait rotations, and multiple binarizers.
 */
private fun processImageProxyWithZXing(
    imageProxy: ImageProxy,
    onSuccess: (String) -> Unit
) {
    try {
        val yPlane = imageProxy.planes[0]
        val buffer = yPlane.buffer
        val rowStride = yPlane.rowStride
        val width = imageProxy.width
        val height = imageProxy.height
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        // Clean stride extraction
        val yBytes: ByteArray
        if (rowStride == width) {
            yBytes = ByteArray(buffer.remaining())
            buffer.get(yBytes)
        } else {
            yBytes = ByteArray(width * height)
            for (row in 0 until height) {
                buffer.position(row * rowStride)
                buffer.get(yBytes, row * width, width)
            }
        }

        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(
                BarcodeFormat.QR_CODE,
                BarcodeFormat.DATA_MATRIX,
                BarcodeFormat.CODE_128,
                BarcodeFormat.EAN_13,
                BarcodeFormat.CODE_39
            ),
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.CHARACTER_SET to "UTF-8"
        )

        // 1. Try Native Frame
        var textFound = decodeBytes(yBytes, width, height, hints)

        // 2. If not found and rotated (e.g. 90 degrees in portrait phone), rotate byte array
        if (textFound == null && rotationDegrees != 0) {
            val rotatedBytes = rotateYUV420Degree(yBytes, width, height, rotationDegrees)
            val newW = if (rotationDegrees == 90 || rotationDegrees == 270) height else width
            val newH = if (rotationDegrees == 90 || rotationDegrees == 270) width else height
            textFound = decodeBytes(rotatedBytes, newW, newH, hints)
        }

        if (!textFound.isNullOrBlank()) {
            onSuccess(textFound)
        }
    } catch (_: Exception) {
        // Frame did not contain a readable barcode
    } finally {
        imageProxy.close()
    }
}

private fun decodeBytes(
    bytes: ByteArray,
    width: Int,
    height: Int,
    hints: Map<DecodeHintType, Any>
): String? {
    val reader = MultiFormatReader()
    reader.setHints(hints)

    val source = PlanarYUVLuminanceSource(
        bytes,
        width,
        height,
        0,
        0,
        width,
        height,
        false
    )

    // Try HybridBinarizer first
    try {
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val result = reader.decodeWithState(bitmap)
        if (result != null && result.text.isNotBlank()) return result.text
    } catch (_: Exception) {}

    // Fallback: GlobalHistogramBinarizer
    try {
        val bitmap = BinaryBitmap(GlobalHistogramBinarizer(source))
        val result = reader.decodeWithState(bitmap)
        if (result != null && result.text.isNotBlank()) return result.text
    } catch (_: Exception) {}

    return null
}

/**
 * Fast rotation for Y plane byte array.
 */
private fun rotateYUV420Degree(data: ByteArray, imageWidth: Int, imageHeight: Int, degrees: Int): ByteArray {
    if (degrees == 0) return data
    val y = ByteArray(imageWidth * imageHeight)
    when (degrees) {
        90 -> {
            var i = 0
            for (x in 0 until imageWidth) {
                for (yr in imageHeight - 1 downTo 0) {
                    y[i] = data[yr * imageWidth + x]
                    i++
                }
            }
        }
        180 -> {
            var i = 0
            for (index in (imageWidth * imageHeight - 1) downTo 0) {
                y[i] = data[index]
                i++
            }
        }
        270 -> {
            var i = 0
            for (x in imageWidth - 1 downTo 0) {
                for (yr in 0 until imageHeight) {
                    y[i] = data[yr * imageWidth + x]
                    i++
                }
            }
        }
        else -> return data
    }
    return y
}

private fun triggerHapticFeedback(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(80)
            }
        }
    } catch (_: Exception) {}
}
