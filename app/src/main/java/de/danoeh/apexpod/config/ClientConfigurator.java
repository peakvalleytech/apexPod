package de.danoeh.apexpod.config;

import de.danoeh.apexpod.BuildConfig;
import de.danoeh.apexpod.core.ClientConfig;

/**
 * Configures the ClientConfig class of the core package.
 */
class ClientConfigurator {

    private ClientConfigurator() {
    }

    static {
        ClientConfig.USER_AGENT = "AntennaPod/" + BuildConfig.VERSION_NAME;
        ClientConfig.applicationCallbacks = new ApplicationCallbacksImpl();
        ClientConfig.downloadServiceCallbacks = new DownloadServiceCallbacksImpl();
        ClientConfig.castCallbacks = new CastCallbackImpl();
    }
}
