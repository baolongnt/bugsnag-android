package com.bugsnag.android

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
internal class StackframeSerializationTest {

    companion object {
        @JvmStatic
        @Parameters
        fun testCases() =
            generateSerializationTestCases(
                "stackframe",
                Stackframe("foo", "Bar", 55, true)
            )
    }

    @Parameter
    lateinit var testCase: Pair<Stackframe, String>

    @Test
    fun testJsonSerialisation() = verifyJsonMatches(testCase.first, testCase.second)
}
