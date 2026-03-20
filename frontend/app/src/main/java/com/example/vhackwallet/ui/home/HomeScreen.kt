package com.example.vhackwallet.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vhackwallet.ui.theme.*

/**
 * Main entry point for the Home Screen.
 * 
 * @param onNavigateToScanner Callback when QR scanner is clicked.
 * @param onNavigateToActivity Callback when activity history/analytics is clicked.
 * @param onNavigateToTransfer Callback when money transfer is initiated.
 * @param onThemeToggle Callback to cycle through app theme modes.
 */
@Composable
fun HomeScreen(
    onNavigateToScanner: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onThemeToggle: () -> Unit
) {
    // Initial entrance animation state
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        bottomBar = { BottomNavigationBar(onScanClick = onNavigateToScanner) },
        containerColor = SoftGreyPurple // Base background color from theme
    ) { padding ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { 40 })
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top section with greeting, notifications and profile
                HomeHeader(onThemeToggle = onThemeToggle)
                
                // Hero Section: Contains the Balance Card and overlapping Quick Actions Bar
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column {
                        BalanceCard(onViewDetails = onNavigateToActivity)
                        // This spacer controls how much the QuickActionBar overlaps the card above it
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                    
                    // Floating bar with Transfer, Add, Receive, History
                    QuickActionBar(
                        onTransfer = onNavigateToTransfer,
                        onAdd = { /* Handle Add */ },
                        onReceive = { /* Handle Receive */ },
                        onHistory = onNavigateToActivity,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                // Grid of utility services
                ServiceGrid(onActivityClick = onNavigateToActivity, onTransferClick = onNavigateToTransfer)
                
                // List of latest transactions
                RecentActivitySection(onViewAll = onNavigateToActivity)
            }
        }
    }
}

/**
 * Top header containing greeting text and action icons (Notification & Profile/Theme Switcher).
 */
@Composable
fun HomeHeader(onThemeToggle: () -> Unit) {
    // Interaction source for the profile icon tap animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "profileIconScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: User greeting
        Column {
            Text(
                text = "Good Morning,",
                style = MaterialTheme.typography.bodyLarge,
                color = VerityBlue.copy(alpha = 0.88f)
            )
            Text(
                text = "Aina Rahim",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = VerityDark
            )
        }
        
        // Right side: Action buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Notification Bell
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { /* Handle Notifications */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(26.dp),
                    tint = VerityDark
                )
                // Cyan badge dot for unread notifications
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(VerityCyan)
                        .border(1.5.dp, SoftGreyPurple, CircleShape)
                )
            }
            
            // Profile Icon (doubles as a Theme Toggle)
            Box(
                modifier = Modifier
                            .size(52.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(VerityCyan.copy(alpha = 0.06f))
                                .border(1.dp, VerityDark, CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onThemeToggle
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(32.dp),
                    tint = VerityDark
                )
            }
        }
    }
}

/**
 * Quick Action Bar
 * Floating bar that overlaps the BalanceCard, providing quick access to primary wallet actions.
 */
@Composable
fun QuickActionBar(
    onTransfer: () -> Unit,
    onAdd: () -> Unit,
    onReceive: () -> Unit,
    onHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(36.dp),
                spotColor = VerityDark.copy(alpha = 0.35f)
            ),
        shape = RoundedCornerShape(36.dp),
        color = QuickActionSurface,
        tonalElevation = 4.dp,
        // Gradient border helps the bar stand out when overlapping the card's gradient
            border = BorderStroke(
            1.2.dp,
            Brush.verticalGradient(listOf(White, VerityBlue.copy(alpha = 0.12f)))
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use a single, consistent icon color for quick actions for visual clarity
            val quickActionIconColor = VerityBlue
            QuickActionItem(
                icon = Icons.AutoMirrored.Filled.Send,
                label = "Transfer",
                onClick = onTransfer,
                color = quickActionIconColor
            )
            QuickActionItem(
                icon = Icons.Default.Add,
                label = "Add",
                onClick = onAdd,
                color = quickActionIconColor
            )
            QuickActionItem(
                icon = Icons.Default.ArrowDownward,
                label = "Receive",
                onClick = onReceive,
                color = quickActionIconColor
            )
            QuickActionItem(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                label = "History",
                onClick = onHistory,
                color = quickActionIconColor
            )
        }
    }
}

