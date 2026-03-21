/**
 * Transfer Search Screen
 *
 * This screen allows users to search for existing contacts or enter a new phone number
 * to initiate a fund transfer. It features a demo mode for quick populating of data,
 * real-time contact filtering, and visual highlights for numeric input.
 */

package com.example.vhackwallet.ui.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vhackwallet.ui.components.LoadingFilesAnimation
import com.example.vhackwallet.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ========== DEMO MODE CONFIGURATION ==========
// Set to TRUE for quick demo - auto-populates phone number when clicking search bar
// Set to FALSE for normal operation
private const val DEMO_MODE_ENABLED = true
private const val DEMO_PHONE_NUMBER = "+60 16-123 4567"
// ============================================

/**
 * Main Composable for the Transfer Search Screen.
 *
 * @param onBack Callback to navigate back to the previous screen.
 * @param onContactSelected Callback when a contact or new recipient is selected.
 *        Arguments: (Name, Phone Number, IsNewRecipient)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferSearchScreen(
    onBack: () -> Unit,
    onContactSelected: (String, String, Boolean) -> Unit
) {

    /**
     * A custom shape with a slightly concave bottom edge for the celestial cherry theme.
     */
    val ConcaveHeaderShape = GenericShape { size, _ ->
        val width = size.width
        val height = size.height
        lineTo(width, 0f)
        lineTo(width, height)
        // Create a deeper convex effect by curving the bottom edge further downwards
        quadraticTo(width / 2f, height + 160f, 0f, height)
        close()
    }
    // State for the search query input (use TextFieldValue to control cursor)
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    // State to control showing the search/loading animation
    var isSearching by remember { mutableStateOf(false) }
    // Temporary storage for the selected contact before navigating
    var selectedContact by remember { mutableStateOf<Pair<String, String>?>(null) }
    // Flag to indicate if the recipient is not in the recent contacts list
    var isNewRecipient by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Mock data for recent contacts
    val contacts = listOf(
        Contact("Sarah Jenkins", "012-345 6789"),
        Contact("Michael Chen", "017-987 6543"),
        Contact("Ahmad Razak", "019-222 3333"),
        Contact("Lim Wei Han", "011-555 4444"),
        Contact("Jessica Tan", "016-111 2222")
    )

    // Favorites (pick three mock favorites)
    val favoriteContacts = listOf(
        Contact("Sarah Jenkins", "012-345 6789"),
        Contact("Michael Chen", "017-987 6543"),
        Contact("Jessica Tan", "016-111 2222")
    )

    // Filter contacts based on the search query (name or phone)
    val filteredContacts = if (searchQuery.text.isEmpty()) {
        contacts
    } else {
        contacts.filter { it.name.contains(searchQuery.text, ignoreCase = true) || it.phone.contains(searchQuery.text) }
    }

    /**
     * Visual transformation to highlight digits in the search bar.
     * This makes phone numbers stand out more clearly as the user types.
     */
    val numericHighlightTransformation = VisualTransformation { text ->
        val annotatedString = buildAnnotatedString {
            text.text.forEach { char ->
                if (char.isDigit()) {
                            withStyle(style = SpanStyle(color = VerityBlue, fontWeight = FontWeight.Bold)) {
                        append(char)
                    }
                } else {
                    append(char)
                }
            }
        }
        TransformedText(annotatedString, OffsetMapping.Identity)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer", fontWeight = FontWeight.Bold, color = White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = SoftGreyPurple
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Concave header — now using BalanceCard-like gradient and decorative accents
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.22f)
                    .clip(ConcaveHeaderShape)
                    .background(
                        Brush.linearGradient(
                            // Match HomeScreen BalanceCard gradient
                            colors = listOf(VerityDark, VerityIndigo, VerityBlue)
                        )
                    )
            ) {
                // Decorative mesh and accents to mirror BalanceCard styling
                Canvas(modifier = Modifier.matchParentSize()) {
                    // Top-right royal indigo mesh accent (match BalanceCard)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(VerityIndigo.copy(alpha = 0.28f), Color.Transparent),
                            center = center.copy(x = size.width * 0.82f, y = size.height * 0.18f),
                            radius = size.minDimension
                        )
                    )

                    // Bottom-left cyan burst accent (subtle, match BalanceCard)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(VerityCyan.copy(alpha = 0.18f), Color.Transparent),
                            center = center.copy(x = size.width * 0.18f, y = size.height * 0.78f),
                            radius = size.minDimension
                        )
                    )

                    // Very subtle indigo hint near the upper center for depth
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(VerityIndigo.copy(alpha = 0.06f), Color.Transparent),
                            center = center.copy(x = size.width * 0.52f, y = size.height * 0.32f),
                            radius = size.minDimension * 0.6f
                        )
                    )

                    // More pronounced indigo hint on the left side for clearer transition
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(VerityIndigo.copy(alpha = 0.28f), Color.Transparent),
                            center = center.copy(x = size.width * 0.10f, y = size.height * 0.28f),
                            radius = size.minDimension * 0.9f
                        )
                    )

                    // Subtle soft-white overlay for depth (kept very light)
                    drawRect(
                        color = White.copy(alpha = 0.015f),
                        topLeft = Offset.Zero,
                        size = Size(size.width * 0.6f, size.height * 0.36f)
                    )
                }
            }
            // Show a "searching" animation when a NEW recipient is manually entered
            if (isSearching && isNewRecipient) {
                LoadingFilesAnimation(onFinished = {
                    isSearching = false
                    selectedContact?.let { (name, phone) ->
                        onContactSelected(name, phone, true)
                    }
                    selectedContact = null
                    isNewRecipient = false
                })
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    // Search Bar for name or phone number
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .onFocusEvent { focusState ->
                                // Auto-fill demo number after a slight delay when the field is focused
                                if (focusState.isFocused && DEMO_MODE_ENABLED && searchQuery.text.isEmpty()) {
                                    scope.launch {
                                        delay(300) // Delay to prevent abrupt UI jump
                                        if (searchQuery.text.isEmpty()) {
                                            // set TextFieldValue and move cursor to end
                                            searchQuery = TextFieldValue(DEMO_PHONE_NUMBER, TextRange(DEMO_PHONE_NUMBER.length))
                                        }
                                    }
                                }
                            },
                        placeholder = { Text("Enter name or phone number") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VerityBlue.copy(alpha = 0.6f)) },
                        trailingIcon = {
                            if (searchQuery.text.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = TextFieldValue("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = VerityBlue.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = numericHighlightTransformation,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VerityBlue,
                            unfocusedBorderColor = VerityBlue.copy(alpha = 0.18f), // more visible but still subtle
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedTextColor = VerityDark,
                            unfocusedTextColor = VerityDark,
                            focusedLabelColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray
                        ),
                        singleLine = true
                    )

                    // Show "Direct Entry" option if there's text in search but no matching contact
                    if (searchQuery.text.isNotEmpty() && filteredContacts.isEmpty()) {
                        // little padding
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { 
                                    selectedContact = "MAJU JAYA ENTERPRISE" to searchQuery.text
                                    isNewRecipient = true  // Mark as unknown recipient to trigger animation
                                    isSearching = true 
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(5.dp))
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceSecondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = VerityBlue,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(28.dp))
                        Column(
                            // Add padding between texts
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "Tap here to send to",
                                fontSize = 15.sp,
                                color = VerityBlue
                            )
                            Text(
                                text = searchQuery.text,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerityDark
                            )
                        }
                    }
                }
            }

                    // Favorites row
                    Text(
                        text = "Favorites",
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = VerityMuted
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // track active (tapped) favorites to animate their appearance
                        val activeFavorites = remember { mutableStateMapOf<String, Boolean>() }

                        favoriteContacts.forEach { fav ->
                            val isActive = activeFavorites[fav.name] ?: false
                            val bgColor by animateColorAsState(
                                targetValue = if (isActive) VerityDark.copy(alpha = 0.12f) else VerityBlue.copy(alpha = 0.10f)
                            )
                            val iconColor by animateColorAsState(
                                targetValue = if (isActive) VerityDark else VerityBlue
                            )

                            Column(
                                modifier = Modifier.width(92.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(bgColor)
                                        .clickable {
                                            // toggle active state for visual feedback
                                            activeFavorites[fav.name] = !isActive
                                            // treat favorites as known recipients
                                            onContactSelected(fav.name, fav.phone, false)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = fav.name.take(1),
                                        color = iconColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = fav.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    color = VerityDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Recent Contacts",
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = VerityMuted
                    )

                    // List of filtered contacts
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(filteredContacts) { contact ->
                            ContactItem(contact) {
                                // Known recipient - navigate directly without extra searching animation
                                onContactSelected(contact.name, contact.phone, false)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A single contact item row.
 *
 * @param contact The contact data to display.
 * @param onClick Callback when the contact is clicked.
 */
@Composable
fun ContactItem(contact: Contact, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        color = White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle with the first letter of the name
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SurfaceSecondary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1),
                    color = VerityMuted.copy(alpha = 0.92f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = contact.name, fontWeight = FontWeight.Bold, color = VerityDark)
                Text(text = contact.phone, fontSize = 13.sp, color = VerityMuted.copy(alpha = 0.6f))
            }
        }
    }
}

/**
 * Data class representing a contact.
 */
data class Contact(val name: String, val phone: String)
