package world.respect.shared.domain.testing

import io.ktor.http.Url

data class DbFileForUpload(
    val filename: String,
    val bytes: ByteArray,
)

interface GetDbFilesForUploadUseCase {
    suspend operator fun invoke(schoolUrl: Url): DbFileForUpload?
}
