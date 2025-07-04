
package com.example.birthdayvideo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.arthenica.ffmpegkit.FFmpegKit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private val imageUris = mutableListOf<Uri>()
    private var musicUri: Uri? = null

    private val pickImages = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris != null) {
            imageUris.clear()
            imageUris.addAll(uris)
        }
    }

    private val pickMusic = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        musicUri = uri
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), 0)

        findViewById<Button>(R.id.btnSelect).setOnClickListener {
            pickImages.launch(arrayOf("image/*"))
            pickMusic.launch(arrayOf("audio/*"))
        }

        findViewById<Button>(R.id.btnCreate).setOnClickListener {
            if (imageUris.isNotEmpty() && musicUri != null) {
                createVideo()
            }
        }
    }

    private fun createVideo() {
        // Simplified: copy images to cache dir sequentially as img001.jpg...
        val cacheDir = File(cacheDir, "inputs")
        cacheDir.mkdirs()
        imageUris.forEachIndexed { index, uri ->
            val inStream: InputStream? = contentResolver.openInputStream(uri)
            val outFile = File(cacheDir, String.format("img%03d.jpg", index + 1))
            val outStream: OutputStream = outFile.outputStream()
            inStream?.copyTo(outStream)
            inStream?.close(); outStream.close()
        }
        val musicFile = File(cacheDir, "music.mp3")
        contentResolver.openInputStream(musicUri!!)?.use { it.copyTo(musicFile.outputStream()) }

        val imagesPattern = "${cacheDir.absolutePath}/img%03d.jpg"
        val videoPath = File(cacheDir, "slideshow.mp4").absolutePath
        val finalPath = File(cacheDir, "birthday_with_music.mp4").absolutePath

        // 1. Make slideshow 1.5 fps (each pic 1.5s) using FFmpeg
        val fps = 1.0 / 1.5
        FFmpegKit.execute("-y -framerate $fps -i $imagesPattern -vf scale=720:1280,setsar=1 -c:v libx264 -r 30 -pix_fmt yuv420p $videoPath")

        // 2. Merge music
        FFmpegKit.execute("-y -i $videoPath -i ${musicFile.absolutePath} -shortest -c:v copy -c:a aac -b:a 128k $finalPath")
    }

    // Example OpenAI call (text prompt)
    private fun askLLM(prompt: String): String {
        val apiKey = System.getenv("OPENAI_API_KEY") ?: return ""
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", listOf(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            }))
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body).build()

        client.newCall(request).execute().use { resp ->
            return resp.body?.string() ?: ""
        }
    }
}
