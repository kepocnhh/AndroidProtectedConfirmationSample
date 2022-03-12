package sp.apc.sample

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

sealed interface AuthenticateResult {
    object Success : AuthenticateResult
    class Error(val type: Type) : AuthenticateResult {
        enum class Type {
            NO_HARDWARE, HARDWARE_UNAVAILABLE, NONE_ENROLLED
        }
    }
}

fun Context.canAuthenticate(authenticators: Int): AuthenticateResult {
    return when (BiometricManager.from(this).canAuthenticate(authenticators)) {
        BiometricManager.BIOMETRIC_SUCCESS -> AuthenticateResult.Success
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> AuthenticateResult.Error(AuthenticateResult.Error.Type.NO_HARDWARE)
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> AuthenticateResult.Error(AuthenticateResult.Error.Type.HARDWARE_UNAVAILABLE)
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> AuthenticateResult.Error(AuthenticateResult.Error.Type.NONE_ENROLLED)
        else -> TODO()
    }
}

fun FragmentActivity.authenticate(
    authenticators: Int,
    title: String,
    subtitle: String,
    callback: BiometricPrompt.AuthenticationCallback
) {
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setAllowedAuthenticators(authenticators)
        .build()
    BiometricPrompt(this, callback).authenticate(info)
}
