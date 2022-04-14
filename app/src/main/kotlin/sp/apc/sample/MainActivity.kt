package sp.apc.sample

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.security.ConfirmationPrompt
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Base64Encoder
import org.bouncycastle.util.encoders.Hex
import org.json.JSONObject
import sp.apc.sample.util.androidx.compose.foundation.text.Text
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.SecureRandom
import java.security.Security
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

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

    private fun ByteArray.toString(charset: Charset): String {
        return String(this, charset)
    }

    private fun Base64Encoder.encode(decoded: ByteArray): ByteArray {
        return ByteArrayOutputStream().use {
            encode(decoded, 0, decoded.size, it)
            it.toByteArray()
        }
    }

    private fun Base64Encoder.decode(encoded: ByteArray): ByteArray {
        return ByteArrayOutputStream().use {
            decode(encoded, 0, encoded.size, it)
            it.toByteArray()
        }
    }

    private fun keyDerivationFunction(
        password: String,
        salt: ByteArray,
        size: Int
    ): ByteArray {
        val type = Argon2Parameters.ARGON2_id
        val builder = Argon2Parameters.Builder(type)
            .withVersion(Argon2Parameters.ARGON2_VERSION_10)
            .withIterations(2)
            .withMemoryPowOfTwo(16)
            .withParallelism(1)
//            .withSecret(password.toByteArray(Charsets.UTF_8)) // todo
            .withSalt(salt)
        val generator = Argon2BytesGenerator()
        generator.init(builder.build())
        return ByteArray(size).also {
            generator.generateBytes(password.toCharArray(), it, 0, it.size)
        }
    }

    private fun encrypt(decrypted: String, password: String) {
//        val provider = BouncyCastleProvider.PROVIDER_NAME
        val random = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SecureRandom.getInstanceStrong()
        } else {
            val algorithm = "SHA1PRNG"
            SecureRandom.getInstance(algorithm)
        }
        val salt = ByteArray(16).also {
            random.nextBytes(it)
        }
        val hashed = keyDerivationFunction(
            password = password,
            salt = salt,
            size = 16
        )
        val params = ByteArray(16).let {
            random.nextBytes(it)
            IvParameterSpec(it)
        }
        val algorithm = "AES"
        val key: SecretKey = SecretKeySpec(hashed, algorithm)
        val blockMode = "CBC"
//        val paddings = "NoPadding"
        val paddings = "PKCS7Padding"
        val transformation = "$algorithm/$blockMode/$paddings"
        val encoder = Base64Encoder()
        val encrypted = Cipher.getInstance(transformation).let {
            it.init(Cipher.ENCRYPT_MODE, key, params)
            it.doFinal(encoder.encode(decrypted.toByteArray(Charsets.UTF_8)))
        }
        val json = JSONObject()
            .put("salt", encoder.encode(salt).toString(Charsets.UTF_8))
            .put("iv", encoder.encode(params.iv).toString(Charsets.UTF_8))
            .put("encrypted", encoder.encode(encrypted).toString(Charsets.UTF_8))
        getSharedPreferences().edit().putString("data", json.toString()).apply()
    }

    private fun decrypt(json: JSONObject, password: String) {
        val encoder = Base64Encoder()
        val salt = json.getString("salt").let {
            encoder.decode(it.toByteArray(Charsets.UTF_8))
        }
        val hashed = keyDerivationFunction(
            password = password,
            salt = salt,
            size = 16
        )
        val algorithm = "AES"
        val key: SecretKey = SecretKeySpec(hashed, algorithm)
        val blockMode = "CBC"
        val paddings = "PKCS7Padding"
        val transformation = "$algorithm/$blockMode/$paddings"
        val params = json.getString("iv").let {
            val d = encoder.decode(it.toByteArray(Charsets.UTF_8))
            IvParameterSpec(d)
        }
        val encrypted = json.getString("encrypted").let {
            encoder.decode(it.toByteArray(Charsets.UTF_8))
        }
        try {
            val decrypted = Cipher.getInstance(transformation).let {
                it.init(Cipher.DECRYPT_MODE, key, params)
                it.doFinal(encrypted)
            }
            showToast(encoder.decode(decrypted).toString(Charsets.UTF_8))
        } catch (e: Throwable) {
            showToast("decrypt error: $e")
        }
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
        val algorithms = Security.getAlgorithms("SecretKeyFactory")
//        println("algorithms: " + algorithms.sorted().joinToString(separator = "\n"))
//        val decrypted = "2022/04/14 19:04"
//        val password = "123456"
//        val random = SecureRandom()
//        val salt = ByteArray(16).also {
//            random.nextBytes(it)
//        }
//        val hashed = keyDerivationFunction(
//            password = password,
//            salt = salt,
//            size = 32
//        )
//        val algorithm = "AES"
//        val params = ByteArray(16).let {
//            random.nextBytes(it)
//            IvParameterSpec(it)
//        }
//        val key: SecretKey = SecretKeySpec(hashed, 0, hashed.size, algorithm)
//        val blockMode = "CBC"
//        val paddings = "NoPadding"
//        val transformation = "$algorithm/$blockMode/$paddings"
//        val cipher = Cipher.getInstance(transformation)
//        val encrypted = cipher.let {
//            it.init(Cipher.ENCRYPT_MODE, key, params)
//            it.doFinal(decrypted.toByteArray(Charsets.UTF_8))
//        }
//        val result = cipher.let {
//            it.init(Cipher.DECRYPT_MODE, key, params)
//            it.doFinal(encrypted)
//        }
//        println("""
//            decrypted: $decrypted
//            result: ${String(result, Charsets.UTF_8)}
//        """.trimIndent())
        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            view.setOnApplyWindowInsetsListener(null)
            setContent {
                Presentation()
            }
            windowInsets
        }
    }
}