/**
 * Individual action item within the QuickActionBar.
 * Includes a bounce-scale animation on tap.
 */
@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "quickActionScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = VerityDark.copy(alpha = 0.8f)
        )
    }
}

/**
 * The primary balance display card with a rich gradient background and "glass" effects.
 */
@Composable
fun BalanceCard(onViewDetails: () -> Unit) {
    var isBalanceVisible by remember { mutableStateOf(true) }
    val animatedBalanceScale by animateFloatAsState(
        targetValue = if (isBalanceVisible) 1f else 0.95f,
        label = "balanceScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .scale(animatedBalanceScale)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = VerityIndigo.copy(alpha = 0.5f)
            )
            .border(1.dp, GlassBorder, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.linearGradient(
                    // Darker balance card gradient: navy -> indigo -> electric
                    colors = listOf(VerityDark, VerityIndigo, VerityBlue)
                )
            )
    ) {
        // Subtle mesh gradient overlay circles for a premium liquid-look
        Canvas(modifier = Modifier.matchParentSize()) {
            // Top-right mesh accent: Royal Purple (slightly stronger)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(VerityIndigo.copy(alpha = 0.28f), Color.Transparent),
                    center = center.copy(x = size.width * 0.8f, y = size.height * 0.2f),
                    radius = size.minDimension
                )
            )
            // Bottom-left mesh accent: Cyan Burst (subtle)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(VerityCyan.copy(alpha = 0.18f), Color.Transparent),
                    center = center.copy(x = size.width * 0.2f, y = size.height * 0.8f),
                    radius = size.minDimension
                )
            )
        }

        // Elongated downwards by increasing bottom padding and spacer before button
        Column(modifier = Modifier.padding(start = 28.dp, top = 28.dp, end = 28.dp, bottom = 42.dp)) {
            Text(
                text = "Total Balance",
                color = White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.titleSmall,
                letterSpacing = 1.1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Switch between actual balance and masked balance
                AnimatedContent(
                    targetState = isBalanceVisible,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                                fadeOut(animationSpec = tween(90))
                    }, label = "balanceText"
                ) { targetVisible ->
                    if (targetVisible) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "RM",
                                color = White.copy(alpha = 0.9f),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "12,450.80",
                                color = White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "RM",
                                color = White.copy(alpha = 0.9f),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "**** ****",
                                color = White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 4.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                // Toggle balance visibility button
                IconButton(
                    onClick = { isBalanceVisible = !isBalanceVisible },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Balance",
                        tint = White.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(44.dp))
            
            // Call-to-action button for analytics (glassmorphism style)
            Button(
                onClick = onViewDetails,
                colors = ButtonDefaults.buttonColors(containerColor = White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "View Analytics",
                    color = White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Grid layout for auxiliary wallet services (Bills, Invest, Rewards, etc.)
 */
@Composable
fun ServiceGrid(onActivityClick: () -> Unit, onTransferClick: () -> Unit) {
    // Define the list of services here. Easy to add/remove or change icons.
    // Rotating palette per design reference
    // Use a darker, less attention-grabbing tint for service icons
    val serviceIconTint = VerityMuted.copy(alpha = 0.92f)
    val services = listOf(
        ServiceItemData("Scan & Pay", Icons.Default.QrCode, serviceIconTint, {}),
        ServiceItemData("History", Icons.AutoMirrored.Filled.ReceiptLong, serviceIconTint, onActivityClick),
        ServiceItemData("Cards", Icons.Default.CreditCard, serviceIconTint, {}),
        ServiceItemData("Rewards", Icons.Default.Stars, serviceIconTint, {}),
        ServiceItemData("Bills", Icons.Default.Description, serviceIconTint, {}),
        ServiceItemData("Invest", Icons.AutoMirrored.Filled.TrendingUp, serviceIconTint, {}),
        ServiceItemData("eShop", Icons.Default.ShoppingBag, serviceIconTint, {}),
        ServiceItemData("More", Icons.Default.GridView, serviceIconTint, {})
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Services",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = VerityDark,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 20.dp)
        )
        
        // Grid constrained to a specific width to keep it centered and compact
        Column(
            modifier = Modifier.widthIn(max = 340.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (i in 0 until 2) { // 2 rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (j in 0 until 4) { // 4 items per row
                        val index = i * 4 + j
                        ServiceIconItem(services[index])
                    }
                }
            }
        }
    }
}

/**
 * Data structure for service items.
 */
data class ServiceItemData(val name: String, val icon: ImageVector, val color: Color, val onClick: () -> Unit)

/**
 * Individual service icon button with a soft colored background and bounce animation.
 */
@Composable
fun ServiceIconItem(service: ServiceItemData) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "serviceIconScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = service.onClick
            )
    ) {
        // Icon container box
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceSecondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = service.icon,
                contentDescription = service.name,
                tint = service.color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = service.name,
            style = MaterialTheme.typography.labelSmall,
            color = VerityDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Section header and list for recent wallet activity.
 */
@Composable
fun RecentActivitySection(onViewAll: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VerityDark
            )
            TextButton(onClick = onViewAll) {
                Text(text = "See All", color = VerityMuted, fontWeight = FontWeight.Bold)
            }
        }
        
        // Simulated activity items
        ActivityItem("Apple Store", "Electronic", "- RM 999.00", "Just now", Icons.Default.Laptop)
        ActivityItem("Transfer from Sarah", "Income", "+ RM 150.00", "2h ago", Icons.Default.ArrowDownward)
        ActivityItem("Netflix", "Subscription", "- RM 15.99", "Yesterday", Icons.Default.PlayCircle)
    }
}

