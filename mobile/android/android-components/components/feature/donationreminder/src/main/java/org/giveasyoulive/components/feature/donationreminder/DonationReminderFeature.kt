/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.giveasyoulive.components.feature.donationreminder

import android.content.Context
import mozilla.components.concept.engine.webextension.MessageHandler
import mozilla.components.concept.engine.webextension.Port
import mozilla.components.concept.engine.webextension.WebExtensionRuntime
import mozilla.components.support.base.log.logger.Logger
import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONObject
import java.util.Base64

/**
 * Feature to enable website-hotfixing via the Web Compatibility System-Addon.
 */
object DonationReminderFeature {
    private val logger = Logger("donation-reminder")

    internal const val DONATION_REMINDER_EXTENSION_ID = "gayl_firefox@everyclick.com"
    internal const val DONATION_REMINDER_EXTENSION_URL = "resource://android/assets/extensions/donation-reminder/"

    /**
     * Installs the web extension in the runtime through the WebExtensionRuntime install method
     */
    fun install(runtime: WebExtensionRuntime, context: Context) {

        runtime.installBuiltInWebExtension(
            DONATION_REMINDER_EXTENSION_ID,
            DONATION_REMINDER_EXTENSION_URL,
            onSuccess = { extension ->

                // Once, the extension is loaded.  Transfer the notification token to the website
                val messageHandler: MessageHandler = object :
                    MessageHandler {

                    // A native port has been connected by the extension
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onPortConnected(port: Port) {

                        logger.debug("onPortConnected")

                        // Post the latest notification token to the extension - so, it can be saved as a cookie
                        val token = context.getSharedPreferences("mozac_feature_push", Context.MODE_PRIVATE ).getString("token", null)

                        if(token != null) {
                            val message = JSONObject()

                            logger.debug("Firebase Token: $token")

                            message.put("token", Base64.getEncoder().encodeToString(token.toByteArray()))

                            port.postMessage(message)
                        }

                    }

                }

                // Listen for DonationReminderNotification native messages
                extension.registerBackgroundMessageHandler("DonationReminderNotification", messageHandler)

                logger.debug("Installed donation reminder webextension: ${extension.id}")
            },
            onError = { throwable ->
                logger.error("Failed to donation reminder webextension", throwable)
            },
        )
    }
}
