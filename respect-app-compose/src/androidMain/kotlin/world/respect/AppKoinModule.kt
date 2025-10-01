@file:Suppress("UnusedImport")

package world.respect

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.ustadmobile.core.domain.storage.GetOfflineStorageOptionsUseCase
import com.ustadmobile.libcache.CachePathsProvider
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.db.ClearNeighborsCallback
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.okhttp.UstadCacheInterceptor
import com.ustadmobile.libcache.webview.OkHttpWebViewClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import world.respect.callback.AddSchoolDirectoryCallback
import world.respect.credentials.passkey.CheckPasskeySupportUseCase
import world.respect.credentials.passkey.CheckPasskeySupportUseCaseAndroidImpl
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.credentials.passkey.CreatePasskeyUseCaseAndroidChannelHost
import world.respect.credentials.passkey.CreatePasskeyUseCaseAndroidImpl
import world.respect.credentials.passkey.GetCredentialUseCase
import world.respect.credentials.passkey.GetCredentialUseCaseAndroidImpl
import world.respect.credentials.passkey.VerifyDomainUseCase
import world.respect.credentials.passkey.VerifyDomainUseCaseImpl
import world.respect.credentials.passkey.password.SavePasswordUseCase
import world.respect.credentials.passkey.request.CreatePublicKeyCredentialCreationOptionsJsonUseCase
import world.respect.credentials.passkey.request.CreatePublicKeyCredentialRequestOptionsJsonUseCase
import world.respect.credentials.passkey.request.EncodeUserHandleUseCase
import world.respect.credentials.passkey.request.GetAaguidAndProvider
import world.respect.credentials.password.SavePasswordUseCaseAndroidImpl
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.MIGRATION_2_3
import world.respect.datalayer.db.RespectAppDataSourceDb
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.SchoolDataSourceDb
import world.respect.datalayer.db.addCommonMigrations
import world.respect.datalayer.db.networkvalidation.ExtendedDataSourceValidationHelperImpl
import world.respect.datalayer.db.personPassword.GetPersonPassword
import world.respect.datalayer.db.personPassword.GetPersonPasswordDbImpl
import world.respect.datalayer.db.school.entities.PersonPasskeyEntity
import world.respect.datalayer.db.school.writequeue.RemoteWriteQueueDbImpl
import world.respect.datalayer.db.schooldirectory.SchoolDirectoryDataSourceDb
import world.respect.datalayer.http.RespectAppDataSourceHttp
import world.respect.datalayer.http.SchoolDataSourceHttp
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.RespectAppDataSourceRepository
import world.respect.datalayer.repository.SchoolDataSourceRepository
import world.respect.datalayer.repository.school.writequeue.DrainRemoteWriteQueueUseCase
import world.respect.datalayer.repository.school.writequeue.EnqueueDrainRemoteWriteQueueUseCaseAndroidImpl
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.school.writequeue.EnqueueDrainRemoteWriteQueueUseCase
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.libutil.ext.sanitizedForFilename
import world.respect.libxxhash.XXHasher64Factory
import world.respect.libxxhash.XXStringHasher
import world.respect.libxxhash.jvmimpl.XXHasher64FactoryCommonJvm
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.RespectAccountSchoolScopeLink
import world.respect.shared.domain.account.RespectTokenManager
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCaseClient
import world.respect.shared.domain.account.invite.ApproveOrDeclineInviteRequestUseCase
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.domain.account.invite.GetInviteInfoUseCaseClient
import world.respect.shared.domain.account.invite.RedeemInviteUseCase
import world.respect.shared.domain.account.invite.RedeemInviteUseCaseClient
import world.respect.shared.domain.account.passkey.EncodeUserHandleUseCaseImpl
import world.respect.shared.domain.account.passkey.GetAaguidAndProviderImpl
import world.respect.shared.domain.account.passkey.GetActivePersonPasskeysClient
import world.respect.shared.domain.account.passkey.GetActivePersonPasskeysUseCase
import world.respect.shared.domain.account.passkey.LoadAaguidJsonUseCase
import world.respect.shared.domain.account.passkey.LoadAaguidJsonUseCaseAndroid
import world.respect.shared.domain.account.passkey.RevokePasskeyUseCase
import world.respect.shared.domain.account.passkey.RevokePasskeyUseCaseClient
import world.respect.shared.domain.account.passkey.VerifyPasskeyUseCase
import world.respect.shared.domain.account.username.UsernameSuggestionUseCase
import world.respect.shared.domain.account.username.UsernameSuggestionUseCaseClient
import world.respect.shared.domain.account.username.filterusername.FilterUsernameUseCase
import world.respect.shared.domain.account.username.validateusername.ValidateUsernameUseCase
import world.respect.shared.domain.clipboard.SetClipboardStringUseCase
import world.respect.shared.domain.clipboard.SetClipboardStringUseCaseAndroid
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCase
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCaseAndroid
import world.respect.shared.domain.launchapp.LaunchAppUseCase
import world.respect.shared.domain.launchapp.LaunchAppUseCaseAndroid
import world.respect.shared.domain.onboarding.ShouldShowOnboardingUseCase
import world.respect.shared.domain.report.formatter.CreateGraphFormatterUseCase
import world.respect.shared.domain.report.query.MockRunReportUseCaseClientImpl
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.domain.school.RespectSchoolPath
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.domain.storage.CachePathsProviderAndroid
import world.respect.shared.domain.storage.GetAndroidSdCardDirUseCase
import world.respect.shared.domain.storage.GetOfflineStorageOptionsUseCaseAndroid
import world.respect.shared.domain.storage.GetOfflineStorageSettingUseCase
import world.respect.shared.domain.usagereporting.GetUsageReportingEnabledUseCase
import world.respect.shared.domain.usagereporting.GetUsageReportingEnabledUseCaseAndroid
import world.respect.shared.domain.usagereporting.SetUsageReportingEnabledUseCase
import world.respect.shared.domain.usagereporting.SetUsageReportingEnabledUseCaseAndroid
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.app_name
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.NavResultReturnerImpl
import world.respect.shared.util.di.RespectAccountScopeId
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.viewmodel.acknowledgement.AcknowledgementViewModel
import world.respect.shared.viewmodel.apps.detail.AppsDetailViewModel
import world.respect.shared.viewmodel.apps.enterlink.EnterLinkViewModel
import world.respect.shared.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.shared.viewmodel.apps.list.AppListViewModel
import world.respect.shared.viewmodel.assignments.AssignmentViewModel
import world.respect.shared.viewmodel.clazz.addperson.AddPersonToClazzViewModel
import world.respect.shared.viewmodel.clazz.detail.ClazzDetailViewModel
import world.respect.shared.viewmodel.clazz.edit.ClazzEditViewModel
import world.respect.shared.viewmodel.clazz.list.ClazzListViewModel
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailViewModel
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel
import world.respect.shared.viewmodel.manageuser.accountlist.AccountListViewModel
import world.respect.shared.viewmodel.manageuser.confirmation.ConfirmationViewModel
import world.respect.shared.viewmodel.manageuser.enterpasswordsignup.EnterPasswordSignupViewModel
import world.respect.shared.viewmodel.manageuser.getstarted.GetStartedViewModel
import world.respect.shared.viewmodel.manageuser.howpasskeywork.HowPasskeyWorksViewModel
import world.respect.shared.viewmodel.manageuser.joinclazzwithcode.JoinClazzWithCodeViewModel
import world.respect.shared.viewmodel.manageuser.login.LoginViewModel
import world.respect.shared.viewmodel.manageuser.otheroption.OtherOptionsViewModel
import world.respect.shared.viewmodel.manageuser.otheroptionsignup.OtherOptionsSignupViewModel
import world.respect.shared.viewmodel.manageuser.profile.SignupViewModel
import world.respect.shared.viewmodel.manageuser.signup.CreateAccountViewModel
import world.respect.shared.viewmodel.manageuser.termsandcondition.TermsAndConditionViewModel
import world.respect.shared.viewmodel.manageuser.waitingforapproval.WaitingForApprovalViewModel
import world.respect.shared.viewmodel.onboarding.OnboardingViewModel
import world.respect.shared.viewmodel.person.detail.PersonDetailViewModel
import world.respect.shared.viewmodel.person.edit.PersonEditViewModel
import world.respect.shared.viewmodel.person.list.PersonListViewModel
import world.respect.shared.viewmodel.person.manageaccount.ManageAccountViewModel
import world.respect.shared.viewmodel.person.passkeylist.PasskeyListViewModel
import world.respect.shared.viewmodel.report.ReportViewModel
import world.respect.shared.viewmodel.report.detail.ReportDetailViewModel
import world.respect.shared.viewmodel.report.edit.ReportEditViewModel
import world.respect.shared.viewmodel.report.filteredit.ReportFilterEditViewModel
import world.respect.shared.viewmodel.report.indictor.detail.IndicatorDetailViewModel
import world.respect.shared.viewmodel.report.indictor.edit.IndicatorEditViewModel
import world.respect.shared.viewmodel.report.indictor.list.IndicatorListViewModel
import world.respect.shared.viewmodel.report.list.ReportListViewModel
import world.respect.shared.viewmodel.report.list.ReportTemplateListViewModel
import java.io.File

