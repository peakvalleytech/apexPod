package de.danoeh.apexpod.model.stats

import java.lang.IllegalArgumentException

/**
 * Holds a set of PlayStat objects whose starttime and endtime is bound to a given range.
 */
class PlayStatRange(val start : Long, val end : Long) {
    private lateinit var playStats : MutableList<PlayStat>
    init {
        if (start > end) {
            throw IllegalArgumentException()
        }
        playStats = mutableListOf()
    }

    /**
     * @param the PlayStat to add
     * @throws IllegalArgumentException if start time or end time is not within range
     */
    fun add(playStat: PlayStat) {
//        if(playStat.startTime )

    }

    fun remove(playStat: PlayStat) {

    }

    fun get(index : Int) : PlayStat {
        return playStats.get(0)
    }

    /**
     * Create a new PlayStatRange object from given sub range
     * @param start
     * @param end
     * @throws IllegalArgumentException if start time or end time is not within range or range is
     * invalid
     */
    fun from(start : Long, end : Long) : PlayStatRange {
        return PlayStatRange(start, end)
    }

    fun size(): Int {
        return playStats.size
    }
}