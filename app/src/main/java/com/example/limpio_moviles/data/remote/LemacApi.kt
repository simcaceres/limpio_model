package com.example.limpio_moviles.data.remote

import com.example.limpio_moviles.data.local.RegistroEntity
import kotlinx.coroutines.delay

interface LemacApi {
    suspend fun sincronizar(registros: List<RegistroEntity>): Result<Unit>
}

class FakeLemacApi : LemacApi {

    override suspend fun sincronizar(registros: List<RegistroEntity>): Result<Unit> {
        delay(700)
        return Result.success(Unit)
    }
}
