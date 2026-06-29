package world.respect.shared.domain.testing

import io.ktor.http.Url

interface SendDbToServerUseCase {

    suspend operator fun invoke(schoolUrl: Url)

}
