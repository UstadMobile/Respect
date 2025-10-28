package world.respect.datalayer.db.shared.ext

import world.respect.datalayer.db.school.entities.ReportQueryResult

/**
 * Determine the age of the report query results
 *
 * @param sinceTimestamp the timestamp to compare against (ms since epoch)
 * @return the age (as per http) of the report (in seconds since timestamp)
 */
fun List<ReportQueryResult>.age(sinceTimestamp: Long): Int {
    return (firstOrNull()?.rqrLastModified?.let {
        sinceTimestamp - it
    }?.toInt() ?: 0) / 1000
}

