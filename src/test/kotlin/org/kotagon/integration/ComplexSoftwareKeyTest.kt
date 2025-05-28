package org.kotagon.integration

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.kotagon.Labeled
import org.kotagon.Policy
import org.kotagon.exception.InformationFlowException
import org.kotagon.labeled
import org.kotagon.withPolicyEvaluationContext


class CustomerData(var data: String, var softwareKey: String?)

class SpecificCustomer(customer: Customer) : Policy({ +customer })

class CustomerStorage {
    fun getCustomerData(customer: Customer): Labeled<SpecificCustomer, CustomerData> {
        return labeled(SpecificCustomer(customer)) { CustomerData("name", null) }
    }
}

class KeyGenerator {
    fun generateKey(): Labeled<PaidCustomer, String> {
        return labeled(PaidCustomer) { "secretKey" }
    }
}

private class ComplexKeySeller(val processPayment: (Customer) -> Boolean) {
    val customerStorage: CustomerStorage = CustomerStorage()
    val keyGenerator: KeyGenerator = KeyGenerator()

    fun getSoftwareKey(customer: Customer) {
        val customerData = customerStorage.getCustomerData(customer)
        if (processPayment(customer)) {
            Paid.open(customer)
        } else {
            //...
        }
        withPolicyEvaluationContext(Paid(customer)) {
            customerData.map {
                it.softwareKey = keyGenerator.generateKey().get()
            }
        }
    }
}

class ComplexSoftwareKeyTest {

    @Test
    fun testLegalFlow() {
        val keySeller = ComplexKeySeller { true }
        keySeller.getSoftwareKey(Customer())
    }

    @Test
    fun testIllegalFlow() {
        val keySeller = ComplexKeySeller { false }
        assertThrows (InformationFlowException::class.java) {
            keySeller.getSoftwareKey(Customer())
        }
    }
}
