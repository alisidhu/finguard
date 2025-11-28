package com.finguard.sdk.auth

import com.finguard.sdk.core.AuthSession
import com.finguard.sdk.core.CryptoService
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

private const val SERIAL_VERSION = 1

internal class SessionSerializer(
    private val crypto: CryptoService,
) {
    fun serialize(record: PersistedRecord): ByteArray {
        val raw =
            ByteArrayOutputStream().use { bos ->
                DataOutputStream(bos).use { out ->
                    out.writeInt(SERIAL_VERSION)
                    writeString(out, record.session.userId)
                    writeString(out, record.session.accessToken)
                    writeNullableString(out, record.session.refreshToken)
                    writeString(out, record.session.issuer)
                    out.writeLong(record.session.createdAt)
                    out.writeLong(record.session.expiresAt)
                    writeBytes(out, record.currentRefreshDigest)
                    writeBytes(out, record.previousRefreshDigest)
                    writeNullableLong(out, record.lastRefreshAt)
                    out.writeInt(record.rotationCount)
                }
                bos.toByteArray()
            }
        return crypto.encrypt(raw)
    }

    fun deserialize(payload: ByteArray): PersistedRecord {
        val decrypted = crypto.decrypt(payload)
        val input = DataInputStream(ByteArrayInputStream(decrypted))
        val version = input.readInt()
        require(version == SERIAL_VERSION) { "Unsupported auth session version" }
        val userId = readString(input)
        val accessToken = readString(input)
        val refreshToken = readNullableString(input)
        val issuer = readString(input)
        val createdAt = input.readLong()
        val expiresAt = input.readLong()
        val currentDigest = readBytes(input)
        val previousDigest = readBytes(input)
        val lastRefreshAt = readNullableLong(input)
        val rotationCount = input.readInt()
        val session =
            AuthSession(
                userId = userId,
                accessToken = accessToken,
                refreshToken = refreshToken,
                createdAt = createdAt,
                expiresAt = expiresAt,
                issuer = issuer,
            )
        return PersistedRecord(
            session = session,
            currentRefreshDigest = currentDigest,
            previousRefreshDigest = previousDigest,
            lastRefreshAt = lastRefreshAt,
            rotationCount = rotationCount,
        )
    }

    private fun writeString(
        out: DataOutputStream,
        value: String,
    ) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        out.writeInt(bytes.size)
        out.write(bytes)
    }

    private fun writeNullableString(
        out: DataOutputStream,
        value: String?,
    ) {
        if (value == null) {
            out.writeInt(-1)
        } else {
            writeString(out, value)
        }
    }

    private fun writeBytes(
        out: DataOutputStream,
        value: ByteArray?,
    ) {
        if (value == null) {
            out.writeInt(-1)
            return
        }
        out.writeInt(value.size)
        out.write(value)
    }

    private fun writeNullableLong(
        out: DataOutputStream,
        value: Long?,
    ) {
        if (value == null) {
            out.writeLong(-1)
        } else {
            out.writeLong(value)
        }
    }

    private fun readString(input: DataInputStream): String {
        val len = input.readInt()
        require(len >= 0) { "Corrupted auth session payload" }
        val bytes = ByteArray(len)
        input.readFully(bytes)
        return String(bytes, Charsets.UTF_8)
    }

    private fun readNullableString(input: DataInputStream): String? {
        val len = input.readInt()
        if (len < 0) return null
        val bytes = ByteArray(len)
        input.readFully(bytes)
        return String(bytes, Charsets.UTF_8)
    }

    private fun readBytes(input: DataInputStream): ByteArray? {
        val len = input.readInt()
        if (len < 0) return null
        val bytes = ByteArray(len)
        input.readFully(bytes)
        return bytes
    }

    private fun readNullableLong(input: DataInputStream): Long? {
        val value = input.readLong()
        return if (value < 0) null else value
    }
}
