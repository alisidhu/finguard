package com.finguard.sample.support

import com.finguard.sdk.core.StorageService
import java.util.concurrent.ConcurrentHashMap

class InMemorySecureStorage : StorageService {
    private val data = ConcurrentHashMap<String, ByteArray>()

    override fun save(
        key: String,
        value: ByteArray,
    ) {
        data[key] = value.copyOf()
    }

    override fun load(key: String): ByteArray? = data[key]?.copyOf()

    override fun clear(key: String) {
        data.remove(key)
    }
}
