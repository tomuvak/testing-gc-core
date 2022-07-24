package com.tomuvak.testing.gc

import kotlinx.coroutines.CoroutineScope

expect fun whenCollectingGarbage()
expect suspend fun CoroutineScope.tryToAchieveByForcingGc(condition: () -> Boolean): Boolean
