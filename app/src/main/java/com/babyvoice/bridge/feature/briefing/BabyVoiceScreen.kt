package com.babyvoice.bridge.feature.briefing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.babyvoice.bridge.core.common.formatAmountMl
import com.babyvoice.bridge.core.common.DemoScenario
import com.babyvoice.bridge.core.common.formatKoreanClockTime
import com.babyvoice.bridge.core.common.formatRefreshTimestamp
import com.babyvoice.bridge.core.model.BabyId
import com.babyvoice.bridge.core.model.BabyProfile
import com.babyvoice.bridge.core.model.DiaperType
import com.babyvoice.bridge.core.model.FeedingType
import com.babyvoice.bridge.core.model.ProviderState
import com.babyvoice.bridge.core.model.SyncStatus
import com.babyvoice.bridge.core.voice.VoiceIntent
import java.time.Clock
import java.time.ZoneId

@Composable
fun BabyVoiceScreen(
    state: BabyVoiceUiState,
    onBriefingRequested: () -> Unit,
    onSpeakRequested: () -> Unit,
    onStopRequested: () -> Unit,
    onScenarioSelected: (DemoScenario) -> Unit,
    onHideSensitiveChanged: (Boolean) -> Unit,
    onPatternEstimateChanged: (Boolean) -> Unit,
    onSpeechRateChanged: (Float) -> Unit,
    onBabySelected: (BabyId) -> Unit,
    onCacheClearRequested: () -> Unit,
    onAccessibilitySettingsRequested: () -> Unit,
    onStatusDismissed: () -> Unit,
) {
    val isExpanded = state.babies.size > 1 || state.providerInfo.flavor != "demo"
    val scrollState = rememberScrollState()
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val wideLayout = maxWidth >= 840.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeaderCard(state = state, onStatusDismissed = onStatusDismissed)
            if (wideLayout) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ActionCard(
                            state = state,
                            onBriefingRequested = onBriefingRequested,
                            onSpeakRequested = onSpeakRequested,
                            onStopRequested = onStopRequested,
                        )
                        BriefingCard(state = state)
                        SnapshotGrid(state = state)
                    }
                    Column(modifier = Modifier.weight(0.9f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FamilyCard(
                            state = state,
                            onBabySelected = onBabySelected,
                        )
                        ScenarioCard(state = state, onScenarioSelected = onScenarioSelected)
                        SettingsCard(
                            state = state,
                            onHideSensitiveChanged = onHideSensitiveChanged,
                            onPatternEstimateChanged = onPatternEstimateChanged,
                            onSpeechRateChanged = onSpeechRateChanged,
                            onCacheClearRequested = onCacheClearRequested,
                            onAccessibilitySettingsRequested = onAccessibilitySettingsRequested,
                        )
                    }
                }
            } else {
                ActionCard(
                    state = state,
                    onBriefingRequested = onBriefingRequested,
                    onSpeakRequested = onSpeakRequested,
                    onStopRequested = onStopRequested,
                )
                BriefingCard(state = state)
                SnapshotGrid(state = state)
                FamilyCard(state = state, onBabySelected = onBabySelected)
                ScenarioCard(state = state, onScenarioSelected = onScenarioSelected)
                SettingsCard(
                    state = state,
                    onHideSensitiveChanged = onHideSensitiveChanged,
                    onPatternEstimateChanged = onPatternEstimateChanged,
                onSpeechRateChanged = onSpeechRateChanged,
                onCacheClearRequested = onCacheClearRequested,
                onAccessibilitySettingsRequested = onAccessibilitySettingsRequested,
            )
        }
    }
}
}

@Composable
private fun HeaderCard(
    state: BabyVoiceUiState,
    onStatusDismissed: () -> Unit,
) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("아기 브리핑", style = MaterialTheme.typography.displayLarge)
                    Text(
                        text = "한국어 음성 브리핑",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "출처: ${state.providerInfo.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ProviderPill(state.providerInfo.state)
            }
            if (state.statusMessage != null) {
                StatusBanner(message = state.statusMessage, onDismissed = onStatusDismissed)
            }
            if (state.snapshot != null) {
                val fetchedAt = formatRefreshTimestamp(state.snapshot.fetchedAt, ZoneId.systemDefault())
                Text(
                    text = "마지막 업데이트 $fetchedAt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    state: BabyVoiceUiState,
    onBriefingRequested: () -> Unit,
    onSpeakRequested: () -> Unit,
    onStopRequested: () -> Unit,
) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("주요 동작", style = MaterialTheme.typography.headlineMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onBriefingRequested,
                    modifier = Modifier.weight(1f).heightIn(min = 72.dp),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text("브리핑 듣기")
                }
                Button(
                    onClick = onSpeakRequested,
                    modifier = Modifier.weight(1f).heightIn(min = 72.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                    ),
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = null)
                    Text("말하기")
                }
            }
            OutlinedButton(
                onClick = onStopRequested,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Icon(Icons.Filled.Stop, contentDescription = null)
                Text("그만")
            }
            Text(
                text = voiceStateLabel(state.voiceState),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            state.briefing?.followUpPrompt?.let {
                TextButton(onClick = onSpeakRequested) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    Text("후속 질문")
                }
            }
        }
    }
}

