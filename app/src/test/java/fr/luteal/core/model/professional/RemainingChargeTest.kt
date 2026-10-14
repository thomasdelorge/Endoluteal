package fr.luteal.core.model.professional

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemainingChargeTest {

    @Test
    fun subtractsCpamAndMutuelle() {
        assertEquals(1500, remainingChargeCents(5000, 1500, 2000))
    }

    @Test
    fun treatsMissingPartsAsZero() {
        assertEquals(5000, remainingChargeCents(5000, null, null))
    }

    @Test
    fun staysNullWithoutAFee() {
        assertNull(remainingChargeCents(null, 100, 100))
    }

    @Test
    fun canBeNegative() {
        assertEquals(-500, remainingChargeCents(1000, 1500, 0))
    }

    @Test
    fun parsesFrenchAndDotDecimals() {
        assertEquals(1550, parseEurosToCents("15,50"))
        assertEquals(1550, parseEurosToCents("15.50"))
        assertNull(parseEurosToCents(""))
    }
}