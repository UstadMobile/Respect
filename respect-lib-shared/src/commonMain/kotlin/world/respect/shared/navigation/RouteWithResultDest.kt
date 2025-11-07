package world.respect.shared.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed interface ResultDest{
    val resultKey: String


    companion object {

        fun fromStringOrNull(str: String?) : ResultDest? {
            return str?.let { Json.decodeFromString(serializer(), it) }
        }

    }
}

fun ResultDest?.encodeToJsonStringOrNull() : String? {
    return this?.let { Json.encodeToString(ResultDest.serializer(), it) }
}

@Serializable
data class ResultDestClass(
    val resultPopUpTo: KClassAsString,
    override val resultKey: String,
): ResultDest

@Serializable
data class ResultDestRoute(
    val resultPopUpTo: RespectAppRoute,
    override val resultKey: String,
): ResultDest

/**
 * Interface used by a RespectAppRoute that can include arguments used to return a pick result that
 * can then be collected e.g. where a user may navigate through multiple screens to pick a person,
 * lesson, etc and return to the screen they came from.
 */
interface RouteWithResultDest {

    val resultDest: ResultDest?

}