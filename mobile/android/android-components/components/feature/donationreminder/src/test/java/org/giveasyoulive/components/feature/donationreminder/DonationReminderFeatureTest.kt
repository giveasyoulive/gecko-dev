/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.giveasyoulive.components.feature.donationreminder

import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.webextension.WebExtension
import mozilla.components.support.test.argumentCaptor
import mozilla.components.support.test.eq
import mozilla.components.support.test.mock
import mozilla.components.support.webextensions.WebExtensionController
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class DonationReminderFeatureTest {

    @Before
    fun setup() {
        WebExtensionController.installedExtensions.clear()
    }

    @Test
    fun `installs the webextension`() {
        val engine: Engine = mock()

        val donationReminderFeature = spy(DonationReminderFeature)
        donationReminderFeature.install(engine)

        val onSuccess = argumentCaptor<((WebExtension) -> Unit)>()
        val onError = argumentCaptor<((Throwable) -> Unit)>()
        verify(engine, times(1)).installBuiltInWebExtension(
            eq(DonationReminderFeature.DONATION_REMINDER_EXTENSION_ID),
            eq(DonationReminderFeature.DONATION_REMINDER_EXTENSION_URL),
            onSuccess.capture(),
            onError.capture(),
        )
    }
}
