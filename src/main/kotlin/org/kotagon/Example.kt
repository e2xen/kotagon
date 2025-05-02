package org.kotagon

import org.kotagon.lock.UnaryLock


class Customer
object Paid : UnaryLock<Customer>()

object AnyCustomer : Policy({
    any<Customer>() // same as suchThat<Customer> { true }
})
object PaidCustomer : Policy({
    suchThat<Customer> { customer -> Paid(customer) }
})

class CustomerData(var data: String, var softwareKey: String?)

class SpecificCustomer(customer: Customer) : Policy({ +customer })

class CustomerStorage {
    fun getCustomerData(customer: Customer): Labeled<SpecificCustomer, CustomerData> {
        return labeled(SpecificCustomer(customer)) { CustomerData("name", null) }
    }
}

class KeySeller {
    val customerStorage: CustomerStorage = CustomerStorage()
    val softwareKey: Labeled<PaidCustomer, String> = labeled(PaidCustomer) { "secretKey" }

    fun getSoftwareKey(customer: Customer) {
        val customerData = customerStorage.getCustomerData(customer)
        if (processPayment(customer)) {
            Paid.open(customer)
        } else {
            //...
        }
        withPolicyEvaluationContext(Paid(customer)) {
            customerData.map {
                it.softwareKey = softwareKey.get()
            }
        }
    }

    private fun processPayment(customer: Customer): Boolean {
        return true
    }
}
