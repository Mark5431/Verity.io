package com.example.vhackwallet.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.vhackwallet.ui.theme.CelestialBerry
import com.example.vhackwallet.ui.theme.CelestialPink
import com.example.vhackwallet.ui.theme.White
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(onBack: () -> Unit, onScanSuccess: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var isProcessing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Scan QR", color = White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                if (!isProcessing) {
                                    processImageProxy(imageProxy) { result ->
                                        if (!isProcessing) {
                                            isProcessing = true
                                            Log.d("Scanner", "Scanned: $result")
                                            onScanSuccess(result)
                                        }
                                    }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (exc: Exception) {
                                Log.e("Scanner", "Use case binding failed", exc)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Camera permission is required", color = Color.White)
                }
            }

            // QR Overlay with Animation
            QRScannerOverlay()

            // Bottom Instructions
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Align the QR code within the frame",
                    color = White,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    IconButton(
                        onClick = { /* Toggle Flash */ },
                        modifier = Modifier.background(White.copy(alpha = 0.2f), RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = "Flash", tint = White)
                    }
                    IconButton(
                        onClick = { /* Open Gallery */ },
                        modifier = Modifier.background(White.copy(alpha = 0.2f), RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = White)
                    }
                }
            }
        }
    }
}

@Composable
fun QRScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scannerLineY = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scannerLine"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val boxSize = width * 0.7f
        val left = (width - boxSize) / 2
        val top = (height - boxSize) / 2
        val right = left + boxSize
        val bottom = top + boxSize

        val rectPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(left, top, right, bottom),
                    cornerRadius = CornerRadius(24.dp.toPx())
                )
            )
        }

        // 1. Draw the dark translucent background with cutout
        clipPath(rectPath, clipOp = ClipOp.Difference) {
            drawRect(color = Color.Black.copy(alpha = 0.6f))
        }

        // 2. Draw Brackets (Corners)
        val lineLength = 30.dp.toPx()
        val strokeWidth = 4.dp.toPx()
        val cornerColor = CelestialBerry

        // Top Left
        drawLine(cornerColor, Offset(left, top + lineLength), Offset(left, top), strokeWidth, StrokeCap.Round)
        drawLine(cornerColor, Offset(left, top), Offset(left + lineLength, top), strokeWidth, StrokeCap.Round)

        // Top Right
        drawLine(cornerColor, Offset(right - lineLength, top), Offset(right, top), strokeWidth, StrokeCap.Round)
        drawLine(cornerColor, Offset(right, top), Offset(right, top + lineLength), strokeWidth, StrokeCap.Round)

        // Bottom Left
        drawLine(cornerColor, Offset(left, bottom - lineLength), Offset(left, bottom), strokeWidth, StrokeCap.Round)
        drawLine(cornerColor, Offset(left, bottom), Offset(left + lineLength, bottom), strokeWidth, StrokeCap.Round)

        // Bottom Right
        drawLine(cornerColor, Offset(right - lineLength, bottom), Offset(right, bottom), strokeWidth, StrokeCap.Round)
        drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - lineLength), strokeWidth, StrokeCap.Round)

        // 3. Draw Scanning Line
        val currentLineY = top + (boxSize * scannerLineY.value)
        
        // Gradient for the line to make it look "glowy"
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    CelestialBerry.copy(alpha = 0.5f),
                    CelestialBerry,
                    CelestialBerry.copy(alpha = 0.5f),
                    Color.Transparent
                ),
                startY = currentLineY - 10.dp.toPx(),
                endY = currentLineY + 10.dp.toPx()
            ),
            topLeft = Offset(left + 4.dp.toPx(), currentLineY - 2.dp.toPx()),
            size = Size(boxSize - 8.dp.toPx(), 4.dp.toPx())
        )
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onSuccess: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { onSuccess(it) }
                }
            }
            .addOnFailureListener {
                Log.e("Scanner", "Barcode scanning failed", it)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
