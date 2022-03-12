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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec

class MainActivity : AppCompatActivity() {
    companion object {
        private fun Context.showToast(message: String) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

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
                            BiometricPrompt(this@MainActivity, callback).authenticate(info)
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
