package world.respect.shared.navigation

import kotlinx.coroutines.flow.Flow

/**
 * NavResultReturner is responsible for "returning" results via the Navigation e.g. where one screen
 * returns a result to another screen.
 *
 */
interface NavResultReturner {

    fun resultFlowForKey(key: String): Flow<NavResult>

    fun sendResult(result: NavResult)

}