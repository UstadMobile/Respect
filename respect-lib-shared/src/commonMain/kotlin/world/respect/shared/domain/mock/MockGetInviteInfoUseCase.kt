package world.respect.shared.domain.mock

import world.respect.datalayer.oneroster.model.OneRosterClassGUIDRef
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import com.eygraber.uri.Uri
import io.ktor.http.Url
import world.respect.datalayer.opds.model.LangMapStringValue
import world.respect.datalayer.respect.model.SchoolDirectoryEntry

class MockGetInviteInfoUseCase : GetInviteInfoUseCase {
    override suspend fun invoke(code: String): RespectInviteInfo {
        return RespectInviteInfo(
            code = code,
            school = SchoolDirectoryEntry(
                name = LangMapStringValue("School"),
                self = Url("https://example.org/respect-realm.json"),
                xapi = Url("https://example.org/xapi"),
                oneRoster = Url("https://example.org/oneroster"),
                respectExt = Url("https://example.org/respect-ext"),
            ),
            classGUIDRef = OneRosterClassGUIDRef(
                href = Uri.parse("https://mockserver.respect.world/class/123"),
                sourcedId = "mock-class-123",
                type = OneRosterClassGUIDRef.ClassGUIDRefTypeEnum.CLASS
            ),
            className = "Grade-4",
            schoolName = "Spix Foundation School",
            userInviteType = RespectInviteInfo.UserInviteType.STUDENT_OR_PARENT
        )
    }
}
