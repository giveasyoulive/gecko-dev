package org.mozilla.fenix.components.feature.giveasyoulive.storage

import mozilla.components.support.base.observer.Observable
import org.mozilla.fenix.components.feature.giveasyoulive.model.DonationReminderAdvert


/**
 * Abstraction layer above the [DonationReminderAdvertDefaultStorage] storages.
 */
interface DonationReminderAdvertStorage : Observable<DonationReminderAdvertStorage.Observer> {

    suspend fun getAdverts(
    ): List<DonationReminderAdvert>

    /**
     * Interface to be implemented by classes that want to observe the top site storage.
     */
    interface Observer {
        /**
         * Notify the observer when changes are made to the storage.
         */
        fun onStorageUpdated()
    }
}


