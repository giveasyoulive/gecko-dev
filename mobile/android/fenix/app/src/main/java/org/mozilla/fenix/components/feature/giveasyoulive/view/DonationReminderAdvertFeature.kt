package org.mozilla.fenix.components.feature.giveasyoulive.view

import mozilla.components.support.base.feature.LifecycleAwareFeature
import org.mozilla.fenix.components.feature.giveasyoulive.presenter.DonationReminderAdvertDefaultPresenter
import org.mozilla.fenix.components.feature.giveasyoulive.presenter.DonationReminderAdvertPresenter
import org.mozilla.fenix.components.feature.giveasyoulive.storage.DonationReminderAdvertStorage

class DonationReminderAdvertFeature (
    private val view: DonationReminderAdvertsView,
    val storage: DonationReminderAdvertStorage,
    private val presenter: DonationReminderAdvertPresenter = DonationReminderAdvertDefaultPresenter(
        view,
        storage
    )
) : LifecycleAwareFeature {

    override fun start() {
        presenter.start()
    }

    override fun stop() {
        presenter.stop()
    }
}
