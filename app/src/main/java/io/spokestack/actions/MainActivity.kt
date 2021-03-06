package io.spokestack.actions

import android.os.Bundle
import android.util.Log
import android.view.View
import io.spokestack.spokestack.SpokestackAdapter
import io.spokestack.spokestack.nlu.NLUResult
import io.spokestack.spokestack.tts.TTSEvent

/**
 * The main screen just contains buttons for switching to other
 * activities.
 *
 * These buttons can also be activated by voice, but there's no
 * microphone button, so you'll have to say the wakeword
 * ("Spokestack") first.
 */
class MainActivity : VoiceActivity() {

    override fun createListener(): SpokestackAdapter {
        return Listener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun settingsTapped(view: View) {
        transitionToActivity(this, SettingsActivity::class.java)
    }

    fun searchTapped(view: View) {
        transitionToActivity(this, SearchActivity::class.java)
    }

    fun controlTapped(view: View) {
        transitionToActivity(this, DeviceControlActivity::class.java)
    }

    /**
     * Voice activation listener
     *
     * This listener only implements the NLU event and switches to a
     * different activitiy if a supported intent has been recognized.
     *
     * As such, it's light on user feedback. See the other methods in
     * `SpokestackAdapter` for ways to let the user know ASR is active,
     * display the transcript of what the user said, and more.
     */
    inner class Listener : SpokestackAdapter() {

        override fun nluResult(result: NLUResult) {
            val transitionClass =
                when (result.intent) {
                    "command.control_device" -> DeviceControlActivity::class.java
                    "navigate.settings" -> SettingsActivity::class.java
                    "command.search" -> SearchActivity::class.java
                    else -> null
                }

            if (transitionClass != null) {
                // unconditionally translate slots in the utterance into query
                // parameters for the Intent URI
                val stringSlots = result.slots.entries.associate { entry ->
                    entry.key to entry.value.rawValue
                }
                transitionToActivity(this@MainActivity, transitionClass, stringSlots)
            }
        }
    }
}