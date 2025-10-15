package world.respect.datalayer.db.compatibleapps.adapters

import com.eygraber.uri.Uri
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.json.Json
import world.respect.datalayer.DataLoadMetaInfo
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.db.shared.adapters.asEntities
import world.respect.datalayer.db.shared.adapters.toModel
import world.respect.datalayer.db.compatibleapps.entities.CompatibleAppEntity
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.libxxhash.XXStringHasher
import world.respect.datalayer.db.shared.entities.LangMapEntity.PropType.RESPECT_MANIFEST_NAME
import world.respect.datalayer.db.shared.entities.LangMapEntity.PropType.RESPECT_MANIFEST_DESCRIPTION

/**
 * All the entities required to represent a RespectAppManifest.
 */
data class CompatibleAppEntities(
    val compatibleAppEntity: CompatibleAppEntity,
    val langMapEntities: List<LangMapEntity>,
)

fun DataReadyState<RespectAppManifest>.asEntities(
    json: Json,
    xxStringHasher: XXStringHasher,
) : CompatibleAppEntities? {
    val caeUid = xxStringHasher.hash(metaInfo.requireUrl().toString())
    return CompatibleAppEntities(
        compatibleAppEntity = CompatibleAppEntity(
            caeUid = caeUid,
            caeUrl = metaInfo.requireUrl(),
            caeIcon = data.icon,
            caeLastModified = metaInfo.lastModified,
            caeEtag = metaInfo.etag,
            caeLicense = data.license,
            caeWebsite = data.website?.toString() ?: "",
            caeLearningUnits = data.learningUnits.toString(),
            caeAndroidPackageId = data.android?.packageId,
            caeAndroidStoreList = data.android?.stores?.map { it.toString() }?.let {
                json.encodeToString(it)
            },
            caeDefaultLaunchUri = data.defaultLaunchUri.toString(),
            caeAndroidSourceCode = data.android?.sourceCode?.toString(),
        ),
        langMapEntities = data.name.asEntities(
            lmeTopParentType = LangMapEntity.TopParentType.RESPECT_MANIFEST,
            lmeTopParentUid1 = caeUid,
            lmePropType= RESPECT_MANIFEST_NAME,
            lmePropFk = 0,
        ) + (data.description?.asEntities(
            lmeTopParentType = LangMapEntity.TopParentType.RESPECT_MANIFEST,
            lmeTopParentUid1 = caeUid,
            lmePropType = RESPECT_MANIFEST_DESCRIPTION,
            lmePropFk = 0,
        ) ?: emptyList()),
    )
}

fun CompatibleAppEntity.asModel(
    langMapEntities: List<LangMapEntity>,
    json: Json,
): DataReadyState<RespectAppManifest>? {
    return CompatibleAppEntities(
        compatibleAppEntity = this,
        langMapEntities = langMapEntities,
    ).asModel(
        json = json
    )
}

fun CompatibleAppEntities.asModel(
    json: Json,
): DataReadyState<RespectAppManifest> {
    return DataReadyState(
        data = RespectAppManifest(
            name = langMapEntities.filter { it.lmePropType == RESPECT_MANIFEST_NAME }.toModel(),
            description = langMapEntities.filter { it.lmePropType == RESPECT_MANIFEST_DESCRIPTION }.toModel(),
            license = compatibleAppEntity.caeLicense,
            icon = compatibleAppEntity.caeIcon,
            website = compatibleAppEntity.caeWebsite.takeIf { it.isNotBlank() }?.let { Url(it) },
            learningUnits = Uri.parse(compatibleAppEntity.caeLearningUnits),
            defaultLaunchUri = Uri.parse(compatibleAppEntity.caeDefaultLaunchUri),
            android = compatibleAppEntity.caeAndroidPackageId?.let { androidPackageId ->
                RespectAppManifest.AndroidDetails(
                    packageId = androidPackageId,
                    sourceCode = compatibleAppEntity.caeAndroidSourceCode?.let { Url(it) },
                    stores = compatibleAppEntity.caeAndroidStoreList?.let {
                        json.decodeFromString(it)
                    } ?: emptyList()
                )
            },
        ),
        metaInfo = DataLoadMetaInfo(
            lastModified = compatibleAppEntity.caeLastModified,
            etag = compatibleAppEntity.caeEtag,
            url =compatibleAppEntity.caeUrl,
        )
    )
}

fun Flow<List<CompatibleAppEntity>>.combineWithLangMaps(
    langmaps: Flow<List<LangMapEntity>>,
    json: Json,
): Flow<DataLoadState<List<DataLoadState<RespectAppManifest>>>> {
    return combine(langmaps) { appEntities, langmaps ->
        DataReadyState(
            data = appEntities.map { appEntity ->
                appEntity.asModel(
                    langMapEntities = langmaps.filter { it.lmeTopParentUid1 == appEntity.caeUid },
                    json = json
                )
                CompatibleAppEntities(
                    compatibleAppEntity = appEntity,
                    langMapEntities = langmaps.filter { it.lmeTopParentUid1 == appEntity.caeUid }
                ).asModel(json)
            },
            metaInfo = DataLoadMetaInfo(
                lastModified = appEntities.maxOfOrNull { it.caeLastModified } ?: 0,
                etag = null,
                url = null,
            )
        )
    }
}
