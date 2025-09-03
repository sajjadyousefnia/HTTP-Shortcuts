package ch.rmy.android.http_shortcuts.scheduling.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.data.domains.sync.SyncRepository
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.Importer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.toJavaDuration

@HiltWorker
class SyncWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val userPreferences: UserPreferences,
    private val syncRepository: SyncRepository,
    private val importer: Importer,
    private val exporter: Exporter,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val syncType = userPreferences.syncType ?: return Result.success()
        val config = syncRepository.getConfig(syncType)

        when (config.type) {
            SyncType.IMPORT -> {
                // TODO
            }
            SyncType.EXPORT -> {
                // TODO
            }
        }
        return Result.success() // TODO
    }

    class Starter
    @Inject
    constructor(
        private val context: Context,
    ) {
        operator fun invoke(interval: Duration, requiresNetwork: Boolean) {
            with(WorkManager.getInstance(context)) {
                cancelAllWorkByTag(TAG)
                enqueue(
                    PeriodicWorkRequestBuilder<SyncWorker>(interval.toJavaDuration())
                        .addTag(TAG)
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiresBatteryNotLow(true)
                                .runIf(requiresNetwork) {
                                    setRequiredNetworkType(NetworkType.CONNECTED)
                                }
                                .build(),
                        )
                        .build(),
                )
            }
        }

        fun cancel() {
            with(WorkManager.getInstance(context)) {
                cancelAllWorkByTag(TAG)
            }
        }
    }

    companion object {
        private const val TAG = "sync_worker"
    }
}
