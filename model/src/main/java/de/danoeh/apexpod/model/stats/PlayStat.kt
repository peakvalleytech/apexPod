package de.danoeh.apexpod.model.stats

data class PlayStat(
    var id : Long,
    var feedItemId : Long,
    var feedId : Long,
    var startTime : Long,
    var endTime : Long,
    var startPos : Int,
    var endPos : Int
    )