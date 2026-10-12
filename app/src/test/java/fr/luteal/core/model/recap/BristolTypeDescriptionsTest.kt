package fr.luteal.core.model.recap

import org.junit.Assert.assertTrue
import org.junit.Test

class BristolTypeDescriptionsTest {

    @Test
    fun descriptionsAreNonEmptyForAllTypes() {
        (1..7).forEach { type ->
            assertTrue(BristolTypeDescriptions.description(type).isNotBlank())
            assertTrue(BristolTypeDescriptions.label(type).contains("Type $type"))
        }
    }
}
