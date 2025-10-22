package world.respect.shared.navigation

import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Send a result back using NavResultReturner and pop the navigatoin stack if the given route
 * expects a result to be sent back.
 *
 * @return true if a result was sent, false otherwise
 */
fun NavResultReturner.sendResultIfResultExpected(
    route: RouteWithResultDest,
    navCommandFlow: MutableSharedFlow<NavCommand>,
    result: Any?
): Boolean {
    val popUpTo = route.resultPopUpTo
    val resultKey = route.resultKey
    return if(popUpTo != null && resultKey != null) {
        sendResult(
            NavResult(
                key = resultKey,
                result = result,
            )
        )
        navCommandFlow.tryEmit(
            NavCommand.PopToRouteClass(
                destination = popUpTo,
                inclusive = false,
            )
        )
        true
    }else {
        false
    }
}