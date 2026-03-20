package com.example.vhackwallet.ui.transfer

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.vhackwallet.R
import com.example.vhackwallet.ui.theme.CelestialBerry
import com.example.vhackwallet.ui.theme.CelestialDarkNavy
import com.example.vhackwallet.ui.theme.SoftGreyPurple
import com.example.vhackwallet.ui.theme.VerityBlue
import com.example.vhackwallet.ui.theme.VerityMuted
import com.example.vhackwallet.ui.theme.VerityDark
import kotlinx.coroutines.delay

@Composable
fun LottieLoadingAnimation(text: String = "") {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_gray))
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(80.dp)
            )
            if (text.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = text,
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun BiometricBottomPanel(onSuccess: () -> Unit, onUsePin: () -> Unit, onCancel: () -> Unit) {
    var isPressing by remember { mutableStateOf(false) }
    
    val progress by animateFloatAsState(
        targetValue = if (isPressing) 1f else 0f,
        animationSpec = if (isPressing) tween(1000, easing = LinearEasing) else tween(300),
        label = "biometricProgress"
    )
    
    LaunchedEffect(progress) {
        if (progress == 1f && isPressing) {
            onSuccess()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)).clickable { onCancel() }, contentAlignment = Alignment.BottomCenter) {
        Surface(modifier = Modifier.fillMaxWidth().clickable(enabled = false) {}, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), color = Color.White) {
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp).fillMaxWidth().navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Authenticate with your biometrics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CelestialDarkNavy
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    contentAlignment = Alignment.Center, 
                    modifier = Modifier
                        .size(80.dp) // Radius slightly smaller
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressing = true
                                    try {
                                        awaitRelease()
                                    } finally {
                                        isPressing = false
                                    }
                                }
                            )
                        }
                ) {
                    // Circular loading progress (black) - Removed track outline
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color.Black,
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint Sensor",
                        modifier = Modifier.size(50.dp),
                        tint = CelestialDarkNavy
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Press and hold",
                    fontSize = 13.sp,
                    color = if (isPressing) CelestialBerry else Color(0xFF999999),
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cancel",
                        color = Color(0xFF999999),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .clickable { onCancel() }
                            .padding(12.dp)
                    )
                    // padding
                    Spacer(modifier = Modifier.width(48.dp))
                    Text(
                        text = "Use PIN",
                        color = VerityBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .clickable { onUsePin() }
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PinBottomPanel(onSuccess: () -> Unit, onCancel: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

    LaunchedEffect(pin) {
        if (pin.length == 6) {
            isVerifying = true
            delay(1500)
            isVerifying = false
            onSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f))
            .clickable { if (!isVerifying) onCancel() },
        contentAlignment = Alignment.BottomCenter
    ) {
        if (!isVerifying) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter 6-Digit PIN",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = VerityDark
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Enter your security PIN to authorize this transfer",
                        textAlign = TextAlign.Center,
                        color = VerityMuted,
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Display PIN dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(6) { index ->
                            val isFilled = index < pin.length
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        if (isFilled) VerityBlue else SoftGreyPurple,
                                        CircleShape
                                    )
                            )
                        }
                    }

                    // Hidden TextField for input
                    BasicTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
                        modifier = Modifier
                            .size(1.dp)
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = VerityBlue, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.imePadding()) // Pushes surface up cleanly
                }
            }
        }
        
        if (isVerifying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}

@Composable
fun CashTransferAnimationOverlay(onComplete: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
    
    LaunchedEffect(Unit) {
        delay(3000)
        onComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(250.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Securing your payment...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CelestialDarkNavy
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We’re verifying your transfer.",
                style = MaterialTheme.typography.bodyMedium,
                color = VerityMuted
            )
        }
    }
}

