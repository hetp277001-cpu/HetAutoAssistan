package com.hetauto.assistant.overlay

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.hetauto.assistant.HetApplication
import com.hetauto.assistant.MainActivity
import com.hetauto.assistant.R
import com.hetauto.assistant.loop.AutomationState
import com.hetauto.assistant.workflow.DefaultFourBoxWorkflow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FloatingPanelService : Service() {

    companion object {
        const val ACTION_START_OVERLAY = "com.hetauto.assistant.action.START_OVERLAY"
        const val ACTION_STOP_OVERLAY = "com.hetauto.assistant.action.STOP_OVERLAY"
        private const val NOTIFICATION_CHANNEL_ID = "het_floating_panel_channel"
        private const val NOTIFICATION_ID = 2001

        fun start(context: Context) {
            val intent = Intent(context, FloatingPanelService::class.java).apply {
                action = ACTION_START_OVERLAY
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingPanelService::class.java).apply {
                action = ACTION_STOP_OVERLAY
            }
            context.startService(intent)
        }
    }

    private lateinit var windowManager: WindowManager
    private var overlayRootView: View? = null
    private lateinit var windowParams: WindowManager.LayoutParams

    private val scope = CoroutineScope(Dispatchers.Main)
    private var collectorJob: Job? = null

    private var isMinimized = false

    private lateinit var expandedLayout: LinearLayout
    private lateinit var minimizedLayout: LinearLayout
    private lateinit var tvStatus: TextView
    private lateinit var tvStage: TextView
    private lateinit var tvCount: TextView
    private lateinit var tvMinCount: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnResume: Button
    private lateinit var btnStop: Button
    private lateinit var statusIndicatorDot: View

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        initOverlayView()
        observeAutomationState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_OVERLAY) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("HET AUTO ASSISTANT Active")
            .setContentText("Automation Floating Control Panel is running")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOverlayView() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }

        val context = this
        val root = FrameLayout(context)

        minimizedLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 16, 24, 16)
            background = createCardBackground(Color.parseColor("#E61E293B"), 40f)
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE

            statusIndicatorDot = View(context).apply {
                val dotSize = 20
                layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                    marginEnd = 16
                }
                background = createDotDrawable(Color.GREEN)
            }
            addView(statusIndicatorDot)

            tvMinCount = TextView(context).apply {
                text = "HET: 0"
                setTextColor(Color.WHITE)
                textSize = 13f
            }
            addView(tvMinCount)

            setOnClickListener {
                toggleMinimized(false)
            }
        }

        expandedLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
            background = createCardBackground(Color.parseColor("#F20F172A"), 28f)

            val header = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                val title = TextView(context).apply {
                    text = "HET ASSISTANT"
                    setTextColor(Color.parseColor("#38BDF8"))
                    textSize = 14f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                addView(title)

                val btnMin = TextView(context).apply {
                    text = " _ "
                    setTextColor(Color.LTGRAY)
                    textSize = 16f
                    setPadding(12, 0, 12, 0)
                    setOnClickListener { toggleMinimized(true) }
                }
                addView(btnMin)

                val btnClose = TextView(context).apply {
                    text = " ✕ "
                    setTextColor(Color.parseColor("#EF4444"))
                    textSize = 14f
                    setPadding(12, 0, 8, 0)
                    setOnClickListener { stopSelf() }
                }
                addView(btnClose)
            }
            addView(header)

            tvStatus = TextView(context).apply {
                text = "Status: IDLE"
                setTextColor(Color.WHITE)
                textSize = 13f
                setPadding(0, 10, 0, 4)
            }
            addView(tvStatus)

            tvStage = TextView(context).apply {
                text = "Stage: None"
                setTextColor(Color.parseColor("#94A3B8"))
                textSize = 12f
                setPadding(0, 0, 0, 4)
            }
            addView(tvStage)

            tvCount = TextView(context).apply {
                text = "Completed: 0"
                setTextColor(Color.parseColor("#22C55E"))
                textSize = 12f
                setPadding(0, 0, 0, 14)
            }
            addView(tvCount)

            val btnRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER

                val loopController = (application as HetApplication).loopController

                btnStart = createPanelButton("START", Color.parseColor("#2563EB")) {
                    loopController.start(DefaultFourBoxWorkflow.create(), 5, false)
                }
                addView(btnStart)

                btnPause = createPanelButton("PAUSE", Color.parseColor("#D97706")) {
                    loopController.pause()
                }
                addView(btnPause)

                btnResume = createPanelButton("RESUME", Color.parseColor("#059669")) {
                    loopController.resume()
                }
                addView(btnResume)

                btnStop = createPanelButton("STOP", Color.parseColor("#DC2626")) {
                    loopController.stop()
                }
                addView(btnStop)
            }
            addView(btnRow)
        }

        root.addView(expandedLayout)
        root.addView(minimizedLayout)

        setupDraggable(root)

        overlayRootView = root
        windowManager.addView(root, windowParams)
    }

    private fun toggleMinimized(minimize: Boolean) {
        isMinimized = minimize
        if (minimize) {
            expandedLayout.visibility = View.GONE
            minimizedLayout.visibility = View.VISIBLE
        } else {
            expandedLayout.visibility = View.VISIBLE
            minimizedLayout.visibility = View.GONE
        }
    }

    private fun createPanelButton(text: String, bgColor: Int, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 11f
            background = createCardBackground(bgColor, 12f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                90
            ).apply {
                setMargins(6, 0, 6, 0)
            }
            setOnClickListener { onClick() }
        }
    }

    private fun createCardBackground(bgColor: Int, cornerRadius: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(bgColor)
            this.cornerRadius = cornerRadius
            setStroke(2, Color.parseColor("#334155"))
        }
    }

    private fun createDotDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDraggable(view: View) {
        view.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = windowParams.x
                        initialY = windowParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            windowParams.x = initialX + dx
                            windowParams.y = initialY + dy
                            try {
                                windowManager.updateViewLayout(overlayRootView, windowParams)
                            } catch (_: Exception) {}
                            return true
                        }
                    }
                }
                return false
            }
        })
    }

    private fun observeAutomationState() {
        val loopController = (application as HetApplication).loopController

        collectorJob = scope.launch {
            launch {
                loopController.automationState.collect { state ->
                    tvStatus.text = "Status: ${state.name}"
                    when (state) {
                        AutomationState.RUNNING -> {
                            btnStart.isEnabled = false
                            btnPause.isEnabled = true
                            btnResume.isEnabled = false
                            btnStop.isEnabled = true
                            statusIndicatorDot.background = createDotDrawable(Color.GREEN)
                        }
                        AutomationState.PAUSED -> {
                            btnStart.isEnabled = false
                            btnPause.isEnabled = false
                            btnResume.isEnabled = true
                            btnStop.isEnabled = true
                            statusIndicatorDot.background = createDotDrawable(Color.YELLOW)
                        }
                        AutomationState.STOPPED, AutomationState.COMPLETED, AutomationState.IDLE -> {
                            btnStart.isEnabled = true
                            btnPause.isEnabled = false
                            btnResume.isEnabled = false
                            btnStop.isEnabled = false
                            statusIndicatorDot.background = createDotDrawable(Color.GRAY)
                        }
                        AutomationState.ERROR -> {
                            btnStart.isEnabled = true
                            btnPause.isEnabled = false
                            btnResume.isEnabled = false
                            btnStop.isEnabled = false
                            statusIndicatorDot.background = createDotDrawable(Color.RED)
                        }
                    }
                }
            }

            launch {
                loopController.currentStage.collect { stage ->
                    tvStage.text = "Stage: ${stage?.displayName ?: "None"}"
                }
            }

            launch {
                loopController.completedActions.collect { count ->
                    tvCount.text = "Completed: $count actions"
                    tvMinCount.text = "Actions: $count"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        collectorJob?.cancel()
        overlayRootView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
        overlayRootView = null
    }
}
