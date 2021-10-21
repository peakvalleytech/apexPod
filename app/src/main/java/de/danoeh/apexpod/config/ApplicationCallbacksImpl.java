package de.danoeh.apexpod.config;


import android.app.Application;
import android.content.Context;
import android.content.Intent;

import de.danoeh.apexpod.PodcastApp;
import de.danoeh.apexpod.activity.StorageErrorActivity;
import de.danoeh.apexpod.core.ApplicationCallbacks;

public class ApplicationCallbacksImpl implements ApplicationCallbacks {

    @Override
    public Application getApplicationInstance() {
        return PodcastApp.getInstance();
    }

    @Override
    public Intent getStorageErrorActivity(Context context) {
        return new Intent(context, StorageErrorActivity.class);
    }

}
