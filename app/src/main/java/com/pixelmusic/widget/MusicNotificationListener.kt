package com.pixelmusic.widget

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService

class MusicNotificationListener : NotificationListenerService() {

    private var sessionManager: MediaSessionManager? = null
    private var activeController: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            pushUpdate(metadata, activeController?.playbackState)
        }
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            pushUpdate(activeController?.metadata, state)
        }
    }

    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        refreshActive(controllers)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        sessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        try {
            val self = ComponentName(this, MusicNotificationListener::class.java)
            sessionManager?.addOnActiveSessionsChangedListener(sessionListener, self)
            refreshActive(sessionManager?.getActiveSessions(self))
        } catch (_: SecurityException) { /* permission not yet granted */ }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        activeController?.unregisterCallback(controllerCallback)
        sessionManager?.removeOnActiveSessionsChangedListener(sessionListener)
    }

    private fun refreshActive(controllers: List<MediaController>?) {
        activeController?.unregisterCallback(controllerCallback)
        // Prefer a controller that is actively playing; fall back to the first available
        activeController = controllers?.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: controllers?.firstOrNull()
        activeController?.registerCallback(controllerCallback)
        pushUpdate(activeController?.metadata, activeController?.playbackState)
    }

    private fun pushUpdate(metadata: MediaMetadata?, state: PlaybackState?) {
        val title    = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "No Media"
        val artist   = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: "---"
        val playing  = state?.state == PlaybackState.STATE_PLAYING

        getSharedPreferences(MusicWidget.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("title", title)
            .putString("artist", artist)
            .putBoolean("playing", playing)
            .apply()

        MusicWidget.updateWidget(this, title, artist, playing)
    }
}
