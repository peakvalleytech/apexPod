package de.danoeh.apexpod.model.feed;

public class AutoDownload {
    private int numberToUpdate;
    private boolean newestFirst;
    private boolean includeAll;

    public AutoDownload(int numberToUpdate, boolean newestFirst, boolean includeAll) {
        this.numberToUpdate = numberToUpdate;
        this.newestFirst = newestFirst;
        this.includeAll = includeAll;
    }

    public int getNumberToUpdate() {
        return numberToUpdate;
    }

    public boolean isNewestFirst() {
        return newestFirst;
    }

    public boolean isIncludeAll() {
        return includeAll;
    }
}
