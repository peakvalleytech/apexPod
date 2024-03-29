package de.danoeh.apexpod.ui.appstartintent;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Launches the main activity of the app with specific arguments.
 * Does not require a dependency on the actual implementation of the activity.
 */
public class MainActivityStarter {
    public static final String INTENT = "de.danoeh.apexpod.intents.MAIN_ACTIVITY";
    public static final String EXTRA_OPEN_PLAYER = "open_player";

    private final Intent intent;
    private final Context context;

    public MainActivityStarter(Context context) {
        this.context = context;
        intent = new Intent(INTENT);
        intent.setPackage(context.getPackageName());
    }

    public Intent getIntent() {
        return intent;
    }

    public PendingIntent getPendingIntent() {
        return PendingIntent.getActivity(context, R.id.pending_intent_player_activity,
                getIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void start() {
        context.startActivity(getIntent());
    }

    public MainActivityStarter withOpenPlayer() {
        intent.putExtra(EXTRA_OPEN_PLAYER, true);
        return this;
    }
}
