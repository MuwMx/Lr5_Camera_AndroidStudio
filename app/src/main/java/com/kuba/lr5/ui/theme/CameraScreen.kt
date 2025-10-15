package com.kuba.lr5.ui.theme

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.kuba.lr5.utils.PermissionUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            // Тут можна додати логіку, якщо дозвіл отримано/відхилено
            if (!isGranted) {
                // Наприклад, показати повідомлення
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!PermissionUtils.hasCameraPermission(context)) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var brightness by remember { mutableStateOf(0.5f) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Прев'ю камери через AndroidView
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    // Отримуємо SurfaceProvider і передаємо його в Preview
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(this.surfaceProvider)
                    }

                    // Ініціалізація камери
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview
                            )
                        } catch (e: Exception) {
                            Log.e("CameraScreen", "Use case binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Прозорий слайдер яскравості (варіант 11)
        Slider(
            value = brightness,
            onValueChange = { brightness = it },
            modifier = Modifier
                .padding(end = 16.dp)
                .width(200.dp)
                .align(Alignment.CenterEnd),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        )

        Text(
            text = "Яскравість: ${String.format("%.1f", brightness)}",
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(top = 16.dp, end = 16.dp)
                .align(Alignment.TopEnd)
        )
    }
}

@Composable
fun RequestPermissionScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Потрібен дозвіл на камеру", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Надайте дозвіл у налаштуваннях додатку")
    }
}