package sp.apc.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import sp.apc.sample.util.androidx.compose.foundation.layout.RowButtons
import sp.apc.sample.util.androidx.compose.ui.window.DialogTextField
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.spec.IvParameterSpec

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

    private fun encrypt(list: List<String>) {
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
            list.forEach { array.put(it) }
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

    private fun decrypt(json: JSONObject, password: String): JSONArray {
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
        return JSONArray(decoded)
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
            val values = remember { mutableStateOf<List<String>?>(null)}
            if (value.isNullOrEmpty()) {
                Init.Screen(
                    onEncrypt = { password ->
                        init(password = password)
                        data.value = getSharedPreferences().getString("data", null)
                        values.value = emptyList()
                    }
                )
            } else {
                val list = values.value?.toMutableList()
                if (list == null) {
                    OnUnlock(
                        onUnlock = { password ->
                            values.value = decrypt(JSONObject(value), password = password).let { jsonArray ->
                                (0 until jsonArray.length()).map {
                                    jsonArray.getString(it)
                                }
                            }
                        },
                        onDelete = {
                            getSharedPreferences().edit().remove("data").apply()
                            data.value = null
                        }
                    )
                } else {
                    Encrypted.Screen(
                        list = list,
                        onDelete = {
                            getSharedPreferences().edit().remove("data").apply()
                            data.value = null
                        },
                        onLock = {
                            data.value = getSharedPreferences().getString("data", null)
                            values.value = null
                        },
                        onAdd = { item ->
                            values.value = list.also {
                                it.add(item)
                                encrypt(it)
                            }
                        },
                        onDeleteItem = { index ->
                            values.value = list.also {
                                it.removeAt(index)
                                encrypt(it)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            view.setOnApplyWindowInsetsListener(null)
            setContent {
                Presentation()
            }
            windowInsets
        }
    }
}
