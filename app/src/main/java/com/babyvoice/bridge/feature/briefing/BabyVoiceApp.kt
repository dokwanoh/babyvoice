package com.babyvoice.bridge.feature.briefing

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.babyvoice.bridge.core.ui.BabyVoiceTheme

@Composable
fun BabyVoiceApp(
    viewModel: BabyVoiceViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onSpeakRequested(granted)
    }

    BabyVoiceTheme {
        BabyVoiceScreen(
            state = uiState,
            onBriefingRequested = viewModel::onBriefingRequested,
            onSpeakRequested = {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO,
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    viewModel.onSpeakRequested(true)
                } else {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onStopRequested = viewModel::onStopRequested,
            onScenarioSelected = viewModel::onScenarioSelected,
            onHideSensitiveChanged = viewModel::onHideSensitiveChanged,
            onPatternEstimateChanged = viewModel::onPatternEstimateChanged,
            onSpeechRateChanged = viewModel::onSpeechRateChanged,
            onBabySelected = viewModel::onBabySelected,
            onCacheClearRequested = viewModel::onCacheClearRequested,
            onAccessibilitySettingsRequested = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            onStatusDismissed = viewModel::clearStatusMessage,
        )
    }
}
