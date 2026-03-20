package com.example.vhackwallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.vhackwallet.R
import com.example.vhackwallet.ui.theme.CelestialDarkNavy
import kotlinx.coroutines.delay

@Composable
fun LoadingAnimation() {
    LoadingScreen(nextRoute = "", onFinished = {})
}

@Composable
fun LoadingScreen(
    nextRoute: String,
    onFinished: (String) -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
    LaunchedEffect(Unit) {
        delay(2500)
        onFinished(nextRoute)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(350.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Processing Request...",
            color = CelestialDarkNavy,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Please do not close the app",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun LoadingFilesAnimation(onFinished: () -> Unit = {}) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_files))
    LaunchedEffect(Unit) {
        delay(2000)
        onFinished()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(350.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Searching...",
            color = CelestialDarkNavy,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Please wait while we find your recipient",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun GoPaySuccessAnimation(onFinished: () -> Unit = {}) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.gopay_successful_payment))
    LaunchedEffect(Unit) {
        delay(2000)
        onFinished()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            iterations = 1,
            modifier = Modifier.size(140.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
