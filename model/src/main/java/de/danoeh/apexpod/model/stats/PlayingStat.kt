package de.danoeh.apexpod.model.stats

data class PlayingStat(
    var id : Long,
    var feedItemId : Long,
    var startTime : Long,
    var endTime : Long,
    var startPos : Int,
    var endPos : Int)