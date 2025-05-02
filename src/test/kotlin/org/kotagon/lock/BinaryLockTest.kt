package org.kotagon.lock

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class User
private class Document

private object ReadDoc : BinaryLock<User, Document>()

class BinaryLockTest {

    @Test
    fun testOpenLock() {
        val user = User()
        val doc = Document()
        ReadDoc.open(user, doc)
        assertTrue(ReadDoc.isOpen(user, doc))
        assertFalse(ReadDoc.isClosed(user, doc))
    }

    @Test
    fun testOpenLock2() {
        val user = User()
        val doc = Document()
        ReadDoc.open(user, doc)
        assertTrue(ReadDoc.isOpen(user, doc))
        assertFalse(ReadDoc.isClosed(user, doc))

        // Test for some random customer
        assertFalse(ReadDoc.isOpen(User(), Document()))
    }

    @Test
    fun testClosedLockByDefault() {
        val user = User()
        val doc = Document()
        assertFalse(ReadDoc.isOpen(user, doc))
        assertTrue(ReadDoc.isClosed(user, doc))
    }

    @Test
    fun testCloseWithoutOpenLock() {
        val user = User()
        val doc = Document()
        ReadDoc.close(user, doc)
        assertFalse(ReadDoc.isOpen(user, doc))
        assertTrue(ReadDoc.isClosed(user, doc))
    }

    @Test
    fun testCloseAfterOpenLock() {
        val user = User()
        val doc = Document()
        ReadDoc.open(user, doc)
        ReadDoc.close(user, doc)
        assertFalse(ReadDoc.isOpen(user, doc))
        assertTrue(ReadDoc.isClosed(user, doc))
    }
}