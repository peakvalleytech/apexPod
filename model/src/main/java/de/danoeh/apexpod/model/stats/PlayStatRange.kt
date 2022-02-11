package de.danoeh.apexpod.model.stats

import android.util.Range
import java.lang.IllegalArgumentException
import java.util.*

/**
 * Holds a set of PlayStat objects whose non negative starttime and endtime is bound to a given range.
 */
class PlayStatRange() {
    private lateinit var playStats : MutableList<PlayStat>
    var startTime : Long = 0
    var endTime : Long = 0

    init {
        playStats = mutableListOf()
    }

    /**
     *  Adds a PlayStat to existing set
     *  Insert time can be expected to be O(1)
     * @param the PlayStat to add
     * @throws IllegalArgumentException
     *      if start time or end time is not within range
     *      if range already contains a PlayStat with the same start time
     * @
     */
    fun add(playStat: PlayStat) {
        if (playStat.startTime <= startTime) {
            startTime = playStat.startTime
        }
        if (playStat.endTime >= endTime) {
            endTime = playStat.endTime
        }
        playStats.add(playStat)
    }

    fun remove(playStat: PlayStat) {

    }

    fun get(index : Int) : PlayStat {
        return playStats.get(index)
    }

    /**
     * Create a new PlayStatRange object from given sub range
     * @param start
     * @param end
     * @throws IllegalArgumentException if start time or end time is not within range or range is
     * invalid
     */
    fun from(start : Long, end : Long) : PlayStatRange {
        return PlayStatRange()
    }

    fun size(): Int {
        return playStats.size
    }

    fun getTotalTime() : Long {
        var totalTime = 0L
        playStats.forEach {
            totalTime += it.endTime - it.startTime
        }
        return totalTime
    }

    fun getTotalDuration() : Long {
        var totalDuration = 0L
        playStats.forEach {
            totalDuration += it.endPos - it.startPos
        }
        return  totalDuration
    }
    /**
     * Determines if range is valid for given range
     */
    fun isValidRange(start : Long, end : Long) : Boolean {
        return false
    }

    private fun validateRange(start : Long, end : Long) {
    }

    fun forEach(action : (PlayStat) -> Unit) {
        playStats.forEach {
            action(it)
        }
    }

//    fun getStart() : Long {
//
//    }
//
//    fun getEnd() {
//
//    }
}