package io.spokestack.actions

import android.net.Uri
import android.os.Bundle
import android.util.Log
import io.spokestack.actions.databinding.ActivitySearchBinding
import io.spokestack.spokestack.SpokestackAdapter
import io.spokestack.spokestack.SpokestackModule
import io.spokestack.spokestack.nlu.NLUResult
import io.spokestack.spokestack.tts.SynthesisRequest
import io.spokestack.spokestack.tts.TTSEvent

/**
 * An activity that simulates a user search by simply
 * displaying the search term.
 *
 */
class SearchActivity : VoiceActivity() {
    private lateinit var binding: ActivitySearchBinding

    override fun createListener(): SpokestackAdapter {
        return Listener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        setUiFromIntent(intent?.data)
    }

    private fun setUiFromIntent(data: Uri?) {
        data?.getQueryParameter("item")?.let {
            updateItem(it)
        }
    }

    private fun updateItem(item: String) {
        respond(item)

        runOnUiThread {
            binding.searchContent.text = item
        }
    }

    private fun respond(item: String) {
        // Note that this system prompt ends with a question.
        // This simulates a follow-up interaction; see the listener below
        // for how to prepare for follow-up user speech.
        val synthesisRequest =
            SynthesisRequest.Builder("I found some $item. Want anything else?")
                .build()
        spokestack.synthesize(synthesisRequest)
    }

    /**
     * Voice activation listener
     *
     * This NLU listener only responds to a single intent, logging all
     * others. A real application should provide more user feedback
     * than this.
     *
     * We also include a TTS listener to handle follow-up user interactions.
     */
    inner class Listener : SpokestackAdapter() {

        override fun nluResult(result: NLUResult) {
            Log.i(logTag, "intent: ${result.intent}")
            if (result.intent == "command.search") {
                val dataUri = Uri.Builder()
                    .appendQueryParameter("item", result.slots["item"]?.rawValue)
                    .build()
                setUiFromIntent(dataUri)
            } else {
                Log.i(logTag, "Unsupported intent: ${result.intent}")
            }
        }

        override fun ttsEvent(event: TTSEvent) {
            // activate/reactivate the pipeline after a TTS response is read
            when (event.type) {
                TTSEvent.Type.PLAYBACK_COMPLETE -> spokestack.activate()
                TTSEvent.Type.ERROR -> Log.w(logTag, "TTS ERROR: ${event.error.localizedMessage}")
                else -> {
                    Log.i(logTag, "TTS EVENT: ${event.type}")
                }
            }
        }

        override fun trace(module: SpokestackModule, message: String) {
            Log.i(logTag, "${module}: ${message}")
        }

        override fun error(module: SpokestackModule, err: Throwable) {
            Log.w(logTag, err)
        }
    }
}