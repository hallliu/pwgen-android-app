package com.hallliu.passwordgenerator

import java.security.MessageDigest

const val UPPERS = "QWERTYUIOPASDFGHJKLZXCVBNM"
const val NUMBERS = "1234567890"
const val SYMBOLS = "!@#\$%^&*()~`{}[];:<>,.?/"

data class PasswordSpecification(val siteName: String, val pwLength: Int,
                                 val permittedChars: String, val requiredChars: String = "",
                                 val pwVersion: Int) {
    init {
        if (!requiredChars.all { permittedChars.contains(it) }) {
            throw IllegalArgumentException("Required chars must be permitted")
        }
    }
}

fun generatePw(spec: PasswordSpecification, masterPw: String): String {
    val siteBase = masterPw + spec.siteName
    val byteMap = (spec.permittedChars + genLowers(64 - spec.permittedChars.length)).toByteArray()

    var versionCounter = 0
    var iterationCounter = 0

    while (true) {
        // Compatibility with old system -- don't tack on a postfix unless version > 1 or
        // requirements not met.
        val postfix = if (iterationCounter > 0) iterationCounter.toString() else ""

        val digest = MessageDigest.getInstance("SHA-256")
        val output = digest.digest((siteBase + postfix).toByteArray())

        val potentialPassword = encodeToBase64(output, byteMap).substring(0 until spec.pwLength)
        if (spec.requiredChars.all { potentialPassword.contains(it) }) {
            versionCounter++
            if (versionCounter >= spec.pwVersion) {
                return potentialPassword
            }
        }
        iterationCounter++
    }
}

fun encodeToBase64(bytes: ByteArray, map: ByteArray): String {
    val base64Chars = mutableListOf<Byte>()
    for (i in 0 until bytes.size step 3) {
        val byte1 = bytes[i].toInt()
        val byte2 = if (i < bytes.size - 1) bytes[i + 1].toInt() else 0
        val byte3 = if (i < bytes.size - 2) bytes[i + 2].toInt() else 0

        val triplet = (byte1 shl 16) or (byte2 shl 8) or byte3

        (0..3).takeWhile { i + it * 0.75 < bytes.size }
                .mapTo(base64Chars) { map[((triplet ushr (6 * (3 - it))) and 0x3f)] }
    }

    // Add padding
    if (map.size > 64) {
        val paddingChar = map[64]
        while (base64Chars.size % 4 != 0) {
            base64Chars.add(paddingChar)
        }
    }

    return String(base64Chars.toByteArray())
}

fun genLowers(length: Int): String {
    val result = CharArray(length)
    for (i in 0 until length) {
        result[i] = ((i % 26) + 97).toChar()
    }
    return String(result)
}

fun createInitialCharMap(uppers: Boolean, numbers: Boolean, symbols: Boolean): String {
    var result : String = ""
    if (uppers) {
        result += UPPERS
    }
    if (numbers) {
        result += NUMBERS
    }
    if (symbols) {
        result += SYMBOLS
    }
    return result
}