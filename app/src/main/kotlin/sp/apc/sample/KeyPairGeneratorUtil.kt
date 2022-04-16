package sp.apc.sample

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom

object KeyPairGeneratorUtil {
    fun generateKeyPair(provider: String, algorithm: String, size: Int, random: SecureRandom): KeyPair {
        val generator = KeyPairGenerator.getInstance(algorithm, provider)
        generator.initialize(size, random)
        return generator.generateKeyPair()
    }
}
