package org.kotagon.integration

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.kotagon.Labeled
import org.kotagon.Policy
import org.kotagon.exception.InformationFlowException
import org.kotagon.labeled
import org.kotagon.withPolicyEvaluationContext

private enum class Role {
    MANAGER,
    CTO,
    CEO;
}

// Define users and roles
private abstract class User(val name: String, val role: Role)
private class ManagerUser(name: String): User(name, Role.MANAGER)
private class CtoUser(name: String): User(name, Role.CTO)
private class CeoUser(name: String): User(name, Role.CEO)

// Define politics
private class SpecificUser(user: User) : Policy({ +user })
private object AnyEmployee: Policy({
    any(User::class)
})
private object AnyCLevelUser: Policy({
    any(CtoUser::class)
    any(CeoUser::class)
})

private data class CompanyStrategy(var data: String?)
private data class ProfileInfo(var data: String?)

private class HrPlatform {
    val companyStrategy: Labeled<AnyCLevelUser, String> = labeled(AnyCLevelUser) { "Secret strategy" }
    val profileStorage: Labeled<AnyEmployee, Map<Long, User>>

    constructor(profileStorage: Map<Long, User>) {
        this.profileStorage = labeled(AnyEmployee) { profileStorage }
    }

    fun getUserProfile(user: User, profileId: Long): Labeled<SpecificUser, ProfileInfo> {
        val result: Labeled<SpecificUser, ProfileInfo> = labeled(SpecificUser(user)) { ProfileInfo(null) }
        withPolicyEvaluationContext {
            result.map {
                it.data = profileStorage.get()[profileId]?.toString()
            }
        }
        return result
    }

    fun getCompanyStrategy(user: User): Labeled<SpecificUser, CompanyStrategy> {
        val result = labeled(SpecificUser(user)) { CompanyStrategy(null) }
        var res: String? = null
        withPolicyEvaluationContext {
            result.map {
                it.data = companyStrategy.get()
                res = companyStrategy.get()
            }
        }
        return result
    }
}


class HrPlatformTest {
    private val ceoUserId = 1L
    private val ctoUserId = 2L
    private val managerUserId = 3L
    private val profileStorage = buildMap<Long, User> {
        put(ceoUserId, CeoUser(""))
        put(ctoUserId, CtoUser(""))
        put(managerUserId, ManagerUser(""))
    }
    private val hrPlatform = HrPlatform(profileStorage)
    private val ceoUser = CeoUser("")
    private val ctoUser = CtoUser("")
    private val managerUser = ManagerUser("")

    @Test
    @DisplayName("CEO is allowed to access company strategy")
    fun testGetCompanyStrategyLegalFlow1() {
        hrPlatform.getCompanyStrategy(ceoUser)
    }

    @Test
    @DisplayName("CTO is allowed to access company strategy")
    fun testGetCompanyStrategyLegalFlow2() {
        hrPlatform.getCompanyStrategy(ctoUser)
    }

    @Test
    @DisplayName("Manager is not allowed to access company strategy")
    fun testGetCompanyStrategyIllegalFlow() {
        assertThrows(InformationFlowException::class.java) {
            hrPlatform.getCompanyStrategy(managerUser)
        }
    }

    @Test
    @DisplayName("Everyone is allowed to read ceo profile")
    fun testGetUserProfileCeo() {
        hrPlatform.getUserProfile(managerUser, ceoUserId)
        hrPlatform.getUserProfile(ctoUser, ceoUserId)
        hrPlatform.getUserProfile(ceoUser, ceoUserId)
    }

    @Test
    @DisplayName("Everyone is allowed to read cto profile")
    fun testGetUserProfileCto() {
        hrPlatform.getUserProfile(managerUser, ctoUserId)
        hrPlatform.getUserProfile(ctoUser, ctoUserId)
        hrPlatform.getUserProfile(ceoUser, ctoUserId)
    }

    @Test
    @DisplayName("Everyone is allowed to read manager profile")
    fun testGetUserProfileManager() {
        hrPlatform.getUserProfile(managerUser, managerUserId)
        hrPlatform.getUserProfile(ctoUser, managerUserId)
        hrPlatform.getUserProfile(ceoUser, managerUserId)
    }
}