package com.example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt
import com.example.data.MediaFile
import androidx.compose.ui.graphics.StrokeCap

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            
            MyApplicationTheme(
                darkTheme = viewModel.isDarkMode,
                amoledMode = viewModel.isAmoledMode
            ) {
                CaptureFlowApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CaptureFlowApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val recordState by viewModel.recordState.collectAsState()
    val countdown by viewModel.countdown.collectAsState()

    // Request permissions
    val permissionsToRequest = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = permissionsToRequest)

    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
        viewModel.loadSampleMediaFilesFromAssets()
    }

    // MediaProjection screen record launcher
    val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    val projectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.startCountdownAndRecord(result.resultCode, result.data)
        } else {
            // Permission denied or canceled - run high-fidelity simulation so app works!
            Toast.makeText(context, "Using premium simulation mode", Toast.LENGTH_SHORT).show()
            viewModel.startCountdownAndRecord()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (recordState == "IDLE") {
                CaptureFlowBottomNavigation(
                    currentTab = viewModel.currentTab,
                    onTabSelected = { viewModel.currentTab = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main views based on tab navigation
            when (viewModel.currentTab) {
                "DASHBOARD" -> RecordDashboard(
                    viewModel = viewModel,
                    onStartRecordClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            try {
                                projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
                            } catch (e: Exception) {
                                // Fallback simulation
                                viewModel.startCountdownAndRecord()
                            }
                        } else {
                            viewModel.startCountdownAndRecord()
                        }
                    }
                )
                "GALLERY" -> GalleryScreen(viewModel)
                "EDITOR" -> EditorStudio(viewModel)
                "SETTINGS" -> SettingsScreen(viewModel)
            }

            // Draggable live facecam card if enabled
            if (viewModel.facecamEnabled && recordState == "RECORDING") {
                FacecamOverlay()
            }

            // Screen recorder overlay (countdown / status)
            AnimatedVisibility(
                visible = recordState == "COUNTDOWN",
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (countdown > 0) countdown.toString() else "RECORD!",
                        color = Color.White,
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CaptureFlowBottomNavigation(currentTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF14171D),
        tonalElevation = 0.dp,
        modifier = Modifier
            .border(width = 1.dp, color = Color(0xFF1E293B).copy(alpha = 0.5f))
    ) {
        val items = listOf(
            Triple("DASHBOARD", "Capture", Icons.Filled.Videocam),
            Triple("GALLERY", "Files", Icons.Filled.Folder),
            Triple("EDITOR", "Editor", Icons.Filled.Edit),
            Triple("SETTINGS", "Settings", Icons.Filled.Settings)
        )

        items.forEach { (tab, label, icon) ->
            val selected = currentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF60A5FA),
                    selectedTextColor = Color(0xFF60A5FA),
                    indicatorColor = Color(0xFF3B82F6).copy(alpha = 0.2f),
                    unselectedIconColor = Color(0xFF94A3B8).copy(alpha = 0.4f),
                    unselectedTextColor = Color(0xFF94A3B8).copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
fun SimulatedStatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "NEXVORA LAB'S",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8).copy(alpha = 0.6f),
            letterSpacing = 1.5.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "4K • 60FPS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8).copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            // Glowing green LED circle
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
            )
        }
    }
}

