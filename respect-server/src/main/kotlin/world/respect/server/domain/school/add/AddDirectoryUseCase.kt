package world.respect.server.domain.school.add

import io.ktor.http.Url
import org.koin.core.component.KoinComponent
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.server.domain.school.add.AddServerManagedDirectoryCallback.Companion.MAX_INVITE_PREFIX
import kotlin.random.Random

class AddDirectoryUseCase(
    private val directoryDataSource: SchoolDirectoryDataSourceLocal,
): KoinComponent {

    suspend operator fun invoke(
        url: Url
    ) {
        val randomInvitePrefix = Random.nextInt(1, MAX_INVITE_PREFIX)
        val invitePrefixStr = randomInvitePrefix.toString().padStart(
            MAX_INVITE_PREFIX.toString().length, '0'
        )
        directoryDataSource.insertDirectory(RespectSchoolDirectory(invitePrefixStr, url))

    }
}