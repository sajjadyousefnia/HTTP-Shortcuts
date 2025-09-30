package ch.rmy.android.http_shortcuts.logging

import android.util.Log
import ch.rmy.android.http_shortcuts.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean
import timber.log.Timber

object TimberInitializer {
    private val initialized = AtomicBoolean(false)

    fun init() {
        if (!initialized.compareAndSet(false, true)) {
            return
        }
        val tree = if (BuildConfig.DEBUG) {
            Timber.DebugTree()
        } else {
            object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                        return
                    }
                    Log.println(priority, tag ?: DEFAULT_TAG, message)
                    if (t != null && priority >= Log.ERROR) {
                        Log.println(priority, tag ?: DEFAULT_TAG, Log.getStackTraceString(t))
                    }
                }
            }
        }
        Timber.plant(tree)
        Timber.tag(DEFAULT_TAG).i("Timber logging initialized. Debug build: %s", BuildConfig.DEBUG)
    }

    private const val DEFAULT_TAG = "HTTP-Shortcuts"
}
