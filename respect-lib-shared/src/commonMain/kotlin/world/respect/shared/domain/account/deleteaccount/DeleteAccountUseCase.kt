package world.respect.shared.domain.account.deleteaccount

interface DeleteAccountUseCase {
    suspend operator fun invoke(guid: String): Boolean
}