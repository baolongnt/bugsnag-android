package com.bugsnag.android

import android.content.Context
import com.bugsnag.android.EventStore.EVENT_COMPARATOR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class EventFilenameTest {

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var event: Event

    @Mock
    lateinit var file: File

    @Mock
    lateinit var app: AppWithState

    private lateinit var eventStore: EventStore

    private val config = BugsnagTestUtils.generateImmutableConfig()

    /**
     * Generates a client and ensures that its eventStore has 0 files persisted
     *
     * @throws Exception if initialisation failed
     */
    @Before
    fun setUp() {
        eventStore = EventStore(
            config,
            context,
            NoopLogger,
            Notifier(),
            null
        )
        `when`(event.app).thenReturn(app)
        `when`(event.apiKey).thenReturn("0000111122223333aaaabbbbcccc9999")
        `when`(app.duration).thenReturn(null)
    }

    @Test
    fun testIsLaunchCrashReport() {
        val valid =
            arrayOf("1504255147933_30b7e350-dcd1-4032-969e-98d30be62bbc_startupcrash.json")
        val invalid = arrayOf(
            "",
            ".json",
            "abcdeAO.json",
            "!@£)(%)(",
            "1504255147933.txt",
            "1504255147933.json"
        )

        for (s in valid) {
            assertTrue(eventStore.isLaunchCrashReport(File(s)))
        }
        for (s in invalid) {
            assertFalse(eventStore.isLaunchCrashReport(File(s)))
        }
    }

    @Test
    fun testComparator() {
        val first = "1504255147933_683c6b92-b325-4987-80ad-77086509ca1e.json"
        val second = "1505000000000_683c6b92-b325-4987-80ad-77086509ca1e.json"
        val startup = "1504500000000_683c6b92-b325-4987-80ad-77086509ca1e_startupcrash.json"

        // handle defaults
        assertEquals(0, EVENT_COMPARATOR.compare(null, null).toLong())
        assertEquals(-1, EVENT_COMPARATOR.compare(File(""), null).toLong())
        assertEquals(1, EVENT_COMPARATOR.compare(null, File("")).toLong())

        // same value should always be 0
        assertEquals(0, EVENT_COMPARATOR.compare(File(first), File(first)).toLong())
        assertEquals(0, EVENT_COMPARATOR.compare(File(startup), File(startup)).toLong())

        // first is before second
        assertTrue(EVENT_COMPARATOR.compare(File(first), File(second)) < 0)
        assertTrue(EVENT_COMPARATOR.compare(File(second), File(first)) > 0)

        // startup is handled correctly
        assertTrue(EVENT_COMPARATOR.compare(File(first), File(startup)) < 0)
        assertTrue(EVENT_COMPARATOR.compare(File(second), File(startup)) > 0)
    }

    @Test
    fun regularJvmEventName() {
        val filename = eventStore.getFilename(event, "my-uuid-123", 1504255147933, "/errors/")
        assertEquals("/errors/1504255147933_0000111122223333aaaabbbbcccc9999_my-uuid-123.json", filename)
    }

    /**
     * Simulates a crash 1s after launch which is considered a startup crash
     */
    @Test
    fun startupCrashJvmEventName() {
        `when`(app.duration).thenReturn(1000)
        val filename = eventStore.getFilename(event, "my-uuid-123", 1504255147933, "/errors/")
        assertEquals("/errors/1504255147933_0000111122223333aaaabbbbcccc9999_my-uuid-123_startupcrash.json", filename)
    }

    /**
     * Simulates a crash 10s after launch which is not considered a startup crash
     */
    @Test
    fun nonStartupCrashCrashJvmEventName() {
        `when`(app.duration).thenReturn(10000)
        val filename = eventStore.getFilename(event, "my-uuid-123", 1504255147933, "/errors/")
        assertEquals("/errors/1504255147933_0000111122223333aaaabbbbcccc9999_my-uuid-123.json", filename)
    }

    @Test
    fun ndkEventName() {
        val filename = eventStore.getFilename("{}", "my-uuid-123", 1504255147933, "/errors/")
        assertEquals("/errors/1504255147933_my-uuid-123not-jvm.json", filename)
    }

    @Test
    fun apiKeyFromEmptyFilename() {
        `when`(file.name).thenReturn("")
        assertNull(eventStore.getApiKeyFromFilename(file))
    }

    /**
     * Should return null as no api key is present
     */
    @Test
    fun apiKeyFromLegacyFilename() {
        `when`(file.name).thenReturn("1504500000000_683c6b92-b325-4987-80ad-77086509ca1e_startupcrash.json")
        assertNull(eventStore.getApiKeyFromFilename(file))
    }

    @Test
    fun apiKeyFromNewFilename() {
        `when`(file.name).thenReturn("1504255147933_0000111122223333aaaabbbbcccc9999" +
                "_683c6b92-b325-4987-80ad-77086509ca1e.json")
        assertEquals("0000111122223333aaaabbbbcccc9999", eventStore.getApiKeyFromFilename(file))
    }
}
