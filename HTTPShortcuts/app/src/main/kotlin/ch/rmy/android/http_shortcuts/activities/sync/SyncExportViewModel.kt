package ch.rmy.android.http_shortcuts.activities.sync

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.domains.sync.SyncRepository
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.models.SyncConfig
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SyncExportViewModel
@Inject
constructor(
    application: Application,
    private val userPreferences: UserPreferences,
    private val syncRepository: SyncRepository,
) : BaseViewModel<Unit, SyncExportViewState>(application) {
    private lateinit var configFlow: MutableStateFlow<SyncConfig>

    @OptIn(FlowPreview::class)
    override suspend fun initialize(data: Unit): SyncExportViewState {
        if (userPreferences.syncType != SyncType.EXPORT) {
            terminateInitialization()
        }
        val config = syncRepository.getConfig(SyncType.EXPORT)
        configFlow = MutableStateFlow(config)
        viewModelScope.launch(Dispatchers.Default) {
            configFlow.drop(1)
                .debounce(300.milliseconds)
                .collectLatest { config ->
                    syncRepository.updateConfig(config)
                }
        }
        return SyncExportViewState(
            schedule = config.schedule,
            password = config.password,
            hasChanged = false,
        )
    }

    private fun updateConfig(update: SyncConfig.() -> SyncConfig) {
        configFlow.update { update(it) }
    }

    fun onScheduleChanged(schedule: SyncSchedule) = runAction {
        updateViewState {
            copy(
                schedule = schedule,
                hasChanged = true,
            )
        }
        updateConfig {
            copy(schedule = schedule)
        }
    }

    fun onPasswordChanged(password: String) = runAction {
        updateViewState {
            copy(
                password = password,
                hasChanged = true,
            )
        }
        updateConfig {
            copy(password = password)
        }
    }

    fun onBackPressed() = runAction {
        closeScreen(NavigationDestination.SyncExport.RESULT_CHANGED)
    }
}
