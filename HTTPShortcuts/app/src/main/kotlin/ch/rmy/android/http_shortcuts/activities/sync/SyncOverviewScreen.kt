package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.ResultHandler

@Composable
fun SyncOverviewScreen(savedStateHandle: SavedStateHandle) {
    val (viewModel, state) = bindViewModel<SyncOverviewViewState, SyncOverviewViewModel>()

    ResultHandler(savedStateHandle) { result ->
        when (result) {
            NavigationDestination.SyncImport.RESULT_CHANGED,
            NavigationDestination.SyncExport.RESULT_CHANGED,
            -> {
                viewModel.onConfigurationChanged()
            }
        }
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.settings_automatic_import_export),
    ) { viewState ->
        SyncOverviewContent(
            viewState,
            onSyncTypeSelected = viewModel::onSyncTypeSelected,
            onConfigureImportClicked = viewModel::onConfigureImportClicked,
            onConfigureExportClicked = viewModel::onConfigureExportClicked,
        )
    }
}
