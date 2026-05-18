package com.gamevault.data.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor() {

    companion object {
        private const val KEYSTORE_ALIAS = "gamevault_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createSecretKey()
    }

    private fun createSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val keyGenSpec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(keyGenSpec)
        return keyGenerator.generateKey()
    }

    fun encryptFile(sourceFile: File, destFile: File): Boolean {
        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv

            FileInputStream(sourceFile).use { fis ->
                FileOutputStream(destFile).use { fos ->
                    // Write IV first (12 bytes)
                    fos.write(iv)

                    // Encrypt and write data
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        val encrypted = cipher.update(buffer, 0, bytesRead)
                        if (encrypted != null) {
                            fos.write(encrypted)
                        }
                    }
                    val finalBlock = cipher.doFinal()
                    if (finalBlock != null) {
                        fos.write(finalBlock)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun decryptFile(encryptedFile: File, destFile: File): Boolean {
        return try {
            val secretKey = getOrCreateSecretKey()

            FileInputStream(encryptedFile).use { fis ->
                // Read IV first (12 bytes)
                val iv = ByteArray(GCM_IV_LENGTH)
                fis.read(iv)

                val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

                FileOutputStream(destFile).use { fos ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        val decrypted = cipher.update(buffer, 0, bytesRead)
                        if (decrypted != null) {
                            fos.write(decrypted)
                        }
                    }
                    val finalBlock = cipher.doFinal()
                    if (finalBlock != null) {
                        fos.write(finalBlock)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun encryptBytes(data: ByteArray): ByteArray? {
        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(data)
            iv + encrypted
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decryptBytes(encryptedData: ByteArray): ByteArray? {
        return try {
            val secretKey = getOrCreateSecretKey()
            val iv = encryptedData.copyOfRange(0, GCM_IV_LENGTH)
            val data = encryptedData.copyOfRange(GCM_IV_LENGTH, encryptedData.size)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            cipher.doFinal(data)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isEncryptionAvailable(): Boolean {
        return try {
            getOrCreateSecretKey()
            true
        } catch (e: Exception) {
            false
        }
    }
}