package world.respect

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import world.respect.app.datasource.RespectAppDataSourceProvider
import world.respect.app.datasource.fakeds.FakeRespectAppDataSourceProvider
import world.respect.app.viewmodel.apps.detail.AppsDetailViewModel
import world.respect.app.viewmodel.apps.enterlink.EnterLinkViewModel
import world.respect.app.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.app.viewmodel.apps.list.AppListViewModel
import world.respect.app.viewmodel.assignments.AssignmentViewModel
import world.respect.app.viewmodel.clazz.ClazzViewModel
import world.respect.app.viewmodel.lessons.detail.LessonDetailViewModel
import world.respect.app.viewmodel.lessons.list.LessonListViewModel
import world.respect.app.viewmodel.report.ReportViewModel


val appKoinModule = module {
    singleOf(::DummyRepoImpl) {
        bind<DummyRepo>()
    }

    viewModelOf(::AppsDetailViewModel)
    viewModelOf(::AppLauncherViewModel)
    viewModelOf(::EnterLinkViewModel)
    viewModelOf(::AppListViewModel)
    viewModelOf(::AssignmentViewModel)
    viewModelOf(::ClazzViewModel)
    viewModelOf(::LessonListViewModel)
    viewModelOf(::LessonDetailViewModel)
    viewModelOf(::ReportViewModel)

    single<RespectAppDataSourceProvider> {
        FakeRespectAppDataSourceProvider()
    }

}
