package com.finguard.sdk.auth

import com.finguard.sdk.core.AuthSession
import com.finguard.sdk.core.StorageService

private const val VAULT_KEY = "auth.session"

internal data class PersistedRecord(
    val session: AuthSession,
    val currentRefreshDigest: ByteArray?,
    val previousRefreshDigest: ByteArray?,
    val lastRefreshAt: Long?,
    val rotationCount: Int,
)

internal class CredentialVault(
    private val storage: StorageService,
    private val serializer: SessionSerializer,
) {
    fun store(record: PersistedRecord) {
        storage.save(VAULT_KEY, serializer.serialize(record))
    }

    fun load(): PersistedRecord? = storage.load(VAULT_KEY)?.let { serializer.deserialize(it) }

    fun clear() {
        storage.clear(VAULT_KEY)
    }
}