@Composable
fun FaceIdBottomPanel(onSuccess: () -> Unit, onUsePassword: () -> Unit, onCancel: () -> Unit) {
    var showPasswordDialog by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var faceTapped by remember { mutableStateOf(false) }
    
    // Face icon tap animation
    val faceScale by animateFloatAsState(
        targetValue = if (faceTapped) 1.2f else 1f,
        animationSpec = tween(300, easing = LinearEasing),
        label = "faceScale"
    )
    
    LaunchedEffect(faceTapped) {
        if (faceTapped) {
            delay(300)
            isVerifying = true
            delay(2000)
            faceTapped = false
            isVerifying = false
            isProcessing = true
        }
    }
    
    LaunchedEffect(isVerifying) {
        if (isVerifying && !showPasswordDialog) {
            delay(2000)
            isVerifying = false
            isProcessing = true
        }
    }
    
    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            delay(2000)
            isProcessing = false
            onSuccess()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)).clickable { if (!isVerifying && !isProcessing && !showPasswordDialog) onCancel() }, contentAlignment = Alignment.BottomCenter) {
        // Show normal panel unless verifying or processing
        if (!isVerifying && !isProcessing && !showPasswordDialog) {
            Surface(modifier = Modifier.fillMaxWidth().clickable(enabled = false) {}, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Title
                    Text(
                        text = "Confirm with face recognition",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CelestialDarkNavy
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Face icon with tap animation
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp).clickable { faceTapped = true }.graphicsLayer(scaleX = faceScale, scaleY = faceScale)) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Face ID",
                            modifier = Modifier.fillMaxSize(),
                            tint = CelestialBerry
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Look at the camera",
                        fontSize = 13.sp,
                        color = Color(0xFF999999),
                        fontWeight = FontWeight.Normal
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Text("Cancel", color = Color(0xFF666666), fontWeight = FontWeight.Medium)
                        }
                        Button(
                            onClick = { showPasswordDialog = true },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CelestialBerry)
                        ) {
                            Text("Use Password", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
        
        // Show loading overlay on same page when verifying
        if (isVerifying) {
            LottieLoadingAnimation(text = "Scanning face...")
        }
        
        // Show processing overlay on same page when processing
        if (isProcessing) {
            LottieLoadingAnimation(text = "Processing request...")
        }
        
        // Password Dialog - shown as overlay only when dialog state is true
        if (showPasswordDialog) {
            SecurityInputDialog(
                title = "Account Password",
                description = "Verify your account password to complete transaction",
                icon = Icons.Default.VpnKey,
                onSuccess = { 
                    showPasswordDialog = false
                    isVerifying = true
                },
                onCancel = { showPasswordDialog = false },
                isPassword = true,
                placeholder = "Enter Password"
            )
        }
    }
}

