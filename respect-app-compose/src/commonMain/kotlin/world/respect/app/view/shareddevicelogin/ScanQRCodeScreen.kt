package world.respect.app.view.shareddevicelogin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import qrscanner.CameraLens
import qrscanner.OverlayShape
import qrscanner.QrScanner
import world.respect.shared.viewmodel.sharedschooldevicelogin.ScanQRCodeViewModel

@Composable
fun ScanQRCodeScreen(
    viewModel: ScanQRCodeViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val zoomLevels = listOf(1f, 2f, 3f)
    var selectedZoomIndex = 0

    var qrCodeURL by remember { mutableStateOf("") }
    var flashlightOn by remember { mutableStateOf(false) }
    var openImagePicker by remember { mutableStateOf(false) }
    var overlayShape by remember { mutableStateOf(OverlayShape.Square) }
    var cameraLens by remember { mutableStateOf(CameraLens.Back) }
    var currentZoomLevel by remember { mutableStateOf(zoomLevels[selectedZoomIndex]) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScreenVisible by remember { mutableStateOf(true) }

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
    if (isScreenVisible) {
        QrScanner(
            modifier = Modifier.fillMaxSize(),
            flashlightOn = flashlightOn,
            cameraLens = cameraLens,
            openImagePicker = openImagePicker,
            onCompletion = { qrCodeURL = it },
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
    }
}