package org.kotagon.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.kotagon.Labeled
import org.kotagon.Policy
import org.kotagon.exception.InformationFlowException
import org.kotagon.labeled
import org.kotagon.lock.BinaryLock
import org.kotagon.withPolicyEvaluationContext
import java.io.File

private class Member(val name: String)
private class SpecificMember(member: Member) : Policy({ +member })

private object Read : BinaryLock<Member, File>()
private object Write : BinaryLock<Member, File>()

private class ReadFile(f: File) : Policy({
    suchThat<Member> { Write(it, f) or Read(it, f) }
})
private class WriteFile(f: File) : Policy({
    suchThat<Member> { Write(it, f) }
})

private class FileManager {

    fun writeFile(m: Member, s: Labeled<WriteFile, String>, f: File) {
        val memberBuf = labeled(SpecificMember(m)) {""}
        withPolicyEvaluationContext(Write(m, f)) {
            memberBuf.accept(s)
            f.writeText(memberBuf.unsafeGet())
        }
    }

    fun readFile(f: File): Labeled<ReadFile, String> {
        return labeled(ReadFile(f)) {
            f.readLines().joinToString("\n")
        }
    }

}

class SimpleFileManagerTest {
    private val file: File = File("test.txt")

    private lateinit var readMember: Member
    private lateinit var writeMember: Member
    private lateinit var fileManager: FileManager

    @BeforeEach
    fun before() {
        fileManager = FileManager()
        readMember = Member("read")
        Read.open(readMember, file)
        writeMember = Member("write")
        Write.open(writeMember, file)
    }

    @Test
    fun testReadMemberRead() {
        val member = readMember
        val content = fileManager.readFile(file)
        val memberBuf = labeled(SpecificMember(member)) {""}
        withPolicyEvaluationContext(Read(member, file) or Write(member, file)) {
            memberBuf.accept(content)
        }
        println(memberBuf.unsafeGet())
    }

    @Test
    fun testReadMemberWrite() {
        val member = readMember
        assertThrows<InformationFlowException> {
            val content = labeled(WriteFile(file)) {"i cannot write"}
            fileManager.writeFile(member, content, file)
        }
    }

    @Test
    fun testWriteMemberRead() {
        val member = writeMember
        val content = fileManager.readFile(file)
        val memberBuf = labeled(SpecificMember(member)) {""}
        withPolicyEvaluationContext(Read(member, file) or Write(member, file)) {
            memberBuf.accept(content)
        }
        println(memberBuf.unsafeGet())
    }

    @Test
    fun testWriteMemberWrite() {
        val member = writeMember
        val content = labeled(WriteFile(file)) {"i can write"}
        fileManager.writeFile(member, content, file)
    }

}