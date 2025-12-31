package world.respect.shared.viewmodel.curriculum.mapping

//Add functions that convert CurriculumMapping to OpdsFeed and vice versa. See the adapters in the
//database module

//e.g. have CurriculumMapping.toOpds (convert from CurriculumMapping data class to Opds)
// and OpdsFeed.toCurriculumMapping (convert from OpdsFeed to CurriculumMapping)

import world.respect.lib.opds.model.LangMap
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsFeedMetadata
import world.respect.lib.opds.model.OpdsGroup
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMappingSection
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMappingSectionLink


fun CurriculumMapping.toOpds(selfLink: String): OpdsFeed {
    return OpdsFeed(
        metadata = OpdsFeedMetadata(
            title = this.title,
            description = this.description
        ),
        links = listOf(
            ReadiumLink(
                rel = listOf("self"),
                href = selfLink,
                type = OpdsFeed.MEDIA_TYPE
            )
        ),
        groups = this.sections.map { section ->
            OpdsGroup(
                metadata = OpdsFeedMetadata(title = section.title),
                navigation = section.items.map { item ->
                    ReadiumLink(
                        href = item.href,
                        title = item.title,
                        type = OpdsFeed.MEDIA_TYPE,
                        rel = listOf("related")
                    )
                }
            )
        }
    )
}

fun CurriculumMapping.toOpdsGroup(): OpdsGroup {
    return OpdsGroup(
        metadata = OpdsFeedMetadata(
            title = this.title
        ),
        publications = this.sections.flatMap { section ->
            section.items.map { link ->
                OpdsPublication(
                    metadata = ReadiumMetadata(
                        title = mapOf("en" to (link.title ?: "")) as LangMap,
                    ),
                    links = listOfNotNull(
                        ReadiumLink(
                            href = link.href,
                            rel = listOf("http://opds-spec.org/acquisition"),
                        ),
                        link.appManifestUrl?.let {
                            ReadiumLink(
                                href = it.toString(),
                                rel = listOf("http://opds-spec.org/compatible-app"),
                            )
                        }
                    )
                )
            }
        }
    )
}

fun OpdsFeed.toCurriculumMapping(): CurriculumMapping {
    return CurriculumMapping(
        uid = System.currentTimeMillis(),
        title = this.metadata.title,
        description = this.metadata.description ?: "",
        sections = this.groups?.map { group ->
            CurriculumMappingSection(
                uid = System.currentTimeMillis(),
                title = group.metadata.title,
                items = group.navigation?.map { navLink ->
                    CurriculumMappingSectionLink(
                        uid = System.currentTimeMillis(),
                        href = navLink.href,
                        title = navLink.title ?: ""
                    )
                } ?: emptyList()
            )
        } ?: emptyList()
    )
}