@Composable
private fun BriefingCard(state: BabyVoiceUiState) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("브리핑", style = MaterialTheme.typography.headlineMedium)
            if (state.briefing == null) {
                Text(
                    text = "아직 브리핑할 내용이 없어요.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                state.briefing.utterances.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                state.briefing.followUpPrompt?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SnapshotGrid(state: BabyVoiceUiState) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("기록 요약", style = MaterialTheme.typography.headlineMedium)
            val snapshot = state.snapshot
            val mask = state.settings.hideSensitiveData
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryTile(
                    title = "수유",
                    value = feedingSummary(snapshot, mask),
                    modifier = Modifier.weight(1f),
                    highlighted = state.focusIntent == VoiceIntent.LAST_FEEDING || state.focusIntent == VoiceIntent.NEXT_FEEDING,
                )
                SummaryTile(
                    title = "수면",
                    value = sleepSummary(snapshot, mask),
                    modifier = Modifier.weight(1f),
                    highlighted = state.focusIntent == VoiceIntent.LAST_WAKE || state.focusIntent == VoiceIntent.NEXT_SLEEP,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryTile(
                    title = "기저귀",
                    value = diaperSummary(snapshot, mask),
                    modifier = Modifier.weight(1f),
                    highlighted = state.focusIntent == VoiceIntent.LAST_DIAPER,
                )
                SummaryTile(
                    title = "최신성",
                    value = freshnessSummary(snapshot),
                    modifier = Modifier.weight(1f),
                    highlighted = state.focusIntent == VoiceIntent.DATA_FRESHNESS,
                )
            }
        }
    }
}

