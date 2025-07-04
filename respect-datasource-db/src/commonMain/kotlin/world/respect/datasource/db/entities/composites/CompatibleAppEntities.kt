package world.respect.datasource.db.entities.composites

import world.respect.datasource.db.entities.CompatibleAppEntity
import world.respect.datasource.db.entities.LangMapEntity

/**
 * All the entities required to represent a RespectAppManifest.
 */
data class CompatibleAppEntities(
    val compatibleAppEntity: CompatibleAppEntity,
    val langMapEntities: List<LangMapEntity>,
)
