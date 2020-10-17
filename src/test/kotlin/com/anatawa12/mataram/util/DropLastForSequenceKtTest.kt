package com.anatawa12.mataram.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DropLastForSequenceKtTest {
    @Test
    fun dropLast() {
        assertEquals(listOf("a", "b", "c"), sequenceOf("a", "b", "c", "d", "e").dropLast(2))
        assertEquals(listOf<String>(), sequenceOf("a", "b", "c", "d", "e").dropLast(5))
        assertEquals(listOf<String>(), sequenceOf("a", "b", "c", "d", "e").dropLast(6))
    }

    @Test
    fun dropLastReturnsEmpty() {
        assertEquals(listOf<String>(), sequenceOf("a", "b", "c", "d", "e").dropLast(5))
        assertEquals(listOf<String>(), sequenceOf("a", "b", "c", "d", "e").dropLast(6))
    }

    @Test
    fun dropLast0() {
        assertEquals(listOf("a", "b", "c", "d", "e"), sequenceOf("a", "b", "c", "d", "e").dropLast(0))
    }

    @Test
    fun dropLastWithNegativeArgument() {
        assertThrows(IllegalArgumentException::class.java) {
            sequenceOf<String>().dropLast(-1)
        }
    }
}
