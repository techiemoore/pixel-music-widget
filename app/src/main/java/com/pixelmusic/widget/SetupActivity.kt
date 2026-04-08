package com.pixelmusic.widget

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class SetupActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val granted = isNotificationAccessGranted()

        val tv = TextView(this).apply {
            textSize = 13f
            setTextColor(0xFFA5A5A5.toInt())
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(48, 48, 48, 48)
            text = if (granted) {
                "PIXEL MUSIC\n\n[  OK  ]\n\nNotification access granted.\nAdd the widget to your home screen."
            } else {
                "PIXEL MUSIC\n\n[ SETUP ]\n\nGrant notification access so\nthe widget can show track info.\n\nOpening settings..."
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(0xFF111111.toInt())
            addView(tv)
        }
        setContentView(root)

        Handler(Looper.getMainLooper()).postDelayed({
            if (!granted) startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            finish()
        }, if (granted) 2000L else 1500L)
    }

    private fun isNotificationAccessGranted(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            ?: return false
        val myComponent = ComponentName(this, MusicNotificationListener::class.java).flattenToString()
        return flat.split(":").any { it == myComponent }
    }
}
