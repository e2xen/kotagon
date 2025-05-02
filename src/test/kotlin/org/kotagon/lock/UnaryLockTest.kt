package org.kotagon.lock

import org.junit.jupiter.api.Test
import org.kotagon.Customer
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private object Paid : UnaryLock<Customer>()

class UnaryLockTest {

    @Test
    fun testOpenLock() {
        val customer = Customer()
        Paid.open(customer)
        assertTrue(Paid.isOpen(customer))
        assertFalse(Paid.isClosed(customer))
    }

    @Test
    fun testOpenLock2() {
        val customer = Customer()
        Paid.open(customer)
        assertTrue(Paid.isOpen(customer))
        assertFalse(Paid.isClosed(customer))

        // Test for some random customer
        assertFalse(Paid.isOpen(Customer()))
    }

    @Test
    fun testClosedLockByDefault() {
        val customer = Customer()
        assertFalse(Paid.isOpen(customer))
        assertTrue(Paid.isClosed(customer))
    }

    @Test
    fun testCloseWithoutOpenLock() {
        val customer = Customer()
        Paid.close(customer)
        assertFalse(Paid.isOpen(customer))
        assertTrue(Paid.isClosed(customer))
    }

    @Test
    fun testCloseAfterOpenLock() {
        val customer = Customer()
        Paid.open(customer)
        Paid.close(customer)
        assertFalse(Paid.isOpen(customer))
        assertTrue(Paid.isClosed(customer))
    }
}