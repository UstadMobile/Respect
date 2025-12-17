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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import qrscanner.CameraLens
import qrscanner.OverlayShape
import qrscanner.QrScanner
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.close
import world.respect.shared.generated.resources.ok
import world.respect.shared.generated.resources.paste_url
import world.respect.shared.generated.resources.url
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
    val snackbarHostState = remember { SnackbarHostState() }

    // Manual entry dialog state
    var showManualEntryDialog by remember { mutableStateOf(false) }
    var manualUrlText by remember { mutableStateOf(TextFieldValue("")) }

    // Camera states - all with default values
    var isCameraActive by remember { mutableStateOf(true) }
    var openImagePicker by remember { mutableStateOf(false) }
    var overlayShape by remember { mutableStateOf(OverlayShape.Square) }
    var cameraLens by remember { mutableStateOf(CameraLens.Back) }
    var flashlightOn by remember { mutableStateOf(false) }

    // Zoom levels
    val zoomLevels = listOf(1f, 2f, 3f)
    var selectedZoomIndex by remember { mutableStateOf(0) }
    var currentZoomLevel by remember { mutableStateOf(zoomLevels[selectedZoomIndex]) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isCameraActive) {
            QrScanner(
                modifier = Modifier.fillMaxSize(),
                flashlightOn = flashlightOn,
                cameraLens = cameraLens,
                openImagePicker = openImagePicker,
                onCompletion = { scannedUrl ->
                    coroutineScope.launch {
                        viewModel.processQrCodeUrl(scannedUrl)
                    }
                },
                zoomLevel = currentZoomLevel,
                maxZoomLevel = 3f,
                imagePickerHandler = { openImagePicker = it },
                onFailure = { errorMessage ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            errorMessage.ifEmpty { "Failed to scan QR code" }
                        )
                    }
                },
                overlayShape = overlayShape
            )
        }

        if (uiState.showPasteButton) {
            PasteButton(
                onPasteClick = { showManualEntryDialog = true }
            )
        }

        // Manual URL Entry Dialog
        if (showManualEntryDialog) {
            ManualUrlEntryDialog(
                manualUrlText = manualUrlText,
                onUrlTextChange = { manualUrlText = it },
                onDismiss = {
                    showManualEntryDialog = false
                    manualUrlText = TextFieldValue("")
                },
                onSubmit = { url ->
                    showManualEntryDialog = false
                    if (url.isNotEmpty()) {
                        coroutineScope.launch {
                            viewModel.processQrCodeUrl(url)
                        }
                    }
                    manualUrlText = TextFieldValue("")
                }
            )
        }
    }
}

@Composable
private fun PasteButton(
    onPasteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        OutlinedButton(
            onClick = onPasteClick,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = stringResource(Res.string.paste_url),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualUrlEntryDialog(
    manualUrlText: TextFieldValue,
    onUrlTextChange: (TextFieldValue) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraLarge
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.paste_url),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onDismiss,
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
                onValueChange = onUrlTextChange,
                label = {
                    Text(stringResource(Res.string.url))
                },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(stringResource(Res.string.cancel))
                }

                Spacer(modifier = Modifier.width(16.dp))

                TextButton(
                    onClick = {
                        val url = manualUrlText.text.trim()
                        onSubmit(url)
                    },
                    modifier = Modifier.width(100.dp),
                    enabled = manualUrlText.text.isNotBlank()
                ) {
                    Text(stringResource(Res.string.ok))
                }
            }
        }
    }
}