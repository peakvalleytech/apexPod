package de.danoeh.apexpod.core.event;

public class SyncServiceEvent {
    private final int messageResId;

    public SyncServiceEvent(int messageResId) {
        this.messageResId = messageResId;
    }

    public int getMessageResId() {
        return messageResId;
    }
}
