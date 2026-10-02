package world.respect.clitools.domain.testlaunchableapp

import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsFeedMetadata
import world.respect.lib.opds.model.OpdsGroup
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SelectRandomPublicationUseCaseTest {

    private val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
    }

    private val baseFeed = OpdsFeed(
        metadata = OpdsFeedMetadata(title = "Base Feed"),
        links = emptyList(),
    )

    private val basePublication = Publication(
        metadata = ReadiumMetadata(title = LangMapStringValue("Base Publication")),
        links = emptyList(),
    )

    private fun Application.installServerJson() {
        install(ServerContentNegotiation) {
            json()
        }
    }

    @Test
    fun givenFeedWithPublicationsInGroups_whenInvoked_thenReturnsPublication() = testApplication {
        application {
            installServerJson()

            routing {
                get("/grouped.json") {
                    call.respond(
                        baseFeed.copy(
                            groups = listOf(
                                OpdsGroup(
                                    metadata = OpdsFeedMetadata(title = "Featured"),
                                    publications = listOf(basePublication),
                                )
                            )
                        )
                    )
                }
            }
        }

        val result = SelectRandomPublicationUseCase(
            httpClient = client,
            json = json,
        ).invoke(
            SelectRandomPublicationUseCase.Request(
                Url("/grouped.json")
            )
        )

        assertEquals(basePublication, result.publication)
        assertTrue(result.clickPath.isEmpty())
    }

    @Test
    fun givenFeedWithPublications_whenInvoked_thenReturnsRandomPublicationAndEmptyClickPath() = testApplication {
        val pub1 = basePublication.copy(metadata = ReadiumMetadata(title = LangMapStringValue("Publication 1")))
        val pub2 = basePublication.copy(metadata = ReadiumMetadata(title = LangMapStringValue("Publication 2")))

        application {
            installServerJson()

            routing {
                get("/feed.json") {
                    call.respond(
                        baseFeed.copy(
                            metadata = OpdsFeedMetadata(title = "Root Feed"),
                            publications = listOf(pub1, pub2),
                        )
                    )
                }
            }
        }

        val result = SelectRandomPublicationUseCase(
            httpClient = client,
            json = json,
            random = Random(0),
        ).invoke(
            SelectRandomPublicationUseCase.Request(
                Url("/feed.json")
            )
        )

        assertTrue(result.publication == pub1 || result.publication == pub2)
        assertTrue(result.clickPath.isEmpty())
    }

    @Test
    fun givenFeedWithNavigationOnly_whenInvoked_thenFollowsLinkAndReturnsPublicationWithClickPath() = testApplication {
        application {
            installServerJson()

            routing {
                get("/opds/root.json") {
                    call.respond(
                        baseFeed.copy(
                            metadata = OpdsFeedMetadata(title = "Main Menu"),
                            navigation = listOf(
                                ReadiumLink(
                                    href = "math.json",
                                    type = OpdsFeed.MEDIA_TYPE,
                                    title = "Math",
                                )
                            )
                        )
                    )
                }
                get("/opds/math.json") {
                    call.respond(
                        baseFeed.copy(
                            metadata = OpdsFeedMetadata(title = "Math Category"),
                            publications = listOf(basePublication),
                        )
                    )
                }
            }
        }

        val result = SelectRandomPublicationUseCase(
            httpClient = client,
            json = json,
        ).invoke(
            SelectRandomPublicationUseCase.Request(
                Url("/opds/root.json")
            )
        )

        assertEquals(basePublication, result.publication)
        assertEquals(1, result.clickPath.size)
        assertEquals("Math", result.clickPath[0].text)
        assertEquals("Main Menu", result.clickPath[0].feed.metadata.title)
    }

    @Test
    fun givenMultiLevelNavigation_whenInvoked_thenTraversesHierarchyAndRecordsAllClickSteps() = testApplication {
        application {
            installServerJson()

            routing {
                get("/catalog/root.json") {
                    call.respond(
                        baseFeed.copy(
                            metadata = OpdsFeedMetadata(title = "Home"),
                            navigation = listOf(
                                ReadiumLink(
                                    href = "math/index.json",
                                    type = OpdsFeed.MEDIA_TYPE,
                                    title = "Math Section",
                                )
                            )
                        )
                    )
                }
                get("/catalog/math/index.json") {
                    call.respond(
                        baseFeed.copy(
                            metadata = OpdsFeedMetadata(title = "Math"),
                            navigation = listOf(
                                ReadiumLink(
                                    href = "geometry.json",
                                    type = OpdsFeed.MEDIA_TYPE,
                                    title = "Geometry",
                                )
                            )
                        )
                    )
                }
                get("/catalog/math/geometry.json") {
                    call.respond(
                        baseFeed.copy(
                            metadata = OpdsFeedMetadata(title = "Geometry"),
                            publications = listOf(basePublication),
                        )
                    )
                }
            }
        }

        val result = SelectRandomPublicationUseCase(
            httpClient = client,
            json = json,
        ).invoke(
            SelectRandomPublicationUseCase.Request(
                Url("/catalog/root.json")
            )
        )

        assertEquals(basePublication, result.publication)
        assertEquals(2, result.clickPath.size)
        assertEquals("Math Section", result.clickPath[0].text)
        assertEquals("Home", result.clickPath[0].feed.metadata.title)
        assertEquals("Geometry", result.clickPath[1].text)
        assertEquals("Math", result.clickPath[1].feed.metadata.title)
    }

    @Test
    fun givenFeedWithNeitherPublicationsNorNavigation_whenInvoked_thenThrowsException() = testApplication {
        application {
            installServerJson()

            routing {
                get("/empty.json") {
                    call.respond(baseFeed)
                }
            }
        }

        assertFailsWith<IllegalStateException> {
            SelectRandomPublicationUseCase(
                httpClient = client,
                json = json,
            ).invoke(
                SelectRandomPublicationUseCase.Request(
                    Url("/empty.json")
                )
            )
        }
    }
}
