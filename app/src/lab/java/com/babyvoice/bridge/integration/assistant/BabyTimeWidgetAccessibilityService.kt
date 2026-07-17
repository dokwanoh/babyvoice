package com.babyvoice.bridge.integration.assistant

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BabyTimeWidgetAccessibilityService : AccessibilityService() {
    @Inject
    lateinit var adapter: BabyTimeUiAdapter

    @Inject
    lateinit var repository: BabyTimeWidgetSnapshotRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val source = event?.source ?: rootInActiveWindow ?: return
        val serialized = source.serializeTree()
        source.recycle()
        val snapshot = adapter.readVisibleSnapshot(serialized).getOrNull() ?: return
        scope.launch {
            repository.record(snapshot)
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun AccessibilityNodeInfo.serializeTree(): String {
        val builder = StringBuilder()
        serializeInto(builder, depth = 0)
        return builder.toString()
    }

    private fun AccessibilityNodeInfo.serializeInto(
        builder: StringBuilder,
        depth: Int,
    ) {
        val indent = "  ".repeat(depth)
        val id = viewIdResourceName
        val text = text?.toString()?.trim().orEmpty()
        val content = contentDescription?.toString()?.trim().orEmpty()
        val chosenText = when {
            text.isNotBlank() -> text
            content.isNotBlank() -> content
            else -> ""
        }
        if (id != null && chosenText.isNotBlank()) {
            builder.append(indent)
                .append("id=")
                .append(id)
                .append(" text=")
                .append(chosenText)
                .append('\n')
        }
        for (index in 0 until childCount) {
            getChild(index)?.useNode { child ->
                child.serializeInto(builder, depth + 1)
            }
        }
    }

    private inline fun AccessibilityNodeInfo.useNode(block: (AccessibilityNodeInfo) -> Unit) {
        try {
            block(this)
        } finally {
            recycle()
        }
    }
}