@Composable
private fun FamilyCard(
    state: BabyVoiceUiState,
    onBabySelected: (BabyId) -> Unit,
) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("아기", style = MaterialTheme.typography.headlineMedium)
            if (state.babies.isEmpty()) {
                Text("아직 아기가 없어요.", style = MaterialTheme.typography.bodyMedium)
            } else {
                state.babies.forEach { baby ->
                    AssistChip(
                        onClick = { onBabySelected(baby.id) },
                        label = { Text(baby.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ScenarioCard(
    state: BabyVoiceUiState,
    onScenarioSelected: (DemoScenario) -> Unit,
) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("데모 시나리오", style = MaterialTheme.typography.headlineMedium)
            DemoScenario.entries.forEach { scenario ->
                FilterChip(
                    selected = state.settings.demoScenario == scenario,
                    onClick = { onScenarioSelected(scenario) },
                    label = { Text(scenarioLabel(scenario)) },
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    state: BabyVoiceUiState,
    onHideSensitiveChanged: (Boolean) -> Unit,
    onPatternEstimateChanged: (Boolean) -> Unit,
    onSpeechRateChanged: (Float) -> Unit,
    onCacheClearRequested: () -> Unit,
    onAccessibilitySettingsRequested: () -> Unit,
) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("설정", style = MaterialTheme.typography.headlineMedium)
            ToggleRow(
                label = "민감정보 가리기",
                checked = state.settings.hideSensitiveData,
                onCheckedChange = onHideSensitiveChanged,
            )
            ToggleRow(
                label = "패턴 추정 사용",
                checked = state.settings.usePatternEstimate,
                onCheckedChange = onPatternEstimateChanged,
            )
            Column {
                Text("말하기 속도", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = state.settings.speechRate,
                    onValueChange = onSpeechRateChanged,
                    valueRange = 0.5f..2.0f,
                )
            }
            TextButton(onClick = onCacheClearRequested) {
                Text("캐시 삭제")
            }
            if (state.providerInfo.flavor == "lab") {
                Text(
                    text = "BabyTime 위젯을 읽으려면 접근성 서비스를 허용해야 합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = onAccessibilitySettingsRequested) {
                    Text("접근성 설정 열기")
                }
            }
        }
    }
}

@Composable
private fun AppCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun SummaryTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    val colors = if (highlighted) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    }
    Card(
        modifier = modifier,
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StatusBanner(
    message: String,
    onDismissed: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, contentDescription = null)
                Text(
                    text = message,
                    modifier = Modifier.padding(start = 8.dp).weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(onClick = onDismissed) {
                Icon(Icons.Filled.Close, contentDescription = "메시지 닫기")
            }
        }
    }
}

@Composable
private fun ProviderPill(state: ProviderState) {
    val color = when (state) {
        ProviderState.READY -> MaterialTheme.colorScheme.secondaryContainer
        ProviderState.NEEDS_PERMISSION -> MaterialTheme.colorScheme.tertiaryContainer
        ProviderState.NOT_CONFIGURED -> MaterialTheme.colorScheme.surfaceVariant
        ProviderState.UNSUPPORTED -> MaterialTheme.colorScheme.surfaceVariant
        ProviderState.ERROR -> MaterialTheme.colorScheme.errorContainer
    }
    Card(colors = CardDefaults.cardColors(containerColor = color)) {
        Text(
            text = providerLabel(state),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun providerLabel(state: ProviderState): String = when (state) {
    ProviderState.READY -> "공급자 연결됨"
    ProviderState.NEEDS_PERMISSION -> "권한 필요"
    ProviderState.NOT_CONFIGURED -> "연결 준비 중"
    ProviderState.UNSUPPORTED -> "지원 안 됨"
    ProviderState.ERROR -> "오류"
}

private fun voiceStateLabel(state: com.babyvoice.bridge.core.voice.VoiceState): String = when (state) {
    com.babyvoice.bridge.core.voice.VoiceState.Idle -> "대기 중"
    com.babyvoice.bridge.core.voice.VoiceState.Preparing -> "준비 중"
    com.babyvoice.bridge.core.voice.VoiceState.Listening -> "듣는 중"
    com.babyvoice.bridge.core.voice.VoiceState.Processing -> "처리 중"
    com.babyvoice.bridge.core.voice.VoiceState.Speaking -> "말하는 중"
    com.babyvoice.bridge.core.voice.VoiceState.PermissionRequired -> "권한 필요"
    is com.babyvoice.bridge.core.voice.VoiceState.Error -> "오류"
}

private fun scenarioLabel(scenario: DemoScenario): String = when (scenario) {
    DemoScenario.NORMAL -> "정상 데이터"
    DemoScenario.NO_RECORDS -> "기록 없음"
    DemoScenario.STALE_DATA -> "오래된 데이터"
    DemoScenario.CURRENT_FEEDING -> "현재 수유 중"
    DemoScenario.CURRENT_SLEEP -> "현재 수면 중"
    DemoScenario.MULTIPLE_BABIES -> "다태아"
    DemoScenario.NO_NEXT_FEEDING_AMOUNT -> "다음 수유량 없음"
    DemoScenario.PROVIDER_ERROR -> "공급자 오류"
    DemoScenario.PERMISSION_DENIED -> "권한 거부"
    DemoScenario.SYNCING -> "동기화 중"
}

private fun feedingSummary(snapshot: com.babyvoice.bridge.core.model.BabyCareSnapshot?, mask: Boolean): String {
    if (snapshot == null) return "아직 수유 기록이 없어요."
    if (snapshot.currentFeeding != null) return "현재 수유 중"
    val feeding = snapshot.lastFeeding ?: return "아직 수유 기록이 없어요."
    val time = feeding.value.startedAt ?: feeding.observedAt ?: snapshot.fetchedAt
    val amount = if (mask) "가려짐" else feeding.value.amountMl?.let(::formatAmountMl) ?: "알 수 없음"
    val type = when (feeding.value.feedingType) {
        FeedingType.BREAST_MILK -> "모유"
        FeedingType.FORMULA -> "분유"
        FeedingType.SOLID -> "이유식"
        FeedingType.MIXED -> "혼합"
        FeedingType.UNKNOWN -> "수유"
    }
    return "${formatKoreanClockTime(time, ZoneId.systemDefault())}, $amount, $type"
}

private fun sleepSummary(snapshot: com.babyvoice.bridge.core.model.BabyCareSnapshot?, mask: Boolean): String {
    if (snapshot == null) return "최근 수면 정보 없음"
    val sleep = snapshot.currentSleep
    if (sleep?.isCurrentlySleeping == true) {
        val startedAt = sleep.startedAt ?: snapshot.lastWakeAt?.value ?: snapshot.fetchedAt
        return "수면 중, ${formatKoreanClockTime(startedAt, ZoneId.systemDefault())}"
    }
    val wake = snapshot.lastWakeAt ?: return "최근 수면 정보 없음"
    return if (mask) {
        "깨어 있음"
    } else {
        formatKoreanClockTime(wake.value, ZoneId.systemDefault())
    }
}

private fun diaperSummary(snapshot: com.babyvoice.bridge.core.model.BabyCareSnapshot?, mask: Boolean): String {
    if (snapshot == null) return "기저귀 정보 없음"
    val diaper = snapshot.lastDiaper ?: return "기저귀 정보 없음"
    val time = diaper.value.changedAt ?: diaper.observedAt ?: snapshot.fetchedAt
    val type = when (diaper.value.diaperType) {
        DiaperType.URINE -> "소변"
        DiaperType.STOOL -> "대변"
        DiaperType.BOTH -> "소변+대변"
        DiaperType.DRY -> "건조"
        DiaperType.UNKNOWN -> "기저귀"
    }
    return if (mask) "가려짐" else "${formatKoreanClockTime(time, ZoneId.systemDefault())}, $type"
}

private fun freshnessSummary(snapshot: com.babyvoice.bridge.core.model.BabyCareSnapshot?): String {
    if (snapshot == null) return "정보 없음"
    return when (val status = snapshot.syncStatus) {
        SyncStatus.Synced -> "최신"
        SyncStatus.Syncing -> "동기화 중"
        is SyncStatus.Stale -> "오래됨"
        is SyncStatus.Failed -> "실패"
        SyncStatus.NotConfigured -> "미설정"
        SyncStatus.Unsupported -> "미지원"
    }
}
