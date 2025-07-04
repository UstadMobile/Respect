package world.respect.datasource.db.adapters

import com.eygraber.uri.Uri
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.datasource.DataLoadMetaInfo
import world.respect.datasource.DataLoadResult
import world.respect.datasource.LoadingStatus
import world.respect.datasource.compatibleapps.model.RespectAppManifest
import world.respect.datasource.db.entities.CompatibleAppEntity
import world.respect.datasource.db.entities.CompatibleAppEntity.Companion.LANGMAP_PROP_DESC
import world.respect.datasource.db.entities.CompatibleAppEntity.Companion.LANGMAP_PROP_NAME
import world.respect.datasource.db.entities.composites.CompatibleAppEntities
import world.respect.libxxhash.XXStringHasher

fun DataLoadResult<RespectAppManifest>.asCompatibleAppEntities(
    json: Json,
    xxStringHasher: XXStringHasher,
) : CompatibleAppEntities? {
    val manifest = data ?: return null
    val caeUid = xxStringHasher.hash(metaInfo.requireUrl().toString())

    return CompatibleAppEntities(
        compatibleAppEntity = CompatibleAppEntity(
            caeUid = caeUid,
            caeUrl = metaInfo.requireUrl().toString(),
            caeLastModified = metaInfo.lastModified,
            caeEtag = metaInfo.etag,
            caeLicense = manifest.license,
            caeWebsite = manifest.website?.toString() ?: "",
            caeLearningUnits = manifest.learningUnits.toString(),
            caeAndroidPackageId = manifest.android?.packageId,
            caeAndroidStoreList = manifest.android?.stores?.map { it.toString() }?.let {
                json.encodeToString(it)
            },
            caeDefaultLaunchUri = manifest.defaultLaunchUri.toString(),
            caeAndroidSourceCode = manifest.android?.sourceCode?.toString(),
        ),
        langMapEntities = manifest.name.toEntities(
            lmeTableId = CompatibleAppEntity.TABLE_ID,
            lmeEntityUid1 = caeUid,
            lmePropId = LANGMAP_PROP_NAME,
        ) + (manifest.description?.toEntities(
            lmeTableId = CompatibleAppEntity.TABLE_ID,
            lmeEntityUid1 = caeUid,
            lmePropId = LANGMAP_PROP_DESC,
        ) ?: emptyList()),
    )
}


fun CompatibleAppEntities.asRespectManifestLoadResult(
    json: Json,
): DataLoadResult<RespectAppManifest> {
    return DataLoadResult(
        data = RespectAppManifest(
            name = langMapEntities.filter { it.lmePropId == LANGMAP_PROP_NAME }.toLangMap(),
            description = langMapEntities.filter { it.lmePropId == LANGMAP_PROP_DESC }.toLangMap(),
            license = compatibleAppEntity.caeLicense,
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
            status = LoadingStatus.LOADED,
            lastModified = compatibleAppEntity.caeLastModified,
            etag = compatibleAppEntity.caeEtag,
            url = Url(compatibleAppEntity.caeUrl),
        )
    )
}
