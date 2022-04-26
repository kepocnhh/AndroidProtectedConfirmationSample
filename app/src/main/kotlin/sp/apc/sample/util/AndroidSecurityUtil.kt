package sp.apc.sample.util

import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher

object AndroidSecurityUtil {
    const val TYPE = "AndroidKeyStore"
    const val ALGORITHM = KeyProperties.KEY_ALGORITHM_RSA
    const val BLOCK_MODE = KeyProperties.BLOCK_MODE_ECB
    const val PADDING = KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1

    fun keyStore(): KeyStore {
        val result = KeyStore.getInstance(TYPE)
        result.load(null)
        return result
    }

    fun cipher(): Cipher {
        val transformation = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        return Cipher.getInstance(transformation)
    }

    fun keyPairGenerator(params: AlgorithmParameterSpec): KeyPairGenerator {
        val result = KeyPairGenerator.getInstance(ALGORITHM, TYPE)
        result.initialize(params)
        return result
    }
}
