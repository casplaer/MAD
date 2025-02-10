import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec

class PassKeyManager(private val context: Context) {

    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val passKeyAlias = "my_pass_key_alias"

    // Проверяем, есть ли уже сохраненный Pass Key
    fun isPassKeyInitialized(): Boolean {
        return try {
            val key = keyStore.getKey(passKeyAlias, null)
            key != null
        } catch (e: Exception) {
            false
        }
    }

    // Генерация нового Pass Key
    fun generateNewPassKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGen.init(
            KeyGenParameterSpec.Builder(passKeyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGen.generateKey()
    }

    // Получение Pass Key
    fun getPassKey(): SecretKey {
        return keyStore.getKey(passKeyAlias, null) as SecretKey
    }

    // Шифрование данных (например, Pass Key)
    fun encryptData(data: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getPassKey())
        val iv = cipher.iv
        val encryption = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encryption + iv, Base64.DEFAULT)
    }

    // Расшифровка данных
    fun decryptData(encryptedData: String): String {
        val data = Base64.decode(encryptedData, Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = data.copyOfRange(data.size - 12, data.size)
        val encryptedBytes = data.copyOfRange(0, data.size - 12)
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getPassKey(), gcmSpec)
        val decryptedData = cipher.doFinal(encryptedBytes)
        return String(decryptedData)
    }
}
