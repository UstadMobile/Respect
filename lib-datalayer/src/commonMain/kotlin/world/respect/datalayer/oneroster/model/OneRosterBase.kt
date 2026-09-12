package world.respect.datalayer.oneroster.model

import kotlinx.serialization.json.JsonObject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * See OneRoster base class spec:
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Data_Base
 */
sealed interface OneRosterBase {

    val sourcedId: String

    val status: OneRosterBaseStatusEnum

    /**
     * As per https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#DataAttribute_Base_dateLastModified
     * last modified MUST be W3C profile of ISO 8601
     */
    @OptIn(ExperimentalTime::class)
    val dateLastModified: Instant

    val metadata: JsonObject?

}
