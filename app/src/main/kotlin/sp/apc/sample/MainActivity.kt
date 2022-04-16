package sp.apc.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.bouncycastle.crypto.params.Argon2Parameters
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Base64Encoder
import org.json.JSONArray
import org.json.JSONObject
import sp.apc.sample.util.androidx.compose.foundation.text.Text
import sp.apc.sample.util.androidx.compose.ui.window.DialogTextField
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.spec.IvParameterSpec

class MainActivity : AppCompatActivity() {
    @Composable
    private fun BoxScope.OnUnlock(
        onDelete: () -> Unit,
        onUnlock: (String) -> Unit
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(128.dp)
                .background(color = Color(0xff222222))
        ) {
            val isRequested = remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .background(color = Color(0xffffffff))
                        .clickable {
                            isRequested.value = true
                        },
                    alignment = Alignment.Center,
                    text = "unlock",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xff000000)
                    )
                )
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .background(color = Color(0xffffffff))
                        .clickable {
                            onDelete()
                        },
                    alignment = Alignment.Center,
                    text = "delete",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xffff0000)
                    )
                )
            }
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
            }
        }
    }

    @Composable
    private fun BoxScope.OnEncrypted(
        list: List<String>,
        onDelete: () -> Unit,
        onLock: () -> Unit,
        onAdd: (String) -> Unit,
        onDeleteItem: (Int) -> Unit
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(256.dp)
                .background(color = Color(0xff222222))
        ) {
            for (i in list.indices) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    BasicText(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        text = list[i],
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xffffffff)
                        )
                    )
                    BasicText(
                        modifier = Modifier
                            .width(48.dp)
                            .fillMaxHeight()
                            .background(color = Color(0xff000000))
                            .clickable {
                                onDeleteItem(i)
                            },
                        text = "x",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xffff0000)
                        )
                    )
                }
            }
            val isRequested = remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                val isAdd = remember { mutableStateOf(false) }
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .background(color = Color(0xffffffff))
                        .clickable {
                            isAdd.value = true
                        },
                    alignment = Alignment.Center,
                    text = "add",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xff000000)
                    )
                )
                if (isAdd.value) {
                    DialogTextField(
                        label = "add",
                        onDismissRequest = {
                            isAdd.value = false
                        },
                        onConfirm = { item ->
                            onAdd(item)
                            isAdd.value = false
                        }
                    )
                }
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .background(color = Color(0xff000000))
                        .clickable {
                            onDelete()
                        },
                    alignment = Alignment.Center,
                    text = "delete",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xffff0000)
                    )
                )
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .background(color = Color(0xffffffff))
                        .clickable {
                            onLock()
                        },
                    alignment = Alignment.Center,
                    text = "lock",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xffff0000)
                    )
                )
            }
            if (isRequested.value) {
                Dialog(
                    onDismissRequest = {
                        isRequested.value = false
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .width(128.dp)
                            .background(color = Color(0xff222222))
                    ) {
                        BasicText(
                            text = "password",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color(0xff000000)
                            )
                        )
                        val password = remember { mutableStateOf("") }
                        BasicTextField(
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            maxLines = 1,
                            value = password.value,
                            onValueChange = {
                                password.value = it
                            },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                color = Color(0xffffffff)
                            )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clickable {
                                        isRequested.value = false
                                        TODO()
                                    }
                                    .padding(start = 8.dp, end = 8.dp),
                                alignment = Alignment.Center,
                                text = "ok",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color(0xff00ff00)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BoxScope.OnEmpty(text: String, onEncrypt: (String) -> Unit) {
        val isRequested = remember { mutableStateOf(false) }
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp)
                .background(color = Color(0xffffffff))
                .clickable {
                    isRequested.value = true
                },
            alignment = Alignment.Center,
            text = text,
            style = TextStyle(
                fontSize = 16.sp,
                color = Color(0xff000000)
            )
        )
        if (isRequested.value) {
            Dialog(
                onDismissRequest = {
                    isRequested.value = false
                }
            ) {
                Column(
                    modifier = Modifier
                        .width(128.dp)
                        .background(color = Color(0xff222222))
                ) {
                    BasicText(
                        text = "password",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xff000000)
                        )
                    )
                    val password = remember { mutableStateOf("") }
                    BasicTextField(
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        maxLines = 1,
                        value = password.value,
                        onValueChange = {
                            password.value = it
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color(0xffffffff)
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxHeight()
                                .clickable {
                                    isRequested.value = false
                                    onEncrypt(password.value)
                                }
                                .padding(start = 8.dp, end = 8.dp),
                            alignment = Alignment.Center,
                            text = "ok",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Color(0xff00ff00)
                            )
                        )
                    }
                }
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
        val provider = BouncyCastleProvider.PROVIDER_NAME
        val pair = KeyPairGeneratorUtil.generateKeyPair(
            provider = provider,
            algorithm = "RSA",
            size = 2048,
            random = random
        )
        val params = IvParameterSpec(random.nextBytes(16))
        val private = cipher(
            provider = provider,
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
            provider = provider,
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
        val provider = BouncyCastleProvider.PROVIDER_NAME
        val factory = KeyFactory.getInstance("RSA", provider)
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
            provider = provider,
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

    private fun encrypt(decrypted: String, password: String) {
        val random = secureRandom()
        val salt = random.nextBytes(16)
        val encoder = Base64Encoder()
        val algorithm = "AES"
        val key = Argon2KeyDerivationFunction(
            type = Argon2Parameters.ARGON2_id,
            version = Argon2Parameters.ARGON2_VERSION_10,
            secret = encoder.encode(getDeviceId().toByteArray(Charsets.UTF_8)),
            additional = encoder.encode(getAdditional().toByteArray(Charsets.UTF_8))
        ).generateBytes(
            password = password.toCharArray(),
            salt = salt,
            size = 16
        ).toSecretKey(algorithm)
        val params = IvParameterSpec(random.nextBytes(16))
        val provider = BouncyCastleProvider.PROVIDER_NAME
        val encrypted = cipher(
            provider = provider,
            algorithm = algorithm,
            blockMode = "CBC",
            paddings = "PKCS7Padding"
        ).encrypt(
            key = key,
            params = params,
            decrypted = encoder.encode(decrypted.toByteArray(Charsets.UTF_8))
        )
        val json = JSONObject()
            .put("salt", encoder.encode(salt, Charsets.UTF_8))
            .put("iv", encoder.encode(params.iv, Charsets.UTF_8))
            .put("encrypted", encoder.encode(encrypted, Charsets.UTF_8))
        getSharedPreferences().edit().putString("data", json.toString()).apply()
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
        val provider = BouncyCastleProvider.PROVIDER_NAME
        val private = cipher(
            provider = provider,
            algorithm = algorithm,
            blockMode = "CBC",
            paddings = "PKCS7Padding"
        ).decrypt(
            key = key,
            params = IvParameterSpec(encoder.decode(json.getString("iv"))),
            encrypted = encoder.decode(json.getJSONObject("key").getString("private"))
        )
        val factory = KeyFactory.getInstance("RSA", provider)
        val decrypted = cipher(
            provider = provider,
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

    private fun decryptOld(json: JSONObject, password: String) {
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
        val provider = BouncyCastleProvider.PROVIDER_NAME
        try {
            val decrypted = cipher(
                provider = provider,
                algorithm = algorithm,
                blockMode = "CBC",
                paddings = "PKCS7Padding"
            ).decrypt(
                key = key,
                params = IvParameterSpec(encoder.decode(json.getString("iv"))),
                encrypted = encoder.decode(json.getString("encrypted"))
            )
            showToast(encoder.decode(decrypted, Charsets.UTF_8))
        } catch (e: Throwable) {
            showToast("decrypt error: $e")
        }
        println("json: " + json.toString())
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
                OnEmpty(
                    text = "init",
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
                    OnEncrypted(
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