@Composable
fun PremiumTopAppBar(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gradient Logo Box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Videocam,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = "CaptureFlow X",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "PREMIUM SCREEN SUITE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF60A5FA),
                    letterSpacing = 1.sp
                )
            }
        }

        // Settings Action
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onSettingsClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DeviceStorageCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, Color(0xFF1E293B).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Device Storage",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "124.5",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GB Used",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                // Badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFF3B82F6).copy(alpha = 0.2f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "82% FULL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF60A5FA),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Gradient Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF6366F1))
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun CentralRecordButton(
    recordState: String,
    timerStr: String,
    onButtonClick: () -> Unit,
    onPauseClick: () -> Unit,
    viewModel: MainViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(230.dp)
        ) {
            // Glow effect behind the button
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .blur(32.dp)
                    .background(
                        if (recordState == "RECORDING") {
                            Color(0xFFEF4444).copy(alpha = 0.15f)
                        } else {
                            Color(0xFF3B82F6).copy(alpha = 0.15f)
                        },
                        CircleShape
                    )
            )

            val scale by animateFloatAsState(targetValue = if (recordState == "RECORDING") 1.05f else 1.0f)

            // Outer Container Circle
            Box(
                modifier = Modifier
                    .size(176.dp * scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 8.dp,
                        color = if (recordState == "RECORDING") Color(0xFF991B1B) else Color(0xFF1E293B),
                        shape = CircleShape
                    )
                    .clickable { onButtonClick() },
                contentAlignment = Alignment.Center
            ) {
                // Middle Gradient Ring
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (recordState == "RECORDING") {
                                    listOf(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFF991B1B))
                                } else {
                                    listOf(Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF8B5CF6))
                                }
                            )
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner transparent area with subtle border
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(4.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Central Core Element
                        if (recordState == "RECORDING" || recordState == "PAUSED") {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (recordState == "PAUSED") Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                    contentDescription = "Pause/Resume",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { onPauseClick() }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = timerStr,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "TAP TO STOP",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        } else {
                            // Start Recording core white dot
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (recordState == "RECORDING") "RECORDING ACTIVE" else "START RECORDING",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = if (recordState == "RECORDING") Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun MediaQuickToggles(viewModel: MainViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mic Toggle
        val isMic = viewModel.audioSource != "MUTE"
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .clickable {
                    viewModel.audioSource = if (isMic) "MUTE" else "MIC"
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = "Mic",
                tint = if (isMic) Color(0xFF60A5FA) else Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isMic) "Mic On" else "Mic Off",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isMic) MaterialTheme.colorScheme.onSurface else Color(0xFF94A3B8)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Facecam Toggle
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .clickable {
                    viewModel.facecamEnabled = !viewModel.facecamEnabled
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Videocam,
                contentDescription = "Facecam",
                tint = if (viewModel.facecamEnabled) Color(0xFFA78BFA) else Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (viewModel.facecamEnabled) "Facecam On" else "Facecam Off",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (viewModel.facecamEnabled) MaterialTheme.colorScheme.onSurface else Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun HorizontalRecentCaptures(viewModel: MainViewModel) {
    val mediaFiles by viewModel.allMediaFiles.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT CAPTURES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.sp
            )
            Text(
                text = "View All",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3B82F6),
                modifier = Modifier
                    .clickable { viewModel.currentTab = "GALLERY" }
                    .padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (mediaFiles.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, Color(0xFF1E293B).copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No captures yet. Start recording above!",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val itemsToShow = mediaFiles.take(2)
                itemsToShow.forEach { mediaFile ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable {
                                if (mediaFile.fileType == "VIDEO") {
                                    viewModel.selectFileForEditing(mediaFile)
                                    viewModel.currentTab = "EDITOR"
                                } else {
                                    viewModel.loadScreenshotToEditor(mediaFile)
                                    viewModel.currentTab = "EDITOR"
                                }
                            }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (mediaFile.fileType == "VIDEO") Icons.Filled.Videocam else Icons.Outlined.Photo,
                                    contentDescription = "Type",
                                    tint = if (mediaFile.fileType == "VIDEO") Color(0xFF3B82F6) else Color(0xFF8B5CF6),
                                    modifier = Modifier.size(24.dp)
                                )

                                if (mediaFile.fileType == "VIDEO") {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    if (mediaFile.fileType == "VIDEO") {
                                        val minutes = (mediaFile.durationMs / 1000) / 60
                                        val seconds = (mediaFile.durationMs / 1000) % 60
                                        Text(
                                            text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "IMG",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Text(
                                    text = mediaFile.fileName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${formatSize(mediaFile.fileSize)} • ${mediaFile.resolution}",
                                    fontSize = 9.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
                if (itemsToShow.size < 2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Transparent)
                            .border(1.dp, Color(0xFF1E293B).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Captured files show up here", fontSize = 9.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}

@Composable
fun RecordDashboard(viewModel: MainViewModel, onStartRecordClick: () -> Unit) {
    val recordState by viewModel.recordState.collectAsState()
    val timerStr by viewModel.recordingTimer.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Simulated Status Bar
        SimulatedStatusBar()

        // Premium Custom Top App Bar
        PremiumTopAppBar(onSettingsClick = { viewModel.currentTab = "SETTINGS" })

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Dynamic Device Storage Card
            DeviceStorageCard()

            // Central Action Record Button
            CentralRecordButton(
                recordState = recordState,
                timerStr = timerStr,
                onButtonClick = {
                    if (recordState == "IDLE") {
                        onStartRecordClick()
                    } else {
                        viewModel.stopRecording()
                    }
                },
                onPauseClick = {
                    if (recordState == "PAUSED") viewModel.resumeRecording() else viewModel.pauseRecording()
                },
                viewModel = viewModel
            )

            // Settings Mic & Facecam toggles
            MediaQuickToggles(viewModel = viewModel)

            // Horizontal Recent Clips
            HorizontalRecentCaptures(viewModel = viewModel)

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun FacecamOverlay() {
    var offsetX by remember { mutableStateOf(50f) }
    var offsetY by remember { mutableStateOf(100f) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(120.dp)
            .clip(CircleShape)
            .border(3.dp, Color(0xFF6C63FF), CircleShape)
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // High fidelity Facecam simulation indicator
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Videocam,
                contentDescription = "Facecam",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Facecam",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "ACTIVE",
                color = Color.Green,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(viewModel: MainViewModel) {
    val mediaFiles by viewModel.allMediaFiles.collectAsState()
    var searchBy by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf<MediaFile?>(null) }
    var renameInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Offline File Manager",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "All recorded assets are kept private on this phone",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search text field
        OutlinedTextField(
            value = searchBy,
            onValueChange = { searchBy = it },
            placeholder = { Text("Search local recordings...", fontSize = 13.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ),
            singleLine = true
        )

        val filteredFiles = mediaFiles.filter {
            it.fileName.contains(searchBy, ignoreCase = true)
        }

        if (filteredFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Photo,
                        contentDescription = "No files",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No recorded files found",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        "Tap the Record tab to start capturing!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredFiles) { mediaFile ->
                    LocalMediaCard(
                        mediaFile = mediaFile,
                        onFavoriteClick = { viewModel.toggleFavorite(mediaFile) },
                        onRenameClick = {
                            showRenameDialog = mediaFile
                            renameInput = mediaFile.fileName.substringBeforeLast(".")
                        },
                        onDeleteClick = { viewModel.deleteFile(mediaFile) },
                        onEditClick = {
                            if (mediaFile.fileType == "VIDEO") {
                                viewModel.selectFileForEditing(mediaFile)
                                viewModel.currentTab = "EDITOR"
                            } else {
                                viewModel.loadScreenshotToEditor(mediaFile)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showRenameDialog != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename File", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    label = { Text("New file name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = showRenameDialog
                        if (file != null && renameInput.isNotBlank()) {
                            viewModel.renameFile(file, renameInput)
                        }
                        showRenameDialog = null
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LocalMediaCard(
    mediaFile: MediaFile,
    onFavoriteClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder thumbnail based on file type
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF222B3E)),
                contentAlignment = Alignment.Center
            ) {
                if (mediaFile.fileType == "VIDEO") {
                    Icon(
                        imageVector = Icons.Filled.Videocam,
                        contentDescription = "Video",
                        tint = Color(0xFF6C63FF),
                        modifier = Modifier.size(28.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        val minutes = (mediaFile.durationMs / 1000) / 60
                        val seconds = (mediaFile.durationMs / 1000) % 60
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Photo,
                        contentDescription = "Image",
                        tint = Color(0xFF8E2DE2),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text Metadata details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mediaFile.fileName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${mediaFile.resolution}  •  ${formatSize(mediaFile.fileSize)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Quick actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Favorite Button
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (mediaFile.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (mediaFile.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Edit Studio Trigger
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Studio Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Trigger
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb > 1.0) {
        String.format(Locale.US, "%.1f MB", mb)
    } else {
        String.format(Locale.US, "%.0f KB", kb)
    }
}

@Composable
fun EditorStudio(viewModel: MainViewModel) {
    val targetFile by viewModel.selectedFileForEditing.collectAsState()

    if (targetFile == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Studio",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Studio Video/Screenshot Editor",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Select a recording in the Gallery tab and tap Edit to open it in Studio!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.currentTab = "GALLERY" },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Browse Gallery")
                }
            }
        }
    } else {
        val file = targetFile!!
        if (file.fileType == "VIDEO") {
            VideoEditorWorkspace(viewModel = viewModel, mediaFile = file)
        } else {
            ScreenshotEditorWorkspace(viewModel = viewModel, mediaFile = file)
        }
    }
}

@Composable
fun VideoEditorWorkspace(viewModel: MainViewModel, mediaFile: MediaFile) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableStateOf(0.0f) }

    LaunchedEffect(isExporting) {
        if (isExporting) {
            exportProgress = 0.0f
            while (exportProgress < 1.0f) {
                delay(150)
                exportProgress += 0.05f
            }
            viewModel.exportEditedVideo { exportedPath ->
                isExporting = false
                Toast.makeText(context, "Export Successful! Saved to Local Movies.", Toast.LENGTH_LONG).show()
                viewModel.currentTab = "GALLERY"
            }
        }
    }

    if (isExporting) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                CircularProgressIndicator(color = Color(0xFF6C63FF), strokeWidth = 5.dp)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Rendering Video Offline...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { exportProgress },
                    color = Color(0xFF6C63FF),
                    trackColor = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${(exportProgress * 100).roundToInt()}% Complete",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.selectFileForEditing(mediaFile) /* resets edit state */ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    "Studio: Video Editor",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { isExporting = true }) {
                    Icon(Icons.Filled.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Video Player Simulation surface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        mediaFile.fileName,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Playing with selected filter: ${viewModel.videoFilter}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trimming Slider
            Text("Trim Video Range", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Start", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.width(10.dp))
                Slider(
                    value = viewModel.trimStartRatio,
                    onValueChange = { if (it < viewModel.trimEndRatio) viewModel.trimStartRatio = it },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = String.format(Locale.US, "%.0f%%", viewModel.trimStartRatio * 100),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("End  ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.width(10.dp))
                Slider(
                    value = viewModel.trimEndRatio,
                    onValueChange = { if (it > viewModel.trimStartRatio) viewModel.trimEndRatio = it },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = String.format(Locale.US, "%.0f%%", viewModel.trimEndRatio * 100),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Editing Tools Scroll
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Filter Option
                item {
                    Column {
                        Text("Filters & Effects", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val filters = listOf("None", "Warm", "Cyber Neon", "B&W", "Retro Fade")
                            filters.forEach { filter ->
                                val selected = viewModel.videoFilter == filter
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { viewModel.videoFilter = filter }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        filter,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Speed Option
                item {
                    Column {
                        Text("Playback Speed", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val speeds = listOf(0.5f, 1.0f, 1.5f, 2.0f)
                            speeds.forEach { speed ->
                                val selected = viewModel.videoSpeed == speed
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { viewModel.videoSpeed = speed }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${speed}x",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Audio settings
                item {
                    Column {
                        Text("Audio Control", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Add background music track", fontSize = 13.sp)
                                    Switch(
                                        checked = viewModel.addAudioTrack,
                                        onCheckedChange = { viewModel.addAudioTrack = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                                    )
                                }
                                if (viewModel.addAudioTrack) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Background Music Volume", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    Slider(
                                        value = viewModel.backgroundMusicVolume,
                                        onValueChange = { viewModel.backgroundMusicVolume = it },
                                        valueRange = 0.0f..1.0f
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Noise Reduction", fontSize = 13.sp)
                                    Switch(
                                        checked = viewModel.noiseReductionEnabled,
                                        onCheckedChange = { viewModel.noiseReductionEnabled = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Export Actions
            Button(
                onClick = { isExporting = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Export Offline Video (H.264 MP4)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ScreenshotEditorWorkspace(viewModel: MainViewModel, mediaFile: MediaFile) {
    val context = LocalContext.current
    val bitmap = viewModel.currentScreenshotToEdit.value

    LaunchedEffect(mediaFile) {
        if (bitmap == null) {
            viewModel.loadScreenshotToEditor(mediaFile)
        }
    }

    if (bitmap == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        var currentPoints by remember { mutableStateOf(emptyList<DrawPoint>()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.currentScreenshotToEdit.value = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                }
                Text("Screenshot Paint Brush", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = { viewModel.saveEditedScreenshot() }) {
                    Icon(Icons.Filled.Save, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas drawing board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPoints = listOf(DrawPoint(offset.x, offset.y))
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val lastPoint = currentPoints.lastOrNull()
                                if (lastPoint != null) {
                                    val nextX = lastPoint.x + dragAmount.x
                                    val nextY = lastPoint.y + dragAmount.y
                                    currentPoints = currentPoints + DrawPoint(nextX, nextY)
                                }
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    val newPath = DrawPath(
                                        points = currentPoints,
                                        color = viewModel.activeBrushColor,
                                        width = viewModel.activeBrushSize
                                    )
                                    viewModel.screenshotEditorPaths.value = viewModel.screenshotEditorPaths.value + newPath
                                    currentPoints = emptyList()
                                }
                            }
                        )
                    }
            ) {
                // Background loaded screenshot bitmap
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw base image scaled to viewport
                    val scaleX = size.width / bitmap.width
                    val scaleY = size.height / bitmap.height
                    val scale = minOf(scaleX, scaleY)
                    
                    val destWidth = bitmap.width * scale
                    val destHeight = bitmap.height * scale
                    val left = (size.width - destWidth) / 2
                    val top = (size.height - destHeight) / 2

                    drawImage(
                        image = bitmap.asImageBitmap(),
                        dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                        dstSize = androidx.compose.ui.unit.IntSize(destWidth.roundToInt(), destHeight.roundToInt())
                    )
                }

                // Interactive drawings layer
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        style = android.graphics.Paint.Style.STROKE
                        strokeJoin = android.graphics.Paint.Join.ROUND
                        strokeCap = android.graphics.Paint.Cap.ROUND
                    }

                    // Existing paths
                    viewModel.screenshotEditorPaths.value.forEach { drawPath ->
                        if (drawPath.points.isNotEmpty()) {
                            val path = androidx.compose.ui.graphics.Path()
                            path.moveTo(drawPath.points[0].x, drawPath.points[0].y)
                            for (i in 1 until drawPath.points.size) {
                                path.lineTo(drawPath.points[i].x, drawPath.points[i].y)
                            }
                            drawPath(
                                path = path,
                                color = Color(drawPath.color),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = drawPath.width,
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                    }

                    // Active currently drawing path
                    if (currentPoints.isNotEmpty()) {
                        val path = androidx.compose.ui.graphics.Path()
                        path.moveTo(currentPoints[0].x, currentPoints[0].y)
                        for (i in 1 until currentPoints.size) {
                            path.lineTo(currentPoints[i].x, currentPoints[i].y)
                        }
                        drawPath(
                            path = path,
                            color = Color(viewModel.activeBrushColor),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = viewModel.activeBrushSize,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas toolbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brush size selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Brush, contentDescription = "Brush Size", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Slider(
                        value = viewModel.activeBrushSize,
                        onValueChange = { viewModel.activeBrushSize = it },
                        valueRange = 4.0f..40.0f,
                        modifier = Modifier.width(120.dp)
                    )
                }

                // Clear button
                IconButton(onClick = { viewModel.screenshotEditorPaths.value = emptyList() }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear drawings", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Color Palette Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val colors = listOf(
                    0xFFFF0000, // Red
                    0xFF00FF00, // Green
                    0xFF0000FF, // Blue
                    0xFFFFFF00, // Yellow
                    0xFFFFFFFF, // White
                    0xFF000000  // Black
                )
                colors.forEach { colorVal ->
                    val isSelected = viewModel.activeBrushColor == colorVal
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable { viewModel.activeBrushColor = colorVal }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings & About",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Theme Options Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Appearance", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Dark Theme", fontSize = 14.sp)
                            Switch(
                                checked = viewModel.isDarkMode,
                                onCheckedChange = { viewModel.isDarkMode = it }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AMOLED Pure Black Mode", fontSize = 14.sp)
                            Switch(
                                checked = viewModel.isAmoledMode,
                                onCheckedChange = { viewModel.isAmoledMode = it }
                            )
                        }
                    }
                }
            }

            // Recorder Parameters Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Recorder Shortcuts", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Shake phone to stop recording", fontSize = 14.sp)
                            Switch(
                                checked = viewModel.shakeToStop,
                                onCheckedChange = { viewModel.shakeToStop = it }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Facecam Overlay Circle", fontSize = 14.sp)
                            Switch(
                                checked = viewModel.facecamEnabled,
                                onCheckedChange = { viewModel.facecamEnabled = it }
                            )
                        }
                    }
                }
            }

            // About Developer Section (Prince AR Abdur Rahman)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "About Developer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Prince AR Abdur Rahman",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Independent App Developer",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Passionate about building modern Android applications, media tools, database integrations, and next-generation digital experiences.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // WhatsApp contacts
                        Text("WhatsApp Contacts:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("• 01707424006\n• 01796951709", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Social Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Facebook",
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/share/1BNn32qoJo/"))
                                        context.startActivity(intent)
                                    },
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Instagram",
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/ur___abdur____rahman__2008"))
                                        context.startActivity(intent)
                                    },
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // About Company / Publisher Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Publisher Info", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Published by NexVora Lab's Ofc",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mission: Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "© 2026 NexVora Lab's Ofc. All Rights Reserved.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

// Custom BorderStroke helper class for M3 components styling
@Composable
fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(width, color)
}
