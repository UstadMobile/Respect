package world.respect.shared.domain.account.passkey

import world.respect.credentials.passkey.model.AaguidProviderData


interface LoadAaguidJsonUseCase{
    suspend operator fun invoke(): AaguidProviderData?
} 
