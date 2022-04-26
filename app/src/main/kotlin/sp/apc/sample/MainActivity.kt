package sp.apc.sample

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.bouncycastle.crypto.params.Argon2Parameters
import org.bouncycastle.util.encoders.Base64Encoder
import org.json.JSONArray
import org.json.JSONObject
import sp.apc.sample.module.Encrypted
import sp.apc.sample.module.Init
import sp.apc.sample.util.AndroidSecurityUtil
import sp.apc.sample.util.androidx.compose.foundation.layout.RowButtons
import sp.apc.sample.util.androidx.compose.ui.window.DialogTextField
import sp.apc.sample.util.biometricPrompt
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.security.auth.x500.X500Principal

class MainActivity : AppCompatActivity() {
    @Composable
    private fun BoxScope.OnUnlock(
        onDelete: () -> Unit,
        onUnlock: (String) -> Unit
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(128.dp)
        ) {
            val isRequested = remember { mutableStateOf(false) }
            if (isRequested.value) {
                DialogTextField(
                    label = "password",
                    onDismissRequest = {
                        isRequested.value = false
                    },
                    onConfirm = { password ->
                        isRequested.value = false
                        onUnlock(password)
                    }
                )
            } else {
                RowButtons(
                    buttons = mapOf(
                        "unlock" to {
                            isRequested.value = true
                        },
                        "delete" to {
                            onDelete()
                        }
                    )
                )
            }
        }
    }

    private fun generateBytes(
        password: CharArray,
        salt: ByteArray,
        secret: ByteArray,
        additional: ByteArray
    ): ByteArray {
        return Argon2KeyDerivationFunction(
            type = Argon2Parameters.ARGON2_id,
            version = Argon2Parameters.ARGON2_VERSION_10,
            secret = secret,
            additional = additional
        ).generateBytes(
            password = password,
            salt = salt,
            size = 16
        )
    }

    private fun init(password: String) {
        val random = secureRandom()
        val salt = random.nextBytes(16)
        val encoder = Base64Encoder()
        val algorithm = "AES"
        val pair = KeyPairGeneratorUtil.generateKeyPair(
            algorithm = "RSA",
            size = 2048,
            random = random
        )
        val params = IvParameterSpec(random.nextBytes(16))
        val private = cipher(
            algorithm = algorithm,
            blockMode = "CBC",
            paddings = "PKCS7Padding"
        ).encrypt(
            key = generateBytes(
                secret = encoder.encode(getDeviceId().toByteArray(Charsets.UTF_8)),
                additional = encoder.encode(getAdditional().toByteArray(Charsets.UTF_8)),
                password = password.toCharArray(),
                salt = salt
            ).toSecretKey(algorithm),
            params = params,
            decrypted = pair.private.encoded
        )
        val values = cipher(
            algorithm = "RSA",
            blockMode = "ECB",
            paddings = "PKCS1Padding"
        ).encrypt(
            key = pair.public,
            decrypted = encoder.encode("[]".toByteArray(Charsets.UTF_8))
        )
        val json = JSONObject()
            .put("values", encoder.encode(values, Charsets.UTF_8))
            .put("salt", encoder.encode(salt, Charsets.UTF_8))
            .put("iv", encoder.encode(params.iv, Charsets.UTF_8))
            .put(
                "key",
                JSONObject()
                    .put("private", encoder.encode(private, Charsets.UTF_8))
                    .put("public", encoder.encode(pair.public.encoded, Charsets.UTF_8))
            )
        println("json: " + json.toString())
        getSharedPreferences().edit().putString("data", json.toString()).apply()
    }

    private fun encrypt(map: Map<String, String>) {
        val data = getSharedPreferences().getString("data", null)
        check(!data.isNullOrEmpty())
        val json = JSONObject(data)
        val factory = KeyFactory.getInstance("RSA")
        val encoder = Base64Encoder()
        val public = factory.generatePublic(
            X509EncodedKeySpec(
                encoder.decode(json.getJSONObject("key").getString("public"))
            )
        )
        val array = JSONArray().also { array ->
            map.forEach { (key, encrypted) ->
                array.put(JSONObject().put("key", key).put("encrypted", encrypted))
            }
        }
        val encrypted = cipher(
            algorithm = "RSA",
            blockMode = "ECB",
            paddings = "PKCS1Padding"
        ).encrypt(
            key = public,
            decrypted = encoder.encode(array.toString().toByteArray(Charsets.UTF_8))
        )
        getSharedPreferences()
            .edit()
            .putString(
                "data",
                json.put(
                    "values",
                    encoder.encode(encrypted, Charsets.UTF_8)
                ).toString()
            )
            .apply()
    }

    private fun decrypt(json: JSONObject, password: String): Map<String, String> {
        val encoder = Base64Encoder()
        val algorithm = "AES"
        val key = Argon2KeyDerivationFunction(
            type = Argon2Parameters.ARGON2_id,
            version = Argon2Parameters.ARGON2_VERSION_10,
            secret = encoder.encode(getDeviceId().toByteArray(Charsets.UTF_8)),
            additional = encoder.encode(getAdditional().toByteArray(Charsets.UTF_8))
        ).generateBytes(
            password = password.toCharArray(),
            salt = encoder.decode(json.getString("salt")),
            size = 16
        ).toSecretKey(algorithm)
        val private = cipher(
            algorithm = algorithm,
            blockMode = "CBC",
            paddings = "PKCS7Padding"
        ).decrypt(
            key = key,
            params = IvParameterSpec(encoder.decode(json.getString("iv"))),
            encrypted = encoder.decode(json.getJSONObject("key").getString("private"))
        )
        val factory = KeyFactory.getInstance("RSA")
        val decrypted = cipher(
            algorithm = "RSA",
            blockMode = "ECB",
            paddings = "PKCS1Padding"
        ).decrypt(
            key = factory.generatePrivate(PKCS8EncodedKeySpec(private)),
            encrypted = encoder.decode(json.getString("values"))
        )
        val decoded = encoder.decode(decrypted, Charsets.UTF_8)
        println("decoded: $decoded")
        return JSONArray(decoded).let { array ->
            (0 until array.length()).associate { index ->
                val jo = array.getJSONObject(index)
                jo.getString("key") to jo.getString("encrypted")
            }
        }
    }

    private fun onEncrypt(key: String, decrypted: String) {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                TODO("code $errorCode $errString")
            }

            override fun onAuthenticationFailed() {
                TODO()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                TODO()
            }
        }
        val prompt = biometricPrompt(callback)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("...title")
            .setSubtitle("...subtitle")
            .setDescription("...description")
            .setNegativeButtonText("...negative")
