package com.vyayah.app.di

import com.vyayah.app.ai.CategorizationEngine
import com.vyayah.app.data.local.VyayahDatabase
import com.vyayah.app.ui.screens.cards.CardsViewModel
import com.vyayah.app.ui.screens.ledger.LedgerViewModel
import com.vyayah.app.ui.screens.onboarding.OnboardingViewModel
import com.vyayah.app.ui.screens.save.SaveViewModel
import com.vyayah.app.ui.screens.settings.SettingsViewModel
import com.vyayah.app.ui.screens.today.TodayViewModel
import org.koin.android.ext.kinit
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single { VyayahDatabase.getInstance(get()) }
    single { get<VyayahDatabase>().transactionDao() }
    single { get<VyayahDatabase>().accountDao() }
    single { get<VyayahDatabase>().categoryDao() }
    single { get<VyayahDatabase>().budgetDao() }
    single { get<VyayahDatabase>().goalDao() }
    single { get<VyayahDatabase>().ruleDao() }
    single { get<VyayahDatabase>().syncDao() }

    // AI & Engines
    single { CategorizationEngine(get(), get()) }

    // ViewModels
    viewModel { TodayViewModel(get(), get(), get(), get()) }
    viewModel { LedgerViewModel(get(), get(), get()) }
    viewModel { CardsViewModel(get(), get()) }
    viewModel { SaveViewModel(get()) }
    viewModel { com.vyayah.app.ui.screens.trips.TripsViewModel() }
    viewModel { SettingsViewModel(get(), get(), get(), get()) }
    viewModel { OnboardingViewModel(get(), get(), get()) }
}
