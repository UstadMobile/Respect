package world.respect.datalayer.db.school.domain.report.query

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.entities.xapi.StatementEntity
import java.util.UUID
import kotlin.time.Clock

class InsertReportTestDataUseCase(
    private val schoolDatabase: RespectSchoolDatabase
) {
    suspend operator fun invoke() {
        try {
            val today = Clock.System.now().toLocalDateTime(TimeZone.UTC)

            val statementList = (0 until 5).flatMap { dayIndex ->
                val timestamp = LocalDateTime(
                    today.date.minus(DatePeriod(days = dayIndex)), today.time
                ).toInstant(TimeZone.UTC).toEpochMilliseconds()

                listOf(
                    // Different durations for testing
                    createStatement(timestamp, duration = 1000L, clazzUid = 42L, success = true),
                    createStatement(timestamp, duration = 2000L, clazzUid = 43L, success = true),
                    createStatement(timestamp, duration = 3000L, clazzUid = 44L, success = false),
                    createStatement(timestamp, duration = 1500L, clazzUid = 42L, success = true),
                    createStatement(timestamp, duration = 2500L, clazzUid = 43L, success = true),
                )
            }

            schoolDatabase.statementDao().insertOrIgnoreListAsync(statementList)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createStatement(
        timestamp: Long,
        duration: Long,
        clazzUid: Long,
        success: Boolean
    ): StatementEntity {
        val statementUid = UUID.randomUUID()
        return StatementEntity(
            statementIdHi = statementUid.mostSignificantBits,
            statementIdLo = statementUid.leastSignificantBits,
            timestamp = timestamp,
            resultDuration = duration,
            resultSuccess = success,
            statementClazzUid = clazzUid,
            // Add other required fields with default values
            statementActorPersonUid = 1001L,
            statementVerbUid = 1L,
            statementObjectType = 1,
            statementObjectUid1 = 1L,
            statementObjectUid2 = 0L,
            statementActorUid = 1L,
            authorityActorUid = 1L,
            teamUid = 0L,
            resultCompletion = true,
            resultScoreScaled = 0.8f,
            resultScoreRaw = 80f,
            resultScoreMin = 0f,
            resultScoreMax = 100f,
            resultResponse = null,
            stored = System.currentTimeMillis(),
            contextRegistrationHi = 0L,
            contextRegistrationLo = 0L,
            contextRegistrationHash = 0L,
            contextPlatform = "Test",
            contextStatementRefIdHi = 0L,
            contextStatementRefIdLo = 0L,
            contextInstructorActorUid = 1L,
            statementLct = System.currentTimeMillis(),
            extensionProgress = 100,
            completionOrProgress = true,
            statementContentEntryUid = 1L,
            statementLearnerGroupUid = 0L,
            statementCbUid = 1L,
            statementDoorNode = 0L,
            isSubStatement = false
        )
    }
}