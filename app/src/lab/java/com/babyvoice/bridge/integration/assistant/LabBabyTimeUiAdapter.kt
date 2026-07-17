package com.babyvoice.bridge.integration.assistant

import com.babyvoice.bridge.core.model.BabyCareSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabBabyTimeUiAdapter @Inject constructor(
    private val parser: BabyTimeWidgetSnapshotParser,
) : BabyTimeUiAdapter {
    override val isSupported: Boolean = true

    override val unsupportedReason: String? = null

    override fun readVisibleSnapshot(rawWindowText: CharSequence?): Result<BabyCareSnapshot> =
        parser.parse(rawWindowText)
}
