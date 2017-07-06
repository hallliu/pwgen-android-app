package com.hallliu.passwordgenerator

import org.junit.Assert
import org.junit.Test

class Base64GenTest {
    val standardBase64Map : ByteArray by lazy {
        val result = mutableListOf<Byte>()
        // A-Z
        (0..25).mapTo(result) {
            (it + 'A'.toInt()).toByte()
        }
        // a-z
        (0..25).mapTo(result) {
            (it + 'a'.toInt()).toByte()
        }
        // 0-9
        (0..9).mapTo(result) {
            (it + '0'.toInt()).toByte()
        }
        result.addAll("+/=".map { it.toByte() })
        result.toByteArray()
    }

    @Test
    fun simple_string_test() {
        val inputs = listOf(
                listOf(0, 0, 0).map { it.toByte() }.toByteArray(),
                "test input".toByteArray(),
                "test input test input 2".toByteArray(),
                ByteArray(0)
        )
        val expectedOutputs = listOf("AAAA", "dGVzdCBpbnB1dA==",
                "dGVzdCBpbnB1dCB0ZXN0IGlucHV0IDI=", "")
        inputs.zip(expectedOutputs).forEach {
            Assert.assertEquals("input len ${it.first.size}",
                    it.second, encodeToBase64(it.first, standardBase64Map))
        }
    }
}
