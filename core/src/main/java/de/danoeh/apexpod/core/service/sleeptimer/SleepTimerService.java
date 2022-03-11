package de.danoeh.apexpod.core.service.sleeptimer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.danoeh.apexpod.core.service.SleepTimer;
import de.danoeh.apexpod.core.service.playback.PlaybackServiceTaskManager;

/**
 * Sleeps for a given time and then pauses playback.
 */
public class SleepTimerService {
    private static final String TAG = "SleepTimerService";
    private final ScheduledThreadPoolExecutor schedExecutor;
    private ScheduledFuture<?> sleepTimerFuture;
    private SleepTimer sleepTimer;
    private final Context context;
    private final PlaybackServiceTaskManager.TaskManagerCallback callback;
    private static final int SCHED_EX_POOL_SIZE = 2;
    /**
     * Sets up a new PSTM. This method will also start the queue loader task.
     *
     * @param context
     * @param callback A PSTMCallback object for notifying the user about updates. Must not be null.
     */
    public SleepTimerService(@NonNull Context context,
                                      @NonNull PlaybackServiceTaskManager.TaskManagerCallback callback) {
        this.context = context;
        this.callback = callback;
        schedExecutor = new ScheduledThreadPoolExecutor(SCHED_EX_POOL_SIZE, r -> {
            Thread t = new Thread(r);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
    }
    /**
     * Starts a new sleep timer with the given waiting time. If another sleep timer is already active, it will be
     * cancelled first.
     * After waitingTime has elapsed, onSleepTimerExpired() will be called.
     *
     * @throws java.lang.IllegalArgumentException if waitingTime <= 0
     */
    public synchronized void setSleepTimer(long waitingTime) {
        if (waitingTime <= 0) {
            throw new IllegalArgumentException("Waiting time <= 0");
        }

        Log.d(TAG, "Setting sleep timer to " + waitingTime + " milliseconds");
        if (isSleepTimerActive()) {
            sleepTimerFuture.cancel(true);
        }
        sleepTimer = new SleepTimer(context, this, callback, waitingTime);
        sleepTimerFuture = schedExecutor.schedule(sleepTimer, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns true if the sleep timer is currently active.
     */
    public synchronized boolean isSleepTimerActive() {
        return sleepTimer != null
                && sleepTimerFuture != null
                && !sleepTimerFuture.isCancelled()
                && !sleepTimerFuture.isDone()
                && sleepTimer.getWaitingTime() > 0;
    }
    /**
     * Disables the sleep timer. If the sleep timer is not active, nothing will happen.
     */
    public synchronized void disableSleepTimer() {
        if (isSleepTimerActive()) {
            Log.d(TAG, "Disabling sleep timer");
            sleepTimer.cancel();
        }
    }

    /**
     * Restarts the sleep timer. If the sleep timer is not active, nothing will happen.
     */
    public synchronized void restartSleepTimer() {
        if (isSleepTimerActive()) {
            Log.d(TAG, "Restarting sleep timer");
            sleepTimer.restart();
        }
    }

    /**
     * Returns the current sleep timer time or 0 if the sleep timer is not active.
     */
    public synchronized long getSleepTimerTimeLeft() {
        if (isSleepTimerActive()) {
            return sleepTimer.getWaitingTime();
        } else {
            return 0;
        }
    }
}
