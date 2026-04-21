package world.respect.datalayer.school.xapi

import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.xapi.model.XapiActivity


/**
 * As per the Xapi spec:
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#25-activities-resource
 */
interface XapiActivityDataSource {

    suspend fun get(activityId: String): DataLoadState<XapiActivity>

}