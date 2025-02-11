import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class PassKeyManager(private val context: Context) {

    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val passKeyAlias = "user_passkey"

    fun isPassKeyInitialized(): Boolean {
        val sharedPreferences = context.getSharedPreferences("passkey_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.contains("encrypted_passkey")
    }

    fun generateNewPassKey(passKey: String) {
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGen.init(
            KeyGenParameterSpec.Builder(passKeyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )

        val secretKey = keyGen.generateKey()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedPassKey = cipher.doFinal(passKey.toByteArray(Charset.forName("UTF-8")))

        val finalData = iv + encryptedPassKey

        val sharedPreferences = context.getSharedPreferences("passkey_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("encrypted_passkey", Base64.encodeToString(finalData, Base64.DEFAULT))
            .apply()
    }

    fun validatePasskey(inputPasskey: String): Boolean {
        val sharedPreferences = context.getSharedPreferences("passkey_prefs", Context.MODE_PRIVATE)
        val encryptedPasskey = sharedPreferences.getString("encrypted_passkey", null) ?: return false

        val decodedPasskey = Base64.decode(encryptedPasskey, Base64.DEFAULT)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = decodedPasskey.take(12).toByteArray()
        val encryptedData = decodedPasskey.drop(12).toByteArray()

        val secretKey = keyStore.getKey(passKeyAlias, null) as SecretKey
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))

        val decryptedPasskey = String(cipher.doFinal(encryptedData), Charset.forName("UTF-8"))
        return decryptedPasskey == inputPasskey
    }

    fun resetPassKey() {
        val sharedPreferences = context.getSharedPreferences("passkey_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("encrypted_passkey").apply()

        try {
            keyStore.deleteEntry(passKeyAlias)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
