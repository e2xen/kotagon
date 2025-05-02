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