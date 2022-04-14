package sp.apc.sample

interface KeyDerivationFunction {
    fun generateBytes(password: CharArray, salt: ByteArray, size: Int): ByteArray
}