class MainActivityOld : AppCompatActivity() {
    companion object {
        private fun generateKey(
            algorithm: String,
            blockMode: String,
            paddings: String,
            keySize: Int,
            purposes: Int,
            isAuthenticationRequired: Boolean
        ): SecretKey {
            val keyGenerator = KeyGenerator.getInstance(algorithm, "AndroidKeyStore")
            val keyGenParameterSpec =
                KeyGenParameterSpec.Builder("sp.apc.sample", purposes)
                    .setBlockModes(blockMode)
                    .setEncryptionPaddings(paddings)
                    .setKeySize(keySize)
                    .setUserAuthenticationRequired(isAuthenticationRequired)
                    .build()
            keyGenerator.init(keyGenParameterSpec)
            return keyGenerator.generateKey()
        }
    }

    enum class Step { PROMPTED, AUTHORIZED }

    enum class AuthenticateError {
        NO_HARDWARE, HARDWARE_UNAVAILABLE, NONE_ENROLLED,
    }

    private fun encryptOld(value: String): String {
        val transformation = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
        val cipher = Cipher.getInstance(transformation)
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val publicKey = keyStore.getCertificate("sp.apc.sample").publicKey
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return String(cipher.doFinal(value.toByteArray(Charset.forName("UTF-8"))), Charset.forName("UTF-8"))
    }

    private fun cipherEncrypted(): Cipher {
        val algorithm = KeyProperties.KEY_ALGORITHM_AES
        val blockMode = KeyProperties.BLOCK_MODE_CBC
        val paddings = KeyProperties.ENCRYPTION_PADDING_PKCS7
        val keySize = 256
        val transformation = "$algorithm/$blockMode/$paddings"
        val cipher = Cipher.getInstance(transformation)
        val secretKey = generateKey(
            algorithm = algorithm,
            blockMode = blockMode,
            paddings = paddings,
            keySize = keySize,
            purposes = KeyProperties.PURPOSE_ENCRYPT,
            isAuthenticationRequired = true
        )
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    private fun cipherDecrypted(
        isAuthenticationRequired: Boolean
    ): Cipher {
        val algorithm = KeyProperties.KEY_ALGORITHM_AES
        val blockMode = KeyProperties.BLOCK_MODE_GCM
        val paddings = KeyProperties.ENCRYPTION_PADDING_NONE
        val keySize = 256
        val transformation = "$algorithm/$blockMode/$paddings"
        val cipher = Cipher.getInstance(transformation)
        val secretKey = generateKey(
            algorithm = algorithm,
            blockMode = blockMode,
            paddings = paddings,
            keySize = keySize,
            purposes = KeyProperties.PURPOSE_DECRYPT,
            isAuthenticationRequired = isAuthenticationRequired
        )
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(keySize, null))
        return cipher
    }

