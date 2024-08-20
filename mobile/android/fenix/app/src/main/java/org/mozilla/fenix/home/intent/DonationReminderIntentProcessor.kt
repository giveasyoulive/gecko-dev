/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.fenix.home.intent

import android.content.Intent
import androidx.navigation.NavController
import mozilla.components.support.base.log.logger.Logger
import org.mozilla.fenix.BrowserDirection
import org.mozilla.fenix.HomeActivity

class DonationReminderIntentProcessor(
    private val activity: HomeActivity
): HomeIntentProcessor {

    override fun process(intent: Intent, navController: NavController, out: Intent): Boolean {

        if (intent.`package` !="org.giveasyoulive.donationreminder" || !intent.hasExtra("url")) {
            return false
        }

        launchToBrowser(
            intent.getStringExtra("url").orEmpty()
        )

        return true
    }

    private fun launchToBrowser(text: String) {

        activity.openToBrowserAndLoad(
            searchTermOrURL = text,
            newTab = true,
            from = BrowserDirection.FromGlobal,
        )

    }
}