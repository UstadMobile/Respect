package world.respect.shared.domain.account.setpassword

import world.respect.datalayer.school.model.PersonPassword

interface EncryptPersonPasswordUseCase {

    data class Request(
        val personGuid: String,
        val password: String,
    )

    operator fun invoke(
        request: Request,
    ): PersonPassword

}