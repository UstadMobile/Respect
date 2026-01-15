package world.respect.app.view.scanqrcode

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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalWindowInfo
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
import world.respect.shared.generated.resources.qr_code_invalid_format
import world.respect.shared.generated.resources.try_again
import world.respect.shared.generated.resources.url
import world.respect.shared.viewmodel.scanqrcode.ScanQRCodeUiState
import world.respect.shared.viewmodel.scanqrcode.ScanQRCodeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQRCodeScreen(
    viewModel: ScanQRCodeViewModel
) {
    val uiState: ScanQRCodeUiState by viewModel.uiState.collectAsState(
        ScanQRCodeUiState()
    )
    val coroutineScope = rememberCoroutineScope()

    var isCameraActive by remember { mutableStateOf(true) }
    var openImagePicker by remember { mutableStateOf(false) }
    var overlayShape by remember { mutableStateOf(OverlayShape.Square) }
    var cameraLens by remember { mutableStateOf(CameraLens.Back) }
    var flashlightOn by remember { mutableStateOf(false) }

    // Zoom levels
    val zoomLevels = listOf(1f, 2f, 3f)
    var selectedZoomIndex by remember { mutableStateOf(0) }
    var currentZoomLevel by remember { mutableStateOf(zoomLevels[selectedZoomIndex]) }

    val windowInfo = LocalWindowInfo.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Show error screen if there's an error
        if (uiState.errorMessage != null) {
            ErrorScreen(
                onTryAgain = {
                    viewModel.resetErrorState()
                    isCameraActive = true
                }
            )
        } else if (isCameraActive && !uiState.showManualEntryDialog) {
            // Show QR scanner when no error and camera is active
            val isLandscape = windowInfo.containerSize.width > windowInfo.containerSize.height
            QrScanner(
                modifier = Modifier.fillMaxSize(),
                flashlightOn = flashlightOn,
                cameraLens = cameraLens,
                openImagePicker = openImagePicker,
                onCompletion = { scannedUrl ->
                    if (isCameraActive) { // Guard against multiple triggers
                        isCameraActive = false // Pause camera immediately on success
                        coroutineScope.launch {
                            viewModel.processQrCodeUrl(scannedUrl)
                        }
                    }
                },
                zoomLevel = currentZoomLevel,
                maxZoomLevel = 3f,
                imagePickerHandler = { openImagePicker = it },
                onFailure = { errorMessage ->
                    viewModel.resetErrorState()
                },
                customOverlay = {
                    // Calculate scan area size - smaller in landscape, larger in portrait
                    val scanAreaFraction = if (isLandscape) 0.6f else 0.8f
                    val scanAreaSize = minOf(size.width, size.height) * scanAreaFraction

                    val left = (size.width - scanAreaSize) / 2
                    val top = (size.height - scanAreaSize) / 2

                    // Draw overlay outside scan area
                    drawRect(
                        color = Color(0x88000000),
                        size = Size(size.width, size.height),
                    )

                    // Clear scan area
                    drawRect(
                        color = Color.Transparent,
                        blendMode = BlendMode.Overlay,
                        topLeft = Offset(left, top),
                        size = Size(scanAreaSize, scanAreaSize)
                    )

                    // Draw border
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(left, top),
                        size = Size(scanAreaSize, scanAreaSize),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            )
        }

        // Manual URL Entry Dialog
        if (uiState.showManualEntryDialog) {
            var manualUrlText by remember { mutableStateOf(TextFieldValue("")) }
            ManualUrlEntryDialog(
                manualUrlText = manualUrlText,
                onUrlTextChange = { manualUrlText = it },
                onDismiss = {
                    viewModel.hideManualEntryDialog()
                    manualUrlText = TextFieldValue("")
                },
                onSubmit = { url ->
                    if (url.isNotEmpty()) {
                        coroutineScope.launch {
                            viewModel.processQrCodeUrl(url)
                        }
                    }
                },
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
    onSubmit: (String) -> Unit,
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
                onValueChange = { newValue ->
                    onUrlTextChange(newValue)
                },
                label = {
                    Text(stringResource(Res.string.url))
                },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                singleLine = true,
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

@Composable
private fun ErrorScreen(
    onTryAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(100.dp).padding(bottom = 10.dp),
            imageVector = Icons.Default.WarningAmber,
            contentDescription = "WarningAmber",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(Res.string.qr_code_invalid_format),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = onTryAgain,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(
                text = stringResource(Res.string.try_again),
            )
        }
    }
}