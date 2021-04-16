package io.spokestack.actions

import android.content.Context
import android.content.res.AssetManager
import androidx.lifecycle.Lifecycle
import io.spokestack.spokestack.Spokestack
import java.io.File
import java.io.FileOutputStream

/**
 * A singleton object that simplifies access to Spokestack,
 * which should only be created once and shared among activities
 * that use it.
 */
object Voice {
    private val modelFiles =
        listOf(
            "detect.tflite", "encode.tflite", "filter.tflite", "nlu.tflite", "metadata.json",
            "vocab.txt"
        )

    private var spokestack: Spokestack? = null

    /**
     * Retrieve the singleton instance of Spokestack.
     *
     * @param activity The activity requesting the Spokestack instance.
     *
     * @return The instance of Spokestack.
     */
    fun getSpokestack(activity: VoiceActivity): Spokestack {
        if (spokestack == null) {
            spokestack = initSpokestack(activity.applicationContext, activity.lifecycle)
        } else {
            // If the Spokestack instance has already been created, let it know
            // about the requesting activity. Setting the Android Context and
            // lifecycle are important for using the platform's on-device
            // ASR and for managing TTS playback.
            //
            // If you don't use either of these features, you might not need
            // to track the context and lifecycle
            spokestack!!.setAndroidContext(activity.applicationContext)
        }
        return spokestack!!
    }

    private fun initSpokestack(context: Context, lifecycle: Lifecycle): Spokestack {
        val cacheDir = context.cacheDir.absolutePath

        // We're distributing the models along with the app in the assets/
        // directory, so we can simply decompress them.
        //
        // You might want to download them on first launch instead, especially
        // for the NLU model, which you might wish to change over time.
        //
        // Either way, you'll need to have them in the local filesystem
        // and know their absolute path in order to create a Spokestack
        // instance.
        if (!modelsCached(cacheDir)) {
            decompressModels(context.assets, cacheDir)
        }

        return Spokestack.Builder()
            // get your free credentials at https://www.spokestack.io/create
            .setProperty("spokestack-id", "f0bc990c-e9db-4a0c-a2b1-6a6395a3d97e")
            .setProperty(
                "spokestack-secret",
                "5BD5483F573D691A15CFA493C1782F451D4BD666E39A9E7B2EBE287E6A72C6B6"
            )
            .setProperty("wake-detect-path", "$cacheDir/detect.tflite")
            .setProperty("wake-encode-path", "$cacheDir/encode.tflite")
            .setProperty("wake-filter-path", "$cacheDir/filter.tflite")
            .setProperty("nlu-metadata-path", "$cacheDir/metadata.json")
            .setProperty("nlu-model-path", "$cacheDir/nlu.tflite")
            .setProperty("wordpiece-vocab-path", "$cacheDir/vocab.txt")
            // .withLifecycle(lifecycle)
            .withAndroidContext(context)
            .build()
    }

    private fun modelsCached(cacheDir: String): Boolean {
        return modelFiles.all {
            val f = File("$cacheDir/$it")
            f.exists()
        }
    }

    private fun decompressModels(assets: AssetManager, cacheDir: String) {
        modelFiles.forEach { filename ->
            cacheAsset(assets, filename, cacheDir)
        }
    }

    private fun cacheAsset(assets: AssetManager, modelName: String, cacheDir: String) {
        val filterFile = File("$cacheDir/$modelName")
        val inputStream = assets.open(modelName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        val fos = FileOutputStream(filterFile)
        fos.write(buffer)
        fos.close()
    }
}