package io.spokestack.actions

import android.net.Uri
import android.os.Bundle
import android.util.Log
import io.spokestack.actions.databinding.ActivitySearchBinding
import io.spokestack.spokestack.nlu.NLUResult
import io.spokestack.tray.SpokestackTrayListener
import io.spokestack.tray.VoicePrompt

/**
 * An activity that simulates a user search by simply
 * displaying the search term.
 *
 */
class SearchActivity : VoiceActivity() {
    private lateinit var binding: ActivitySearchBinding

    override fun getTrayListener(): SpokestackTrayListener {
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

    private fun setUiFromIntent(data: Uri?, fromTray: Boolean = false) {
        data?.getQueryParameter("item")?.let {
            runOnUiThread {
                binding.searchContent.text = it
            }

            // `fromTray` is our cue for whether the command originally came from
            // Spokestack Tray or Google Assistant. If it's the former, the Tray
            // will automatically display and play the response; if the latter,
            // we'll need to explicitly respond.
            if (!fromTray) {
                tray.say(response(it))
            }
        }
    }

    private fun response(item: String): VoicePrompt {
        // Note that this system prompt ends with a question, thus we pass
        // expectFollowup in our response so the Tray resumes listening
        // after reading the prompt
        return VoicePrompt("I found some $item. Want anything else?", expectFollowup = true)
    }

    /**
     * Voice activation listener
     *
     * This listener only responds to a single intent, returning an error
     * for all others. A real application should provide more user feedback
     * than this.
     */
    inner class Listener : SpokestackTrayListener {

        override fun onClassification(result: NLUResult): VoicePrompt {
            var item: String? = null
            if (result.intent == "command.search") {
                item = result.slots["item"]?.rawValue
                val dataUri = Uri.Builder()
                    .appendQueryParameter("item", item)
                    .build()
                setUiFromIntent(dataUri, true)
            }

            return if (item != null) {
                response(item)
            } else {
                fallbackPrompt()
            }
        }

        override fun onLog(message: String) {
            Log.i(logTag, message)
        }

        override fun onError(error: Throwable) {
            Log.w(logTag, error)
        }
    }
}