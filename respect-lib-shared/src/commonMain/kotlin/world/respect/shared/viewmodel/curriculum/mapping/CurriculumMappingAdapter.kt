package world.respect.shared.viewmodel.curriculum.mapping

//Add functions that convert CurriculumMapping to OpdsFeed and vice versa. See the adapters in the
//database module

//e.g. have CurriculumMapping.toOpds (convert from CurriculumMapping data class to Opds)
// and OpdsFeed.toCurriculumMapping (convert from OpdsFeed to CurriculumMapping)

import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMappingSection
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMappingSectionLink
import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Serializable
data class OpdsFeed(
    val metadata: OpdsMetadata,
    val links: List<OpdsLink> = emptyList(),
    val groups: List<OpdsGroup> = emptyList()
)

@Serializable
data class OpdsMetadata(
    val title: String = ""
)

@Serializable
data class OpdsLink(
    val rel: String,
    val href: Url,
    val type: String = "application/opds+json"
)

@Serializable
data class OpdsGroup(
    val metadata: OpdsMetadata,
    val navigation: List<OpdsNavigationLink> = emptyList()
)

@Serializable
data class OpdsNavigationLink(
    val href: Url,
    val title: String= "",
    val type: String = "application/opds+json",
    val rel: String = "related"
)

fun CurriculumMapping.toOpds(selfLink: Url = Url("http://example.com/grouped")): OpdsFeed {
    return OpdsFeed(
        metadata = OpdsMetadata(title = this.title),
        links = listOf(
            OpdsLink(
                rel = "self",
                href = selfLink,
                type = "application/opds+json"
            )
        ),
        groups = this.sections.map { section ->
            OpdsGroup(
                metadata = OpdsMetadata(title = section.title),
                navigation = section.items.map { item ->
                    OpdsNavigationLink(
                        href = item.href,
                        title = item.title!!,
                        type = "application/opds+json",
                        rel = "related"
                    )
                }
            )
        }
    )
}

fun OpdsFeed.toCurriculumMapping(): CurriculumMapping {
    return CurriculumMapping(
        title = this.metadata.title,
        sections = this.groups.map { group ->
            CurriculumMappingSection(
                title = group.metadata.title,
                items = group.navigation.map { navLink ->
                    CurriculumMappingSectionLink(
                        href = navLink.href,
                        title = navLink.title
                    )
                }
            )
        }
    )
}