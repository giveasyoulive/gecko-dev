package org.mozilla.fenix.components.feature.giveasyoulive.view

import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.components.appstate.AppAction
import org.mozilla.fenix.components.feature.giveasyoulive.model.DonationReminderAdvert
import org.mozilla.fenix.components.feature.giveasyoulive.model.donationReminderAdvertExamples
import org.mozilla.fenix.utils.Settings

class DonationReminderAdvertsDefaultView (
    val store: AppStore,
    val settings: Settings
) : DonationReminderAdvertsView {

    override fun displayDonationReminderAdverts(adverts: List<DonationReminderAdvert>) {
        store.dispatch(
            AppAction.DonationReminderAdvertChange(
                adverts
                //donationReminderAdvertExamples
            )
        )
    }
}


