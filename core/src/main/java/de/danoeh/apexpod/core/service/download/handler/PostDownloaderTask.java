package de.danoeh.apexpod.core.service.download.handler;

import de.danoeh.apexpod.core.event.DownloadEvent;
import de.danoeh.apexpod.core.service.download.Downloader;
import de.danoeh.apexpod.core.storage.DownloadRequester;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostDownloaderTask implements Runnable {
    private List<Downloader> downloads;

    public PostDownloaderTask(List<Downloader> downloads) {
        this.downloads = downloads;
    }

    @Override
    public void run() {
        List<Downloader> runningDownloads = new ArrayList<>();
        for (Downloader downloader : downloads) {
            if (!downloader.cancelled) {
                runningDownloads.add(downloader);
            }
        }
        DownloadRequester.getInstance().updateProgress(downloads);
        List<Downloader> list = Collections.unmodifiableList(runningDownloads);
        EventBus.getDefault().postSticky(DownloadEvent.refresh(list));
    }
}
