package com.babyvoice.bridge.integration.assistant

import com.babyvoice.bridge.core.model.BabyCareSnapshot

interface BabyTimeUiAdapter {
    val isSupported: Boolean
    val unsupportedReason: String?
    fun readVisibleSnapshot(rawWindowText: CharSequence?): Result<BabyCareSnapshot>
}

object UnsupportedBabyTimeUiAdapter : BabyTimeUiAdapter {
    override val isSupported: Boolean = false
    override val unsupportedReason: String = "접근성 스크래핑은 법적 승인 전까지 비활성화되어 있어요."

    override fun readVisibleSnapshot(rawWindowText: CharSequence?): Result<BabyCareSnapshot> =
        Result.failure(IllegalStateException("accessibility_scraping_disabled"))
}

