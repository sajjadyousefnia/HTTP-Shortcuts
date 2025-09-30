package ch.rmy.android.framework.ui

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import ch.rmy.android.framework.extensions.finishWithoutAnimation
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.utils.SnackbarManager
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import timber.log.Timber

abstract class BaseActivity : AppCompatActivity() {

    val context: Context
        get() = this

    protected open val eventLogTag: String
        get() = "${this::class.java.simpleName}-Events"

    open fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is ViewModelEvent.SendIntent -> {
                logEvent("Sending intent via ${event.intentBuilder}")
                try {
                    event.intentBuilder.startActivity(this)
                } catch (_: ActivityNotFoundException) {
                    showToast(R.string.error_not_supported)
                    Timber.tag(eventLogTag).w("Failed to resolve intent for %s", event.intentBuilder)
                }
            }
            is ViewModelEvent.SendBroadcast -> {
                logEvent("Sending broadcast intent: ${event.intent}")
                sendBroadcast(event.intent)
            }
            is ViewModelEvent.OpenURL -> {
                logEvent("Opening URL ${event.url}")
                openURL(event.url)
            }
            is ViewModelEvent.CloseScreen -> {
                logEvent("Closing screen")
                finishWithoutAnimation()
            }
            is ViewModelEvent.Finish -> {
                logEvent("Finishing (resultCode=${event.resultCode}, skipAnimation=${event.skipAnimation})")
                if (event.resultCode != null) {
                    setResult(event.resultCode, event.intent)
                }
                if (event.skipAnimation) {
                    finishWithoutAnimation()
                } else {
                    finish()
                }
            }
            is ViewModelEvent.SetActivityResult -> {
                logEvent("Setting result (result=${event.result})")
                setResult(event.result, event.intent)
            }
            is ViewModelEvent.ShowSnackbar -> {
                logEvent("Showing snackbar: ${event.message}")
                SnackbarManager.enqueueSnackbar(event.message.localize(context).toString(), long = event.long)
            }
            is ViewModelEvent.ShowToast -> {
                logEvent("Showing toast: ${event.message}")
                showToast(event.message.localize(context).toString(), long = event.long)
            }
            else -> {
                showToast(R.string.error_generic)
                val exception = IllegalArgumentException("Unhandled event: $event")
                Timber.tag(eventLogTag).e(exception, "Unhandled event received")
                logException(exception)
            }
        }
    }

    private fun logEvent(message: String) {
        Timber.tag(eventLogTag).i(message)
    }
}