/**
 * Row representing a single transaction item.
 */
@Composable
fun ActivityItem(name: String, category: String, amount: String, time: String, icon: ImageVector) {
    var isHovered by remember { mutableStateOf(false) }
    val animatedBg by animateColorAsState(if (isHovered) White.copy(alpha = 0.5f) else Color.Transparent, label = "hoverColor")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(animatedBg)
            .clickable { isHovered = !isHovered }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(White),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = VerityMuted)
        }
        Spacer(modifier = Modifier.width(16.dp))
        // Transaction details
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontWeight = FontWeight.Bold, color = VerityDark)
            Text(text = category, style = MaterialTheme.typography.bodySmall, color = VerityBlue.copy(alpha = 0.7f))
        }
        // Amount and time
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amount,
                fontWeight = FontWeight.Bold,
                color = if (amount.startsWith("+")) SuccessGreen else VerityDark
            )
            Text(text = time, style = MaterialTheme.typography.bodySmall, color = VerityDark.copy(alpha = 0.5f))
        }
    }
}

/**
 * Custom bottom navigation bar with a centered floating scan button.
 */
@Composable
fun BottomNavigationBar(onScanClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Curved background bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            color = VerityDark, // darker navy for nav bar to match mock
            tonalElevation = 12.dp,
            shadowElevation = 24.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(icon = Icons.Default.Home, label = "Home", isSelected = true)
                NavItem(icon = Icons.Default.ShoppingBag, label = "eShop", isSelected = false)
                // Spacer makes room for the floating button in the middle
                Spacer(modifier = Modifier.width(70.dp))
                NavItem(icon = Icons.Default.BarChart, label = "History", isSelected = false)
                NavItem(icon = Icons.Default.Person, label = "Profile", isSelected = false)
            }
        }

        // Floating Scan QR Button
        Box(
            modifier = Modifier
                .offset(y = (-20).dp)
                .size(72.dp)
                .clip(CircleShape)
                .background(White)
                .padding(5.dp) // Outer ring effect
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(VerityCyan, VerityBlue)))
                .clickable { onScanClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Scan",
                tint = White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

/**
 * Individual tab item for the BottomNavigationBar.
 */
@Composable
fun NavItem(icon: ImageVector, label: String, isSelected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { }
            .padding(4.dp)
    ) {
        // Use bright accent for selected state and light unselected tints for contrast
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) VerityCyan.copy(alpha = 0.86f) else White.copy(alpha = 0.5f),
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) VerityCyan.copy(alpha = 0.86f) else White.copy(alpha = 0.5f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
