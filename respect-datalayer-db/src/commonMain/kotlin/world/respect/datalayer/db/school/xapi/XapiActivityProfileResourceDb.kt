package world.respect.datalayer.db.school.xapi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.xapi.adapters.toXapiActivityProfileDocumentEntity
import world.respect.datalayer.db.shared.InstantAsTimestampString
import world.respect.datalayer.school.xapi.XapiActivityProfileResourceLocal
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.ext.mergeTopLevel
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.model.XapiDocumentByteArrayImpl
import world.respect.lib.xapi.resources.XapiActivityProfileResource

class XapiActivityProfileResourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val json: Json,
) : XapiActivityProfileResourceLocal {

    override suspend fun updateLocal(
        params: XapiActivityProfileResource.ActivityProfileDocumentParams,
        document: XapiDocument
    ) {
        val existing = schoolDb.getActivityProfileDocumentDao().findByActivityIriAndProfileId(
            activityIri = params.activityId,
            profileId = params.profileId,
        )
        val entity = document.toXapiActivityProfileDocumentEntity(
            params = params,
            id = existing?.id,
        )
        schoolDb.getActivityProfileDocumentDao().upsert(entity)
    }

    override suspend fun getMultipleDocuments(
        params: XapiActivityProfileResource.GetActivityProfilesParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<List<String>> {
        val profileIds = schoolDb.getActivityProfileDocumentDao().getProfileIds(
            activityIri = params.activityId,
            since = params.since?.let { InstantAsTimestampString(it) },
        )
        return DataReadyState(profileIds)
    }

    override suspend fun get(
        params: XapiActivityProfileResource.ActivityProfileDocumentParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<XapiDocument> {
        val entity = schoolDb.getActivityProfileDocumentDao().findByActivityIriAndProfileId(
            activityIri = params.activityId,
            profileId = params.profileId,
        )
        return if (entity != null) {
            DataReadyState(entity)
        } else {
            NoDataLoadedState.notFound()
        }
    }

    override suspend fun post(
        params: XapiActivityProfileResource.ActivityProfileDocumentParams,
        document: XapiDocument
    ) {
        if(!document.type.startsWith("application/json"))
            throw XapiException(400, "Cannot post non-JSON document")

        val existing = schoolDb.getActivityProfileDocumentDao().findByActivityIriAndProfileId(
            activityIri = params.activityId,
            profileId = params.profileId,
        )

        if(existing?.type?.startsWith("application/json") == false)
            throw XapiException(400, "Cannot post when there is an existing non-JSON document")

        schoolDb.getActivityProfileDocumentDao().upsert(
            entity = if(existing != null) {
                XapiDocumentByteArrayImpl(
                    type = document.type,
                    updated = document.updated,
                    contents = json.parseToJsonElement(existing.contents.decodeToString())
                        .jsonObject.mergeTopLevel(
                            other = json.parseToJsonElement(
                                document.contentsAsByteArray().decodeToString()
                            ).jsonObject
                        ).let { mergedObj ->
                            json.encodeToString(
                                JsonObject.serializer(), mergedObj
                            ).encodeToByteArray()
                        }
                ).toXapiActivityProfileDocumentEntity(
                    params = params,
                    id = existing.id,
                )
            }else {
                document.toXapiActivityProfileDocumentEntity(params)
            }
        )
    }

    override suspend fun put(
        params: XapiActivityProfileResource.ActivityProfileDocumentParams,
        document: XapiDocument
    ) {
        val existing = schoolDb.getActivityProfileDocumentDao().findByActivityIriAndProfileId(
            activityIri = params.activityId,
            profileId = params.profileId,
        )
        val entity = document.toXapiActivityProfileDocumentEntity(
            params = params,
            id = existing?.id,
        )
        schoolDb.getActivityProfileDocumentDao().upsert(entity)
    }

    override suspend fun delete(params: XapiActivityProfileResource.ActivityProfileDocumentParams) {
        schoolDb.getActivityProfileDocumentDao().deleteByActivityIriAndProfileId(
            activityIri = params.activityId,
            profileId = params.profileId,
        )
    }
}