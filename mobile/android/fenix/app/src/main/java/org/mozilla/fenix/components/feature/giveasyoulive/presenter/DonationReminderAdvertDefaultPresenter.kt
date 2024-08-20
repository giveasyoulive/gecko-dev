package org.mozilla.fenix.components.feature.giveasyoulive.presenter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.fenix.components.feature.giveasyoulive.storage.DonationReminderAdvertStorage
import org.mozilla.fenix.components.feature.giveasyoulive.view.DonationReminderAdvertsView
import kotlin.coroutines.CoroutineContext

internal class DonationReminderAdvertDefaultPresenter (
    override val view: DonationReminderAdvertsView,
    override val storage: DonationReminderAdvertStorage,
    coroutineContext: CoroutineContext = Dispatchers.IO
    ) : DonationReminderAdvertPresenter, DonationReminderAdvertStorage.Observer {

        private val scope = CoroutineScope(coroutineContext)

        override fun start() {
            onStorageUpdated()

            storage.register(this)
        }

        override fun stop() {
            storage.unregister(this)
        }

        override fun onStorageUpdated() {

            scope.launch {
                val adverts = storage.getAdverts()

                scope.launch(Dispatchers.Main) {
                    view.displayDonationReminderAdverts(adverts)
                }
            }
        }
    }
