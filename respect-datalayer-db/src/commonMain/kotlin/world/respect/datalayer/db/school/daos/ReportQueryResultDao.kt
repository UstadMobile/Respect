package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import world.respect.datalayer.db.school.entities.ReportQueryResult

@Dao
interface ReportQueryResultDao {

    @Query("""
        DELETE FROM ReportQueryResult
         WHERE rqrReportUid = :reportUid
           AND rqrTimeZone = :timeZone
    """)
    suspend fun deleteByReportUidAndTimeZone(reportUid: Long, timeZone: String)

    @Insert
    suspend fun insertAllAsync(results: List<ReportQueryResult>)


    @Query("""
        SELECT ReportQueryResult.*
          FROM ReportQueryResult
         WHERE ReportQueryResult.rqrReportUid = :reportUid 
           AND ReportQueryResult.rqrTimeZone = :timeZone
    """)
    suspend fun getAllByReportUidAndTimeZone(
        reportUid: Long,
        timeZone: String
    ): List<ReportQueryResult>

    /**
     * Determine if a previous report run is fresh (as the term is used in caching). This checks
     * that a) the results were generated after the report was last modified AND b) the result was
     * generated after the freshThresholdTime (eg does not exceed max age).
     *
     * @param reportUid reportUid
     * @param freshThresholdTime minimum timestamp for ReportQueryResult to be considered fresh
     */
    @Query("""
        SELECT COALESCE(
               (SELECT ReportQueryResult.rqrLastModified
                  FROM ReportQueryResult
                 WHERE ReportQueryResult.rqrReportUid = :reportUid
                   AND ReportQueryResult.rqrTimeZone = :timeZone
                 LIMIT 1), 0) >= 
               (SELECT MAX(:freshThresholdTime, 
                            (SELECT COALESCE(
                                    (SELECT ReportEntity.rLastModified
                                       FROM ReportEntity
                                      WHERE ReportEntity.rGuid = :reportUid), 0))))
    """,)
    suspend fun isReportFresh(
        reportUid: Long,
        timeZone: String,
        freshThresholdTime: Long,
    ): Boolean

    @RawQuery
    suspend fun executeRawQuery(query: RoomRawQuery): Int

}