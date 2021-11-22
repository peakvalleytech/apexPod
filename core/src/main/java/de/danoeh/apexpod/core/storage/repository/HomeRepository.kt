package de.danoeh.apexpod.core.storage.repository

import de.danoeh.apexpod.model.feed.FeedItem

interface HomeRepository {
    fun getFeaturedEpisode() : FeedItem?
}