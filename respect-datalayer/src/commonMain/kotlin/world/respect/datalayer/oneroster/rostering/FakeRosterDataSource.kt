package world.respect.datalayer.oneroster.rostering

import com.eygraber.uri.Uri
import world.respect.datalayer.oneroster.rostering.model.OneRosterClass
import world.respect.datalayer.oneroster.rostering.model.OneRosterEnrollment
import world.respect.datalayer.oneroster.rostering.model.OneRosterOrgGUIDRef
import world.respect.datalayer.oneroster.rostering.model.OneRosterRole
import world.respect.datalayer.oneroster.rostering.model.OneRosterRoleEnum
import world.respect.datalayer.oneroster.rostering.model.OneRosterRoleTypeEnum
import world.respect.datalayer.oneroster.rostering.model.OneRosterUser
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class FakeRosterDataSource : OneRosterRosterDataSource {

    override suspend fun getAllUsers(): List<OneRosterUser> {
        val now = Instant.DISTANT_PAST
        val org = OneRosterOrgGUIDRef(
            href = Uri.parse("https://example.org/orgs/org1"),
            sourcedId = "org1",
            type = OneRosterOrgGUIDRef.OrgGUIDRefTypeEnum.ORG
        )

        val teacherRole = OneRosterRole(
            roleTypeEnum = OneRosterRoleTypeEnum.PRIMARY,
            role = OneRosterRoleEnum.TEACHER,
            org = org
        )

        val studentRole = OneRosterRole(
            roleTypeEnum = OneRosterRoleTypeEnum.PRIMARY,
            role = OneRosterRoleEnum.STUDENT,
            org = org
        )
        return listOf(
            // Teachers
            OneRosterUser(
                sourcedId = "t1",
                dateLastModified = now,
                givenName = "Alice",
                familyName = "Anderson",
                roles = listOf(teacherRole),
                username = "alice.anderson",
                email = "alice@example.com",
                enabledUser = false
            ),
            OneRosterUser(
                sourcedId = "t2",
                dateLastModified = now,
                givenName = "Bob",
                familyName = "Brown",
                roles = listOf(teacherRole),
                username = "bob.brown",
                email = "bob@example.com"
            ),
            // Students
            OneRosterUser(
                sourcedId = "s1",
                dateLastModified = now,
                givenName = "Charlie",
                familyName = "Clark",
                roles = listOf(studentRole),
                username = "charlie.clark",
                email = "charlie@example.com"
            ),
            OneRosterUser(
                sourcedId = "s2",
                dateLastModified = now,
                givenName = "Diana",
                familyName = "Davis",
                roles = listOf(studentRole),
                username = "diana.davis",
                email = "diana@example.com",
                enabledUser = false
            ),
            OneRosterUser(
                sourcedId = "s3",
                dateLastModified = now,
                givenName = "Ethan",
                familyName = "Edwards",
                roles = listOf(studentRole),
                username = "ethan.edwards",
                email = "ethan@example.com"
            )
        )
    }

    override suspend fun putUser(user: OneRosterUser) {}

    private val classList = mutableListOf<OneRosterClass>()

    init {
        classList.addAll(
            listOf(
                OneRosterClass(
                    sourcedId = "1",
                    title = "Class 1",
                    dateLastModified = Instant.DISTANT_PAST,
                ),
                OneRosterClass(
                    sourcedId = "2",
                    title = "Class 2",
                    dateLastModified = Instant.DISTANT_PAST
                ),
                OneRosterClass(
                    sourcedId = "3",
                    title = "Class 3",
                    dateLastModified = Instant.DISTANT_PAST
                )
            )
        )
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun getAllClasses(): List<OneRosterClass> {
        return classList
    }
    override suspend fun getClassBySourcedId(sourcedId: String): OneRosterClass {
        return classList.first { it.sourcedId == sourcedId }
    }
    override suspend fun putClass(clazz: OneRosterClass) {
        val index = classList.indexOfFirst { it.sourcedId == clazz.sourcedId }
        if (index != -1) {
            classList[index] = clazz
        } else {
            classList.add(clazz)
        }
    }


    override suspend fun getEnrolmentsByClass(classSourcedId: String) {}

    override suspend fun putEnrollment(enrollment: OneRosterEnrollment) {}
}
