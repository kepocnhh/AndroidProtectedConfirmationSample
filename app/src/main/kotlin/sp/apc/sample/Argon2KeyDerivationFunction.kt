package sp.apc.sample

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

class Argon2KeyDerivationFunction(
    private val type: Int,
    private val version: Int,
    private val additional: ByteArray,
    private val secret: ByteArray,
) : KeyDerivationFunction {
    override fun generateBytes(
        password: CharArray,
        salt: ByteArray,
        size: Int
    ): ByteArray {
        val builder = Argon2Parameters.Builder(type)
            .withVersion(version)
            .withIterations(2)
            .withMemoryPowOfTwo(16)
            .withParallelism(1)
            .withAdditional(additional)
            .withSecret(secret)
            .withSalt(salt)
        val generator = Argon2BytesGenerator()
        generator.init(builder.build())
        val array = ByteArray(size)
        generator.generateBytes(password, array)
        return array
    }
}
