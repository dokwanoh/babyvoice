package com.babyvoice.bridge

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import com.babyvoice.bridge.feature.briefing.BabyVoiceApp
import com.babyvoice.bridge.feature.briefing.BabyVoiceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: BabyVoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val shouldHandleInitialIntent = savedInstanceState == null
        setContent {
            LaunchedEffect(shouldHandleInitialIntent) {
                if (shouldHandleInitialIntent) {
                    handleIntent(intent)
                }
            }
            BabyVoiceApp(viewModel = viewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val deepLink = intent.data
        if (deepLink != null) {
            viewModel.onDeepLink(deepLink)
            return
        }
        if (intent.action == Intent.ACTION_MAIN) {
            viewModel.onBriefingRequested()
        }
    }
}
