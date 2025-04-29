package org.kotagon

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.kotagon.exception.InformationFlowException

private class SimpleKeySeller(val processPayment: (Customer) -> Boolean) {
    val customerData: Labeled<AnyCustomer, String> = labeled(AnyCustomer) { "" }
    val softwareKey: Labeled<PaidCustomer, String> = labeled(PaidCustomer) { "secretKey" }

    fun getSoftwareKey(customer: Customer) {
        if (processPayment(customer)) {
            Paid.open(customer)
        } else {
            //...
        }
        withPolicyEvaluationContext(Paid(customer)) {
            customerData.accept(softwareKey) // legal data flow
        }
    }
}

class SoftwareKeyTest {

    @Test
    fun testLegalFlow() {
        val keySeller = SimpleKeySeller { true }
        keySeller.getSoftwareKey(Customer())
    }

    @Test
    fun testIllegalFlow() {
        val keySeller = SimpleKeySeller { false }
        assertThrows (InformationFlowException::class.java, {
            keySeller.getSoftwareKey(Customer())
        })
    }

    // TODO: add test after Paid.close(customer)

}