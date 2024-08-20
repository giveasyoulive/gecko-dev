package org.mozilla.fenix.components.feature.giveasyoulive.storage

import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.base.observer.Observable
import mozilla.components.support.base.observer.ObserverRegistry
import org.mozilla.fenix.components.feature.giveasyoulive.model.DonationReminderAdvert
import org.mozilla.fenix.components.feature.giveasyoulive.provider.DonationReminderAdvertProvider

class DonationReminderAdvertDefaultStorage(
    private val advertProvider: DonationReminderAdvertProvider? = null,
) : DonationReminderAdvertStorage, Observable<DonationReminderAdvertStorage.Observer> by ObserverRegistry()
{
    // Cache of the last retrieved top sites
    var cachedAdverts = listOf<DonationReminderAdvert>()

    private val logger = Logger("DefaultDonationReminderAdvertStorage")

    override suspend fun getAdverts(): List<DonationReminderAdvert> {

        val adverts = ArrayList<DonationReminderAdvert>()
        var providerAdverts: List<DonationReminderAdvert>

        if (advertProvider != null) {
            try {
                providerAdverts = advertProvider.getAdverts(allowCache = true)

                adverts.addAll(providerAdverts)

            } catch (e: Exception) {
                logger.error("Failed to fetch adverts from provider", e)
            }

        }

        cachedAdverts = adverts

        return adverts
    }
}