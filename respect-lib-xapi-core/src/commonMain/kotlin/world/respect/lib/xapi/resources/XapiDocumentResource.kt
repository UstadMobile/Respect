package world.respect.lib.xapi.resources

import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.model.XapiDocument

/**
 * Generic interface for Experience API document resources including the activity profile resource,
 * the agent profile resource, and the state resource.
 *
 *
 * @param MultiDocParams the params class that is used to get a list of available document ids
 * @param SingleDocParams the params class that is used to get, post, put, or delete a single
 *        document.
 */
interface XapiDocumentResource<MultiDocParams: Any, SingleDocParams: Any> {

    /**
     * Get a list of available document ids e.g. stateIds, profileIds etc.
     */
    suspend fun getMultipleDocuments(
        params: MultiDocParams,
        dataLoadParams: DataLoadParams = DataLoadParams()
    ): DataLoadState<List<String>>


    /**
     * Get a single document
     */
    suspend fun get(
        params: SingleDocParams,
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
        params: SingleDocParams,
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
        params: SingleDocParams,
        document: XapiDocument
    )

    /**
     * Delete an xAPI activity profile document as per
     *
     * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#single-agent-or-profile-document-put--post--get--delete
     */
    suspend fun delete(
        params: SingleDocParams
    )

}