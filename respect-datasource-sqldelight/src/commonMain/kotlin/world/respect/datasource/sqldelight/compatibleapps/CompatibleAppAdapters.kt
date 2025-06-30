package world.respect.datasource.sqldelight.compatibleapps

import com.eygraber.uri.Uri
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.datasource.DataLoadMetaInfo
import world.respect.datasource.DataLoadResult
import world.respect.datasource.LoadingStatus
import world.respect.datasource.compatibleapps.model.RespectAppManifest
import world.respect.datasource.opds.model.LangMapObjectValue
import world.respect.datasource.opds.model.LangMapStringValue
import world.respect.datasource.sqldelight.CompatibleAppEntity

fun DataLoadResult<RespectAppManifest>.asCompatibleAppEntity(
    json: Json,
) : CompatibleAppEntity? {
    val manifest = data ?: return null

    return CompatibleAppEntity(
        caeUid = metaInfo.requireUrl().toString().hashCode().toLong(),
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
    )
}


fun CompatibleAppEntity.asRespectManifestLoadResult(
    json: Json,
): DataLoadResult<RespectAppManifest> {
    return DataLoadResult(
        data = RespectAppManifest(
            name = LangMapObjectValue(mapOf("name-${this.caeWebsite}" to "foo")),
            description = LangMapStringValue("here"),
            license = caeLicense,
            website = caeWebsite.takeIf { it.isNotBlank() }?.let { Url(it) },
            learningUnits = Uri.parse(caeLearningUnits),
            defaultLaunchUri = Uri.parse(caeDefaultLaunchUri),
            android = caeAndroidPackageId?.let { androidPackageId ->
                RespectAppManifest.AndroidDetails(
                    packageId = androidPackageId,
                    sourceCode = caeAndroidSourceCode?.let { Url(it) },
                    stores = caeAndroidStoreList?.let { json.decodeFromString(it) } ?: emptyList()
                )
            },
        ),
        metaInfo = DataLoadMetaInfo(
            status = LoadingStatus.LOADED,
            lastModified = caeLastModified,
            etag = caeEtag,
            url = Url(caeUrl),
        )
    )
}
