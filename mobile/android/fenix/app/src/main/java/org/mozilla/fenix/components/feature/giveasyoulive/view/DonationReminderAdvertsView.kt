package org.mozilla.fenix.components.feature.giveasyoulive.view

import org.mozilla.fenix.components.feature.giveasyoulive.model.DonationReminderAdvert

interface DonationReminderAdvertsView {
    fun displayDonationReminderAdverts(adverts: List<DonationReminderAdvert>)
}