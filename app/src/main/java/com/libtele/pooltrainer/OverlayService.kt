package com.libtele.pooltrainer

import android.app.Service
import android.content.*
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var view: AimOverlayView? = null
    private var panel: LinearLayout? = null
    private var status: TextView? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ProjectionService.ACTION_RESULT -> {
                    val r = AnalysisResult(
                        ScreenPoint(intent.getFloatExtra("cx",0f), intent.getFloatExtra("cy",0f)),
                        ScreenPoint(intent.getFloatExtra("tx",0f), intent.getFloatExtra("ty",0f)),
                        ScreenPoint(intent.getFloatExtra("gx",0f), intent.getFloatExtra("gy",0f)),
                        ScreenPoint(intent.getFloatExtra("px",0f), intent.getFloatExtra("py",0f)),
                        intent.getFloatExtra("rad",16f),
                        intent.getFloatExtra("conf",0f)
                    )
                    view?.setResult(r)
                    status?.text = "OK ${(r.confidence*100).toInt()}%"
                }
                ProjectionService.ACTION_NO_RESULT -> {
                    view?.setResult(null)
                    status?.text = "No detection"
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        view = AimOverlayView(this)
        val drawParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        wm.addView(view, drawParams)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(8,6,8,6)
            setBackgroundColor(0xDD111827.toInt())
        }
        val st = TextView(this).apply {
            text = "TRAIN"; setTextColor(0xFF00E6A8.toInt()); setPadding(4,0,8,0)
        }
        val analyze = Button(this).apply {
            text = "ANALYZE"
            setOnClickListener {
                st.text = "Scanning..."
                ContextCompat.startForegroundService(this@OverlayService,
                    Intent(this@OverlayService, ProjectionService::class.java).setAction(ProjectionService.ACTION_ANALYZE))
            }
        }
        val clear = Button(this).apply {
            text = "CLEAR"
            setOnClickListener { view?.setResult(null); st.text = "TRAIN" }
        }
        val close = Button(this).apply {
            text = "X"
            setOnClickListener { stopSelf() }
        }
        root.addView(st); root.addView(analyze); root.addView(clear); root.addView(close)
        status = st

        val panelParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
        )
        panelParams.gravity = Gravity.TOP or Gravity.END
        panelParams.y = 80
        wm.addView(root, panelParams)
        panel = root

        val f = IntentFilter().apply {
            addAction(ProjectionService.ACTION_RESULT)
            addAction(ProjectionService.ACTION_NO_RESULT)
        }
        ContextCompat.registerReceiver(this, receiver, f, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroy() {
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
        view?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        panel?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
