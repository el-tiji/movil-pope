package pope.project.di

import kotlinx.datetime.TimeZone
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pope.project.data.fake.FakeAuthRepository
import pope.project.data.fake.FakeIncidentRepository
import pope.project.data.fake.FakeJobRepository
import pope.project.data.fake.FakeProfileRepository
import pope.project.domain.repository.AuthRepository
import pope.project.domain.repository.IncidentRepository
import pope.project.domain.repository.JobRepository
import pope.project.domain.repository.ProfileRepository
import pope.project.ui.screens.detail.JobDetailViewModel
import pope.project.ui.screens.history.HistoryViewModel
import pope.project.ui.screens.incident.IncidentViewModel
import pope.project.ui.screens.login.LoginViewModel
import pope.project.ui.screens.profile.ProfileViewModel
import pope.project.ui.screens.today.TodayViewModel
import kotlin.time.Clock

val appModule = module {
    single<Clock> { Clock.System }
    single<TimeZone> { TimeZone.currentSystemDefault() }

    // Swap these fakes for Ktor-backed implementations once the Pope Cleaning API exists.
    single<AuthRepository> { FakeAuthRepository() }
    single<JobRepository> { FakeJobRepository(get(), get()) }
    single<IncidentRepository> { FakeIncidentRepository(get(), get()) }
    single<ProfileRepository> { FakeProfileRepository(get(), get()) }

    viewModel { LoginViewModel(get()) }
    viewModel { TodayViewModel(get()) }
    viewModel { params -> JobDetailViewModel(params.get(), get()) }
    viewModel { params -> IncidentViewModel(params.get(), get(), get()) }
    viewModel { HistoryViewModel(get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get(), get()) }
}

private var koinStarted = false

/** Idempotent: survives Activity recreation so the in-memory data is kept. */
fun initKoin() {
    if (koinStarted) return
    startKoin { modules(appModule) }
    koinStarted = true
}
