package world.respect.lib.xapi.resources

import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.model.XapiDocument
import kotlin.time.Instant

/**
 * Xapi Activity Profile resource : implements the Document Resource for Activity Profiles as per:
 *
 *  https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#27-activity-profile-resource
 *
 */
interface XapiActivityProfileResource {

    data class MultiDocParams(
        val activityId: String,
        val since: Instant? = null,
    )

    data class DocumentParams(
        val activityId: String,
        val profileId: String,
    )

    /**
     * Get a list of available profiles as per
     *
     * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#multiple-document-get-2
     */
    suspend fun getMultipleDocuments(
        params: MultiDocParams,
        dataLoadParams: DataLoadParams = DataLoadParams()
    ): DataLoadState<List<String>>


    /**
     * Get a specific xAPI profile document as per
     *
     * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#single-document-put--post--get--delete-1
     */
    suspend fun get(
        params: DocumentParams,
        dataLoadParams: DataLoadParams = DataLoadParams(),
    ): DataLoadState<XapiDocument>

    /**
     * Post (e.g. update/insert) an xAPI activity profile document as per
     *
     * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#single-agent-or-profile-document-put--post--get--delete
     *
     * As per the xAPI spec: because this is a POST request, if the content-type is application/json
     * it MUST merge the posted document with the existing document. Only top level properties are
     * merged. The entire contents of each original property are replaced with the entire contents
     * of each new property.
     */
    suspend fun post(
        params: DocumentParams,
        document: XapiDocument
    )

    /**
     * Put (e.g. update/insert) an xAPI activity profile document as per
     *
     * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#single-agent-or-profile-document-put--post--get--delete
     *
     * As per the xAPI spec: because this is a PUT request, there will be NO merge of the posted
     * document. Any existing document will be overwritten.
     *
     */
    suspend fun put(
        params: DocumentParams,
        document: XapiDocument
    )

    /**
     * Delete an xAPI activity profile document as per
     *
     * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#single-agent-or-profile-document-put--post--get--delete
     */
    suspend fun delete(
        params: DocumentParams
    )

}