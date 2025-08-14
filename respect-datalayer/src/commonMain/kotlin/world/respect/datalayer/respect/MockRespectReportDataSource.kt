package world.respect.datalayer.respect

import androidx.paging.PagingState
import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import world.respect.datalayer.DataErrorResult
import world.respect.datalayer.DataLoadMetaInfo
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.respect.model.Indicator
import world.respect.datalayer.respect.model.RespectReport

class MockRespectReportDataSource : RespectReportDataSource {
    private val mockReports = mutableListOf(
        RespectReport(
            reportId = "1",
            title = "Weekly Duration",
            reportOptions = """
                {
                    "title": "Weekly Session Duration",
                    "xAxis": "WEEK",
                    "period": {
                        "type": "RelativeRangeReportPeriod",
                        "rangeUnit": "WEEK",
                        "rangeQuantity": 1
                    },
                    "series": [
                        {
                            "reportSeriesUid": 1,
                            "reportSeriesTitle": "Total Duration",
                            "reportSeriesYAxis": {
                                "name": "Total Duration",
                                "description": "Total content usage duration",
                                "sql": "SUM(duration)",
                                "type": "DURATION"
                            },
                            "reportSeriesVisualType": "BAR_CHART",
                            "reportSeriesSubGroup": "GENDER"
                        }
                    ]
                }
            """.trimIndent(),
            reportIsTemplate = false
        ),
        RespectReport(
            reportId = "2",
            title = "Weekly Duration",
            reportOptions = """
                {
                    "title": "Weekly Session Duration",
                    "xAxis": "WEEK",
                    "period": {
                        "type": "RelativeRangeReportPeriod",
                        "rangeUnit": "WEEK",
                        "rangeQuantity": 1
                    },
                    "series": [
                        {
                            "reportSeriesUid": 1,
                            "reportSeriesTitle": "Total Duration",
                            "reportSeriesYAxis": {
                                "name": "Total Duration",
                                "description": "Total content usage duration",
                                "sql": "SUM(duration)",
                                "type": "DURATION"
                            },
                            "reportSeriesVisualType": "BAR_CHART",
                            "reportSeriesSubGroup": "GENDER"
                        }
                    ]
                }
            """.trimIndent(),
            reportIsTemplate = false
        ),
        RespectReport(
            reportId = "2",
            title = "Weekly Duration",
            reportOptions = """
                {
                    "title": "Weekly Session Duration",
                    "xAxis": "WEEK",
                    "period": {
                        "type": "RelativeRangeReportPeriod",
                        "rangeUnit": "WEEK",
                        "rangeQuantity": 1
                    },
                    "series": [
                        {
                            "reportSeriesUid": 1,
                            "reportSeriesTitle": "Total Duration",
                            "reportSeriesYAxis": {
                                "name": "Total Duration",
                                "description": "Total content usage duration",
                                "sql": "SUM(duration)",
                                "type": "DURATION"
                            },
                            "reportSeriesVisualType": "BAR_CHART",
                            "reportSeriesSubGroup": "GENDER"
                        }
                    ]
                }
            """.trimIndent(),
            reportIsTemplate = true
        ),
    )

    private val mockIndicators = mutableListOf(
        Indicator(
            name = "Total Duration",
            description = "Total content usage duration",
            sql = "SUM(duration)",
            type = ""
        ),
    )


    override suspend fun putReport(report: RespectReport) {
        val existingIndex = mockReports.indexOfFirst { it.reportId == report.reportId }
        if (existingIndex >= 0) {
            // Update existing report
            mockReports[existingIndex] = report
        } else {
            // Add new report with generated ID if needed
            val reportToAdd = if (report.reportId.isBlank()) {
                report.copy(reportId = (mockReports.size + 1).toString())
            } else {
                report
            }
            mockReports.add(reportToAdd)
        }
    }

    override fun getReportsPagingSource(): PagingSource<Int, RespectReport> {
        return object : PagingSource<Int, RespectReport>() {
            override suspend fun load(params: PagingSourceLoadParams<Int>): PagingSourceLoadResult<Int, RespectReport> {
                return _root_ide_package_.app.cash.paging.PagingSourceLoadResultPage(
                    data = mockReports,
                    prevKey = null,
                    nextKey = null
                )
            }

            override fun getRefreshKey(state: PagingState<Int, RespectReport>): Int? {
                return state.anchorPosition?.let { anchorPosition ->
                    state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                        ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
                }
            }
        }
    }

    override fun getTemplateReportsPagingSource(): PagingSource<Int, RespectReport> {
        return object : PagingSource<Int, RespectReport>() {
            override suspend fun load(params: PagingSourceLoadParams<Int>): PagingSourceLoadResult<Int, RespectReport> {
                val templateReports = mockReports.filter { it.reportIsTemplate }
                return _root_ide_package_.app.cash.paging.PagingSourceLoadResultPage(
                    data = templateReports,
                    prevKey = null,
                    nextKey = null
                )
            }

            override fun getRefreshKey(state: PagingState<Int, RespectReport>): Int? {
                return state.anchorPosition?.let { anchorPosition ->
                    state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                        ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
                }
            }
        }
    }

    override suspend fun saveIndicator(indicator: Indicator) {
        val existingIndex = mockIndicators.indexOfFirst { it.name == indicator.name }
        if (existingIndex >= 0) {
            // Update existing indicator
            mockIndicators[existingIndex] = indicator
        } else {
            // Add new indicator
            mockIndicators.add(indicator)
        }
    }

    override fun getAllIndicators(): Flow<List<Indicator>> {
        return flowOf(mockIndicators.toList())
    }

    override suspend fun getIndicatorById(id: String): Indicator? {
        return mockIndicators.firstOrNull { it.indicatorId == id }

    }

    override fun getAllReportsAsFlow(): Flow<DataLoadState<RespectReport>> {
        return flowOf(
            DataReadyState(
                data = RespectReport(
                    reportId = "all",
                    title = "All Reports Combined",
                    reportOptions = """{"reports": ${mockReports.map { it.reportId }}}""",
                    reportIsTemplate = false
                ),
                metaInfo = DataLoadMetaInfo()
            )
        )
    }

    override fun getReportAsFlow(reportId: String): Flow<DataLoadState<RespectReport>> {
        val report = mockReports.firstOrNull { it.reportId == reportId }
        return flowOf(
            if (report != null) {
                DataReadyState(
                    data = report,
                    metaInfo = DataLoadMetaInfo()
                )
            } else {
                DataErrorResult(
                    error = NoSuchElementException("Report not found"),
                    metaInfo = DataLoadMetaInfo()
                )
            }
        )
    }
}


class EmptyPagingSource<Key : Any, Value : Any> : PagingSource<Key, Value>() {
    // TODO: Consider if this is needed in production or just for testing
    override fun getRefreshKey(state: PagingState<Key, Value>): Key? = null

    override suspend fun load(params: PagingSourceLoadParams<Key>): PagingSourceLoadResult<Key, Value> {
        return _root_ide_package_.app.cash.paging.PagingSourceLoadResultPage<Key, Value>(
            emptyList(),
            null,
            null
        ) as PagingSourceLoadResult<Key, Value>
    }
}