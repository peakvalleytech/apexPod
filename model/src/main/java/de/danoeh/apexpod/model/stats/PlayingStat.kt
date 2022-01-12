package de.danoeh.apexpod.model.stats

data class PlayingStat(
    val id : Long,
    val feedItemId : Long,
    val startTime : Long,
    val endTime : Long,
    val actualDuration : Long)