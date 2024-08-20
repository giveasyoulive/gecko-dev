/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.push

import org.mozilla.fenix.R
import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import mozilla.components.concept.push.PushService
import mozilla.components.feature.push.AutoPushFeature
import mozilla.components.lib.push.firebase.AbstractFirebasePushService
import mozilla.components.support.base.ids.SharedIdsHelper
import org.mozilla.fenix.IntentReceiverActivity
import org.mozilla.fenix.utils.IntentUtils
import java.net.URL
import java.io.IOException
/**
 * A singleton instance of the FirebasePushService needed for communicating between FCM and the
 * [AutoPushFeature].
 */
@SuppressLint("MissingFirebaseInstanceTokenRefresh") // Implemented internally.
class FirebasePushService : AbstractFirebasePushService() {

    @SuppressWarnings("TooGenericExceptionCaught")
    override fun onMessageReceived(message: RemoteMessage) {

        val chId = message.data.getOrElse(PushService.MESSAGE_KEY_CHANNEL_ID) { null }

        val channelId = ensureChannelExists(chId)

        if (channelId != null) {

            val buildNotification = buildNotification(message, channelId)

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(applicationContext)
                    .notify(
                        NOTIFICATION_TAG,
                        NOTIFICATION_ID,
                        buildNotification
                    )
            }
        } else {
            super.onMessageReceived(message)
        }

    }

    private fun buildNotification(message: RemoteMessage, channelId:String?): Notification {

        val intent = Intent(applicationContext, IntentReceiverActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        intent.setPackage("org.giveasyoulive.donationreminder")
        intent.putExtra("url", message.data.getOrElse("url") { null })

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            SharedIdsHelper.getNextIdForTag(applicationContext,
                NOTIFICATION_PENDING_INTENT_TAG
            ),
            intent,
            IntentUtils.defaultIntentPendingFlags or PendingIntent.FLAG_UPDATE_CURRENT
        )

        with(applicationContext) {

            var title = message.data.getOrElse("title") { "" }

            var builder = NotificationCompat.Builder(this, channelId.toString())
                .setSmallIcon(R.drawable.ic_status_logo)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(message.data.getOrElse("body") { "" }))
                .setColor(ContextCompat.getColor(this, R.color.ic_notification_color))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setShowWhen(false)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val image = message.data.getOrElse("image") { null }

            if(image !=null) {
                applyImageUrl(builder, image.toString())
            }

            return builder.build()

        }
    }


    private fun applyImageUrl(
        builder: NotificationCompat.Builder,
        imageUrl: String

    ): NotificationCompat.Builder? = runBlocking {
        val url = URL(imageUrl)

        withContext(Dispatchers.IO) {
            try {
                val input = url.openStream()
                BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                null
            }
        }?.let { bitmap ->

            builder.setLargeIcon(bitmap)
        }
    }

    /**
     * Make sure a notification channel for default browser notification exists.
     *
     * Returns the channel id to be used for notifications.
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun ensureChannelExists(channel: String?): String? {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val marketingChannel = NotificationChannel(
                MARKETING_NOTIFICATION_CHANNEL_ID,
                applicationContext.getString(R.string.notification_marketing_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannel(marketingChannel)

            val salesChannel = NotificationChannel(
                SALES_NOTIFICATION_CHANNEL_ID,
                applicationContext.getString(R.string.notification_sales_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannel(salesChannel)

        }
        return when (channel) {
            "GAYL Sales" -> SALES_NOTIFICATION_CHANNEL_ID
            "GAYL Marketing" -> MARKETING_NOTIFICATION_CHANNEL_ID
            else -> null
        }
    }

    companion object {
        private const val MARKETING_NOTIFICATION_CHANNEL_ID = "org.giveasyoulive.marketing.channel"
        private const val SALES_NOTIFICATION_CHANNEL_ID = "org.giveasyoulive.sales.channel"
        private const val NOTIFICATION_PENDING_INTENT_TAG = "org.giveasyoulive.donationreminder"
        private const val NOTIFICATION_ID = 101
        private const val NOTIFICATION_TAG = "org.giveasyoulive.tag"
    }

}
