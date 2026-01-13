package com.example.aureus.data.offline

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Publisher/Observer Pattern pour éviter les boucles while(true)
 * Diffusion du statut de sync seulement quand il change réellement
 */
@Singleton
class SyncStatusPublisher @Inject constructor() {

    private val _syncStatusFlow = MutableSharedFlow<SyncStatus>(replay = 1)
    val syncStatusFlow: SharedFlow<SyncStatus> = _syncStatusFlow.asSharedFlow()

    private var lastStatus: SyncStatus? = null

    /**
     * Publier un nouveau statut seulement s'il a changé
     */
    suspend fun publishSyncStatus(newStatus: SyncStatus) {
        if (lastStatus != newStatus) {
            lastStatus = newStatus
            _syncStatusFlow.emit(newStatus)
        }
    }

    /**
     * Obtenir le dernier statut connu
     */
    fun getLastStatus(): SyncStatus? = lastStatus
}