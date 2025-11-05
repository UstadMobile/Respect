package world.respect.datalayer.db.school.domain.report.query

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.domain.report.ext.withWriterTransaction
import world.respect.datalayer.db.school.domain.report.query.RunReportUseCase.Companion.reportQueryResultsToResultStatementReportRows
import world.respect.datalayer.db.school.entities.ReportQueryResult
import world.respect.datalayer.db.shared.ext.age
import world.respect.libutil.util.time.systemTimeInMillis

/**
 * Client side implementation of RunReportUseCase. The flow will return cached results from the
 * local database immediately when available. If cached results are not available, or no longer
 * fresh, then a request will be made to the server. Those updated results will be cached and then
 * emitted from the flow.
 */
class RunReportUseCaseClientImpl(
    private val db: RespectSchoolDatabase,
    private val httpClient: HttpClient,
    private val json: Json,
) : RunReportUseCase {

    override fun invoke(
        request: RunReportUseCase.RunReportRequest
    ): Flow<RunReportUseCase.RunReportResult> {
        return flow {
            val currentReportQueryResults = db.reportRunResultRowDao().getAllByReportUidAndTimeZone(
                request.reportUid, request.timeZone.id
            )

            emit(
                RunReportUseCase.RunReportResult(
                    timestamp = systemTimeInMillis(),
                    request = request,
                    results = reportQueryResultsToResultStatementReportRows(
                        queryResults = currentReportQueryResults,
                        request = request
                    ),
                    age = currentReportQueryResults.age(sinceTimestamp = systemTimeInMillis()),
                )
            )

            val isFresh = db.reportRunResultRowDao().isReportFresh(
                reportUid = request.reportUid,
                freshThresholdTime = systemTimeInMillis() - (request.maxFreshAge * 1000),
                timeZone = request.timeZone.id
            )

            if (!isFresh) {
                val rowsJsonText = try {
                    val response = httpClient.post {
                        url {
                            appendPathSegments("api/report/run")
                        }
                        contentType(ContentType.Application.Json)
                        setBody(json.encodeToString(RunReportUseCase.RunReportRequest.serializer(), request))
                    }

                    if (!response.status.isSuccess()) {
                        val errorBody = response.bodyAsText()
                        throw IOException("HTTP ${response.status}: ${errorBody.take(200)}")
                    }

                    val responseText = response.bodyAsText()
                    println("DEBUG: Server response: $responseText")
                    responseText
                } catch (e: IOException) {
                    emit(
                        RunReportUseCase.RunReportResult(
                            timestamp = systemTimeInMillis(),
                            request = request,
                            results = emptyList(),
                            age = 0,
                        )
                    )
                    return@flow
                }

                if (rowsJsonText.isBlank()) {
                    emit(
                        RunReportUseCase.RunReportResult(
                            timestamp = systemTimeInMillis(),
                            request = request,
                            results = emptyList(),
                            age = 0,
                        )
                    )
                    return@flow
                }

                val response = try {
                    json.decodeFromString<RunReportUseCase.RunReportResult>(rowsJsonText)
                } catch (e: Exception) {
                    emit(
                        RunReportUseCase.RunReportResult(
                            timestamp = systemTimeInMillis(),
                            request = request,
                            results = emptyList(),
                            age = 0,
                        )
                    )
                    return@flow
                }
                val lastModTime = systemTimeInMillis() - (response.age * 1000)

                val responseQueryResults = response.results.flatMapIndexed { index, rows ->
                    val requestSeries = request.reportOptions.series[index]

                    rows.map {
                        ReportQueryResult(
                            rqrReportUid = request.reportUid,
                            rqrLastModified = lastModTime,
                            rqrLastValidated = systemTimeInMillis(),
                            rqrReportSeriesUid = requestSeries.reportSeriesUid,
                            rqrYAxis = it.yAxis,
                            rqrXAxis = it.xAxis,
                            rqrSubgroup = it.subgroup,
                        )
                    }
                }

                db.withWriterTransaction {
                    db.reportRunResultRowDao().deleteByReportUidAndTimeZone(
                        request.reportUid, request.timeZone.id
                    )
                    db.reportRunResultRowDao().insertAllAsync(responseQueryResults)
                }

                emit(response)
            }
        }
    }
}