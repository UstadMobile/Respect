package world.respect.domain.getfavicons

import io.ktor.http.Url
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class GetFavIconsUseCaseImpl: GetFavIconUseCase {

    /**
     * Get the favicon(s) for a given URL using JSoup
     */
    override suspend fun invoke(url: Url): List<GetFavIconUseCase.FavIcon> {
        val doc: Document = Jsoup.connect(url.toString()).get()

        return doc.select("link[rel=icon]").mapNotNull {
            val sizesVal = it.attr("sizes").ifBlank { null }
            val sizesSplit = sizesVal?.split("x", limit = 2)

            GetFavIconUseCase.FavIcon(
                url = it.absUrl("href"),
                type = it.attr("type").ifBlank { null },
                sizes = sizesVal,
                width = sizesSplit?.firstOrNull()?.toIntOrNull(),
                height = sizesSplit?.getOrNull(1)?.toIntOrNull(),
            )
        }

    }
}