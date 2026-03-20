package com.example.vhackwallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.vhackwallet.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        // Blur Layer
        Surface(
            modifier = Modifier
                .matchParentSize()
                .blur(40.dp)
                .clip(RoundedCornerShape(cornerRadius)),
            color = Color.White.copy(alpha = 0.1f)
        ) {}

        // Glossy Surface
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            White.copy(alpha = 0.15f),
                            White.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            White.copy(alpha = 0.3f),
                            Transparent
                        )
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun LiquidBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CelestialDarkNavy)
    ) {
        // Soft animated-like blobs (static for now, can animate later)
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .blur(100.dp)
                .background(CelestialDarkPurple.copy(alpha = 0.4f), RoundedCornerShape(200.dp))
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .blur(80.dp)
                .background(CelestialBerry.copy(alpha = 0.3f), RoundedCornerShape(150.dp))
        )
    }
}
