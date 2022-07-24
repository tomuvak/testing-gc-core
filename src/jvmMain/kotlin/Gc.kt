package com.tomuvak.testing.gc

import kotlinx.coroutines.CoroutineScope

actual fun whenCollectingGarbage() = System.gc()

actual suspend fun CoroutineScope.tryToAchieveByForcingGc(condition: () -> Boolean): Boolean {
    whenCollectingGarbage()
    return condition()
}
