package com.pixelmusic.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import android.widget.RemoteViews

class MusicWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_PLAY_PAUSE = "com.pixelmusic.widget.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT      = "com.pixelmusic.widget.ACTION_NEXT"
        const val ACTION_PREV      = "com.pixelmusic.widget.ACTION_PREV"
        const val PREFS_NAME       = "music_state"

        fun updateWidget(
            context: Context,
            title: String = "No Media",
            artist: String = "---",
            isPlaying: Boolean = false
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, MusicWidget::class.java))
            if (ids.isEmpty()) return
            val views = buildViews(context, title, artist, isPlaying)
            manager.updateAppWidget(ids, views)
        }

        fun buildViews(
            context: Context,
            title: String,
            artist: String,
            isPlaying: Boolean
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_music)
            views.setImageViewResource(
                R.id.btn_play_pause,
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
            views.setOnClickPendingIntent(R.id.btn_prev,      pendingIntent(context, ACTION_PREV))
            views.setOnClickPendingIntent(R.id.btn_play_pause, pendingIntent(context, ACTION_PLAY_PAUSE))
            views.setOnClickPendingIntent(R.id.btn_next,      pendingIntent(context, ACTION_NEXT))
            return views
        }

        private fun pendingIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, MusicWidget::class.java).apply { this.action = action }
            return PendingIntent.getBroadcast(
                context,
                action.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val views = buildViews(
                context,
                prefs.getString("title", "No Media") ?: "No Media",
                prefs.getString("artist", "---") ?: "---",
                prefs.getBoolean("playing", false)
            )
            for (id in appWidgetIds) appWidgetManager.updateAppWidget(id, views)
        } catch (e: Exception) {
            android.util.Log.e("PixelMusic", "onUpdate failed", e)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (intent.action) {
            ACTION_PLAY_PAUSE -> sendKey(audio, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            ACTION_NEXT       -> sendKey(audio, KeyEvent.KEYCODE_MEDIA_NEXT)
            ACTION_PREV       -> sendKey(audio, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        }
    }

    private fun sendKey(audio: AudioManager, keyCode: Int) {
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }
}
