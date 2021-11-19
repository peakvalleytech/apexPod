package de.danoeh.apexpod.core.storage.autodelete.impl.rules

import de.danoeh.apexpod.core.storage.autodelete.AutoDeleteRule
import de.danoeh.apexpod.model.feed.FeedItem

class IncludeFavoritesAutoDeleteRule : AutoDeleteRule {
    override fun shouldDelete(item: FeedItem): Boolean {
        if (item.hasMedia()
            && item.isTagged(FeedItem.TAG_FAVORITE)
        ) {
            return true
        }
        return false
    }

}