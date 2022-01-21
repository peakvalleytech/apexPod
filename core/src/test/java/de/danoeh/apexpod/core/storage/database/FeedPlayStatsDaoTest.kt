package de.danoeh.apexpod.core.storage.database

import androidx.test.platform.app.InstrumentationRegistry
import de.danoeh.apexpod.core.preferences.UserPreferences
import de.danoeh.apexpod.core.storage.ApexDBAdapter
import de.danoeh.apexpod.core.storage.database.PlayStatDaoTest.TestData
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FeedPlayStatsDaoTest {
    private var adapter: ApexDBAdapter? = null
    var playStatsDao: PlayStatDao? = null
    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        UserPreferences.init(context)
        ApexDBAdapter.init(context)
        ApexDBAdapter.deleteDatabase()
        ApexDBAdapter.tearDownTests()
        adapter = ApexDBAdapter.getInstance()
        adapter?.open()
        playStatsDao = PlayStatDao()
//        data = TestData()
    }
}