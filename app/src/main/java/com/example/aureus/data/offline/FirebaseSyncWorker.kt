package com.example.aureus.data.offline

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker pour la synchronisation automatique Firebase
 */
@HiltWorker
class FirebaseSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val offlineSyncManager: OfflineSyncManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "FirebaseSyncWorker"
    }

    override suspend fun doWork(): androidx.work.Result {
        return try {
            Log.d(TAG, "Starting background sync")
            
            val syncResult = offlineSyncManager.syncNow()
            
            when (syncResult) {
                is SyncResult.Success -> {
                    Log.d(TAG, "Background sync successful")
                    androidx.work.Result.success()
                }
                is SyncResult.Error -> {
                    Log.e(TAG, "Background sync failed: ${syncResult.message}")
                    androidx.work.Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync worker crashed", e)
            androidx.work.Result.failure()
        }
    }
}