package de.danoeh.apexpod.fragment.preferences;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.PreferenceActivity;
import de.danoeh.apexpod.dialog.SwipeActionsDialog;
import de.danoeh.apexpod.fragment.feed.FeedItemlistFragment;
import de.danoeh.apexpod.fragment.QueueFragment;

public class SwipePreferencesFragment extends PreferenceFragmentCompat {
    private static final String PREF_SWIPE_FEED = "prefSwipeFeed";
    private static final String PREF_SWIPE_QUEUE = "prefSwipeQueue";
    //private static final String PREF_SWIPE_INBOX = "prefSwipeInbox";
    //private static final String PREF_SWIPE_EPISODES = "prefSwipeEpisodes";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_swipe);

        findPreference(PREF_SWIPE_FEED).setOnPreferenceClickListener(preference -> {
            new SwipeActionsDialog(requireContext(), FeedItemlistFragment.TAG).show(() -> { });
            return true;
        });
        findPreference(PREF_SWIPE_QUEUE).setOnPreferenceClickListener(preference -> {
            new SwipeActionsDialog(requireContext(), QueueFragment.TAG).show(() -> { });
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ((PreferenceActivity) getActivity()).getSupportActionBar().setTitle(R.string.swipeactions_label);
    }

}