//            .setAllowedAuthenticators(authenticators)
            .build()
        val keyStore = AndroidSecurityUtil.keyStore()
        val keyName = BuildConfig.APPLICATION_ID + "_" + key
        val secret = keyStore.getKey(keyName, null) ?: TODO()
        val cipher = AndroidSecurityUtil.cipher()
        val iv = ByteArray(16) {
            keyName.toByteArray(Charsets.UTF_8)[it % keyName.length]
        }
        cipher.init(Cipher.DECRYPT_MODE, secret, IvParameterSpec(iv))
        prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
    }

    @Composable
    private fun Presentation() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xff222222))
        ) {
            val data = remember {
                mutableStateOf(
                    getSharedPreferences().getString("data", null)
                )
            }
            val value = data.value
            val values = remember { mutableStateOf<Map<String, String>?>(null)}
            if (value.isNullOrEmpty()) {
                Init.Screen(
                    onEncrypt = { password ->
                        init(password = password)
                        data.value = getSharedPreferences().getString("data", null)
                        values.value = emptyMap()
                    }
                )
            } else {
                val map = values.value?.toMutableMap()
                if (map == null) {
                    OnUnlock(
                        onUnlock = { password ->
                            values.value = decrypt(JSONObject(value), password = password)
                        },
                        onDelete = {
                            getSharedPreferences().edit().remove("data").apply()
                            data.value = null
                        }
                    )
                } else {
                    Encrypted.Screen(
                        map = map,
                        onDelete = {
                            getSharedPreferences().edit().remove("data").apply()
                            data.value = null
                        },
                        onLock = {
                            data.value = getSharedPreferences().getString("data", null)
                            values.value = null
                        },
                        onAdd = { key, decrypted ->
                            onEncrypt(key = key, decrypted = decrypted)
//                            values.value = list.also {
//                                it.add(item)
//                                encrypt(it)
//                            }
                        },
                        onDeleteItem = { key ->
                            values.value = map.also {
                                it.remove(key)
                                encrypt(it)
                            }
                        },
                        onDecrypt = { key, encrypted ->
                            TODO()
                        }
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = AndroidSecurityUtil.keyStore()
        val aliases = store.aliases().toList()
        println("aliases: $aliases")
        when (aliases.size) {
            1 -> {
                val alias = aliases.single()
                check(alias == BuildConfig.APPLICATION_ID)
                val pair = store.getEntry(alias, null)
                check(pair is KeyStore.PrivateKeyEntry)
                val certificate = pair.certificate
                check(certificate is X509Certificate)
                println("time: " + (certificate.notAfter.time - System.currentTimeMillis()))
                val encrypted = getSharedPreferences().getString("encrypted", null)
                check(!encrypted.isNullOrEmpty())
                val callback = object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        TODO("code $errorCode $errString")
                    }

                    override fun onAuthenticationFailed() {
                        TODO()
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        val cipher = AndroidSecurityUtil.cipher()
                        cipher.init(Cipher.DECRYPT_MODE, pair.privateKey)
                        val encoder = Base64Encoder()
                        val decrypted = cipher.doFinal(
                            encoder.decode(encrypted.toByteArray(Charsets.UTF_8))
                        ).let {
                            encoder.decode(it, Charsets.UTF_8)
                        }
                        println("""
                            decrypted: $decrypted
                            encrypted: $encrypted
                        """.trimIndent())
                    }
                }
                val prompt = biometricPrompt(callback)
                val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                println("can authenticate: " + canAuthenticate(authenticators))
                val info = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("...title")
                    .setSubtitle("...subtitle")
                    .setDescription("...description")
//                    .setNegativeButtonText("...negative")
                    .setAllowedAuthenticators(authenticators)
//                    .setDeviceCredentialAllowed(true)
                    .build()
                prompt.authenticate(info)
            }
            0 -> {
                val random = secureRandom()
                val alias = BuildConfig.APPLICATION_ID
                val notBefore = System.currentTimeMillis()
                val params = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT)
                    .setCertificateSubject(X500Principal("CN=root/" + BuildConfig.APPLICATION_ID))
                    .setCertificateNotBefore(Date(notBefore))
                    .setCertificateNotAfter(Date(notBefore + 365L * 24 * 60 * 60 * 1_000))
                    .setCertificateSerialNumber(BigInteger(64, random))
                    .setKeyValidityStart(Date(notBefore))
                    .setKeyValidityEnd(Date(notBefore + 365L * 24 * 60 * 60 * 1_000))
                    .setBlockModes(AndroidSecurityUtil.BLOCK_MODE)
                    .setEncryptionPaddings(AndroidSecurityUtil.PADDING)
                    .setKeySize(2048)
                    .setUserAuthenticationRequired(false)
                    .build()
                val pair = AndroidSecurityUtil.keyPairGenerator(params).generateKeyPair()
                //
                val encoder = Base64Encoder()
                val decrypted = BuildConfig.APPLICATION_ID
                val encrypted = AndroidSecurityUtil.cipher().encrypt(
                    key = pair.public,
                    decrypted = encoder.encode(decrypted.toByteArray(Charsets.UTF_8))
                ).let {
                    encoder.encode(it, Charsets.UTF_8)
                }
                println("""
                    decrypted: $decrypted
                    encrypted: $encrypted
                """.trimIndent())
                getSharedPreferences()
                    .edit()
                    .putString("encrypted", encrypted)
                    .apply()
            }
            else -> TODO()
        }
        return
        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            view.setOnApplyWindowInsetsListener(null)
            setContent {
                Presentation()
            }
            windowInsets
        }
    }
}
