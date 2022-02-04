package de.danoeh.apexpod.model.feed;

import androidx.annotation.Nullable;

public class AutoDownload {
    private int cacheSize;
    private boolean newestFirst;
    private boolean includeAll;

    public AutoDownload(int cacheSize, boolean newestFirst, boolean includeAll) {
        this.cacheSize = cacheSize;
        this.newestFirst = newestFirst;
        this.includeAll = includeAll;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public boolean isNewestFirst() {
        return newestFirst;
    }

    public boolean isIncludeAll() {
        return includeAll;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public void setNewestFirst(boolean newestFirst) {
        this.newestFirst = newestFirst;
    }

    public void setIncludeAll(boolean includeAll) {
        this.includeAll = includeAll;
    }

}
