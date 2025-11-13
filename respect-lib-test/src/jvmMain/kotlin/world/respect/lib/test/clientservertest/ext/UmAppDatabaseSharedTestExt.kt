package world.respect.lib.test.clientservertest.ext

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.entities.xapi.StatementEntity
import java.util.UUID

const val DEFAULT_NUM_STATEMENTS_PER_DAY = 2
const val DEFAULT_DURATION_PER_STATEMENT = 2_000L
const val DEFAULT_NUM_DAYS = 3
const val DEFAULT_STATEMENT_CLAZZ_UID = 42L

data class StatementsInsertedInfo(
    val statements: List<StatementEntity>,
)

/**
 * Insert statements that are used for report tests.
 */
suspend fun RespectSchoolDatabase.insertStatementsPerDay(
    numStatementsPerDay: Int = DEFAULT_NUM_STATEMENTS_PER_DAY,
    durationPerStatement: Long = DEFAULT_DURATION_PER_STATEMENT,
    numDays: Int = DEFAULT_NUM_DAYS,
    statementClazzUid: (index: Int) -> Long = { DEFAULT_STATEMENT_CLAZZ_UID },
): StatementsInsertedInfo {
    val today = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.UTC)

    val statementList = (0 until numDays).flatMap { dayIndex ->
        //Adding 24 hours does not always get to the same time next day e.g. when daylight
        // savings time changes. Use LocalDateTime to workaround this.
        val timestamp = LocalDateTime(
            today.date.minus(DatePeriod(days = dayIndex)), today.time
        ).toInstant(TimeZone.UTC)

        (1..numStatementsPerDay).map { statementNum ->
            val statementUid =  UUID.randomUUID()
            StatementEntity(
                statementIdHi = statementUid.mostSignificantBits,
                statementIdLo = statementUid.leastSignificantBits,
                timestamp = timestamp.toEpochMilliseconds(),
                resultDuration = durationPerStatement,
                statementClazzUid = statementClazzUid(statementNum),
            )
        }
    }


    statementDao().insertOrIgnoreListAsync(statementList)
    return StatementsInsertedInfo(statementList)
}
