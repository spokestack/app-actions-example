package io.spokestack.actions

import io.spokestack.spokestack.util.EventTracer
import io.spokestack.tray.TrayActivity
import io.spokestack.tray.TrayConfig
import io.spokestack.tray.VoicePrompt

/**
 * A parent class for activities that wish to expose a voice interface.
 *
 * This class handles necessary scaffolding like ensuring access to a
 * centralized instance of Spokestack and requesting the microphone
 * permission from the user.
 */
abstract class VoiceActivity : TrayActivity() {

    val logTag: String = javaClass.simpleName

    override fun getTrayConfig(): TrayConfig {
        return TrayConfig.Builder()
            .credentials(
                "f0bc990c-e9db-4a0c-a2b1-6a6395a3d97e",
                "5BD5483F573D691A15CFA493C1782F451D4BD666E39A9E7B2EBE287E6A72C6B6"
            )
            .wakewordModelURL("https://d3dmqd7cy685il.cloudfront.net/model/wake/spokestack")
            .nluURL("https://d3dmqd7cy685il.cloudfront.net/nlu/production/f0bc990c-e9db-4a0c-a2b1-6a6395a3d97e/KfqzDHd_QBRMZZgR_VjX-T_LYTxQOVKHsVJzsUhWjZI")
            .logLevel(EventTracer.Level.PERF.value())
            .greeting("")
            .withListener(getTrayListener())
            .build()
    }

    /**
     * Provide a fallback response for the many rough edges we've left in our
     * conversation.
     */
    fun fallbackPrompt(): VoicePrompt {
        return VoicePrompt(
            "Sorry, I didn't understand. Please try again.",
            expectFollowup = true
        )
    }
}