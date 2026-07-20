package com.kusa.cockpitscope

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat

/**
 * Android Auto's MEDIA category requires a MediaBrowserService implementation.
 * This provides a minimal implementation to satisfy the launcher requirements.
 */
class CockpitMediaService : MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null

    override fun onCreate() {
        super.onCreate()

        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(this, "CockpitMediaService").apply {
            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
            isActive = true
        }
        sessionToken = mediaSession?.sessionToken
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        // Return a root ID. We don't actually need to provide media items for this app's purpose.
        return BrowserRoot("root", null)
    }

   // override fun onLoadChildren(
   //     parentId: String,
   //     result: Result<MutableList<MediaBrowserCompat.MediaItem>>
   // ) {
   //     // Return an empty list of media items.
   //     result.sendResult(mutableListOf())
   // }
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.detach()
        result.sendResult(mutableListOf())
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
    }
}
