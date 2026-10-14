package fr.luteal.app.notification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import fr.luteal.core.data.entity.ReminderEntity
import fr.luteal.core.model.ReminderStatus
import fr.luteal.core.model.ReminderType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReminderNotifierTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        NotificationChannelManager(context).registerChannels()
    }

    @Test
    fun builtNotificationHasContentIntentAndThreeActions() {
        val reminder = ReminderEntity(
            id = "rem-test",
            type = ReminderType.APPOINTMENT_REPORT.name,
            triggerAt = 0L,
            payloadJson = "{}",
            eventId = "evt-1",
            status = ReminderStatus.FIRED.name
        )
        val notification = buildReminderNotification(context, reminder).build()
        assertNotNull(notification.contentIntent)
        assertEquals(3, notification.actions?.size)
    }

}
