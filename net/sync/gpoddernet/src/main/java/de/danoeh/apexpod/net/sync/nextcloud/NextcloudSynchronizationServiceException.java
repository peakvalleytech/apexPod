package de.danoeh.apexpod.net.sync.nextcloud;

import de.danoeh.apexpod.net.sync.model.SyncServiceException;

public class NextcloudSynchronizationServiceException extends SyncServiceException {
    public NextcloudSynchronizationServiceException(Throwable e) {
        super(e);
    }
}
