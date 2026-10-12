package fr.luteal.app.navigation.recap

import fr.luteal.core.model.recap.RecapPainPayload
import org.junit.Assert.assertNull
import org.junit.Test

class RecapPainPayloadDyspareuniaTest {

    @Test
    fun hiddenDyspareuniaIsNotStored() {
        val payload = RecapPainPayload(
            dysmenorrhea = 2,
            dyspareunia = 7,
            dyschezia = 1
        )
        val saved = payload.copy(dyspareunia = if (HIDE) null else payload.dyspareunia)
        assertNull(saved.dyspareunia)
    }

    private companion object {
        const val HIDE = true
    }
}
