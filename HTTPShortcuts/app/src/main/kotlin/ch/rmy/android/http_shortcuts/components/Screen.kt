package ch.rmy.android.http_shortcuts.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import timber.log.Timber

@Composable
inline fun <D, VS, reified VM : BaseViewModel<D, VS>> bindViewModel(
    initData: D,
    key: String? = null,
): Pair<VM, VS?> {
    val viewModel = hiltViewModel<VM>(key = key)
    val state by viewModel.viewStateFlow.collectAsStateWithLifecycle()
    val eventHandler = LocalEventinator.current
    DisposableEffect(Unit) {
        viewModel.init(initData)
        onDispose { }
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect(eventHandler::onEvent)
    }
    return Pair(viewModel, state)
}

@Composable
inline fun <VS, reified VM : BaseViewModel<Unit, VS>> bindViewModel(): Pair<VM, VS?> =
    bindViewModel<Unit, VS, VM>(Unit)

@Composable
fun EventHandler(enabled: Boolean = true, onEvent: (ViewModelEvent) -> Boolean) {
    val eventHandler = LocalEventinator.current
    DisposableEffect(enabled) {
        if (enabled) {
            eventHandler.register(onEvent)
            onDispose {
                eventHandler.deregister(onEvent)
            }
        } else {
            onDispose { }
        }
    }
}

class Eventinator(val baseHandler: (ViewModelEvent) -> Unit = {}) {
    private val eventHandlers = mutableListOf<(ViewModelEvent) -> Boolean>()

    fun onEvent(event: ViewModelEvent) {
        Timber.tag(TAG).d("Dispatching event %s to %d handler(s)", event, eventHandlers.size)
        for ((index, handler) in eventHandlers.withIndex()) {
            Timber.tag(TAG).d("Delivering event %s to handler %d/%d", event, index + 1, eventHandlers.size)
            val handled = runCatching { handler(event) }
                .onFailure { error ->
                    Timber.tag(TAG).e(error, "Handler %d failed while processing %s", index + 1, event)
                }
                .getOrDefault(false)
            if (handled) {
                Timber.tag(TAG).d("Event %s handled by handler %d/%d", event, index + 1, eventHandlers.size)
                return
            }
            Timber.tag(TAG).d("Event %s not handled by handler %d/%d", event, index + 1, eventHandlers.size)
        }
        Timber.tag(TAG).d("No handler consumed event %s; delegating to base handler", event)
        runCatching { baseHandler(event) }
            .onFailure { error ->
                Timber.tag(TAG).e(error, "Base handler failed while processing %s", event)
            }
    }

    fun register(eventHandler: (ViewModelEvent) -> Boolean) {
        eventHandlers.add(0, eventHandler)
        Timber.tag(TAG).d("Registered event handler. Total handlers: %d", eventHandlers.size)
    }

    fun deregister(eventHandler: (ViewModelEvent) -> Boolean) {
        eventHandlers.remove(eventHandler)
        Timber.tag(TAG).d("Deregistered event handler. Total handlers: %d", eventHandlers.size)
    }

    companion object {
        private const val TAG = "EventFlow"
    }
}

val LocalEventinator = staticCompositionLocalOf {
    Eventinator()
}
