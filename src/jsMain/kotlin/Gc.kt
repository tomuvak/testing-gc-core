package com.tomuvak.testing.gc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

actual fun whenCollectingGarbage() { error("Triggering garbage collection not supported on platform") }

actual suspend fun CoroutineScope.tryToAchieveByForcingGc(condition: () -> Boolean): Boolean {
    if (condition()) return true

    var result: Boolean
    coroutineScope {
        var data = List(4 * 1024 * 1024) { it }
        delay(1)
        for (i in 0 until 7) {
            if (condition()) {
                result = true
                return@coroutineScope
            }
            delay(1)
            data = data.map { it }
        }
        result = condition()
    }

    return result
}
