package org.mozilla.fenix.components.feature.giveasyoulive.model

data class DonationReminderAdvert(

    val url: String,
    val imageUrl: String,
    val merchantId: Long
)

var donationReminderAdvertExamples = listOf(
    DonationReminderAdvert("https://www.giveasyoulive.com/statistics/gayl/clickthrough?s=tagrecalc&web=Y&allowanon=Y&offerid=124587&code=SUNSHINE&sr=2668&dl=http%3A%2F%2Fwww.tui.co.uk%2Fdestinations%2Fdeals%2Fsummer-holiday-deals",
        "https://www.giveasyoulive.com/images/site-banners/631_mobile.png",
        631),
    DonationReminderAdvert("https://www.giveasyoulive.com/statistics/gayl/clickthrough?s=tagrecalc&web=Y&allowanon=Y&offerid=124587&code=SUNSHINE&sr=2668&dl=http%3A%2F%2Fwww.tui.co.uk%2Fdestinations%2Fdeals%2Fsummer-holiday-deals",
        "https://www.giveasyoulive.com/images/site-banners/630_mobile.png",
        630),
    DonationReminderAdvert("https://www.giveasyoulive.com/statistics/gayl/clickthrough?s=tagrecalc&web=Y&allowanon=Y&offerid=124587&code=SUNSHINE&sr=2668&dl=http%3A%2F%2Fwww.tui.co.uk%2Fdestinations%2Fdeals%2Fsummer-holiday-deals",
        "https://www.giveasyoulive.com/images/site-banners/632_mobile.png",
        632)
)