@Suppress("unused")
const val DEFAULT_COMPATIBLE_APP_LIST_URL = "https://respect.world/respect-ds/manifestlist.json"

const val SHARED_PREF_SETTINGS_NAME = "respect_settings"
const val TAG_TMP_DIR = "tmpDir"

val appKoinModule = module {
    single<Json> {
        Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
        }
    }

    single<XXStringHasher> {
        XXStringHasherCommonJvm()
    }

    single<UidNumberMapper> {
        XXHashUidNumberMapper(xxStringHasher = get())
    }

    single<OkHttpClient> {
        val cachePathProvider: CachePathsProvider = get()

        OkHttpClient.Builder()
            .dispatcher(
                Dispatcher().also {
                    it.maxRequests = 30
                    it.maxRequestsPerHost = 10
                }
            )
            .addInterceptor(
                UstadCacheInterceptor(
                    cache = get(),
                    tmpDirProvider = { File(cachePathProvider().tmpWorkPath.toString()) },
                    logger = NapierLoggingAdapter(),
                    json = get(),
                )
            )
            .build()
    }

    single<HttpClient> {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json = get())
            }
            engine {
                preconfigured = get()
            }
        }
    }

    single<LaunchAppUseCase> {
        LaunchAppUseCaseAndroid(
            appContext = androidContext().applicationContext
        )
    }
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::AppsDetailViewModel)
    viewModelOf(::AppLauncherViewModel)
    viewModelOf(::EnterLinkViewModel)
    viewModelOf(::AppListViewModel)
    viewModelOf(::AssignmentViewModel)
    viewModelOf(::ClazzListViewModel)
    viewModelOf(::ClazzEditViewModel)
    viewModelOf(::ClazzDetailViewModel)
    viewModelOf(::LearningUnitListViewModel)
    viewModelOf(::LearningUnitDetailViewModel)
    viewModelOf(::ReportViewModel)
    viewModelOf(::AddPersonToClazzViewModel)
    viewModelOf(::AcknowledgementViewModel)
    viewModelOf(::JoinClazzWithCodeViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::ConfirmationViewModel)
    viewModelOf(::SignupViewModel)
    viewModelOf(::TermsAndConditionViewModel)
    viewModelOf(::WaitingForApprovalViewModel)
    viewModelOf(::CreateAccountViewModel)
    viewModelOf(::GetStartedViewModel)
    viewModelOf(::PasskeyListViewModel)
    viewModelOf(::HowPasskeyWorksViewModel)
    viewModelOf(::OtherOptionsViewModel)
    viewModelOf(::OtherOptionsSignupViewModel)
    viewModelOf(::EnterPasswordSignupViewModel)
    viewModelOf(::AccountListViewModel)
    viewModelOf(::ManageAccountViewModel)
    viewModelOf(::PersonListViewModel)
    viewModelOf(::PersonEditViewModel)
    viewModelOf(::PersonDetailViewModel)
    viewModelOf(::ReportDetailViewModel)
    viewModelOf(::ReportEditViewModel)
    viewModelOf(::ReportListViewModel)
    viewModelOf(::ReportTemplateListViewModel)
    viewModelOf(::IndicatorEditViewModel)
    viewModelOf(::ReportFilterEditViewModel)
    viewModelOf(::IndicatorListViewModel)
    viewModelOf(::IndicatorDetailViewModel)


    single<GetOfflineStorageOptionsUseCase> {
        GetOfflineStorageOptionsUseCaseAndroid(
            getAndroidSdCardDirUseCase = get()
        )
    }

    single<GetAndroidSdCardDirUseCase> {
        GetAndroidSdCardDirUseCase(
            appContext = androidContext().applicationContext
        )
    }

    single<GetOfflineStorageSettingUseCase> {
        GetOfflineStorageSettingUseCase(
            getOfflineStorageOptionsUseCase = get(),
            settings = get(),
        )
    }

    single<CachePathsProvider> {
        CachePathsProviderAndroid(
            appContext = androidContext().applicationContext,
            getAndroidSdCardPathUseCase = get(),
            getOfflineStorageSettingUseCase = get(),
        )
    }

    single<Settings> {
        SharedPreferencesSettings(
            delegate = androidContext().getSharedPreferences(
                SHARED_PREF_SETTINGS_NAME,
                Context.MODE_PRIVATE
            )
        )
    }

    single<UstadCacheDb> {
        Room.databaseBuilder(
            androidContext().applicationContext,
            UstadCacheDb::class.java,
            UstadCacheBuilder.DEFAULT_DB_NAME
        ).addCallback(ClearNeighborsCallback())
            .build()
    }

    single<UstadCache> {
        UstadCacheBuilder(
            appContext = androidContext().applicationContext,
            storagePath = Path(
                File(androidContext().filesDir, "httpfiles").absolutePath
            ),
            sizeLimit = { 100_000_000L },
            db = get(),
        ).build()
    }

    single<OkHttpWebViewClient> {
        OkHttpWebViewClient(
            okHttpClient = get()
        )
    }
    single(named(TAG_TMP_DIR)) {
        File(androidContext().applicationContext.cacheDir, "tmp").apply { mkdirs() }
    }

    single<RespectAccountManager> {
        RespectAccountManager(
            settings = get(),
            json = get(),
            tokenManager = get(),
            appDataSource = get(),
        )
    }

    single<RespectTokenManager> {
        RespectTokenManager(
            settings = get(),
            json = get(),
        )
    }
    single<ValidateUsernameUseCase> {
        ValidateUsernameUseCase()
    }

    single<FilterUsernameUseCase> {
        FilterUsernameUseCase()
    }

    single<EncodeUserHandleUseCase> {
        EncodeUserHandleUseCaseImpl()
    }

    single {
        CreatePublicKeyCredentialRequestOptionsJsonUseCase()
    }

    single<GetCredentialUseCase> {
        GetCredentialUseCaseAndroidImpl(
            json = get(),
            createPublicKeyCredentialRequestOptionsJsonUseCase = get()
        )
    }
    single<VerifyDomainUseCase> {
        VerifyDomainUseCaseImpl(
            context = androidApplication()
        )
    }
    single<SavePasswordUseCase> {
        SavePasswordUseCaseAndroidImpl()
    }

    single<SchoolDirectoryDataSourceLocal> {
        SchoolDirectoryDataSourceDb(
            respectAppDb = get(),
            json = get(),
            xxStringHasher = get()
        )
    }

    single<RespectAppDatabase> {
        val appContext = androidContext().applicationContext
        Room.databaseBuilder<RespectAppDatabase>(
            appContext, appContext.getDatabasePath("respectapp.db").absolutePath
        ).setDriver(BundledSQLiteDriver())
            .addCallback(AddSchoolDirectoryCallback(xxStringHasher = get()))
            .addCommonMigrations()
            .build()
    }

    single<RespectAppDataSource> {
        RespectAppDataSourceRepository(
            local = RespectAppDataSourceDb(
                respectAppDatabase = get(),
                json = get(),
                xxStringHasher = get(),
                primaryKeyGenerator = PrimaryKeyGenerator(RespectAppDatabase.TABLE_IDS),
            ),
            remote = RespectAppDataSourceHttp(
                local = RespectAppDataSourceDb(
                    respectAppDatabase = get(),
                    json = get(),
                    xxStringHasher = get(),
                    primaryKeyGenerator = PrimaryKeyGenerator(RespectAppDatabase.TABLE_IDS),
                ),
                httpClient = get(),
                defaultCompatibleAppListUrl = DEFAULT_COMPATIBLE_APP_LIST_URL,
            )
        )
    }

    single<NavResultReturner> {
        NavResultReturnerImpl()
    }

    single<VerifyPasskeyUseCase> {
        VerifyPasskeyUseCase(
            httpClient = get(),
            json = get()
        )
    }
    single<XXHasher64Factory> {
        XXHasher64FactoryCommonJvm()
    }

    single<ExtendedDataSourceValidationHelper> {
        ExtendedDataSourceValidationHelperImpl(
            respectAppDb = get(),
            xxStringHasher = get(),
            xxHasher64Factory = get(),
        )
    }

    single<SetClipboardStringUseCase> {
        SetClipboardStringUseCaseAndroid(androidContext().applicationContext)
    }
    single<ShouldShowOnboardingUseCase> {
        ShouldShowOnboardingUseCase(settings = get())
    }

    single<GetUsageReportingEnabledUseCase> {
        GetUsageReportingEnabledUseCaseAndroid(androidContext())
    }

    single<SetUsageReportingEnabledUseCase> {
        SetUsageReportingEnabledUseCaseAndroid(androidContext())
    }

    single<CheckPasskeySupportUseCase> {
        CheckPasskeySupportUseCaseAndroidImpl(
            verifyDomainUseCase = get()
        )
    }

    single<GetDeviceInfoUseCase> {
        GetDeviceInfoUseCaseAndroid(androidContext())
    }

    single<CreatePasskeyUseCaseAndroidChannelHost> {
        CreatePasskeyUseCaseAndroidChannelHost()
    }
    single<LoadAaguidJsonUseCase> {
        LoadAaguidJsonUseCaseAndroid(
            appContext = androidContext().applicationContext,
            json = get(),
        )
    }
    single<GetAaguidAndProvider> {
        GetAaguidAndProviderImpl(
            json = get(),
            loadAaguidJsonUseCase = get()
        )
    }
    /**
     * The SchoolDirectoryEntry scope might be one instance per school url or one instance per account
     * per url.
     *
     * ScopeId is set as per SchoolDirectoryEntryScopeId
     *
     * If the upstream server provides a list of grants/permission rules then the school database
     * can be shared
     */
    scope<SchoolDirectoryEntry> {
        scoped<GetTokenAndUserProfileWithCredentialUseCase> {
            GetTokenAndUserProfileWithCredentialUseCaseClient(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                httpClient = get(),
            )
        }

        scoped<RespectSchoolPath> {
            RespectSchoolPath(
                path = Path(
                    File(
                        androidContext().filesDir,
                        SchoolDirectoryEntryScopeId.parse(id).schoolUrl.sanitizedForFilename()
                    ).absolutePath
                )
            )
        }

        scoped<RespectSchoolDatabase> {
            Room.databaseBuilder<RespectSchoolDatabase>(
                androidContext(),
                SchoolDirectoryEntryScopeId.parse(id).schoolUrl.sanitizedForFilename()
            )
                .addCommonMigrations()
                .addMigrations(MIGRATION_2_3(true))
                .build()
        }

        scoped<SchoolPrimaryKeyGenerator> {
            SchoolPrimaryKeyGenerator(
                primaryKeyGenerator = PrimaryKeyGenerator(SchoolPrimaryKeyGenerator.TABLE_IDS)
            )
        }

        scoped<RedeemInviteUseCase> {
            RedeemInviteUseCaseClient(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                httpClient = get(),
            )
        }


        scoped<GetInviteInfoUseCase> {
            GetInviteInfoUseCaseClient(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                schoolDirectoryEntryDataSource = get<RespectAppDataSource>().schoolDirectoryEntryDataSource,
                httpClient = get(),
            )
        }

        scoped<UsernameSuggestionUseCase> {
            UsernameSuggestionUseCaseClient(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                schoolDirectoryEntryDataSource = get<RespectAppDataSource>().schoolDirectoryEntryDataSource,
                httpClient = get(),
            )
        }

        scoped<CreatePasskeyUseCase> {
            CreatePasskeyUseCaseAndroidImpl(
                sender = get(),
                json = get(),
                createPublicKeyJsonUseCase = get(),
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                uidNumberMapper = get(),
                getAaguidAndProvider = get(),
            )
        }

        scoped<CreatePublicKeyCredentialCreationOptionsJsonUseCase> {
            CreatePublicKeyCredentialCreationOptionsJsonUseCase(
                encodeUserHandleUseCase = get(),
                appName = Res.string.app_name,
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl
            )
        }

    }

    /**
     * ScopeId is set as per RespectAccountScopeId
     *
     * The RespectAccount scope will be linked to SchoolDirectoryEntry (the parent) scope.
     */
    scope<RespectAccount> {
        /* Koin doesn't have an onScopeCreated kind of function or event listener. The
         * RespectAccount scope is linked ot the SchoolDirectoryEntry scope when
         * RespectAccountSchoolScopeLink is retrieved. RespectAccountSchoolScopeLink is a root
         * dependency that all dependencies on RespectAccountScope require.
         */
        scoped<RespectAccountSchoolScopeLink> {
            val accountScopeId = RespectAccountScopeId.parse(id)
            val schoolDirectoryScope = SchoolDirectoryEntryScopeId(
                schoolUrl = accountScopeId.schoolUrl,
                accountPrincipalId = null,
            )

            linkTo(
                getKoin().getOrCreateScope<SchoolDirectoryEntry>(
                    schoolDirectoryScope.scopeId
                )
            )

            RespectAccountSchoolScopeLink(accountScopeId.schoolUrl)
        }


        scoped<AuthTokenProvider> {
            get<RespectTokenManager>().providerFor(id)
        }

        scoped<RemoteWriteQueue> {
            get<RespectAccountSchoolScopeLink>()
            val accountScopeId = RespectAccountScopeId.parse(id)

            RemoteWriteQueueDbImpl(
                schoolDb = get(),
                account = AuthenticatedUserPrincipalId(accountScopeId.accountPrincipalId.guid),
                enqueueDrainRemoteWriteQueueUseCase = get(),
            )
        }

        scoped<GetPersonPassword> {
            GetPersonPasswordDbImpl(
                respectSchoolDatabase = get(),
                xxHash = get()
            )
        }
        scoped<GetActivePersonPasskeysUseCase> {
            GetActivePersonPasskeysClient(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                httpClient = get(),
            )
        }
        scoped<RevokePasskeyUseCase> {
            RevokePasskeyUseCaseClient(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                httpClient = get(),
            )
        }
        scoped<EnqueueDrainRemoteWriteQueueUseCase> {
            EnqueueDrainRemoteWriteQueueUseCaseAndroidImpl(
                context = androidContext().applicationContext,
                scopeId = id,
                scopeClass = RespectAccount::class,
            )
        }

        scoped<DrainRemoteWriteQueueUseCase> {
            DrainRemoteWriteQueueUseCase(
                remoteWriteQueue = get(),
                dataSource = get(),
            )
        }

        scoped<SchoolDataSource> {
            val accountScopeId = RespectAccountScopeId.parse(id)
            val schoolUrl = get<RespectAccountSchoolScopeLink>()

            SchoolDataSourceRepository(
                local = SchoolDataSourceDb(
                    schoolDb = get(),
                    uidNumberMapper = get(),
                    authenticatedUser = AuthenticatedUserPrincipalId(
                        accountScopeId.accountPrincipalId.guid
                    )
                ),
                remote = SchoolDataSourceHttp(
                    schoolUrl = schoolUrl.url,
                    schoolDirectoryEntryDataSource = get<RespectAppDataSource>().schoolDirectoryEntryDataSource,
                    httpClient = get(),
                    tokenProvider = get(),
                    validationHelper = get(),
                ),
                validationHelper = get(),
                remoteWriteQueue = get(),
            )
        }

        scoped<ApproveOrDeclineInviteRequestUseCase> {
            ApproveOrDeclineInviteRequestUseCase(
                schoolDataSource = get(),
            )
        }
    }
    single<RunReportUseCase> {
        MockRunReportUseCaseClientImpl()
    }
    single<CreateGraphFormatterUseCase> {
        CreateGraphFormatterUseCase()
    }
}
