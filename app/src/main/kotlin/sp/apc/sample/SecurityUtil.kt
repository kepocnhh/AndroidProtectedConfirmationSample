package sp.apc.sample

import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

fun Cipher.encrypt(
    key: SecretKey,
    params: AlgorithmParameterSpec,
    decrypted: ByteArray
): ByteArray {
    init(Cipher.ENCRYPT_MODE, key, params)
    return doFinal(decrypted)
}

fun Cipher.decrypt(
    key: SecretKey,
    params: AlgorithmParameterSpec,
    encrypted: ByteArray
): ByteArray {
    init(Cipher.DECRYPT_MODE, key, params)
    return doFinal(encrypted)
}

fun SecureRandom.nextBytes(size: Int): ByteArray {
    val array = ByteArray(size)
    nextBytes(array)
    return array
}

fun ByteArray.toSecretKey(algorithm: String): SecretKey {
    return SecretKeySpec(this, algorithm)
}

fun cipher(
    algorithm: String,
    blockMode: String,
    paddings: String
): Cipher {
    return Cipher.getInstance("$algorithm/$blockMode/$paddings")
}