    private fun encrypt(value: String): String {
        val cipher = cipherEncrypted()
        val bytes = cipher.doFinal(value.toByteArray(Charset.forName("UTF-8")))
        cipher.iv
        return String(bytes, Charset.forName("UTF-8"))
    }

    @Composable
    private fun NewItemDialog(
        onDismissRequest: () -> Unit,
        onItem: (String, String) -> Unit
    ) {
        Dialog(onDismissRequest = onDismissRequest) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xff222222))
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                BasicText(
                    modifier = Modifier.padding(start = 16.dp),
                    text = "key",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xff000000)
                    )
                )
                val keyState = remember { mutableStateOf("") }
                val valueState = remember { mutableStateOf("") }
                val requester = remember { FocusRequester() }
                BasicTextField(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    singleLine = true,
                    maxLines = 1,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xffffffff)
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            requester.requestFocus()
                        }
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    value = keyState.value,
                    onValueChange = {
                        keyState.value = it
                    }
                )
                BasicText(
                    modifier = Modifier.padding(start = 16.dp),
                    text = "value",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xff000000)
                    )
                )
                BasicTextField(
                    modifier = Modifier
                        .focusRequester(requester)
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    singleLine = true,
                    maxLines = 1,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xffffffff)
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val key = keyState.value
                            val value = valueState.value
                            if (key.isNotEmpty() && value.isNotEmpty()) {
                                onItem(key, value)
                            }
                        }
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    value = valueState.value,
                    onValueChange = {
                        valueState.value = it
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable {
                                val key = keyState.value
                                val value = valueState.value
                                if (key.isNotEmpty() && value.isNotEmpty()) {
                                    onItem(key, value)
                                }
                            }
                    ) {
                        BasicText(
                            text = "add",
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

    @Composable
    private fun ConfirmDialog(
        onDismissRequest: () -> Unit,
        onConfirm: () -> Unit
    ) {
        Dialog(onDismissRequest = onDismissRequest) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xff222222))
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                BasicText(
                    modifier = Modifier.padding(start = 16.dp),
                    text = "delete?",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xff000000)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable {
                                onConfirm()
                            }
                    ) {
                        BasicText(
                            text = "ok",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Color(0xffff0000)
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun DecryptedDialog(
        onDismissRequest: () -> Unit,
        text: String,
        onClick: () -> Unit
    ) {
        Dialog(onDismissRequest = onDismissRequest) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xff222222))
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                BasicText(
                    modifier = Modifier.padding(start = 16.dp),
                    text = text,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xffffffff)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable {
                                onClick()
                            }
                    ) {
                        BasicText(
                            text = "ok",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Color(0xff000000)
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun OnAuthorized() {
        val preferences = getSharedPreferences("sp.apc.sample", Context.MODE_PRIVATE)
        val dataState = remember { mutableStateOf<String?>("{}") }
        dataState.value = preferences.getString("data", null)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xff000000))
        ) {
            val data = dataState.value
            val map: Map<String, String>? = data?.let {
                val jObject = JSONObject(it)
                jObject.keys().asSequence().associateWith { key ->
                    jObject.getString(key)
                }
            }
            val dialogState = remember { mutableStateOf(false) }
            if (map.isNullOrEmpty()) {
                if (dialogState.value) {
                    NewItemDialog(
                        onDismissRequest = {
                            dialogState.value = false
                        },
                        onItem = { key, value ->
                            val encrypted = encrypt(value)
                            val jObject = JSONObject(mapOf(key to encrypted))
                            preferences.edit().putString("data", jObject.toString()).apply()
                            dataState.value = null
                            dialogState.value = false
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .clickable {
                                dialogState.value = true
                            }
                            .padding(16.dp)
                    ) {
                        BasicText(
                            modifier = Modifier.align(Alignment.Center),
                            text = "+",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Color(0xff00ff00)
                            )
                        )
                    }
                }
            } else {
                LazyColumn {
                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                    items(map.toList()) { (key, value) ->
                        val confirmDialogState = remember { mutableStateOf(false) }
                        val decryptedDialogState = remember { mutableStateOf<Boolean?>(null) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            decryptedDialogState.value = false
                                        },
                                        onLongPress = {
                                            confirmDialogState.value = true
                                        }
                                    )
                                }
                                .padding(16.dp)
                        ) {
                            BasicText(
                                text = key,
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color(0xffffffff)
                                )
                            )
                        }
                        if (confirmDialogState.value) {
                            ConfirmDialog(
                                onDismissRequest = {
                                    confirmDialogState.value = false
                                },
                                onConfirm = {
                                    val jObject = JSONObject(
                                        map.toMutableMap().also {
                                            it.remove(key)
                                        }.toMap()
                                    )
                                    preferences.edit().putString("data", jObject.toString()).apply()
                                    dataState.value = null
                                    confirmDialogState.value = false
                                }
                            )
                        }
                        val state = decryptedDialogState.value
                        if (state == false) {
                            val callback = object: BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                    showToast("on -> authentication error $errorCode $errString")
                                    decryptedDialogState.value = null
                                }

                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                                    val cryptoObject = result.cryptoObject ?: TODO()
