package de.danoeh.apexpod.core.event;

public class PlaybackHistoryEvent {

    private PlaybackHistoryEvent() {
    }

    public static PlaybackHistoryEvent listUpdated() {
        return new PlaybackHistoryEvent();
    }

    @Override
    public String toString() {
        return "PlaybackHistoryEvent";
    }
}
