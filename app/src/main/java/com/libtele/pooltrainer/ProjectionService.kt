package com.libtele.pooltrainer

import android.app.*
import android.content.*
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import java.util.concurrent.atomic.AtomicBoolean

class ProjectionService : Service() {
    companion object {
        const val ACTION_START = "pta.START"
        const val ACTION_ANALYZE = "pta.ANALYZE"
        const val ACTION_STOP = "pta.STOP"
        const val ACTION_RESULT = "pta.RESULT"
        const val ACTION_NO_RESULT = "pta.NO_RESULT"
        private const val EXTRA_CODE = "code"
        private const val EXTRA_DATA = "data"
        fun startIntent(c: Context, code: Int, data: Intent) =
            Intent(c, ProjectionService::class.java).setAction(ACTION_START)
                .putExtra(EXTRA_CODE, code).putExtra(EXTRA_DATA, data)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val requested = AtomicBoolean(false)
    private var projection: MediaProjection? = null
    private var vd: VirtualDisplay? = null
    private var reader: ImageReader? = null

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel("capture", "Screen Capture", NotificationManager.IMPORTANCE_LOW))
        startForeground(42, NotificationCompat.Builder(this, "capture")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("Pool Training Analyzer")
            .setContentText("Screen capture aktif")
            .setOngoing(true).build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startProjection(intent)
            ACTION_ANALYZE -> if (projection != null) requested.set(true) else sendNoResult()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun startProjection(intent: Intent) {
        if (projection != null) return
        val code = intent.getIntExtra(EXTRA_CODE, 0)
        val data = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(EXTRA_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getParcelableExtra(EXTRA_DATA)
        } ?: return

        val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = mgr.getMediaProjection(code, data)

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val (w,h) = if (Build.VERSION.SDK_INT >= 30) {
            val b = wm.currentWindowMetrics.bounds; b.width() to b.height()
        } else {
            @Suppress("DEPRECATION")
            val dm = android.util.DisplayMetrics().also { wm.defaultDisplay.getRealMetrics(it) }
            dm.widthPixels to dm.heightPixels
        }

        reader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2)
        reader?.setOnImageAvailableListener({ r ->
            val image = r.acquireLatestImage() ?: return@setOnImageAvailableListener
            if (!requested.compareAndSet(true, false)) { image.close(); return@setOnImageAvailableListener }
            val bmp = imageToBitmap(image); image.close()
            Thread {
                val result = try { TrainingAnalyzer.analyze(bmp) } catch (_: Throwable) { null }
                bmp.recycle()
                if (result == null) sendNoResult() else sendResult(result)
            }.start()
        }, handler)

        vd = projection?.createVirtualDisplay(
            "PoolTrainingCapture", w, h, resources.configuration.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader?.surface, null, handler
        )
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val plane = image.planes[0]
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * image.width
        val paddedWidth = image.width + rowPadding / pixelStride
        val padded = Bitmap.createBitmap(paddedWidth, image.height, Bitmap.Config.ARGB_8888)
        padded.copyPixelsFromBuffer(plane.buffer)
        val out = Bitmap.createBitmap(padded, 0, 0, image.width, image.height)
        if (out !== padded) padded.recycle()
        return out
    }

    private fun sendResult(r: AnalysisResult) {
        sendBroadcast(Intent(ACTION_RESULT).setPackage(packageName)
            .putExtra("cx", r.cue.x).putExtra("cy", r.cue.y)
            .putExtra("tx", r.target.x).putExtra("ty", r.target.y)
            .putExtra("gx", r.ghost.x).putExtra("gy", r.ghost.y)
            .putExtra("px", r.pocket.x).putExtra("py", r.pocket.y)
            .putExtra("rad", r.ballRadius).putExtra("conf", r.confidence))
    }

    private fun sendNoResult() = sendBroadcast(Intent(ACTION_NO_RESULT).setPackage(packageName))

    override fun onDestroy() {
        vd?.release(); reader?.close(); projection?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
