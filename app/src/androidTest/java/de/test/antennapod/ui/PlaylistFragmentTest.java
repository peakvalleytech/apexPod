package de.test.antennapod.ui;


import android.content.Intent;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.fragment.PlaylistFragment;
import de.danoeh.apexpod.fragment.QueueFragment;
import de.test.antennapod.EspressoTestUtils;

@RunWith(AndroidJUnit4.class)
public class PlaylistFragmentTest {
    @Rule
    public IntentsTestRule<MainActivity> activityRule = new IntentsTestRule<>(MainActivity.class,
            false,
            false);

    @Before
    public void setUp() {
        EspressoTestUtils.clearPreferences();
        EspressoTestUtils.clearDatabase();
        EspressoTestUtils.setLastNavFragment(PlaylistFragment.TAG);

        activityRule.launchActivity(new Intent());
    }
}
