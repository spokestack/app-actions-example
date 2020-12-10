package io.spokestack.actions

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.spokestack.tray.SpokestackTrayListener

class SettingsActivity : VoiceActivity() {

    override fun getTrayListener(): SpokestackTrayListener {
        return Listener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    inner class Listener : SpokestackTrayListener {
        // left as an exercise to the reader
    }
}