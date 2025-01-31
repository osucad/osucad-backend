package com.osucad.server.multiplayer

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface ILock {
    suspend fun release()
}


@OptIn(ExperimentalContracts::class)
suspend fun <T> ILock.use(block: suspend () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return try {
        block()
    } finally {
        release()
    }
}