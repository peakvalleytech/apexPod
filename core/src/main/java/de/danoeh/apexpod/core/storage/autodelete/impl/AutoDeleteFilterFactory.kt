package de.danoeh.apexpod.core.storage.autodelete.impl

import de.danoeh.apexpod.core.storage.autodelete.AutoDeleteFilter

class AutoDeleteFilterFactory {
    fun createAutoDeleteFilter(hrsAfterPlayback : Int, keepFavorite : Boolean, keepPlaylisted : Boolean) : AutoDeleteFilter {
        val autoDeleteFilterBuilder = AutoDeleteFilterImpl.AutoDeleteFilterBuilderImpl()
        autoDeleteFilterBuilder
            .setDuration(hrsAfterPlayback)
            .keepFavorite(keepFavorite)
//            .keepQueued(keepPlaylisted)
        return autoDeleteFilterBuilder.build()
    }
}