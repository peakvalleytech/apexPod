package de.danoeh.apexpod.core.storage.database

import de.danoeh.apexpod.model.stats.PlayStat
import de.danoeh.apexpod.model.stats.PlayStatRange

interface PlayStatDao {
    fun createPlayStat(playStat : PlayStat) : Long
    fun getAllPlayStats() : PlayStatRange
    fun getAllByFeedItemId(feedItemId : Long) : PlayStatRange
    fun getPlayStatsByRange(fromDateMillis : Long, toDateMillis : Long) : PlayStatRange
    fun updatePlayStat(playStat : PlayStat)
    fun deletePlayStat(playStat: PlayStat)
}