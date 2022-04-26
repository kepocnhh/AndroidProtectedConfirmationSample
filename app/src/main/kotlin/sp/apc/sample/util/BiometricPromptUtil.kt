package sp.apc.sample.util

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.biometricPrompt(callback: BiometricPrompt.AuthenticationCallback): BiometricPrompt {
    return BiometricPrompt(this, ContextCompat.getMainExecutor(this), callback)
}
