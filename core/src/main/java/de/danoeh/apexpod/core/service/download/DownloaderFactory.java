package de.danoeh.apexpod.core.service.download;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface DownloaderFactory {
    @Nullable
    Downloader create(@NonNull DownloadRequest request);
}