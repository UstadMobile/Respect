package world.respect.app.view.shareddevicelogin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import qrscanner.CameraLens
import qrscanner.OverlayShape
import qrscanner.QrScanner
import world.respect.shared.generated.resources.*
import world.respect.shared.viewmodel.sharedschooldevicelogin.ScanQRCodeUiState
import world.respect.shared.viewmodel.sharedschooldevicelogin.ScanQRCodeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQRCodeScreen(
    viewModel: ScanQRCodeViewModel
) {
    val uiState: ScanQRCodeUiState by viewModel.uiState.collectAsState(
        ScanQRCodeUiState()
    )
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val zoomLevels = listOf(1f, 2f, 3f)
    var selectedZoomIndex by remember { mutableStateOf(0) }

    var qrCodeURL by remember { mutableStateOf("") }
    var showManualEntryDialog by remember { mutableStateOf(false) }
    var manualUrlText by remember { mutableStateOf(TextFieldValue("")) }
    var showPasteOption by remember { mutableStateOf(false) }
    var flashlightOn by remember { mutableStateOf(false) }
    var openImagePicker by remember { mutableStateOf(false) }
    var overlayShape by remember { mutableStateOf(OverlayShape.Square) }
    var cameraLens by remember { mutableStateOf(CameraLens.Back) }
    var currentZoomLevel by remember { mutableStateOf(zoomLevels[selectedZoomIndex]) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScreenVisible by remember { mutableStateOf(true) }

    LaunchedEffect(showManualEntryDialog) {
        if (showManualEntryDialog) {
            val clipboardText = clipboardManager.getText()?.toString() ?: ""
            showPasteOption = clipboardText.isNotBlank() &&
                    (clipboardText.startsWith("http://") || clipboardText.startsWith("https://"))

            // If there's a valid URL in clipboard, pre-fill it
            if (showPasteOption) {
                manualUrlText = TextFieldValue(clipboardText)
            }
        }
    }

    // Stop QR scanner when screen is not visible
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isScreenVisible = true
                Lifecycle.Event.ON_PAUSE -> isScreenVisible = false
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            isScreenVisible = false
        }
    }

    // Handle scanned QR code
    LaunchedEffect(qrCodeURL) {
        if (qrCodeURL.isNotEmpty()) {
            // Process the scanned URL
            viewModel.processQrCodeUrl(qrCodeURL)
        }
    }

    if (isScreenVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            QrScanner(
                modifier = Modifier.fillMaxSize(),
                flashlightOn = flashlightOn,
                cameraLens = cameraLens,
                openImagePicker = openImagePicker,
                onCompletion = { scannedUrl ->
                    qrCodeURL = scannedUrl
                },
                zoomLevel = currentZoomLevel,
                maxZoomLevel = 3f,
                imagePickerHandler = { openImagePicker = it },
                onFailure = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(it.ifEmpty { "Invalid QR Code" })
                    }
                },
                overlayShape = overlayShape
            )

            // Paste URL Button
            OutlinedButton(
                onClick = { showManualEntryDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "Paste Url",
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Manual URL Entry Dialog
            if (showManualEntryDialog) {
                BasicAlertDialog(
                    onDismissRequest = {
                        showManualEntryDialog = false
                        manualUrlText = TextFieldValue("")
                    },
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.extraLarge
                            )
                            .padding(top = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.paste_url),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(
                                onClick = {
                                    showManualEntryDialog = false
                                    manualUrlText = TextFieldValue("")
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(Res.string.close),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedTextField(
                            value = manualUrlText,
                            onValueChange = { manualUrlText = it },
                            label = {
                                Text(
                                    text = stringResource(Res.string.url),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth(),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    showManualEntryDialog = false
                                    manualUrlText = TextFieldValue("")
                                },
                                modifier = Modifier.width(100.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(
                                    text = stringResource(Res.string.cancel),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            TextButton(
                                onClick = {
                                    val url = manualUrlText.text.trim()
                                    if (url.isNotEmpty()) {
                                        showManualEntryDialog = false
                                        viewModel.processQrCodeUrl(url)
                                        manualUrlText = TextFieldValue("")
                                    }
                                },
                                modifier = Modifier.width(100.dp),
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Text(
                                    text = stringResource(Res.string.ok),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}