package de.danoeh.apexpod.core.storage.autodownload

interface AutoDownloadFilter {

    class AutoDownloadFilterBuilder {
        fun setItemsToUpdate() : AutoDownloadFilterBuilder {
            return this
        }

        fun setDownloadOrder(newest: Boolean) : AutoDownloadFilterBuilder{
            return this
        }

        fun includeAllItems() : AutoDownloadFilterBuilder {
            return this
        }

        fun build() : AutoDownloadFilter {
        }
    }
}