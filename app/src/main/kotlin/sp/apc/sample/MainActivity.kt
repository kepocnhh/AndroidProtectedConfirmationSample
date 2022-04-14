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
import org.bouncycastle.util.encoders.Base64Encoder
import org.json.JSONObject
import sp.apc.sample.util.androidx.compose.foundation.text.Text
import javax.crypto.spec.IvParameterSpec

class MainActivity : AppCompatActivity() {
    @Composable
    private fun BoxScope.OnEncrypted(
        onDelete: () -> Unit,
        onRead: (String) -> Unit
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(128.dp)
                .background(color = Color(0xff222222))
        ) {
            val isRequested = remember { mutableStateOf(false) }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(color = Color(0xffffffff))
                    .clickable {
                        isRequested.value = true
                    },
                alignment = Alignment.Center,
                text = "read",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xff000000)
                )
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
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
                                        onRead(password.value)
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
    private fun BoxScope.OnEmpty(onEncrypt: (String, String) -> Unit) {
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
            text = "+",
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
                        text = "decrypted",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xff000000)
                        )
                    )
                    val decrypted = remember { mutableStateOf("") }
                    BasicTextField(
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        maxLines = 1,
                        value = decrypted.value,
                        onValueChange = {
                            decrypted.value = it
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color(0xffffffff)
                        )
                    )
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
                                    onEncrypt(decrypted.value, password.value)
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
        val encrypted = cipher(
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

    private fun decrypt(json: JSONObject, password: String) {
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
        try {
            val decrypted = cipher(
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
            if (value.isNullOrEmpty()) {
                OnEmpty(
                    onEncrypt = { decrypted, password ->
                        encrypt(decrypted = decrypted, password = password)
                        data.value = getSharedPreferences().getString("data", null)
                    }
                )
            } else {
                OnEncrypted(
                    onDelete = {
                        getSharedPreferences().edit().remove("data").apply()
                        data.value = null
                    },
                    onRead = { password ->
                        decrypt(json = JSONObject(value), password = password)
                    }
                )
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
