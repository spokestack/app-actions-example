package io.spokestack.actions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.spokestack.spokestack.Spokestack
import io.spokestack.spokestack.SpokestackAdapter

/**
 * A parent class for activities that wish to expose a voice interface.
 *
 * This class handles necessary scaffolding like ensuring access to a
 * centralized instance of Spokestack and requesting the microphone
 * permission from the user.
 */
abstract class VoiceActivity : AppCompatActivity() {
    private val audioPermission = 42

    private var hasRecordPermission = false

    val logTag: String = javaClass.simpleName

    lateinit var listener: SpokestackAdapter
    lateinit var spokestack: Spokestack

    /**
     * Create a listener to receive events from the Spokestack system.
     *
     * This must be implemented by each subclass because the UIs of
     * different activities will necessarily react differently to voice
     * commands.
     */
    abstract fun createListener(): SpokestackAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listener = createListener()
        spokestack = Voice.getSpokestack(this)

        // we have to ask for permission to record audio at runtime -- this will ask every time
        // one of our activities is created, and if the user denies permission, it'll ask again
        // each time. This should be made more user-friendly for a production app.
        hasRecordPermission = checkRecordPermission()
    }

    private fun checkRecordPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            audioPermission
        )
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            audioPermission -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    hasRecordPermission = true

                    // As soon as we know we have permission to listen, we'll
                    // start Spokestack. With the default configuration we've
                    // used, this doesn't mean ASR is active right away.
                    //
                    // ASR is activated after the wakeword (in this case,
                    // "Spokestack") is recognized.
                    //
                    // You might also wish to include a button that calls
                    // `spokestack.activate()` when pressed to start ASR
                    // manually. It's necessary to call `start()`
                    // before `activate()`.
                    spokestack.start()
                } else {
                    Log.w(logTag, "Record permission not granted; "
                                  + "voice control disabled!")
                }
                return
            }
            else -> {
                // do nothing
            }
        }
    }

    override fun onResume() {
        super.onResume()
        spokestack.addListener(listener)

        // note that we only do the actual permission check on activity
        // creation
        if (hasRecordPermission) {
            spokestack.start()
        }
    }

    override fun onPause() {
        super.onPause()
        // get rid of our listener and stop the microphone in case we're
        // about to leave this activity for good
        spokestack.removeListener(listener)

        if (hasRecordPermission) {
            spokestack.stop()
        }
    }
}