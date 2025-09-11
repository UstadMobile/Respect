package world.respect.datalayer.repository.shared.paging

/**
 * Should change from argKey to using a list with names so it can be invalidated (similar to tanstack).
 */
class PagingSourceMediatorStore() {

    private val mediators = mutableMapOf<Int, DoorOffsetLimitRemoteMediator>()

    fun getOrCreateMediator(
        argKey: Int,
        mediatorFactory: () -> DoorOffsetLimitRemoteMediator
    ): DoorOffsetLimitRemoteMediator {
        return mediators.getOrPut(argKey, mediatorFactory)
    }

}