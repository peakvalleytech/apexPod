package de.danoeh.apexpod.core.storage.repository

import de.danoeh.apexpod.model.stats.PlayStat

interface PlayStatsRepository {
    fun createPlayStat(playStat : PlayStat)
    fun getPlayStats() : List<PlayStat>
    fun updatePlayStat(playStat : PlayStat)
    fun deletePlayStat(playStat: PlayStat)
}