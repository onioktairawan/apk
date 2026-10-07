package com.libtele.pooltrainer

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQ_CAPTURE = 7001
        const val REQ_OVERLAY = 7002
        const val GAME_PACKAGE = "com.miniclip.eightballpool"
    }

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.statusText)

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 9001)
        }

        findViewById<Button>(R.id.overlayPermissionButton).setOnClickListener { requestOverlay() }
        findViewById<Button>(R.id.startCaptureButton).setOnClickListener { requestCapture() }
        findViewById<Button>(R.id.startOverlayButton).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) requestOverlay()
            else { startService(Intent(this, OverlayService::class.java)); status.text = "Status: overlay aktif" }
        }
        findViewById<Button>(R.id.openGameButton).setOnClickListener {
            val i = packageManager.getLaunchIntentForPackage(GAME_PACKAGE)
            if (i == null) Toast.makeText(this, "8 Ball Pool tidak ditemukan.", Toast.LENGTH_LONG).show()
            else startActivity(i)
        }
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            stopService(Intent(this, OverlayService::class.java))
            startService(Intent(this, ProjectionService::class.java).setAction(ProjectionService.ACTION_STOP))
            status.text = "Status: analyzer dihentikan"
        }
        refresh()
    }

    private fun requestOverlay() {
        if (Settings.canDrawOverlays(this)) { refresh(); return }
        startActivityForResult(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
            REQ_OVERLAY
        )
    }

    private fun requestCapture() {
        val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mgr.createScreenCaptureIntent(), REQ_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CAPTURE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                ContextCompat.startForegroundService(this, ProjectionService.startIntent(this, resultCode, data))
                status.text = "Status: screen capture aktif"
            } else status.text = "Status: screen capture dibatalkan"
        } else if (requestCode == REQ_OVERLAY) refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        status.text = if (Settings.canDrawOverlays(this))
            "Status: izin overlay OK. Aktifkan screen capture."
        else "Status: izin overlay belum diberikan."
    }
}
