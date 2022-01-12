package de.danoeh.apexpod.core.storage.repository

import de.danoeh.apexpod.model.stats.PlayingStat

interface PlayStatsRepository {
    fun createPlayStat(playStat : PlayingStat)
    fun getPlayStats() : List<PlayingStat>
    fun updatePlayStat(playStat : PlayingStat)
    fun deletePlayStat(playStat: PlayingStat)
}