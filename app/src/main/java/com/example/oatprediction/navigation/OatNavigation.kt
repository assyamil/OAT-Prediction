package com.example.oatprediction.navigation

import androidx.activity.compose.BackHandler
import com.example.oatprediction.ui.components.*
import com.example.oatprediction.ui.about.AboutScreen
import com.example.oatprediction.ui.analysis.AnalysisScreen
import com.example.oatprediction.viewmodel.AnalysisViewModel
import com.example.oatprediction.data.repository.AnalysisRepository
import com.example.oatprediction.data.repository.CsvAnalysisDataset
import com.example.oatprediction.data.export.AndroidAnalysisExporter
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.oatprediction.R
import com.example.oatprediction.data.local.HistoryRepositoryProvider
import com.example.oatprediction.data.repository.HistoryRepository
import com.example.oatprediction.viewmodel.HistorySaveState
import com.example.oatprediction.viewmodel.HistoryViewModel
import com.example.oatprediction.viewmodel.HistoryDetailViewModel
import com.example.oatprediction.ui.history.HistoryDetailScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.oatprediction.ui.history.HistoryScreen
import com.example.oatprediction.ui.home.HomeScreen
import com.example.oatprediction.ui.prediction.PredictionScreen
import com.example.oatprediction.viewmodel.PredictionViewModel
import com.example.oatprediction.viewmodel.PredictionUiState
import com.example.oatprediction.data.PredictionRepositoryProvider
import com.example.oatprediction.ui.result.ResultScreen

private object Route {
    const val HOME = "home"
    const val PREDICTION = "prediction"
    const val HISTORY = "history"
    const val RESULT = "result"
}

@Composable
fun OatNavigation(
    predictionViewModel: PredictionViewModel? = null,
    historyRepository: HistoryRepository = HistoryRepositoryProvider.get(LocalContext.current),
    darkTheme: Boolean = false,
    themeSaving: Boolean = false,
    onToggleTheme: () -> Unit = {},
    themeError: Boolean = false,
    onThemeErrorShown: () -> Unit = {},
) {
    val vm: PredictionViewModel = predictionViewModel ?: viewModel(
        factory = PredictionViewModel.factory(PredictionRepositoryProvider.repository, historyRepository),
    )
    val navController = rememberNavController()
    var homeNotice by rememberSaveable { mutableStateOf<UiNotice?>(null) }
    var historyNotice by rememberSaveable { mutableStateOf<UiNotice?>(null) }
    val savedMessage = stringResource(R.string.history_saved)
    val deletedMessage = stringResource(R.string.history_deleted)
    fun selectTab(tab: MainTab) {
        navController.navigate(tab.route) {
            popUpTo(Route.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    fun returnHome() {
        navController.popBackStack(Route.HOME, inclusive = false)
        vm.leaveForm()
    }
    NavHost(navController = navController, startDestination = Route.HOME) {
        composable(Route.HOME) {
            HomeScreen(
                darkTheme = darkTheme, themeSaving = themeSaving, onToggleTheme = onToggleTheme,
                themeError = themeError, onThemeErrorShown = onThemeErrorShown,
                bottomBar = { MainBottomBar(MainTab.HOME, ::selectTab) },
                notice = homeNotice, onNoticeShown = { homeNotice = null },
                onStartPrediction = {
                    navController.navigate(Route.PREDICTION) { launchSingleTop = true }
                },
                onHistory = {
                    selectTab(MainTab.HISTORY)
                },
            )
        }
        composable(Route.PREDICTION) { entry ->
            LaunchedEffect(vm.resultNavigationPending, entry) {
                if (vm.resultNavigationPending) {
                    entry.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        if (vm.resultNavigationPending) {
                            navController.navigate(Route.RESULT) { launchSingleTop = true }
                            vm.onResultNavigated()
                        }
                    }
                }
            }
            key(vm.formSessionId) { PredictionScreen(
                state = vm.formState,
                onFieldChange = vm::updateField,
                onPeriodChange = vm::updatePeriod,
                uiState = vm.uiState,
                onSubmit = vm::submit,
                onBack = {
                    vm.leaveForm()
                    navController.popBackStack()
                },
            )
            }
        }
        composable(Route.RESULT) { entry ->
            val snapshot = vm.resultSnapshot
            LaunchedEffect(vm.saveState, snapshot == null, entry) {
                entry.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    if (vm.saveState == HistorySaveState.SAVED) {
                        homeNotice = UiNotice(savedMessage)
                        returnHome()
                    } else if (snapshot == null) {
                        navController.popBackStack(Route.PREDICTION, inclusive = false)
                    }
                }
            }
            if (snapshot != null) {
                ResultScreen(snapshot, vm.saveState, vm::saveResult, onBack = { returnHome() }, onRepeat = {
                    if (vm.saveState != HistorySaveState.SAVING) {
                        navController.popBackStack(Route.PREDICTION, inclusive = false)
                        vm.repeatPrediction()
                    }
                })
            }
        }
        composable(MainTab.ABOUT.route) {
            BackHandler { selectTab(MainTab.HOME) }
            AboutScreen(bottomBar = { MainBottomBar(MainTab.ABOUT, ::selectTab) })
        }
        composable(MainTab.ANALYSIS.route) {
            val context = LocalContext.current
            val applicationContext = context.applicationContext
            val repository = remember(historyRepository, applicationContext) { AnalysisRepository(historyRepository,
                CsvAnalysisDataset { applicationContext.assets.open("modeling_dataset.csv") }) }
            val exporter = remember(context.applicationContext) { AndroidAnalysisExporter(context) }
            val analysis: AnalysisViewModel = viewModel(factory = AnalysisViewModel.factory(repository, exporter))
            BackHandler { if (!analysis.busy) selectTab(MainTab.HOME) }
            AnalysisScreen(analysis, bottomBar = {
                MainBottomBar(MainTab.ANALYSIS, enabled = !analysis.busy, onSelect = { if (!analysis.busy) selectTab(it) })
            })
        }
        composable(Route.HISTORY) { entry ->
            val history: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(historyRepository))
            val deleting = history.deletion.deletingId != null
            BackHandler { if (!deleting) selectTab(MainTab.HOME) }
            LaunchedEffect(entry) {
                entry.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { history.refresh() }
            }
            HistoryScreen(
                history.state, history.time, history.puskesmas, history.classification,
                history::selectTime, history::selectPuskesmas, history::selectClassification,
                onDetail = { if (!deleting) navController.navigate("history/$it") { launchSingleTop = true } },
                onRetry = history::refresh, onBack = { if (!deleting) selectTab(MainTab.HOME) },
                bottomBar = { MainBottomBar(MainTab.HISTORY, enabled = !deleting, onSelect = { if (!deleting) selectTab(it) }) },
                onResetFilters = history::resetFilters,
                sort = history.sort, onSort = history::toggleSort,
                deletion = history.deletion, onDelete = history::delete,
                onDeleteNoticeShown = history::onDeleteNoticeShown,
                notice = historyNotice, onNoticeShown = { historyNotice = null },
            )
        }
        composable("history/{id}") { entry ->
            val detail: HistoryDetailViewModel = viewModel(
                factory = HistoryDetailViewModel.factory(checkNotNull(entry.arguments?.getString("id")), historyRepository),
            )
            LaunchedEffect(detail.state.deleted, entry) {
                if (detail.state.deleted) entry.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    historyNotice = UiNotice(deletedMessage)
                    navController.popBackStack()
                }
            }
            HistoryDetailScreen(detail.state, detail::delete, detail::refresh, onBack = { navController.popBackStack() })
        }
    }
}
