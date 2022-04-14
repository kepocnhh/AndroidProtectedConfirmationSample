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
