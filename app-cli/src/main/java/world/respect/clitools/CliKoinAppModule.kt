package world.respect.clitools

import org.koin.dsl.module
import world.respect.clitools.domain.testlaunchableapp.RunLearningUnitTestUseCase
import world.respect.clitools.domain.testlaunchableapp.SelectRandomPublicationUseCase
import world.respect.clitools.domain.testlaunchableapp.TestLaunchableAppUseCaseImpl
import world.respect.shared.domain.testlaunchableapp.TestLaunchableAppUseCase

val cliKoinAppModule = module {
    single<SelectRandomPublicationUseCase> {
        SelectRandomPublicationUseCase(
            httpClient = get(),
            json = get(),
        )
    }

    single<RunLearningUnitTestUseCase> {
        RunLearningUnitTestUseCase()
    }

    single<TestLaunchableAppUseCase>(createdAtStart = true) {
        TestLaunchableAppUseCaseImpl(
            selectRandomPublicationUseCase = get(),
            runLearningUnitTestUseCase = get(),
            httpClient = get(),
        )
    }
}
