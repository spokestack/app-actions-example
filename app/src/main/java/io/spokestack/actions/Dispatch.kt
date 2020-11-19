package io.spokestack.actions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat

const val SCHEME = "example"

val pathMap = mapOf(
    DeviceControlActivity::class.java.name to "control",
    SearchActivity::class.java.name to "search",
    SettingsActivity::class.java.name to "settings"
)

/**
 * Transition to another activity by creating an Intent with a deep link.
 *
 * Relies on link handling and intent filters in the manifest.
 *
 * @param context The source context for the transition
 * @param activity The destination activity
 * @param data Any data that should be visible to `activity` -- i.e., slots coming from a user
 * intent
 */
fun <T : Activity> transitionToActivity(
    context: Context,
    activity: Class<T>,
    data: Map<String, String>? = null
) {
    val path: String = pathMap[activity.name] ?: ""
    val dataUri = createUri(path, data)
    val intent = Intent(context, activity).apply { this.data = dataUri }
    ContextCompat.startActivity(context, intent, null)
}

private fun createUri(path: String, data: Map<String, String>? = null): Uri? {
    if (data == null) {
        return null
    }

    val builder = Uri.Builder()
        .scheme(SCHEME)
        .appendPath(path)

    data.entries.forEach { entry ->
        builder.appendQueryParameter(entry.key, entry.value)
    }

    return builder.build()
}
