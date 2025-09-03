package ch.rmy.android.http_shortcuts.activities.sync

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun SyncImportScreen() {
    val (viewModel, state) = bindViewModel<SyncImportViewState, SyncImportViewModel>()

    BackHandler(enabled = state?.hasChanged == true) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.sync_type_automatic_import),
    ) { viewState ->
        SyncImportContent(
            viewState = viewState,
            onScheduleChanged = viewModel::onScheduleChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
        )
    }
}
