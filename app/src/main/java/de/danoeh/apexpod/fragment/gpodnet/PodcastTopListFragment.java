package de.danoeh.apexpod.fragment.gpodnet;

import de.danoeh.apexpod.net.sync.gpoddernet.GpodnetService;
import de.danoeh.apexpod.net.sync.gpoddernet.GpodnetServiceException;
import de.danoeh.apexpod.net.sync.gpoddernet.model.GpodnetPodcast;

import java.util.List;

public class PodcastTopListFragment extends PodcastListFragment {
    private static final int PODCAST_COUNT = 50;

    @Override
    protected List<GpodnetPodcast> loadPodcastData(GpodnetService service) throws GpodnetServiceException {
        return service.getPodcastToplist(PODCAST_COUNT);
    }
}
