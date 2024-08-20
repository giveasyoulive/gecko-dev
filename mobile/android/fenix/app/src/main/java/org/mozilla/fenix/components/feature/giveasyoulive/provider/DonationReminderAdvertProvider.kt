package org.mozilla.fenix.components.feature.giveasyoulive.provider

import android.content.Context
import android.util.AtomicFile
import androidx.annotation.VisibleForTesting
import mozilla.components.concept.fetch.*
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.android.org.json.asSequence
import mozilla.components.support.ktx.util.readAndDeserialize
import mozilla.components.support.ktx.util.writeString
import org.json.JSONException
import org.json.JSONObject
import org.mozilla.fenix.components.feature.giveasyoulive.model.DonationReminderAdvert
import java.io.File
import java.io.IOException
import java.util.Date
import org.mozilla.fenix.BuildConfig

internal const val ENDPOINT_URL = "https://www.giveasyoulive.com/api/v1/ads/"
internal const val CACHE_FILE_NAME = "donation_reminder_advert_service.json"
internal const val MINUTE_IN_MS = 60 * 1000
internal const val BASE_URL  = "https://www.giveasyoulive.com"
class DonationReminderAdvertProvider(
    context: Context,
    private val client: Client,
    private val endPointURL: String = ENDPOINT_URL,
    private val maxCacheAgeInMinutes: Long = -1
)
{
    private val applicationContext = context.applicationContext
    private val logger = Logger("DonationReminderAdvertProvider")
    private val diskCacheLock = Any()

    // The last modified time of the disk cache.
    @VisibleForTesting
    @Volatile
    internal var diskCacheLastModified: Long? = null

    /**
     * Fetches from the advert url [endPointURL] to provide a list of featured ads.
     * Returns a cached response if [allowCache] is true and the cache is not expired
     * (@see [maxCacheAgeInMinutes]).
     *
     * @param allowCache Whether or not the result may be provided from a previously cached
     * response. Note that a [maxCacheAgeInMinutes] must be provided in order for the cache to be
     * active.
     * @throws IOException if the request failed to fetch any adverts.
     */
    @Throws(IOException::class)
    fun getAdverts(allowCache: Boolean): List<DonationReminderAdvert> {

        val cachedAdverts = if (allowCache && !isCacheExpired()) {
            readFromDiskCache()
        } else {
            null
        }

        if (!cachedAdverts.isNullOrEmpty()) {
            return cachedAdverts
        }

        return try {
            fetchAdverts()
        } catch (e: IOException) {
            logger.error("Failed to fetch adverts", e)
            throw e
        }
    }


    private fun fetchAdverts(): List<DonationReminderAdvert> {
        val headers: MutableHeaders? = MutableHeaders()

        headers?.append("Accept", "application/json")
        headers?.append("Authorization", BuildConfig.ADVERT_AUTHORIZATION)
        headers?.append("username", BuildConfig.ADVERT_USERNAME)
        headers?.append("password", BuildConfig.ADVERT_PASSWORD)
        headers?.append("deviceSecurityCode", BuildConfig.ADVERT_SECURITYCODE)

        client.fetch(
            Request(url = endPointURL, headers = headers)
        ).use { response ->
            if (response.isSuccess) {
                val responseBody = response.body.string(Charsets.UTF_8)

                return try {
                    JSONObject(responseBody).getAdverts().also {
                        if (maxCacheAgeInMinutes > 0) {
                            writeToDiskCache(responseBody)
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()

                    throw IOException(e)
                }
            } else {
                val errorMessage =
                    "Failed to fetch adverts. Status code: ${response.status}"

                throw IOException(errorMessage)
            }
        }
    }


    @VisibleForTesting
    internal fun readFromDiskCache(): List<DonationReminderAdvert>? {
        synchronized(diskCacheLock) {
            return getCacheFile().readAndDeserialize {
                JSONObject(it).getAdverts()
            }
        }
    }

    @VisibleForTesting
    internal fun writeToDiskCache(responseBody: String) {
        synchronized(diskCacheLock) {
            getCacheFile().let {
                it.writeString { responseBody }
                diskCacheLastModified = System.currentTimeMillis()
            }
        }
    }

    @VisibleForTesting
    internal fun isCacheExpired() =
        getCacheLastModified() < Date().time - (maxCacheAgeInMinutes * MINUTE_IN_MS)

    @VisibleForTesting
    internal fun getCacheLastModified(): Long {
        diskCacheLastModified?.let { return it }

        val file = getBaseCacheFile()

        return if (file.exists()) {
            file.lastModified().also {
                diskCacheLastModified = it
            }
        } else {
            -1
        }
    }

    private fun getCacheFile(): AtomicFile = AtomicFile(getBaseCacheFile())

    @VisibleForTesting
    internal fun getBaseCacheFile(): File = File(applicationContext.filesDir, CACHE_FILE_NAME)


    internal fun JSONObject.getAdverts(): List<DonationReminderAdvert> =
        getJSONArray("ads")
            .asSequence { i -> getJSONObject(i) }
            .mapNotNull { it.toAdvert() }
            .toList()

    private fun JSONObject.toAdvert(): DonationReminderAdvert? {
        return try {
            DonationReminderAdvert(
                merchantId = optLong("merchantId",0),
                imageUrl = BASE_URL + getString("smallImageUrl"),
                url = getString("targetUrl"),
            )
        } catch (e: JSONException) {
            null
        }
    }
}