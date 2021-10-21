package de.danoeh.apexpod.core;

import androidx.annotation.Nullable;
import androidx.mediarouter.app.MediaRouteDialogFactory;

/**
 * Callbacks for Chromecast support on the core module
 */
public interface CastCallbacks {

    @Nullable MediaRouteDialogFactory getMediaRouterDialogFactory();
}
