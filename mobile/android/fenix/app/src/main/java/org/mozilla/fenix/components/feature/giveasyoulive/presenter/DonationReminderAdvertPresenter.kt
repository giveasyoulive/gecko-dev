package org.mozilla.fenix.components.feature.giveasyoulive.presenter

import mozilla.components.support.base.feature.LifecycleAwareFeature
import org.mozilla.fenix.components.feature.giveasyoulive.storage.DonationReminderAdvertStorage
import org.mozilla.fenix.components.feature.giveasyoulive.view.DonationReminderAdvertsView


interface DonationReminderAdvertPresenter : LifecycleAwareFeature {
    val view: DonationReminderAdvertsView
    val storage: DonationReminderAdvertStorage
}
