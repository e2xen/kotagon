package org.kotagon

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

object AnyString : Policy({
    any<String>()
})

class LabeledTest {

    @Test
    fun testAllowedToFlowSamePolicy() {
        withPolicyEvaluationContext {
            assertTrue(allowedToFlow(AnyString, AnyString))
        }
    }
}