@Composable
fun SecurityInputDialog(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onSuccess: () -> Unit, onCancel: () -> Unit, isPassword: Boolean = false, isPin: Boolean = false, keyboardType: KeyboardType = KeyboardType.Text, placeholder: String = "") {
    var input by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { delay(300); focusRequester.requestFocus() }
    AlertDialog(onDismissRequest = onCancel, confirmButton = { Button(onClick = onSuccess, colors = ButtonDefaults.buttonColors(containerColor = CelestialBerry), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = if (isPin) input.length == 6 else input.length >= 4) { Text("Verify") } }, dismissButton = { TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") } }, title = { Text(title, fontWeight = FontWeight.ExtraBold, color = CelestialDarkNavy, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }, text = { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) { Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = CelestialBerry); Spacer(modifier = Modifier.height(16.dp)); Text(description, textAlign = TextAlign.Center, color = CelestialDarkNavy); OutlinedTextField(value = input, onValueChange = { if (isPin) { if (it.length <= 6 && it.all { char -> char.isDigit() }) input = it } else { input = it } }, modifier = Modifier.padding(top = 16.dp).focusRequester(focusRequester), placeholder = { Text(placeholder) }, shape = RoundedCornerShape(12.dp), visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = keyboardType), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CelestialBerry, unfocusedBorderColor = SoftGreyPurple, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)) } }, shape = RoundedCornerShape(28.dp), containerColor = Color.White)
}

// Unified 2FA Panel - Single page with both stages embedded
@Composable
fun UnifiedTwoFactorAuthPanel(onSuccess: () -> Unit, onCancel: () -> Unit) {
    var authStage by remember { mutableStateOf(0) }  // 0: Biometric, 1: Face, 2: Processing
    
    // Biometric stage states
    var showPinDialog by remember { mutableStateOf(false) }
    var isPressing by remember { mutableStateOf(false) }
    
    val progress by animateFloatAsState(
        targetValue = if (isPressing) 1f else 0f,
        animationSpec = if (isPressing) tween(1000, easing = LinearEasing) else tween(300),
        label = "biometricProgress"
    )
    
    // Biometric fingerprint effect
    LaunchedEffect(progress) {
        if (progress == 1f && isPressing) {
            authStage = 1  // Move to face stage immediately
        }
    }
    
    // Face verification states
    var showPasswordDialog by remember { mutableStateOf(false) }
    var isVerifyingFace by remember { mutableStateOf(false) }
    var faceTapped by remember { mutableStateOf(false) }
    var useFaceVerification by remember { mutableStateOf(true) }
    
    // Face stage logic
    LaunchedEffect(isVerifyingFace) {
        if (isVerifyingFace && !showPasswordDialog) {
            delay(2000)
            isVerifyingFace = false
            onSuccess()
        }
    }
    
    LaunchedEffect(faceTapped) {
        if (faceTapped) {
            delay(300)
            useFaceVerification = true
            isVerifyingFace = true
            delay(2000)
            faceTapped = false
            isVerifyingFace = false
            onSuccess()
        }
    }
    
    val faceScale by animateFloatAsState(
        targetValue = if (faceTapped) 1.2f else 1f,
        animationSpec = tween(300, easing = LinearEasing),
        label = "faceScale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f))
            .clickable { if (authStage == 0 && !showPinDialog) onCancel() },
        contentAlignment = Alignment.BottomCenter
    ) {
        // STAGE 0: BIOMETRIC AUTHENTICATION
        if (authStage == 0 && !showPinDialog) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Authenticate with your biometrics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CelestialDarkNavy
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp) // Radius slightly smaller
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isPressing = true
                                        try {
                                            awaitRelease()
                                        } finally {
                                            isPressing = false
                                        }
                                    }
                                )
                            }
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Color.Black,
                                startAngle = -90f,
                                sweepAngle = progress * 360f,
                                useCenter = false,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint Sensor",
                            modifier = Modifier.size(50.dp),
                            tint = CelestialDarkNavy
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Press and hold", 
                        fontSize = 13.sp,
                        color = if (isPressing) CelestialBerry else Color(0xFF999999),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF999999),
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .clickable { onCancel() }
                                .padding(12.dp)
                        )
                        
                        Text(
                            text = "Use PIN",
                            color = CelestialBerry,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .clickable { showPinDialog = true }
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
        
        // STAGE 1: FACE AUTHENTICATION
        if (authStage == 1 && !showPasswordDialog) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Confirm with face recognition",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CelestialDarkNavy
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clickable { faceTapped = true }
                            .graphicsLayer(scaleX = faceScale, scaleY = faceScale)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Face ID",
                            modifier = Modifier.fillMaxSize(),
                            tint = CelestialBerry
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Look at the camera",
                        fontSize = 13.sp,
                        color = Color(0xFF999999),
                        fontWeight = FontWeight.Normal
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Text("Cancel", color = Color(0xFF666666), fontWeight = FontWeight.Medium)
                        }
                        Button(
                            onClick = { showPasswordDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CelestialBerry)
                        ) {
                            Text("Use Password", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
        
        // OVERLAY: Face verification
        if (authStage == 1 && isVerifyingFace) {
            val overlayText = if (useFaceVerification) "Scanning face..." else "Verifying password..."
            LottieLoadingAnimation(text = overlayText)
        }
        
        // PIN Dialog
        if (showPinDialog) {
            SecurityInputDialog(
                title = "Security PIN",
                description = "Key in your 6-digit PIN to proceed",
                icon = Icons.Default.Lock,
                onSuccess = {
                    showPinDialog = false
                    authStage = 1 // Move to face stage immediately
                },
                onCancel = { showPinDialog = false },
                keyboardType = KeyboardType.NumberPassword,
                placeholder = "• • • • • •",
                isPin = true
            )
        }
        
        // Password Dialog
        if (showPasswordDialog) {
            SecurityInputDialog(
                title = "Account Password",
                description = "Verify your account password to complete transaction",
                icon = Icons.Default.VpnKey,
                onSuccess = {
                    showPasswordDialog = false
                    useFaceVerification = false
                    isVerifyingFace = true
                },
                onCancel = { showPasswordDialog = false },
                isPassword = true,
                placeholder = "Enter Password"
            )
        }
    }
}
