package world.respect.datalayer.db.realmdirectory.adapters

import world.respect.datalayer.DataLoadMetaInfo
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.db.realmdirectory.entities.RealmEntity
import world.respect.datalayer.db.shared.adapters.asEntities
import world.respect.datalayer.db.shared.adapters.toModel
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.respect.model.RespectRealm
import world.respect.libxxhash.XXStringHasher

data class RespectRealmEntities(
    val realm: RealmEntity,
    val langMapEntities: List<LangMapEntity>,
)

fun DataReadyState<RespectRealm>.toEntities(
    xxStringHasher: XXStringHasher,
): RespectRealmEntities {
    val reUid = xxStringHasher.hash(data.self.toString())
    return RespectRealmEntities(
        realm = RealmEntity(
            reUid = reUid,
            reSelf = data.self,
            reXapi = data.xapi,
            reOneRoster = data.oneRoster,
            reLastMod = metaInfo.lastModified,
            reEtag = metaInfo.etag,
            reRespectExt = data.respectExt,
            reRpId = data.rpId
        ),
        langMapEntities = data.name.asEntities(
            lmeTopParentType = LangMapEntity.TopParentType.RESPECT_REALM,
            lmeTopParentUid1 = reUid,
            lmePropType = LangMapEntity.PropType.RESPECT_REALM_NAME,
            lmePropFk = 0,
        )
    )
}

fun RespectRealmEntities.toModel() : DataReadyState<RespectRealm> {
    return DataReadyState(
        data = RespectRealm(
            self = realm.reSelf,
            xapi = realm.reXapi,
            oneRoster = realm.reOneRoster,
            respectExt = realm.reRespectExt,
            name = langMapEntities.toModel(),
            rpId = realm.reRpId,
        ),
        metaInfo = DataLoadMetaInfo(
            lastModified = realm.reLastMod,
            etag = realm.reEtag,
        )
    )
}
