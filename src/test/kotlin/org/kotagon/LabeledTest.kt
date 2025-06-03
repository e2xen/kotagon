package org.kotagon

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test

object AnyStringPolicy : Policy({
    any<String>()
})

object ConcreteStringPolicy : Policy({
    "ConcreteTestString"
})

class LabeledTest {

    @Test
    @DisplayName("The information flow is allowed to the same policy (same object)")
    fun testAllowedToFlowSamePolicy1() {
        withPolicyEvaluationContext {
            assertTrue(allowedToFlow(AnyStringPolicy, AnyStringPolicy))
        }
    }

    @Test
    @DisplayName("The information flow is allowed to the same policy (different objects)")
    fun testAllowedToFlowSamePolicy2() {
        val anotherAnyStringPolicy = object : Policy({
            any<String>()
        }) {}

        withPolicyEvaluationContext {
            assertTrue(allowedToFlow(AnyStringPolicy, anotherAnyStringPolicy))
        }
    }

    @Test
    @DisplayName("The information flow is allowed to the same policy (different objects)")
    fun testAllowedToFlowSamePolicy3() {
        val anotherAnyStringPolicy = object : Policy({
            any(String::class)
        }) {}

        withPolicyEvaluationContext {
            assertTrue(allowedToFlow(AnyStringPolicy, anotherAnyStringPolicy))
        }
    }

    @Test
    @DisplayName("The information can flow to more restrictive policy")
    fun testAllowedToFlowStrictPolicy() {
        withPolicyEvaluationContext {
            assertTrue(allowedToFlow(AnyStringPolicy, ConcreteStringPolicy))
        }
    }

    @Test
    @DisplayName("The information cannot flow to more liberal policy")
    fun testNotAllowedToFlowGeneralPolicy() {
        withPolicyEvaluationContext {
            assertFalse(allowedToFlow(ConcreteStringPolicy, AnyStringPolicy))
        }
    }
}
