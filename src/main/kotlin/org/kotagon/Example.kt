package org.kotagon

import org.kotagon.Paid

class Customer
object Paid : UnaryLock<Customer>()

object AnyCustomer : Policy({
    any<Customer>() // same as suchThat<Customer> { true }
})
object PaidCustomer : Policy({
    suchThat<Customer> { customer -> Paid(customer) }
})

class KeySeller {
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

    private fun processPayment(customer: Customer): Boolean {
        TODO("Not yet implemented")
    }
}
