package world.respect.shared.domain.account.child



interface GetClassUseCase {
    suspend operator fun invoke(classUid:String): String
}