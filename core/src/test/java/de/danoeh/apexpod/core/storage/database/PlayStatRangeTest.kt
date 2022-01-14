package de.danoeh.apexpod.core.storage.database

import de.danoeh.apexpod.model.stats.PlayStatRange
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.lang.Exception

class PlayStatRangeTest {
    @Test
    fun givenValidRange_whenCreatingInstance_returnEmptyRange() {
        val playStatRange = PlayStatRange(0, 1)
        assertEquals(0, playStatRange.size())
    }

    @Test
    fun givenStartEqEnd_whenCreatingInstance_returnEmptyRange() {
        var playStatRange = PlayStatRange(0, 0)
        var expectedSize = 0
        var actualSize = playStatRange.size()
        assertEquals(expectedSize, actualSize)
        playStatRange = PlayStatRange(5, 5)
        actualSize = playStatRange.size()
        assertEquals(expectedSize, actualSize)
    }

    @Test(expected = Exception::class)
    fun givenStartGTEnd_whenCreatingInstance_throwException() {
        val playStatRange = PlayStatRange(1, 0)
    }
}