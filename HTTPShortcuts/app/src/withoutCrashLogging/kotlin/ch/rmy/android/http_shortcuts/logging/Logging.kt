package ch.rmy.android.http_shortcuts.logging

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.http_shortcuts.BuildConfig
import timber.log.Timber

@SuppressLint("StaticFieldLeak")
object Logging : ch.rmy.android.framework.extensions.Logging {

    private const val TAG = "Logging"

    private var context: Context? = null

    fun initCrashReporting(context: Context) {
        if (BuildConfig.DEBUG) {
            this.context = context
            Timber.tag(TAG).i("Initialized debug logging context")
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun disableCrashReporting(context: Context) {
    }

    @Suppress("MayBeConstant")
    val supportsCrashReporting: Boolean = true

    override fun logException(origin: String?, e: Throwable) {
        if (BuildConfig.DEBUG) {
            Timber.tag(origin ?: TAG).e(e, "An error occurred")
            e.printStackTrace()
            Handler(Looper.getMainLooper()).post {
                context?.showToast("Error: $e", long = true)
            }
        }
    }

    override fun logInfo(origin: String?, message: String) {
        if (BuildConfig.DEBUG) {
            Timber.tag(origin ?: TAG).i(message)
        }
    }
}
