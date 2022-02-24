package de.danoeh.apexpod.core.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;

import de.danoeh.apexpod.core.preferences.SleepTimerPreferences;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.service.playback.PlaybackService;
import de.danoeh.apexpod.core.service.playback.PlaybackServiceTaskManager;
import de.danoeh.apexpod.core.service.playback.ShakeListener;
import de.danoeh.apexpod.core.service.sleeptimer.SleepTimerService;

public class SleepTimer implements Runnable {
        private static final String TAG = "SleepTimer";
        private static final long UPDATE_INTERVAL = 1000L;
        public static final long NOTIFICATION_THRESHOLD = 10000;
        private boolean hasVibrated = false;
        private final long waitingTime;
        private long timeLeft;
        private ShakeListener shakeListener;
        private final Handler handler;
        private Context context;
        private SleepTimerService sleepTimerService;
        private PlaybackServiceTaskManager.TaskManagerCallback callback;
        public SleepTimer(Context context,
                          SleepTimerService sleepTimerService,
                          PlaybackServiceTaskManager.TaskManagerCallback callback,
                          long waitingTime) {
            super();
            this.context = context;
            this.sleepTimerService = sleepTimerService;
            this.callback = callback;
            this.waitingTime = waitingTime;
            this.timeLeft = waitingTime;

            if (UserPreferences.useExoplayer() && Looper.myLooper() == Looper.getMainLooper()) {
                // Run callbacks in main thread so they can call ExoPlayer methods themselves
                this.handler = new Handler(Looper.getMainLooper());
            } else {
                this.handler = null;
            }
        }

        private void postCallback(Runnable r) {
            if (handler == null) {
                r.run();
            } else {
                handler.post(r);
            }
        }

        @Override
        public void run() {
            Log.d(TAG, "Starting");
            long lastTick = System.currentTimeMillis();
            while (timeLeft > 0) {
                try {
                    Thread.sleep(UPDATE_INTERVAL);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Thread was interrupted while waiting");
                    e.printStackTrace();
                    break;
                }

                long now = System.currentTimeMillis();
                timeLeft -= now - lastTick;
                lastTick = now;

                if (timeLeft < NOTIFICATION_THRESHOLD) {
                    Log.d(TAG, "Sleep timer is about to expire");
                    if (SleepTimerPreferences.vibrate() && !hasVibrated) {
                        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        if (v != null) {
                            v.vibrate(500);
                            hasVibrated = true;
                        }
                    }
                    if (shakeListener == null && SleepTimerPreferences.shakeToReset()) {
                        shakeListener = new ShakeListener(context, this);
                    }
                    postCallback(() -> callback.onSleepTimerAlmostExpired(timeLeft));
                }
                if (timeLeft <= 0) {
                    Log.d(TAG, "Sleep timer expired");
                    if (shakeListener != null) {
                        shakeListener.pause();
                        shakeListener = null;
                    }
                    hasVibrated = false;
                    if (!Thread.currentThread().isInterrupted()) {
                        postCallback(callback::onSleepTimerExpired);
                    } else {
                        Log.d(TAG, "Sleep timer interrupted");
                    }
                }
            }
        }

        public long getWaitingTime() {
            return timeLeft;
        }

        public void restart() {
            postCallback(() -> {
                sleepTimerService.setSleepTimer(waitingTime);
                callback.onSleepTimerReset();
            });
            if (shakeListener != null) {
                shakeListener.pause();
                shakeListener = null;
            }
        }

        public void cancel() {
            if (shakeListener != null) {
                shakeListener.pause();
            }
            postCallback(callback::onSleepTimerReset);
        }
    }
