package com.tomuvak.testing.gc

import kotlinx.coroutines.CoroutineScope
import kotlin.native.internal.GC

actual fun whenCollectingGarbage() = GC.collect()

actual suspend fun CoroutineScope.tryToAchieveByForcingGc(condition: () -> Boolean): Boolean {
    whenCollectingGarbage()
    return condition()
}
