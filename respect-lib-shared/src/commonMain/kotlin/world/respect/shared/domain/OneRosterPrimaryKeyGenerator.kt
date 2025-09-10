package world.respect.shared.domain


import world.respect.datalayer.oneroster.model.OneRosterClass
import world.respect.lib.primarykeygen.PrimaryKeyGenerator

/**
 * Wrapper class used only for purposes of differentiating it for dependency injection purposes
 */
data class OneRosterPrimaryKeyGenerator(
    val primaryKeyGenerator: PrimaryKeyGenerator
) {
    companion object {
        val TABLE_IDS = listOf(
            OneRosterClass.TABLE_ID
        )
    }
}