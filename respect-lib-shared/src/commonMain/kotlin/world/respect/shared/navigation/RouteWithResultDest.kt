package world.respect.shared.navigation

import kotlin.reflect.KClass

/**
 * Interface used by a RespectAppRoute that can include arguments used to return a pick result that
 * can then be collected e.g. where a user may navigate through multiple screens to pick a person,
 * lesson, etc and return to the screen they came from.
 */
interface RouteWithResultDest {

    val resultPopUpTo: KClass<*>?

    val resultKey: String?

}