package com.hallliu.passwordgenerator

import org.junit.Assert
import org.junit.Test
import java.util.regex.Pattern

class PasswordGenerationTest {
    @Test(expected = PasswordMisspecificationException::class)
    fun testImpossibleSpec() {
        val spec = PasswordSpecification(
                siteName = "test_site",
                pwLength = 16,
                permittedChars = "",
                requirements = listOf(Pattern.compile("=")),
                pwVersion = 1)
        generatePw(spec, "irrelevant")
    }

    @Test
    fun testVersionDifference() {
        val masterPw = "test password"
        val spec1 = PasswordSpecification(
                siteName = "test_site",
                pwLength = 4,
                permittedChars = "",
                requirements = emptyList(),
                pwVersion = 1)
        val firstPassword = generatePw(spec1, masterPw)
        val secondPassword = generatePw(spec1.copy(pwVersion = 2), masterPw)
        Assert.assertNotEquals(firstPassword, secondPassword)
    }

    @Test
    fun testForCompatibility() {
        val masterPw = "test password 123"
        val spec1 = PasswordSpecification(
                siteName = "www.example1.com",
                pwLength = 16,
                permittedChars = UPPERS + NUMBERS,
                requirements = emptyList(),
                pwVersion = 1)
        Assert.assertEquals("aSXP09hZS6It8iys", generatePw(spec1, masterPw))
        val spec2 = spec1.copy(pwVersion = 2)
        Assert.assertEquals("3xcp8Vu18T92dUVf", generatePw(spec2, masterPw))
        val spec3 = spec1.copy(siteName = "www.example2.com",
                permittedChars = UPPERS + NUMBERS + SYMBOLS)
        Assert.assertEquals("@8%cad^VO5X}<GY<", generatePw(spec3, masterPw))
    }

    @Test
    fun testRequirementsFunction() {
        val masterPw = "test password 123"
        val spec = PasswordSpecification(
                siteName = "www.example1.com",
                pwLength = 32,
                permittedChars = UPPERS + NUMBERS,
                requirements = listOf(Pattern.compile("F$")),
                pwVersion = 1)
        for (i in 1..10) {
            Assert.assertTrue(generatePw(spec.copy(pwVersion = i), masterPw).endsWith("F"))
        }
    }

    @Test
    fun testGenLowers() {
        val lowers = "abcdefghijklmnopqrstuvwxyz".repeat(10)
        for (length in 1..64) {
            Assert.assertEquals(lowers.slice(0..length-1), genLowers(length))
        }
    }
}