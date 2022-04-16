package sp.apc.sample

import java.security.Key
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

fun Cipher.encrypt(
    key: Key,
    params: AlgorithmParameterSpec,
    decrypted: ByteArray
): ByteArray {
    init(Cipher.ENCRYPT_MODE, key, params)
    return doFinal(decrypted)
}

fun Cipher.encrypt(
    key: PublicKey,
    decrypted: ByteArray
): ByteArray {
    init(Cipher.ENCRYPT_MODE, key)
    return doFinal(decrypted)
}

fun Cipher.decrypt(
    key: Key,
    params: AlgorithmParameterSpec,
    encrypted: ByteArray
): ByteArray {
    init(Cipher.DECRYPT_MODE, key, params)
    return doFinal(encrypted)
}

fun Cipher.decrypt(
    key: PrivateKey,
    encrypted: ByteArray
): ByteArray {
    init(Cipher.DECRYPT_MODE, key)
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
    provider: String,
    algorithm: String,
    blockMode: String,
    paddings: String
): Cipher {
    return Cipher.getInstance("$algorithm/$blockMode/$paddings", provider)
}