//                                    val cipher = cryptoObject.cipher ?: TODO()
                                    val cipher = cipherDecrypted(isAuthenticationRequired = false)
                                    val decrypted = cipher.doFinal(value.toByteArray(Charset.forName("UTF-8")))
                                    showToast("on -> succeeded: " + String(decrypted, Charset.forName("UTF-8")))
                                }

                                override fun onAuthenticationFailed() {
                                    TODO()
                                }
                            }
                            val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                            val info = BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Biometric title.")
                                .setSubtitle("Biometric subtitle.")
                                .setAllowedAuthenticators(authenticators)
                                .build()
//                            BiometricPrompt(this@MainActivity, callback).authenticate(info, BiometricPrompt.CryptoObject(cipher))
                            BiometricPrompt(this@MainActivityOld, callback).authenticate(info)
                            decryptedDialogState.value = true
                        }
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(color = Color(0xffffffff))
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
                if (dialogState.value) {
                    NewItemDialog(
                        onDismissRequest = {
                            dialogState.value = false
                        },
                        onItem = { key, value ->
                            val encrypted = encrypt(value)
                            val jObject = JSONObject(
                                map.toMutableMap().also {
                                    it[key] = encrypted
                                }.toMap()
                            )
                            preferences.edit().putString("data", jObject.toString()).apply()
                            dataState.value = null
                            dialogState.value = false
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 64.dp)
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(color = Color(0x88000000))
                            .clickable {
                                dialogState.value = true
                            }
                    ) {
                        BasicText(
                            modifier = Modifier.align(Alignment.Center),
                            text = "+",
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

    @Composable
    private fun Presentation() {
        val stepState = rememberSaveable { mutableStateOf<Step?>(null) }
        when (stepState.value) {
            Step.AUTHORIZED -> OnAuthorized()
            Step.PROMPTED -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xff000000))
                ) {
                    BasicText(
                        modifier = Modifier.align(Alignment.Center),
                        text = "loading...",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color(0xff0000ff)
                        )
                    )
                }
            }
            null -> {
                val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                when (BiometricManager.from(this).canAuthenticate(authenticators)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        stepState.value = Step.PROMPTED
                        val callback = object: BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                showToast("on -> authentication error $errorCode $errString")
                                finish()
                            }

                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                val co = result.cryptoObject ?: TODO()
                                val cipher = co.cipher ?: TODO()
                                stepState.value = Step.AUTHORIZED
                            }

                            override fun onAuthenticationFailed() {
                                showToast("on -> authentication failed")
                                finish()
                            }
                        }
                        val info = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Biometric title.")
                            .setSubtitle("Biometric subtitle.")
                            .setAllowedAuthenticators(authenticators)
                            .build()
                        val cipher = cipherEncrypted()
                        BiometricPrompt(this, callback).authenticate(info, BiometricPrompt.CryptoObject(cipher))
                    }
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                        showToast("BIOMETRIC_ERROR_NO_HARDWARE")
                        finish()
                    }
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                        showToast("BIOMETRIC_ERROR_HW_UNAVAILABLE")
                        finish()
                    }
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                        showToast("BIOMETRIC_ERROR_NONE_ENROLLED")
                        finish()
                    }
                    else -> TODO()
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
