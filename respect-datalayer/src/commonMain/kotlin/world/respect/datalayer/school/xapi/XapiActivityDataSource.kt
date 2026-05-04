package world.respect.datalayer.school.xapi

import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.model.XapiActivity


/**
 * As per the Xapi spec:
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#25-activities-resource
 */
interface XapiActivityDataSource {

    suspend fun get(activityId: String): DataLoadState<XapiActivity>

}