package de.danoeh.apexpod.model.feed;

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
}
