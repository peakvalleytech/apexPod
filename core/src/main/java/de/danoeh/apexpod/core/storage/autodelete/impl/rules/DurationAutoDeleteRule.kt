package de.danoeh.apexpod.core.storage.autodelete.impl.rules

import androidx.annotation.VisibleForTesting
import de.danoeh.apexpod.core.storage.APCleanupAlgorithm
import de.danoeh.apexpod.core.storage.autodelete.AutoDeleteRule
import de.danoeh.apexpod.model.feed.FeedItem
import java.util.*

class DurationAutoDeleteRule(val numberOfHoursAfterPlayback : Int) : AutoDeleteRule {
    override fun shouldDelete(item: FeedItem): Boolean {
        // make sure this candidate was played at least the proper amount of days prior
        // to now
        if (item.hasMedia()) {
            // make sure this candidate was played at least the proper amount of days prior
            // to now
                val media = item.media
            val mostRecentDateForDeletion = calcMostRecentDateForDeletion(Date())

            if (media != null && media.getPlaybackCompletionDate() != null && media.getPlaybackCompletionDate()
                    .before(mostRecentDateForDeletion)
            ) {
                return true
            }
        }
        return false
    }

    @VisibleForTesting
    fun calcMostRecentDateForDeletion(currentDate: Date): Date? {
        return minusHours(currentDate, numberOfHoursAfterPlayback)
    }

    private fun minusHours(baseDate: Date, numberOfHours: Int): Date? {
        val cal = Calendar.getInstance()
        cal.time = baseDate
        cal.add(Calendar.HOUR_OF_DAY, -1 * numberOfHours)
        return cal.time
    }
}