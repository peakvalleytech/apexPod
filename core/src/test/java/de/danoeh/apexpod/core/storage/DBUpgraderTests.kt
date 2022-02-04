package de.danoeh.apexpod.core.storage

import androidx.test.platform.app.InstrumentationRegistry
import de.danoeh.apexpod.core.preferences.UserPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)

class DBUpgraderTests {

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        UserPreferences.init(context)
        PodDBAdapter.init(context)
        var adapter = PodDBAdapter.getInstance()
        adapter.open()
        adapter.close()
        adapter = PodDBAdapter.getVersionInstance(1)
        PodDBAdapter.tearDownTests()


    }

    @Test
    fun testDBUpgrade3_to_4() {
        var adapter = PodDBAdapter.getVersionInstance(4)
    }
}